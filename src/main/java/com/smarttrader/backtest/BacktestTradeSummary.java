package com.smarttrader.backtest;

import com.smarttrader.entity.OrderSide;
import java.math.BigDecimal;
import java.time.Instant;

public record BacktestTradeSummary(
    String symbol,
    OrderSide side,
    BigDecimal quantity,
    BigDecimal entryPrice,
    BigDecimal exitPrice,
    BigDecimal pnl,
    double returnPct,
    Instant entryTime,
    Instant exitTime,
    String exitReason
) {}
