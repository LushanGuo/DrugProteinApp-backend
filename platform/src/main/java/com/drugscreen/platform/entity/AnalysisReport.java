package com.drugscreen.platform.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Entity
@Table(name = "analysis_reports")
public class AnalysisReport {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "compound_id")
    private Long compoundId;

    private Double totalScore;
    private Double potencyScore;
    private Double safetyScore;
    private Double druglikenessScore;

    private Boolean isVetoed;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "advice_tags")
    private List<String> adviceTags;

    @Column(columnDefinition = "TEXT")
    private String expertAdvice;

    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() { createdAt = LocalDateTime.now(); }
}
