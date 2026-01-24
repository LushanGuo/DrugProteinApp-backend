package com.drugscreen.platform.repository;

import com.drugscreen.platform.entity.AnalysisReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AnalysisReportRepository extends JpaRepository<AnalysisReport, Long> {
    
    /**
     * 根据化合物ID查询分析报告
     */
    List<AnalysisReport> findByCompoundId(Long compoundId);
    
    /**
     * 根据化合物ID查询最新的分析报告
     */
    AnalysisReport findFirstByCompoundIdOrderByCreatedAtDesc(Long compoundId);
}
