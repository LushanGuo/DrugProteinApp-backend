package com.drugscreen.platform.dto;

import lombok.Data;

/**
 * 3D 结构数据传输对象
 */
@Data
public class StructureDTO {
    private Long compoundId;
    private String compoundName;
    private String receptorPdb;    // 受体蛋白的 PDB 格式数据
    private String ligandPdbqt;    // 配体的 PDBQT 格式数据
}
