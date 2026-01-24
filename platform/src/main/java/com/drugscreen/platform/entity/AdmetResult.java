package com.drugscreen.platform.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * ADMET 结果实体类
 * 对应数据库 admet_results 表
 */
@Data
@Entity
@Table(name = "admet_results")
public class AdmetResult {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "compound_id", nullable = false)
    private Long compoundId;
    
    @Column(name = "herg_toxicity")
    private Double hergToxicity; // hERG 心脏毒性风险 (0-1)
    
    @Column(name = "ames_toxicity")
    private Integer amesToxicity; // Ames 致突变性 (0=阴性, 1=阳性)
    
    @Column(name = "liver_toxicity")
    private Integer liverToxicity; // 肝毒性 (0=无, 1=有)
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
