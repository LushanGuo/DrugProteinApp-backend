package com.drug.screen.drugscreenbackend.repository;

import com.drug.screen.drugscreenbackend.entity.DockingResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DockingResultRepository extends JpaRepository<DockingResult, Long> {

    @Query("SELECT dr FROM DockingResult dr WHERE dr.compoundId = :compoundId ORDER BY dr.createTime DESC")
    List<DockingResult> findByCompoundIdOrderByCreateTimeDesc(@Param("compoundId") String compoundId);
}