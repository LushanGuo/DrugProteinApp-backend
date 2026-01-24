package com.drugscreen.platform.controller;

import com.drugscreen.platform.dto.ScoringResultDTO;
import com.drugscreen.platform.service.ScoringService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/analysis")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class AnalysisController {

    private final ScoringService scoringService;

    @PostMapping("/{compoundId}/calculate")
    public ResponseEntity<ScoringResultDTO> calculateScore(@PathVariable Long compoundId) {
        return ResponseEntity.ok(scoringService.analyze(compoundId));
    }
}
