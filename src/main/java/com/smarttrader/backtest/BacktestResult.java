package com.smarttrader.backtest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record BacktestResult(
    String strategyName,
    String symbol,
    LocalDate startDate,
    LocalDate endDate,
    BigDecimal initialCapital,
    BigDecimal finalCapital,
    BigDecimal totalReturn,
    double totalReturnPct,
    double cagr,
    double winRate,
    double profitFactor,
    double maxDrawdownPct,
    double sharpeRatio,
    double sortinoRatio,
    int totalTrades,
    int winningTrades,
    int losingTrades,
    BigDecimal avgWin,
    BigDecimal avgLoss,
    BigDecimal largestWin,
    BigDecimal largestLoss,
    double expectancy,
    List<BacktestTradeSummary> trades,
    List<BigDecimal> equityCurve
) {}
