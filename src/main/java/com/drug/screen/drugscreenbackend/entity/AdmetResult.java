package com.drug.screen.drugscreenbackend.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "admet_results")
public class AdmetResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "compound_id", nullable = false)
    private Long compoundId;

    @Column(name = "absorption")
    private Double absorption;

    @Column(name = "distribution")
    private Double distribution;

    @Column(name = "metabolism")
    private Double metabolism;

    @Column(name = "excretion")
    private Double excretion;

    @Column(name = "toxicity")
    private Double toxicity;

    @Column(name = "create_time")
    private LocalDateTime createTime;
}