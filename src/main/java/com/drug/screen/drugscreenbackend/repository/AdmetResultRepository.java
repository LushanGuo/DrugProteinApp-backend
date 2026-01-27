package com.drug.screen.drugscreenbackend.repository;

import com.drug.screen.drugscreenbackend.entity.AdmetResult;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdmetResultRepository extends JpaRepository<AdmetResult, Long> {
    AdmetResult findByCompoundId(Long compoundId);
}