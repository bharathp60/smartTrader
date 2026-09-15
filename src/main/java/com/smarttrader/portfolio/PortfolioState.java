package com.smarttrader.portfolio;

import java.math.BigDecimal;
import java.util.Map;

public record PortfolioState(
    BigDecimal totalCapital,
    BigDecimal availableCapital,
    int openPositionCount,
    Map<String, BigDecimal> sectorExposures,
    Map<String, BigDecimal> symbolExposures,
    BigDecimal totalExposure,
    BigDecimal unrealizedPnl,
    int consecutiveLosses,
    double dailyDrawdown
) {}
