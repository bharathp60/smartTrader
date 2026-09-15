package com.smarttrader.broker;

import com.smarttrader.entity.OrderStatus;
import java.math.BigDecimal;
import java.time.Instant;

public record OrderResponse(
    String clientOrderId,
    String brokerOrderId,
    OrderStatus status,
    BigDecimal filledQuantity,
    BigDecimal averageFillPrice,
    String message,
    Instant timestamp
) {}
