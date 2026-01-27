package com.drugscreen.platform.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * 蛋白质实体类
 * 对应数据库 proteins 表
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "proteins")
public class Protein {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String name; // 蛋白名称，如 "1e9h"
    
    @Column(name = "pdb_content", columnDefinition = "TEXT")
    private String pdbContent; // PDB 文件内容
    
    public Protein(String name, String pdbContent) {
        this.name = name;
        this.pdbContent = pdbContent;
    }
}
