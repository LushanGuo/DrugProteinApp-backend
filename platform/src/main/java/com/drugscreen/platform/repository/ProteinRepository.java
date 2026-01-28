package com.drugscreen.platform.repository;

import com.drugscreen.platform.entity.Protein;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProteinRepository extends JpaRepository<Protein, Long> {
    
    /**
     * 根据蛋白名称查找
     */
    Optional<Protein> findByName(String name);
}
