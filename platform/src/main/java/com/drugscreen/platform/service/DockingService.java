package com.drugscreen.platform.service;

import com.drugscreen.platform.entity.Compound;
import com.drugscreen.platform.entity.DockingResult;
import com.drugscreen.platform.repository.CompoundRepository;
import com.drugscreen.platform.repository.DockingResultRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class DockingService {

    private final DockingResultRepository dockingResultRepository;
    private final CompoundRepository compoundRepository;

    @Value("${vina.path:D:\\tool\\vina\\vina.exe}")
    private String vinaPath;

    /**
     * 执行分子对接
     * @param compoundId 化合物ID
     */
    @Async("asyncExecutor")
    public void executeDocking(Long compoundId) {
        log.info("开始执行分子对接，化合物ID: {}", compoundId);

        // 获取化合物信息
        Compound compound = compoundRepository.findById(compoundId)
                .orElseThrow(() -> new RuntimeException("化合物不存在: " + compoundId));

        // 创建对接结果记录
        DockingResult result = new DockingResult();
        result.setCompoundId(compoundId);
        result.setStatus("PENDING");
        result = dockingResultRepository.save(result);

        try {
            // 校验文件
            validateFiles(compound.getEnglishName());

            // 调用Vina执行对接
            String logContent = runVina(compound.getEnglishName());
            result.setDockedPdbqtContent(logContent);

            // 提取对接结果
            Double affinity = extractAffinity(logContent);
            result.setAffinity(affinity);
            result.setStatus("completed");

        } catch (Exception e) {
            log.error("分子对接失败: {}", e.getMessage(), e);
            result.setStatus("failed");
            result.setDockedPdbqtContent("Error: " + e.getMessage());
        } finally {
            // 保存结果到数据库
            dockingResultRepository.save(result);
        }

        log.info("分子对接完成，结果ID: {}", result.getId());
    }

    /**
     * 校验文件
     */
    private void validateFiles(String englishName) throws Exception {
        log.info("校验化合物 {} 的文件", englishName);
        
        // 获取项目根目录（platform 的父目录）
        File currentDir = new File(System.getProperty("user.dir"));
        File dockingDir;
        
        // 如果当前目录是 platform，则使用父目录的 docking
        if (currentDir.getName().equals("platform")) {
            dockingDir = new File(currentDir.getParentFile(), "docking");
        } else {
            dockingDir = new File(currentDir, "docking");
        }
        
        log.info("Docking 目录路径: {}", dockingDir.getAbsolutePath());
        
        // 检查1e9h.pdbqt文件是否存在
        File receptorFile = new File(dockingDir, "1e9h.pdbqt");
        if (!receptorFile.exists()) {
            throw new Exception("受体文件不存在: " + receptorFile.getAbsolutePath());
        }
        log.info("受体文件存在: {}", receptorFile.getAbsolutePath());
        
        // 检查配体文件是否存在
        File ligandFile = new File(dockingDir, englishName + ".pdbqt");
        if (!ligandFile.exists()) {
            throw new Exception("配体文件不存在: " + ligandFile.getAbsolutePath());
        }
        log.info("配体文件存在: {}", ligandFile.getAbsolutePath());
    }

    /**
     * 运行Vina执行对接
     */
    private String runVina(String englishName) throws Exception {
        log.info("调用Vina执行对接，路径: {}", vinaPath);

        // 检查Vina路径是否存在
        File vinaFile = new File(vinaPath);
        if (!vinaFile.exists()) {
            throw new Exception("Vina可执行文件不存在: " + vinaPath);
        }
        log.info("Vina可执行文件存在: {}", vinaFile.getAbsolutePath());

        // 获取项目根目录（platform 的父目录）
        File currentDir = new File(System.getProperty("user.dir"));
        File dockingDir;
        
        // 如果当前目录是 platform，则使用父目录的 docking
        if (currentDir.getName().equals("platform")) {
            dockingDir = new File(currentDir.getParentFile(), "docking");
        } else {
            dockingDir = new File(currentDir, "docking");
        }
        
        log.info("Docking目录路径: {}", dockingDir.getAbsolutePath());
        if (!dockingDir.exists()) {
            boolean created = dockingDir.mkdirs();
            log.info("创建docking目录: {}", created);
            if (!created) {
                throw new Exception("无法创建docking目录");
            }
        }
        log.info("Docking目录存在: {}", dockingDir.exists());
        
        // 确保目录路径格式正确
        if (!dockingDir.isDirectory()) {
            throw new Exception("docking路径不是有效的目录: " + dockingDir.getAbsolutePath());
        }

        // 构建Vina命令
        ProcessBuilder pb = new ProcessBuilder(
                vinaPath,
                "--receptor", "1e9h.pdbqt",
                "--ligand", englishName + ".pdbqt",
                "--out", englishName + "_out.pdbqt",
                // 1e9h蛋白质的活性位点坐标（准确值）
                "--center_x", "11.114",
                "--center_y", "27.215",
                "--center_z", "90.265",
                // 网格大小，确保覆盖整个活性位点区域
                "--size_x", "49",
                "--size_y", "77",
                "--size_z", "23",
                // 增加搜索彻底性以获得更好的结果
                "--exhaustiveness", "16"
        );

        // 使用绝对路径作为工作目录
        pb.directory(dockingDir);
        pb.redirectErrorStream(true);
        log.info("开始执行Vina命令，工作目录: {}", dockingDir.getAbsolutePath());
        log.info("Vina可执行文件路径: {}", vinaPath);

        Process process = pb.start();
        StringBuilder logContent = new StringBuilder();

        // 读取合并后的输出流（标准输出和标准错误）
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                logContent.append(line).append("\n");
                log.info("Vina输出: {}", line);
            }
        }

        int exitCode = process.waitFor();
        if (exitCode != 0) {
            String errorMessage = "Vina执行失败，退出码: " + exitCode + "\n" + 
                                "输出日志: " + logContent;
            log.error(errorMessage);
            throw new Exception(errorMessage);
        }
        
        log.info("Vina执行成功，退出码: {}", exitCode);

        return logContent.toString();
    }

    /**
     * 从日志中提取亲和力值
     */
    private Double extractAffinity(String logContent) {
        log.info("从日志中提取亲和力值");
        log.info("Vina日志内容: {}", logContent);
        
        // 从Vina日志中提取亲和力值
        String[] lines = logContent.split("\\n");
        
        // 标志：是否已经找到亲和力表格的开始
        boolean foundTable = false;
        
        for (String line : lines) {
            // 检查是否找到亲和力表格的开始
            if (line.contains("mode |   affinity | dist from best mode")) {
                foundTable = true;
                log.info("找到亲和力表格开始");
                continue;
            }
            
            // 如果已经找到表格，检查数据行
            if (foundTable && line.trim().length() > 0) {
                // 跳过分隔线和标题行
                if (line.contains("-----+") || line.contains("(kcal/mol)") || line.contains("mode |")) {
                    continue;
                }
                
                try {
                    // 分割行，提取亲和力值
                    String[] parts = line.trim().split("\\s+");
                    if (parts.length >= 2) {
                        // 第二列是亲和力值
                        try {
                            double affinity = Double.parseDouble(parts[1]);
                            // 亲和力值应该是负数（kcal/mol），且绝对值通常小于15
                            if (affinity < 0 && Math.abs(affinity) < 15) {
                                log.info("提取到亲和力值 (表格格式): {}", affinity);
                                return affinity;
                            }
                        } catch (NumberFormatException e) {
                            // 跳过非数字部分
                        }
                    }
                } catch (Exception e) {
                    log.warn("解析亲和力值失败: {}", e.getMessage());
                }
            }
        }
        
        // 如果没有找到亲和力值，抛出异常
        throw new RuntimeException("无法从Vina日志中提取亲和力值");
    }

    /**
     * 查询对接结果
     */
    public DockingResult getDockingResult(Long id) {
        return dockingResultRepository.findById(id).orElse(null);
    }

    /**
     * 根据化合物ID查询最新的对接结果
     */
    public DockingResult getLatestDockingResult(Long compoundId) {
        return dockingResultRepository.findTopByCompoundIdOrderByAffinityAsc(compoundId).orElse(null);
    }
}
