package com.smarttrader.broker;

import java.math.BigDecimal;

public record AccountInfo(
    BigDecimal totalCapital,
    BigDecimal availableCapital,
    BigDecimal usedMargin,
    BigDecimal unrealizedPnl,
    String currency
) {}
