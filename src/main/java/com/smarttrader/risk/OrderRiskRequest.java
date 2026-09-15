package com.smarttrader.risk;

import com.smarttrader.entity.OrderSide;
import java.math.BigDecimal;
import java.util.Map;

public record OrderRiskRequest(
    String symbol,
    OrderSide side,
    BigDecimal quantity,
    BigDecimal entryPrice,
    BigDecimal stopLoss,
    BigDecimal target,
    BigDecimal currentCapital,
    BigDecimal currentDailyLoss,
    int currentOpenPositions,
    int currentConsecutiveLosses,
    Map<String, BigDecimal> sectorExposures,
    double atrPercent,
    double avgDailyVolume,
    boolean isMarketOpen,
    String tradingMode
) {}
