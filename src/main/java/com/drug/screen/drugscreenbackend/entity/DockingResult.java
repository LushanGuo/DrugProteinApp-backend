package com.drug.screen.drugscreenbackend.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "docking_results")
public class DockingResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "compound_id", nullable = false, length = 50)
    private String compoundId;

    @Column(name = "affinity")
    private Double affinity;

    @Column(name = "status", length = 20, nullable = false)
    private String status;

    @Column(name = "docking_log", columnDefinition = "TEXT")
    private String dockingLog;

    @Column(name = "create_time")
    private LocalDateTime createTime;
}