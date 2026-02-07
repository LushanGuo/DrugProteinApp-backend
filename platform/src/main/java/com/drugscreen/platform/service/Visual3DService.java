package com.drugscreen.platform.service;

import com.drugscreen.platform.dto.Visual3DDTO;
import com.drugscreen.platform.entity.Compound;
import com.drugscreen.platform.repository.CompoundRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * 3D 可视化服务
 * 从文件系统读取 PDB 数据
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class Visual3DService {
    
    private final CompoundRepository compoundRepository;
    
    /**
     * 获取 3D 可视化数据
     */
    public Visual3DDTO getVisual3DData(Long compoundId) {
        // 获取化合物信息
        Compound compound = compoundRepository.findById(compoundId)
                .orElseThrow(() -> new RuntimeException("化合物不存在 ID: " + compoundId));
        
        Visual3DDTO dto = new Visual3DDTO();
        
        try {
            // 读取受体蛋白 PDB（CDK2）
            String receptorPdb = readReceptorPdb();
            dto.setProteinPdb(receptorPdb);
            log.info("成功读取受体蛋白 PDB，长度: {} 字符", receptorPdb.length());
            
            // 读取配体 PDB
            String ligandPdb = readLigandPdb(compound.getEnglishName());
            dto.setDockedLigandPdbqt(ligandPdb);
            log.info("成功读取配体 {} PDB，长度: {} 字符", compound.getName(), ligandPdb.length());
            
            // 设置模拟的结合能（实际应该从对接结果获取）
            dto.setBindingEnergy(generateMockBindingEnergy(compoundId));
            
        } catch (IOException e) {
            log.error("读取 3D 数据失败: compoundId={}, 错误: {}", compoundId, e.getMessage());
            throw new RuntimeException("读取 3D 数据失败: " + e.getMessage());
        }
        
        return dto;
    }
    
    /**
     * 读取受体蛋白 PDB 文件（1e9h 蛋白质）
     */
    private String readReceptorPdb() throws IOException {
        ClassPathResource resource = new ClassPathResource("structures/receptor_1e9h.pdb");
        return new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
    }
    
    /**
     * 读取配体 PDB 文件
     */
    private String readLigandPdb(String englishName) throws IOException {
        if (englishName == null || englishName.isEmpty()) {
            throw new IOException("化合物英文名为空");
        }
        
        // 直接使用英文名作为文件名（保持原始大小写和空格）
        String fileName = englishName;
        String resourcePath = "3ddata/pdb文件/pdb文件/" + fileName + ".pdb";
        
        try {
            ClassPathResource resource = new ClassPathResource(resourcePath);
            if (!resource.exists()) {
                log.warn("文件不存在: {}", resourcePath);
                throw new IOException("找不到配体文件: " + fileName + ".pdb");
            }
            return new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.warn("无法读取配体文件: {}", resourcePath);
            throw new IOException("找不到配体文件: " + fileName + ".pdb");
        }
    }
    
    /**
     * 生成模拟的结合能
     * 实际应该从 docking_results 表获取
     */
    private Double generateMockBindingEnergy(Long compoundId) {
        // 根据 ID 生成不同的结合能（-8.0 到 -11.0）
        return -8.0 - (compoundId % 10) * 0.3;
    }
}
