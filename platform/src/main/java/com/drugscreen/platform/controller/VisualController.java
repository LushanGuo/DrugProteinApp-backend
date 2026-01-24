package com.drugscreen.platform.controller;

import com.drugscreen.platform.dto.Visual3DDTO;
import com.drugscreen.platform.service.Visual3DService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 3D 可视化控制器
 * 处理 3D 分子查看器所需的数据
 */
@Slf4j
@RestController
@RequestMapping("/api/visual")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class VisualController {
    
    private final Visual3DService visual3DService;
    
    /**
     * 获取 3D 可视化所需的所有数据
     * GET /api/visual/{compoundId}
     */
    @GetMapping("/{compoundId}")
    public ResponseEntity<Visual3DDTO> getVisualData(@PathVariable Long compoundId) {
        try {
            log.info("开始获取化合物 {} 的 3D 可视化数据", compoundId);
            Visual3DDTO dto = visual3DService.getVisual3DData(compoundId);
            return ResponseEntity.ok(dto);
        } catch (RuntimeException e) {
            log.error("获取 3D 可视化数据失败: compoundId={}, 错误: {}", compoundId, e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("获取 3D 可视化数据异常: compoundId={}", compoundId, e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
