package com.smarttrader.broker;

import com.smarttrader.entity.OrderSide;
import com.smarttrader.entity.OrderType;
import java.math.BigDecimal;

public record OrderRequest(
    String clientOrderId,
    String symbol,
    OrderSide side,
    OrderType orderType,
    BigDecimal quantity,
    BigDecimal limitPrice,
    BigDecimal stopPrice
) {}
