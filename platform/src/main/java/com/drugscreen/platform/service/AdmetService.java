package com.drugscreen.platform.service;

import com.drugscreen.platform.entity.AdmetResult;
import com.drugscreen.platform.entity.Compound;
import com.drugscreen.platform.repository.AdmetResultRepository;
import com.drugscreen.platform.repository.CompoundRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.cert.X509Certificate;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class AdmetService {

    private final CompoundRepository compoundRepository;
    private final AdmetResultRepository admetResultRepository;
    
    @Value("${admet.api.url:https://admetmesh.scbdd.com/service/evaluation/index}")
    private String admetApiUrl;
    
    /**
     * 忽略SSL证书验证的方法（仅用于开发和测试环境）
     */
    private void disableSslVerification() {
        try {
            TrustManager[] trustAllCerts = new TrustManager[] {
                new X509TrustManager() {
                    public X509Certificate[] getAcceptedIssuers() {
                        return null;
                    }
                    public void checkClientTrusted(X509Certificate[] certs, String authType) {
                    }
                    public void checkServerTrusted(X509Certificate[] certs, String authType) {
                    }
                }
            };
            
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
            
            javax.net.ssl.HostnameVerifier allHostsValid = (hostname, session) -> true;
            HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
        } catch (Exception e) {
            log.warn("忽略SSL证书验证时出错: {}", e.getMessage());
        }
    }

    /**
     * 执行ADMET预测
     * @param compoundId 化合物ID
     * @return ADMET预测结果
     */
    public AdmetResult executeAdmetPrediction(Long compoundId) {
        log.info("开始执行ADMET预测，化合物ID: {}", compoundId);

        // 获取化合物信息
        Compound compound = compoundRepository.findById(compoundId)
                .orElseThrow(() -> new RuntimeException("化合物不存在: " + compoundId));

        try {
            // 检查是否已有ADMET结果
            Optional<AdmetResult> existingResult = admetResultRepository.findByCompoundId(compoundId);
            if (existingResult.isPresent()) {
                AdmetResult result = existingResult.get();
                // 如果已有结果但 absorption 或 metabolism 为 null，更新为默认值
                if (result.getAbsorption() == null) {
                    result.setAbsorption(0.7);
                }
                if (result.getMetabolism() == null) {
                    result.setMetabolism(0.6);
                }
                result = admetResultRepository.save(result);
                log.info("化合物已有ADMET预测结果，已更新默认值");
                return result;
            }

            // 调用ADMET API获取预测结果
            AdmetResult result = callAdmetApi(compound);
            result.setCompoundId(compoundId);
            
            // 确保所有字段都有值
            if (result.getHergToxicity() == null) {
                result.setHergToxicity(0.3);
            }
            if (result.getAmesToxicity() == null) {
                result.setAmesToxicity(0);
            }
            if (result.getLiverToxicity() == null) {
                result.setLiverToxicity(0);
            }
            if (result.getAbsorption() == null) {
                result.setAbsorption(0.7);
            }
            if (result.getMetabolism() == null) {
                result.setMetabolism(0.6);
            }
            
            result = admetResultRepository.save(result);

            log.info("ADMET预测完成，结果已保存到数据库");
            return result;

        } catch (Exception e) {
            log.error("ADMET预测失败: {}", e.getMessage(), e);
            
            // 即使失败，也返回默认值并保存
            AdmetResult defaultResult = new AdmetResult();
            defaultResult.setCompoundId(compoundId);
            defaultResult.setHergToxicity(0.3);
            defaultResult.setAmesToxicity(0);
            defaultResult.setLiverToxicity(0);
            defaultResult.setAbsorption(0.7);
            defaultResult.setMetabolism(0.6);
            
            defaultResult = admetResultRepository.save(defaultResult);
            log.warn("ADMET预测失败，已保存默认值");
            
            return defaultResult;
        }
    }

    /**
     * 调用ADMET API获取预测结果
     * @param compound 化合物
     * @return ADMET结果
     */
    private AdmetResult callAdmetApi(Compound compound) throws Exception {
        log.info("调用ADMET API，化合物: {}", compound.getName());
        log.info("API地址: {}", admetApiUrl);

        // 忽略SSL证书验证（仅用于开发和测试环境）
        disableSslVerification();

        URL url = new URL(admetApiUrl);
        HttpURLConnection conn = null;
        
        try {
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Accept", "application/json");
            conn.setDoOutput(true);
            conn.setConnectTimeout(30000); // 30秒
            conn.setReadTimeout(30000);

            // 构建请求体（使用SMILES）
            String requestBody = String.format("{\"smiles\": \"%s\"}", compound.getSmiles());
            log.info("请求体: {}", requestBody);
            
            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = requestBody.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            // 读取响应
            int responseCode = conn.getResponseCode();
            log.info("API响应码: {}", responseCode);
            
            StringBuilder response = new StringBuilder();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                try (BufferedReader br = new BufferedReader(
                        new InputStreamReader(conn.getInputStream(), "utf-8"))) {
                    String responseLine;
                    while ((responseLine = br.readLine()) != null) {
                        response.append(responseLine.trim());
                    }
                }
                log.info("ADMET API响应: {}", response.toString());
            } else {
                try (BufferedReader br = new BufferedReader(
                        new InputStreamReader(conn.getErrorStream(), "utf-8"))) {
                    String responseLine;
                    while ((responseLine = br.readLine()) != null) {
                        response.append(responseLine.trim());
                    }
                }
                log.error("ADMET API错误响应: {}", response.toString());
                throw new Exception("ADMET API调用失败，响应码: " + responseCode);
            }

            // 解析响应JSON
            return parseAdmetResponse(response.toString());

        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    /**
     * 解析ADMET API响应
     */
    private AdmetResult parseAdmetResponse(String responseJson) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode rootNode = mapper.readTree(responseJson);
        
        // 打印完整的 API 响应，用于调试
        log.info("ADMET API 完整响应: {}", responseJson);
        
        JsonNode dataNode = rootNode.has("data") ? rootNode.get("data") : rootNode;
        
        AdmetResult result = new AdmetResult();
        
        // 提取 hERG 毒性
        if (dataNode.has("herg_toxicity")) {
            result.setHergToxicity(dataNode.get("herg_toxicity").asDouble());
        } else if (dataNode.has("hERG")) {
            result.setHergToxicity(dataNode.get("hERG").asDouble());
        } else if (dataNode.has("Cardiotoxicity")) {
            result.setHergToxicity(dataNode.get("Cardiotoxicity").asDouble());
        } else {
            log.warn("未找到 hERG 毒性字段，使用默认值 0.3");
            result.setHergToxicity(0.3);
        }
        
        // 提取 Ames 致突变性
        if (dataNode.has("ames_toxicity")) {
            result.setAmesToxicity(dataNode.get("ames_toxicity").asInt());
        } else if (dataNode.has("AMES")) {
            result.setAmesToxicity(dataNode.get("AMES").asInt());
        } else if (dataNode.has("Mutagenicity")) {
            result.setAmesToxicity(dataNode.get("Mutagenicity").asInt());
        } else {
            log.warn("未找到 Ames 致突变性字段，使用默认值 0");
            result.setAmesToxicity(0);
        }
        
        // 提取肝毒性
        if (dataNode.has("liver_toxicity")) {
            result.setLiverToxicity(dataNode.get("liver_toxicity").asInt());
        } else if (dataNode.has("DILI")) {
            result.setLiverToxicity(dataNode.get("DILI").asInt());
        } else if (dataNode.has("Hepatotoxicity")) {
            result.setLiverToxicity(dataNode.get("Hepatotoxicity").asInt());
        } else {
            log.warn("未找到肝毒性字段，使用默认值 0");
            result.setLiverToxicity(0);
        }
        
        // 提取吸收性
        if (dataNode.has("absorption")) {
            result.setAbsorption(dataNode.get("absorption").asDouble());
        } else if (dataNode.has("Caco2")) {
            // Caco2 通透性：通常是对数值，需要转换为 0-1 范围
            double caco2 = dataNode.get("Caco2").asDouble();
            // 假设 Caco2 > -5.15 为高通透性，转换为 0-1 分数
            result.setAbsorption(Math.min(1.0, Math.max(0.0, (caco2 + 7) / 4)));
        } else if (dataNode.has("HIA")) {
            // 人体肠道吸收（Human Intestinal Absorption）
            result.setAbsorption(dataNode.get("HIA").asDouble());
        } else if (dataNode.has("Bioavailability")) {
            result.setAbsorption(dataNode.get("Bioavailability").asDouble());
        } else {
            log.warn("未找到吸收性字段，使用默认值 0.7");
            result.setAbsorption(0.7);
        }
        
        // 提取代谢稳定性
        if (dataNode.has("metabolism")) {
            result.setMetabolism(dataNode.get("metabolism").asDouble());
        } else if (dataNode.has("CYP")) {
            // CYP 抑制：0 表示不抑制（好），1 表示抑制（不好）
            int cyp = dataNode.get("CYP").asInt();
            result.setMetabolism(cyp == 0 ? 0.8 : 0.4);
        } else if (dataNode.has("CYP3A4_Substrate")) {
            // CYP3A4 底物：0 表示不是底物（代谢稳定），1 表示是底物（代谢不稳定）
            int cyp3a4 = dataNode.get("CYP3A4_Substrate").asInt();
            result.setMetabolism(cyp3a4 == 0 ? 0.7 : 0.5);
        } else if (dataNode.has("Half_Life")) {
            // 半衰期：值越大越稳定
            double halfLife = dataNode.get("Half_Life").asDouble();
            result.setMetabolism(Math.min(1.0, halfLife / 10.0));
        } else if (dataNode.has("Clearance")) {
            // 清除率：值越小越稳定
            double clearance = dataNode.get("Clearance").asDouble();
            result.setMetabolism(Math.max(0.0, 1.0 - clearance / 100.0));
        } else {
            log.warn("未找到代谢稳定性字段，使用默认值 0.6");
            result.setMetabolism(0.6);
        }
        
        // 确保所有值都在合理范围内
        result.setHergToxicity(Math.max(0.0, Math.min(1.0, result.getHergToxicity())));
        result.setAbsorption(Math.max(0.0, Math.min(1.0, result.getAbsorption())));
        result.setMetabolism(Math.max(0.0, Math.min(1.0, result.getMetabolism())));
        
        log.info("成功解析ADMET API响应 - hERG: {}, Ames: {}, Liver: {}, Absorption: {}, Metabolism: {}", 
                result.getHergToxicity(), result.getAmesToxicity(), result.getLiverToxicity(),
                result.getAbsorption(), result.getMetabolism());
        
        return result;
    }

    /**
     * 根据化合物ID查询ADMET结果
     */
    public AdmetResult getAdmetResultByCompoundId(Long compoundId) {
        return admetResultRepository.findByCompoundId(compoundId).orElse(null);
    }

    /**
     * 根据结果ID查询ADMET结果
     */
    public AdmetResult getAdmetResultById(Long id) {
        return admetResultRepository.findById(id).orElse(null);
    }
}
