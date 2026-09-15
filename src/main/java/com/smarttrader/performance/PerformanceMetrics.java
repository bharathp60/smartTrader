package com.smarttrader.performance;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

public record PerformanceMetrics(
    LocalDate date,
    BigDecimal dailyPnl,
    BigDecimal weeklyPnl,
    BigDecimal monthlyPnl,
    double winRate,
    double profitFactor,
    double expectancy,
    double maxDrawdownPct,
    double sharpeRatio,
    double sortinoRatio,
    int totalTrades,
    int winningTrades,
    int losingTrades,
    Map<String, BigDecimal> strategyPerformance,
    Map<String, BigDecimal> sectorPerformance
) {}
