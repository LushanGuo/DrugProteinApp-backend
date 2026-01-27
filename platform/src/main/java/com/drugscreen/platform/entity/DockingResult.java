package com.drugscreen.platform.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 分子对接结果实体类
 * 对应数据库 docking_results 表
 */
@Data
@Entity
@Table(name = "docking_results")
public class DockingResult {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "compound_id", nullable = false)
    private Long compoundId; // 化合物 ID
    
    @Column(name = "protein_id")
    private Long proteinId; // 蛋白 ID
    
    @Column(name = "affinity")
    private Double affinity; // 结合亲和力 (kcal/mol)
    
    @Column(name = "docked_pdbqt_content", columnDefinition = "TEXT")
    private String dockedPdbqtContent; // 对接后的 PDBQT 内容
    
    @Column(name = "rmsd_lb")
    private Double rmsdLb; // RMSD lower bound
    
    @Column(name = "rmsd_ub")
    private Double rmsdUb; // RMSD upper bound
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
