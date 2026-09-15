package com.smarttrader.controller;

import com.smarttrader.ml.ModelRegistry;
import com.smarttrader.ml.PredictionResult;
import com.smarttrader.ml.PredictionService;
import com.smarttrader.ml.TradingModel;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/ml")
public class MLController {

    private final PredictionService predictionService;
    private final ModelRegistry modelRegistry;

    public MLController(PredictionService predictionService, ModelRegistry modelRegistry) {
        this.predictionService = predictionService;
        this.modelRegistry = modelRegistry;
    }

    @GetMapping("/models")
    @PreAuthorize("hasAnyRole('VIEWER', 'TRADER', 'ADMIN')")
    public ResponseEntity<List<Map<String, Object>>> getDeployedModels() {
        List<Map<String, Object>> models = modelRegistry.listModels().stream()
            .map(m -> Map.<String, Object>of(
                "name", m.getModelName(),
                "version", m.getModelVersion(),
                "featureVersion", m.getFeatureVersion(),
                "deployed", m.isDeployed()))
            .collect(Collectors.toList());
        return ResponseEntity.ok(models);
    }

    @GetMapping("/probability-threshold")
    @PreAuthorize("hasAnyRole('VIEWER', 'TRADER', 'ADMIN')")
    public ResponseEntity<Map<String, Double>> getProbabilityThreshold() {
        return ResponseEntity.ok(Map.of("threshold", predictionService.getProbabilityThreshold()));
    }
}
