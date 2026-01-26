package com.drug.screen.drugscreenbackend.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "compounds")
public class Compound {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long compoundId;

    @Column(name = "smiles", nullable = false, length = 500)
    private String smiles;

    @Column(name = "compound_name", length = 100)
    private String compoundName;

    @Column(name = "create_time")
    private LocalDateTime createTime;
}