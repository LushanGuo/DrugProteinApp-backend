package com.drugscreen.platform.repository;

import com.drugscreen.platform.entity.AdmetResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AdmetResultRepository extends JpaRepository<AdmetResult, Long> {
    
    /**
     * 根据化合物ID查询ADMET结果
     */
    Optional<AdmetResult> findByCompoundId(Long compoundId);
}
