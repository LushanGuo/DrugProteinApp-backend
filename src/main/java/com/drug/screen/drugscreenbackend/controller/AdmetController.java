package com.drug.screen.drugscreenbackend.controller;

import com.drug.screen.drugscreenbackend.entity.AdmetResult;
import com.drug.screen.drugscreenbackend.service.AdmetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admet")
public class AdmetController {

    @Autowired
    private AdmetService admetService;

    /**
     * 提交ADMET预测任务
     * @param compoundId 化合物ID
     * @return 任务执行结果
     */
    @RequestMapping(value = "/predict", method = {RequestMethod.GET, RequestMethod.POST})
    public ResponseEntity<?> submitAdmetPrediction(@RequestParam String compoundId) {
        try {
            AdmetResult result = admetService.executeAdmetPrediction(compoundId);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\": \"预测失败: " + e.getMessage() + "\"}");
        }
    }

    /**
     * 根据化合物ID查询ADMET结果
     * @param compoundId 化合物ID
     * @return ADMET预测结果
     */
    @GetMapping("/result/compound/{compoundId}")
    public ResponseEntity<?> getAdmetResultByCompoundId(@PathVariable String compoundId) {
        try {
            AdmetResult result = admetService.getAdmetResultByCompoundId(compoundId);
            if (result == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("{\"error\": \"ADMET预测结果不存在\"}");
            }
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\": \"查询失败: " + e.getMessage() + "\"}");
        }
    }

    /**
     * 根据结果ID查询ADMET结果
     * @param id 结果ID
     * @return ADMET预测结果
     */
    @GetMapping("/result/{id}")
    public ResponseEntity<?> getAdmetResultById(@PathVariable Long id) {
        try {
            AdmetResult result = admetService.getAdmetResultById(id);
            if (result == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("{\"error\": \"ADMET预测结果不存在\"}");
            }
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\": \"查询失败: " + e.getMessage() + "\"}");
        }
    }
}