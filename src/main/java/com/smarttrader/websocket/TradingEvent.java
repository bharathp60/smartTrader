package com.smarttrader.websocket;

import java.time.Instant;

public record TradingEvent(
    String eventType,
    String symbol,
    Object payload,
    Instant timestamp
) {}
