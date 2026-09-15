package com.smarttrader.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record InstrumentDto(
    UUID id,
    String symbol,
    String exchange,
    String companyName,
    String sector,
    String industry,
    BigDecimal marketCap,
    String status,
    int lotSize,
    BigDecimal tickSize,
    BigDecimal averageDailyVolume
) {}
