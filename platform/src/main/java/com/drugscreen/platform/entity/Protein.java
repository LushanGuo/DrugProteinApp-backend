package com.drugscreen.platform.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

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
    
    @Column(nullable = false, unique = true, length = 50)
    private String name; // 蛋白名称，如 "1e9h"
    
    @Column(columnDefinition = "TEXT")
    private String description; // 蛋白描述
    
    @Column(name = "pdb_content", columnDefinition = "TEXT", nullable = false)
    private String pdbContent; // PDB 文件内容
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    public Protein(String name, String pdbContent) {
        this.name = name;
        this.pdbContent = pdbContent;
    }
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
