package com.smarttrader.marketdata;

import java.math.BigDecimal;
import java.time.Instant;

public record CandleDto(
    String symbol,
    String timeframe,
    Instant openedAt,
    BigDecimal open,
    BigDecimal high,
    BigDecimal low,
    BigDecimal close,
    BigDecimal volume,
    String source
) {}
