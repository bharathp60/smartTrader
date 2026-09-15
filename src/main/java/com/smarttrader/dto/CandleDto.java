package com.smarttrader.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record CandleDto(
    Instant timestamp,
    BigDecimal open,
    BigDecimal high,
    BigDecimal low,
    BigDecimal close,
    long volume
) {}
