package com.drug.screen.drugscreenbackend.repository;

import com.drug.screen.drugscreenbackend.entity.Compound;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CompoundRepository extends JpaRepository<Compound, Long> {
}