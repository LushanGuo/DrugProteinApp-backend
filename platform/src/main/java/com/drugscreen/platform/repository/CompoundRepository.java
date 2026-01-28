package com.drugscreen.platform.repository;

import com.drugscreen.platform.entity.Compound;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CompoundRepository extends JpaRepository<Compound, Long> {
    // 支持按名称模糊搜索 (用于前端搜索框)
    Page<Compound> findByNameContainingOrEnglishNameContainingIgnoreCase(String name, String englishName, Pageable pageable);

    // 检查是否存在 (防止重复导入)
    boolean existsByName(String name);
    
    // 根据分类查询
    List<Compound> findByCategory(String category);
}
