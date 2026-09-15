package com.smarttrader.controller;

import com.smarttrader.backtest.BacktestEngine;
import com.smarttrader.backtest.BacktestRequest;
import com.smarttrader.backtest.BacktestResult;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/backtest")
public class BacktestController {

    private final BacktestEngine backtestEngine;

    public BacktestController(BacktestEngine backtestEngine) {
        this.backtestEngine = backtestEngine;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('TRADER', 'ADMIN')")
    public ResponseEntity<BacktestResult> runBacktest(@Valid @RequestBody BacktestRequest request) {
        BacktestResult result = backtestEngine.runBacktest(request);
        return ResponseEntity.ok(result);
    }
}
