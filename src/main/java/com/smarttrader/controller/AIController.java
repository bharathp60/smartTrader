package com.smarttrader.controller;

import com.smarttrader.ai.AIAnalysisService;
import com.smarttrader.ai.AiDecisionDto;
import com.smarttrader.entity.AiDecision;
import com.smarttrader.exception.EntityNotFoundException;
import com.smarttrader.regime.MarketRegime;
import com.smarttrader.repository.AiDecisionRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/ai")
public class AIController {

    private final AIAnalysisService aiAnalysisService;
    private final AiDecisionRepository aiDecisionRepository;

    public AIController(AIAnalysisService aiAnalysisService, AiDecisionRepository aiDecisionRepository) {
        this.aiAnalysisService = aiAnalysisService;
        this.aiDecisionRepository = aiDecisionRepository;
    }

    @GetMapping("/decisions")
    @PreAuthorize("hasAnyRole('TRADER', 'ADMIN')")
    public ResponseEntity<List<AiDecision>> getRecentDecisions() {
        return ResponseEntity.ok(aiDecisionRepository.findTop20ByOrderByCreatedAtDesc());
    }

    @GetMapping("/decisions/{id}")
    @PreAuthorize("hasAnyRole('TRADER', 'ADMIN')")
    public ResponseEntity<AiDecision> getDecision(@PathVariable UUID id) {
        return aiDecisionRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new EntityNotFoundException("AiDecision", id.toString()));
    }

    @PostMapping("/analyze/{symbol}")
    @PreAuthorize("hasAnyRole('TRADER', 'ADMIN')")
    public ResponseEntity<AiDecisionDto> triggerAnalysis(@PathVariable String symbol) {
        // Trigger an on-demand analysis with neutral context (no ML prediction, no ranking score)
        // This is useful for manual review — the full autonomous pipeline includes these
        AiDecisionDto decision = aiAnalysisService.analyzeStock(symbol, null, null, MarketRegime.SIDEWAYS);
        return ResponseEntity.ok(decision);
    }
}
