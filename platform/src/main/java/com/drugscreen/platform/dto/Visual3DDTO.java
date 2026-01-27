package com.drugscreen.platform.dto;

import lombok.Data;

/**
 * 3D 可视化数据传输对象
 * 用于前端 3D 分子查看器
 */
@Data
public class Visual3DDTO {
    private String proteinPdb;           // 蛋白质 PDB 内容（如 1e9h.pdb）
    private String dockedLigandPdbqt;    // 对接后的配体 PDBQT 内容
    private Double bindingEnergy;        // 结合能 (kcal/mol)
}
