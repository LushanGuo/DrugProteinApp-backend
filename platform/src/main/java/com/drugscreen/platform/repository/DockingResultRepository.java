package com.drugscreen.platform.repository;

import com.drugscreen.platform.entity.DockingResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DockingResultRepository extends JpaRepository<DockingResult, Long> {
    
    /**
     * 查找某个化合物的最佳对接结果（亲和力最低）
     */
    Optional<DockingResult> findTopByCompoundIdOrderByAffinityAsc(Long compoundId);
    
    /**
     * 查找某个化合物的所有对接结果
     */
    List<DockingResult> findByCompoundId(Long compoundId);
    
    /**
     * 获取 Top 10 化合物（按亲和力排序）
     */
    @Query("SELECT dr FROM DockingResult dr WHERE dr.affinity IS NOT NULL ORDER BY dr.affinity ASC")
    List<DockingResult> findTop10ByOrderByAffinityAsc();
}
