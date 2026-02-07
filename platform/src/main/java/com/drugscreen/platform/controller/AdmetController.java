package com.drugscreen.platform.controller;

import com.drugscreen.platform.dto.ApiResponse;
import com.drugscreen.platform.entity.AdmetResult;
import com.drugscreen.platform.service.AdmetService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/admet")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AdmetController {

    private final AdmetService admetService;

    /**
     * 提交ADMET预测任务
     * @param compoundId 化合物ID
     * @return 任务执行结果
     */
    @RequestMapping(value = "/predict", method = {RequestMethod.GET, RequestMethod.POST})
    public ResponseEntity<ApiResponse<AdmetResult>> submitAdmetPrediction(@RequestParam Long compoundId) {
        try {
            log.info("提交ADMET预测任务，化合物ID: {}", compoundId);
            AdmetResult result = admetService.executeAdmetPrediction(compoundId);
            return ResponseEntity.ok(ApiResponse.success("预测成功", result));
        } catch (Exception e) {
            log.error("ADMET预测失败: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error("预测失败: " + e.getMessage()));
        }
    }

    /**
     * 根据化合物ID查询ADMET结果
     * @param compoundId 化合物ID
     * @return ADMET预测结果
     */
    @GetMapping("/result/compound/{compoundId}")
    public ResponseEntity<ApiResponse<AdmetResult>> getAdmetResultByCompoundId(@PathVariable Long compoundId) {
        try {
            AdmetResult result = admetService.getAdmetResultByCompoundId(compoundId);
            if (result == null) {
                return ResponseEntity.ok(ApiResponse.error("ADMET预测结果不存在"));
            }
            return ResponseEntity.ok(ApiResponse.success("查询成功", result));
        } catch (Exception e) {
            log.error("查询ADMET结果失败: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error("查询失败: " + e.getMessage()));
        }
    }

    /**
     * 根据结果ID查询ADMET结果
     * @param id 结果ID
     * @return ADMET预测结果
     */
    @GetMapping("/result/{id}")
    public ResponseEntity<ApiResponse<AdmetResult>> getAdmetResultById(@PathVariable Long id) {
        try {
            AdmetResult result = admetService.getAdmetResultById(id);
            if (result == null) {
                return ResponseEntity.ok(ApiResponse.error("ADMET预测结果不存在"));
            }
            return ResponseEntity.ok(ApiResponse.success("查询成功", result));
        } catch (Exception e) {
            log.error("查询ADMET结果失败: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error("查询失败: " + e.getMessage()));
        }
    }
}
