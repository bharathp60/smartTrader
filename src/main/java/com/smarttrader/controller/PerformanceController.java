package com.smarttrader.controller;

import com.smarttrader.entity.Trade;
import com.smarttrader.performance.PerformanceEngine;
import com.smarttrader.performance.PerformanceMetrics;
import com.smarttrader.repository.TradeRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class PerformanceController {

    private final PerformanceEngine performanceEngine;
    private final TradeRepository tradeRepository;

    public PerformanceController(PerformanceEngine performanceEngine, TradeRepository tradeRepository) {
        this.performanceEngine = performanceEngine;
        this.tradeRepository = tradeRepository;
    }

    @GetMapping("/performance")
    @PreAuthorize("hasAnyRole('VIEWER', 'TRADER', 'ADMIN')")
    public ResponseEntity<PerformanceMetrics> getTodayPerformance() {
        return ResponseEntity.ok(performanceEngine.calculateDailyMetrics(LocalDate.now()));
    }

    @GetMapping("/performance/weekly")
    @PreAuthorize("hasAnyRole('VIEWER', 'TRADER', 'ADMIN')")
    public ResponseEntity<PerformanceMetrics> getWeeklyPerformance() {
        return ResponseEntity.ok(performanceEngine.calculateWeeklyMetrics(LocalDate.now()));
    }

    @GetMapping("/performance/monthly")
    @PreAuthorize("hasAnyRole('VIEWER', 'TRADER', 'ADMIN')")
    public ResponseEntity<PerformanceMetrics> getMonthlyPerformance() {
        LocalDate now = LocalDate.now();
        return ResponseEntity.ok(performanceEngine.calculateMonthlyMetrics(now.getYear(), now.getMonthValue()));
    }

    @GetMapping("/trades")
    @PreAuthorize("hasAnyRole('VIEWER', 'TRADER', 'ADMIN')")
    public ResponseEntity<List<Trade>> getCompletedTrades() {
        return ResponseEntity.ok(
            tradeRepository.findByExecutedAtAfterOrderByExecutedAtDesc(
                Instant.now().minus(30, ChronoUnit.DAYS)));
    }
}
