package com.drugscreen.platform.service;

import com.drugscreen.platform.dto.RankedCompoundDTO;
import com.drugscreen.platform.dto.StructureDTO;
import com.drugscreen.platform.entity.Compound;
import com.drugscreen.platform.entity.DockingResult;
import com.drugscreen.platform.repository.CompoundRepository;
import com.drugscreen.platform.repository.DockingResultRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CompoundService {

    private final CompoundRepository compoundRepository;
    private final DockingResultRepository dockingResultRepository;

    /**
     * 分页查询化合物
     * @param keyword 搜索关键词 (可选)
     * @param pageable 分页参数
     * @return 分页结果
     */
    public Page<Compound> getCompounds(String keyword, Pageable pageable) {
        if (keyword != null && !keyword.isEmpty()) {
            return compoundRepository.findByNameContainingOrEnglishNameContainingIgnoreCase(keyword, keyword, pageable);
        }
        return compoundRepository.findAll(pageable);
    }

    /**
     * 获取单个化合物详情
     */
    public Compound getCompoundById(Long id) {
        return compoundRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("化合物不存在 ID: " + id));
    }

    /**
     * 获取化合物的 3D 结构数据
     * @param id 化合物 ID
     * @return 结构数据 DTO
     */
    public StructureDTO getStructure(Long id) {
        Compound compound = getCompoundById(id);
        
        StructureDTO dto = new StructureDTO();
        dto.setCompoundId(compound.getId());
        dto.setCompoundName(compound.getName());
        
        // 从文件读取 PDB 数据
        try {
            // 读取配体 PDB 文件（根据英文名）
            String ligandPdb = readLigandPdbFile(compound.getEnglishName());
            dto.setLigandPdbqt(ligandPdb); // 使用 PDB 格式作为 PDBQT
            
            // 读取受体 PDB（CDK2 蛋白）
            String receptorPdb = readStructureFile("structures/receptor_cdk2.pdb");
            dto.setReceptorPdb(receptorPdb);
            
            log.info("成功从文件读取化合物 {} 的结构数据", compound.getName());
        } catch (IOException e) {
            log.warn("读取化合物 {} 的结构文件失败: {}", compound.getName(), e.getMessage());
            // 生成模拟数据作为后备
            dto.setReceptorPdb(generateMockReceptorPdb());
            dto.setLigandPdbqt(generateMockLigandPdbqt(compound));
        }
        
        return dto;
    }
    
    /**
     * 读取配体 PDB 文件
     */
    private String readLigandPdbFile(String englishName) throws IOException {
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
            String content = new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            log.debug("成功读取配体文件: {}", resourcePath);
            return content;
        } catch (IOException e) {
            log.warn("无法读取配体文件: {}, 尝试其他路径", resourcePath);
            throw e;
        }
    }

    /**
     * 从 classpath 读取结构文件
     */
    private String readStructureFile(String resourcePath) throws IOException {
        try {
            ClassPathResource resource = new ClassPathResource(resourcePath);
            return new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            // 如果 classpath 中没有，尝试从文件系统读取
            Path path = Paths.get("data/" + resourcePath);
            if (Files.exists(path)) {
                return Files.readString(path, StandardCharsets.UTF_8);
            }
            throw e;
        }
    }

    /**
     * 生成模拟的受体 PDB 数据（CDK2 蛋白）
     */
    private String generateMockReceptorPdb() {
        return "HEADER    PROTEIN KINASE                          01-JAN-20   1HCK\n" +
               "TITLE     CDK2 CYCLIN-DEPENDENT KINASE 2\n" +
               "REMARK   1 MOCK DATA FOR DEMONSTRATION\n" +
               "ATOM      1  N   ILE A  16      47.983  24.433  53.784  1.00 49.39           N\n" +
               "ATOM      2  CA  ILE A  16      48.364  25.785  54.189  1.00 48.12           C\n" +
               "ATOM      3  C   ILE A  16      47.210  26.778  54.089  1.00 46.94           C\n" +
               "ATOM      4  O   ILE A  16      46.043  26.389  54.001  1.00 46.88           O\n" +
               "ATOM      5  CB  ILE A  16      48.956  25.738  55.614  1.00 48.36           C\n" +
               "ATOM      6  CG1 ILE A  16      50.141  24.766  55.668  1.00 48.77           C\n" +
               "ATOM      7  CG2 ILE A  16      49.394  27.123  56.073  1.00 48.10           C\n" +
               "ATOM      8  CD1 ILE A  16      50.729  24.619  57.063  1.00 49.45           C\n" +
               "TER\n" +
               "END\n";
    }

    /**
     * 生成模拟的配体 PDBQT 数据
     */
    private String generateMockLigandPdbqt(Compound compound) {
        return "REMARK  Name = " + compound.getName() + "\n" +
               "REMARK  SMILES = " + compound.getSmiles() + "\n" +
               "REMARK  Molecular Weight = " + compound.getMolecularWeight() + "\n" +
               "ROOT\n" +
               "ATOM      1  C   UNL     1       0.000   0.000   0.000  0.00  0.00    +0.000 C \n" +
               "ATOM      2  C   UNL     1       1.400   0.000   0.000  0.00  0.00    +0.000 C \n" +
               "ATOM      3  C   UNL     1       2.100   1.200   0.000  0.00  0.00    +0.000 C \n" +
               "ATOM      4  C   UNL     1       1.400   2.400   0.000  0.00  0.00    +0.000 C \n" +
               "ATOM      5  C   UNL     1       0.000   2.400   0.000  0.00  0.00    +0.000 C \n" +
               "ATOM      6  C   UNL     1      -0.700   1.200   0.000  0.00  0.00    +0.000 C \n" +
               "ATOM      7  O   UNL     1       3.500   1.200   0.000  0.00  0.00    +0.000 OA\n" +
               "ENDROOT\n" +
               "TORSDOF 0\n";
    }

    /**
     * 初始化 48 个天然产物数据 (示例展示部分，实际需完整录入)
     * 仅当数据库为空时执行
     */
    @Transactional
    public void initMockData() {
        if (compoundRepository.count() > 0) {
            return;
        }
        log.info("数据库为空，开始初始化 48 个天然产物数据...");

        // 示例数据：槲皮素 (Quercetin) - 典型的 CDK2 抑制剂对照
        Compound c1 = new Compound();
        c1.setName("槲皮素");
        c1.setEnglishName("Quercetin");
        c1.setSmiles("O=C1C(O)=C(C2=CC=C(O)C(O)=C2)OC2=CC(O)=CC(O)=C12");
        c1.setMolecularWeight(302.24);
        c1.setLogP(1.48);
        c1.setCategory("黄酮类");
        c1.setDescription("一种广泛存在于植物中的黄酮醇，具有抗氧化和抗癌活性。");

        // 示例数据：染料木素 (Genistein)
        Compound c2 = new Compound();
        c2.setName("染料木素");
        c2.setEnglishName("Genistein");
        c2.setSmiles("O=C1C=C(C2=CC=C(O)C=C2)OC2=CC(O)=CC(O)=C12");
        c2.setMolecularWeight(270.24);
        c2.setLogP(2.84);
        c2.setCategory("异黄酮类");
        c2.setDescription("大豆异黄酮的主要成分，已知的酪氨酸激酶抑制剂。");

        // ... 此处应继续录入剩余 46 个化合物 ...
        // 建议使用 CSV 解析器批量导入，此处为演示直接保存

        compoundRepository.saveAll(List.of(c1, c2));
        log.info("初始化数据完成。");
    }
    
    /**
     * 获取 Top 10 化合物（按亲和力排序）
     */
    public List<RankedCompoundDTO> getTop10Compounds() {
        List<DockingResult> top10Results = dockingResultRepository.findTop10ByOrderByAffinityAsc();
        
        List<RankedCompoundDTO> result = new ArrayList<>();
        int rank = 1;
        
        for (DockingResult dr : top10Results) {
            if (rank > 10) break;
            
            Compound compound = compoundRepository.findById(dr.getCompoundId()).orElse(null);
            if (compound != null) {
                RankedCompoundDTO dto = new RankedCompoundDTO();
                dto.setId(compound.getId());
                dto.setName(compound.getName());
                dto.setAffinity(dr.getAffinity());
                dto.setRank(rank++);
                result.add(dto);
            }
        }
        
        return result;
    }
    
    /**
     * 根据分类获取化合物列表
     */
    public List<Compound> getByCategory(String category) {
        return compoundRepository.findByCategory(category);
    }
}