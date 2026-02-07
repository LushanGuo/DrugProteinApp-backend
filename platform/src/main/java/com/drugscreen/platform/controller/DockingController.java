package com.drugscreen.platform.controller;

import com.drugscreen.platform.dto.ApiResponse;
import com.drugscreen.platform.entity.DockingResult;
import com.drugscreen.platform.service.DockingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/docking")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class DockingController {

    private final DockingService dockingService;

    /**
     * 提交对接任务
     * @param compoundId 化合物ID
     * @return 任务状态
     */
    @RequestMapping(value = "/submit", method = {RequestMethod.GET, RequestMethod.POST})
    public ResponseEntity<ApiResponse<String>> submitDocking(@RequestParam Long compoundId) {
        try {
            log.info("提交对接任务，化合物ID: {}", compoundId);
            dockingService.executeDocking(compoundId);
            return ResponseEntity.ok(ApiResponse.success("任务已提交，正在执行", "PENDING"));
        } catch (Exception e) {
            log.error("提交对接任务失败: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error("提交失败: " + e.getMessage()));
        }
    }

    /**
     * 查询对接结果
     * @param taskId 任务ID
     * @return 对接结果
     */
    @GetMapping("/result/{taskId}")
    public ResponseEntity<ApiResponse<DockingResult>> getDockingResult(@PathVariable Long taskId) {
        try {
            DockingResult result = dockingService.getDockingResult(taskId);
            if (result == null) {
                return ResponseEntity.ok(ApiResponse.error("任务不存在"));
            }
            return ResponseEntity.ok(ApiResponse.success("查询成功", result));
        } catch (Exception e) {
            log.error("查询对接结果失败: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error("查询失败: " + e.getMessage()));
        }
    }

    /**
     * 根据化合物ID查询最新的对接结果
     * @param compoundId 化合物ID
     * @return 对接结果
     */
    @GetMapping("/result/compound/{compoundId}")
    public ResponseEntity<ApiResponse<DockingResult>> getLatestDockingResultByCompoundId(@PathVariable Long compoundId) {
        try {
            DockingResult result = dockingService.getLatestDockingResult(compoundId);
            if (result == null) {
                return ResponseEntity.ok(ApiResponse.error("对接结果不存在"));
            }
            return ResponseEntity.ok(ApiResponse.success("查询成功", result));
        } catch (Exception e) {
            log.error("查询对接结果失败: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error("查询失败: " + e.getMessage()));
        }
    }
}
