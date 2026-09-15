package com.smarttrader.controller;

import com.smarttrader.config.TradingProperties;
import java.time.Clock;
import java.time.Instant;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/health")
public class HealthController {

    private final TradingProperties tradingProperties;
    private final Clock clock;

    public HealthController(TradingProperties tradingProperties, Clock clock) {
        this.tradingProperties = tradingProperties;
        this.clock = clock;
    }

    @GetMapping
    public ResponseEntity<HealthResponse> health() {
        return ResponseEntity.ok(new HealthResponse(
                "UP",
                tradingProperties.mode().name(),
                tradingProperties.liveTradingAllowed(),
                Instant.now(clock)
        ));
    }

    public record HealthResponse(String status, String tradingMode, boolean liveTradingAllowed, Instant timestamp) {
    }
}
