package com.smarttrader.controller;

import com.smarttrader.entity.RiskEvent;
import com.smarttrader.repository.RiskEventRepository;
import com.smarttrader.risk.RiskParameters;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/risk")
public class RiskController {

    private final RiskParameters riskParameters;
    private final RiskEventRepository riskEventRepository;

    public RiskController(RiskParameters riskParameters, RiskEventRepository riskEventRepository) {
        this.riskParameters = riskParameters;
        this.riskEventRepository = riskEventRepository;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('VIEWER', 'TRADER', 'ADMIN')")
    public ResponseEntity<Map<String, Object>> getRiskStatus() {
        return ResponseEntity.ok(Map.of(
            "maxDailyLossPercent",      riskParameters.maxDailyLossPercent(),
            "maxPositionSizePercent",   riskParameters.maxPositionSizePercent(),
            "maxOpenPositions",         riskParameters.maxOpenPositions(),
            "maxSectorExposurePercent", riskParameters.maxSectorExposurePercent(),
            "minRiskRewardRatio",       riskParameters.minRiskRewardRatio(),
            "maxVolatilityPercent",     riskParameters.maxVolatilityPercent(),
            "maxConsecutiveLosses",     riskParameters.maxConsecutiveLosses()
        ));
    }

    @GetMapping("/events")
    @PreAuthorize("hasAnyRole('TRADER', 'ADMIN')")
    public ResponseEntity<List<RiskEvent>> getRecentRiskEvents() {
        Instant since = Instant.now().minus(7, ChronoUnit.DAYS);
        return ResponseEntity.ok(riskEventRepository.findByDecisionAndOccurredAtAfterOrderByOccurredAtDesc("REJECTED", since));
    }
}
