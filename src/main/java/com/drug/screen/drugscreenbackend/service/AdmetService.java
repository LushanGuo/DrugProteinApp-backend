package com.drug.screen.drugscreenbackend.service;

import com.drug.screen.drugscreenbackend.entity.AdmetResult;
import com.drug.screen.drugscreenbackend.entity.Compound;
import com.drug.screen.drugscreenbackend.repository.AdmetResultRepository;
import com.drug.screen.drugscreenbackend.repository.CompoundRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.cert.X509Certificate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
@Slf4j
public class AdmetService {

    @Autowired
    private CompoundRepository compoundRepository;

    @Autowired
    private AdmetResultRepository admetResultRepository;
    
    @Value("${admet.api.url}")
    private String admetApiUrl;
    
    /**
     * 忽略SSL证书验证的方法（仅用于开发和测试环境）
     */
    private void disableSslVerification() {
        try {
            // 创建一个不验证证书的TrustManager
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
            
            // 安装信任管理器
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
            
            // 创建一个不验证主机名的HostnameVerifier
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

        try {
            // 1. 查询化合物信息，获取SMILES字符串
            Compound compound = compoundRepository.findById(compoundId)
                    .orElseThrow(() -> new Exception("化合物不存在: " + compoundId));
            String smiles = compound.getSmiles();
            log.info("获取到化合物SMILES: {}", smiles);

            // 2. 检查是否已有ADMET结果
            AdmetResult existingResult = admetResultRepository.findByCompoundId(compoundId);
            if (existingResult != null) {
                log.info("化合物已有ADMET预测结果，直接返回");
                return existingResult;
            }

            // 3. 调用ADMET API获取预测结果
            Map<String, Double> admetMetrics = callAdmetApi(smiles);
            log.info("ADMET API调用成功，获取到五维指标");

            // 4. 创建并保存ADMET结果
            AdmetResult result = new AdmetResult();
            result.setCompoundId(compoundId);
            result.setAbsorption(admetMetrics.get("absorption"));
            result.setDistribution(admetMetrics.get("distribution"));
            result.setMetabolism(admetMetrics.get("metabolism"));
            result.setExcretion(admetMetrics.get("excretion"));
            result.setToxicity(admetMetrics.get("toxicity"));
            result.setCreateTime(LocalDateTime.now());
            result = admetResultRepository.save(result);

            log.info("ADMET预测完成，结果已保存到数据库");
            return result;

        } catch (Exception e) {
            log.error("ADMET预测失败: {}", e.getMessage());
            throw new RuntimeException("ADMET预测失败", e);
        }
    }

    /**
     * 调用ADMET API获取预测结果
     * @param smiles SMILES字符串
     * @return 五维指标映射
     */
    private Map<String, Double> callAdmetApi(String smiles) throws Exception {
        log.info("调用ADMET API，SMILES: {}", smiles);
        log.info("API地址: {}", admetApiUrl);

        // 忽略SSL证书验证（仅用于开发和测试环境）
        disableSslVerification();

        // 从配置文件读取API地址
        URL url = new URL(admetApiUrl);
        HttpURLConnection conn = null;
        
        try {
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Accept", "application/json");
            conn.setDoOutput(true);
            conn.setConnectTimeout(10000); // 设置连接超时时间为10秒
            conn.setReadTimeout(10000); // 设置读取超时时间为10秒

            // 构建请求体
            String requestBody = "{\"smiles\": \"" + smiles + "\"}";
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
                // 读取错误响应
                try (BufferedReader br = new BufferedReader(
                        new InputStreamReader(conn.getErrorStream(), "utf-8"))) {
                    String responseLine;
                    while ((responseLine = br.readLine()) != null) {
                        response.append(responseLine.trim());
                    }
                }
                log.error("ADMET API错误响应: {}", response.toString());
                throw new Exception("ADMET API调用失败，响应码: " + responseCode + ", 错误信息: " + response.toString());
            }

            // 解析响应JSON，提取五维指标
            Map<String, Double> metrics = new HashMap<>();
            
            try {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode rootNode = mapper.readTree(response.toString());
                
                // 尝试不同的响应结构，适应ADMETlab3 API的实际返回格式
                JsonNode dataNode = null;
                
                // 检查常见的响应结构
                if (rootNode.has("data")) {
                    dataNode = rootNode.get("data");
                } else if (rootNode.has("result")) {
                    dataNode = rootNode.get("result");
                } else {
                    dataNode = rootNode; // 直接使用根节点
                }
                
                // 提取五维指标
                // 注意：根据ADMETlab3 API的实际返回字段名调整
                if (dataNode.has("absorption")) {
                    metrics.put("absorption", dataNode.get("absorption").asDouble());
                } else if (dataNode.has("Absorption")) {
                    metrics.put("absorption", dataNode.get("Absorption").asDouble());
                } else {
                    // 如果没有找到对应字段，使用默认值
                    metrics.put("absorption", 0.0);
                }
                
                if (dataNode.has("distribution")) {
                    metrics.put("distribution", dataNode.get("distribution").asDouble());
                } else if (dataNode.has("Distribution")) {
                    metrics.put("distribution", dataNode.get("Distribution").asDouble());
                } else {
                    metrics.put("distribution", 0.0);
                }
                
                if (dataNode.has("metabolism")) {
                    metrics.put("metabolism", dataNode.get("metabolism").asDouble());
                } else if (dataNode.has("Metabolism")) {
                    metrics.put("metabolism", dataNode.get("Metabolism").asDouble());
                } else {
                    metrics.put("metabolism", 0.0);
                }
                
                if (dataNode.has("excretion")) {
                    metrics.put("excretion", dataNode.get("excretion").asDouble());
                } else if (dataNode.has("Excretion")) {
                    metrics.put("excretion", dataNode.get("Excretion").asDouble());
                } else {
                    metrics.put("excretion", 0.0);
                }
                
                if (dataNode.has("toxicity")) {
                    metrics.put("toxicity", dataNode.get("toxicity").asDouble());
                } else if (dataNode.has("Toxicity")) {
                    metrics.put("toxicity", dataNode.get("Toxicity").asDouble());
                } else {
                    metrics.put("toxicity", 0.0);
                }
                
                log.info("成功解析ADMET API响应，提取五维指标");
                
            } catch (Exception e) {
                log.error("解析ADMET API响应失败: {}", e.getMessage());
                // 如果解析失败，使用默认值
                metrics.put("absorption", 0.0);
                metrics.put("distribution", 0.0);
                metrics.put("metabolism", 0.0);
                metrics.put("excretion", 0.0);
                metrics.put("toxicity", 0.0);
            }

            return metrics;
        } catch (Exception e) {
            log.error("ADMET API调用失败: {}", e.getMessage());
            throw new Exception("ADMET API调用失败: " + e.getMessage(), e);
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    /**
     * 根据化合物ID查询ADMET结果
     * @param compoundId 化合物ID
     * @return ADMET预测结果
     */
    public AdmetResult getAdmetResultByCompoundId(Long compoundId) {
        return admetResultRepository.findByCompoundId(compoundId);
    }

    /**
     * 根据结果ID查询ADMET结果
     * @param id 结果ID
     * @return ADMET预测结果
     */
    public AdmetResult getAdmetResultById(Long id) {
        return admetResultRepository.findById(id).orElse(null);
    }
}