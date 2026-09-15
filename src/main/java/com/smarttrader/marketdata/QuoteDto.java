package com.smarttrader.marketdata;

import java.math.BigDecimal;
import java.time.Instant;

public record QuoteDto(
    String symbol,
    BigDecimal lastPrice,
    BigDecimal bidPrice,
    BigDecimal askPrice,
    BigDecimal volume,
    Instant timestamp,
    String source
) {}
