package com.drugscreen.platform.controller;

import com.drugscreen.platform.dto.RankedCompoundDTO;
import com.drugscreen.platform.dto.StructureDTO;
import com.drugscreen.platform.entity.Compound;
import com.drugscreen.platform.service.CompoundService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/compounds")
@CrossOrigin(origins = "*") // 允许 React Native (Expo) 跨域访问
@RequiredArgsConstructor
public class CompoundController {

    private final CompoundService compoundService;

    /**
     * 获取化合物列表 (支持分页和搜索)
     * GET /api/compounds?page=0&size=10&keyword=槲皮素
     */
    @GetMapping
    public ResponseEntity<Page<Compound>> getAllCompounds(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword) {

        try {
            // 默认按 ID 排序
            PageRequest pageRequest = PageRequest.of(page, size, Sort.by("id").ascending());
            Page<Compound> result = compoundService.getCompounds(keyword, pageRequest);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("获取化合物列表失败: ", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 获取化合物详情
     * GET /api/compounds/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<Compound> getCompoundById(@PathVariable Long id) {
        try {
            Compound compound = compoundService.getCompoundById(id);
            return ResponseEntity.ok(compound);
        } catch (RuntimeException e) {
            log.warn("查询化合物不存在 ID: {}", id);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("获取化合物详情异常 ID: " + id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 获取化合物的 3D 结构数据
     * GET /api/compounds/{id}/structure
     */
    @GetMapping("/{id}/structure")
    public ResponseEntity<StructureDTO> getStructure(@PathVariable Long id) {
        try {
            StructureDTO structure = compoundService.getStructure(id);
            return ResponseEntity.ok(structure);
        } catch (RuntimeException e) {
            log.warn("查询化合物结构不存在 ID: {}", id);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("获取化合物结构异常 ID: " + id, e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * 获取 Top 10 化合物（按亲和力排序）
     * GET /api/compounds/top10
     */
    @GetMapping("/top10")
    public ResponseEntity<List<RankedCompoundDTO>> getTop10() {
        try {
            List<RankedCompoundDTO> top10 = compoundService.getTop10Compounds();
            return ResponseEntity.ok(top10);
        } catch (Exception e) {
            log.error("获取 Top 10 化合物失败", e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * 根据分类获取化合物列表
     * GET /api/compounds/category/{categoryName}
     */
    @GetMapping("/category/{categoryName}")
    public ResponseEntity<List<Compound>> getByCategory(@PathVariable String categoryName) {
        try {
            List<Compound> compounds = compoundService.getByCategory(categoryName);
            return ResponseEntity.ok(compounds);
        } catch (Exception e) {
            log.error("根据分类获取化合物失败: category={}", categoryName, e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
