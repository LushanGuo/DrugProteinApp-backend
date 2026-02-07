package com.drugscreen.platform.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 化合物实体类
 * 对应数据库 compounds 表
 */
@Data
@Entity
@Table(name = "compounds")
public class Compound {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name; // 中文名

    @Column(name = "english_name")
    private String englishName;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String smiles; // SMILES 结构式

    @Column(name = "molecular_weight")
    private Double molecularWeight;

    @Column(name = "log_p")
    private Double logP;

    private String category; // 分类

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "raw_pdbqt_content", columnDefinition = "TEXT")
    private String rawPdbqtContent; // 原始 PDBQT 内容（对接前）

    @Column(name = "heavy_atom_count")
    private Integer heavyAtomCount; // 重原子数

    @Column(name = "hbd")
    private Integer hbd; // 氢键供体数

    @Column(name = "hba")
    private Integer hba; // 氢键受体数

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}