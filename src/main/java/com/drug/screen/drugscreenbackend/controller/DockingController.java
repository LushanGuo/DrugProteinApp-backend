package com.drug.screen.drugscreenbackend.controller;

import com.drug.screen.drugscreenbackend.entity.DockingResult;
import com.drug.screen.drugscreenbackend.service.DockingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/docking")
public class DockingController {

    @Autowired
    private DockingService dockingService;

    /**
     * 提交对接任务
     * @param compoundId 化合物ID
     * @return 任务状态
     */
    @RequestMapping(value = "/submit", method = {RequestMethod.GET, RequestMethod.POST})
    public ResponseEntity<?> submitDocking(@RequestParam String compoundId) {
        try {
            dockingService.executeDocking(compoundId);
            return ResponseEntity.ok()
                    .body("{\"status\": \"任务已提交，正在执行\"}");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\": \"提交失败: " + e.getMessage() + "\"}");
        }
    }

    /**
     * 查询对接结果
     * @param taskId 任务ID
     * @return 对接结果
     */
    @GetMapping("/result/{taskId}")
    public ResponseEntity<?> getDockingResult(@PathVariable Long taskId) {
        try {
            DockingResult result = dockingService.getDockingResult(taskId);
            if (result == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("{\"error\": \"任务不存在\"}");
            }
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\": \"查询失败: " + e.getMessage() + "\"}");
        }
    }

    /**
     * 根据化合物ID查询最新的对接结果
     * @param compoundId 化合物ID
     * @return 对接结果
     */
    @GetMapping("/result/compound/{compoundId}")
    public ResponseEntity<?> getLatestDockingResultByCompoundId(@PathVariable String compoundId) {
        try {
            DockingResult result = dockingService.getLatestDockingResult(compoundId);
            if (result == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("{\"error\": \"对接结果不存在\"}");
            }
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\": \"查询失败: " + e.getMessage() + "\"}");
        }
    }
}