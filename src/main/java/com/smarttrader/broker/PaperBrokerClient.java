package com.smarttrader.broker;

import com.smarttrader.entity.OrderStatus;
import com.smarttrader.entity.TradingModeRecord;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Primary
public class PaperBrokerClient implements BrokerClient {

    private final ConcurrentHashMap<String, OrderResponse> orderBook = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, BigDecimal> positions = new ConcurrentHashMap<>();
    private BigDecimal simulatedCapital = new BigDecimal("1000000");
    private BigDecimal usedMargin = BigDecimal.ZERO;

    @Override
    public String getBrokerName() {
        return "PaperBroker";
    }

    @Override
    public TradingModeRecord getSupportedMode() {
        return TradingModeRecord.PAPER; // Enum must exist
    }

    @Override
    public AccountInfo getAccountInfo() {
        return new AccountInfo(
            simulatedCapital,
            simulatedCapital.subtract(usedMargin),
            usedMargin,
            BigDecimal.ZERO,
            "USD"
        );
    }

    @Override
    public List<OrderResponse> getOpenOrders() {
        List<OrderResponse> openOrders = new ArrayList<>();
        for (OrderResponse res : orderBook.values()) {
            if (res.status() == OrderStatus.SUBMITTED || res.status() == OrderStatus.PARTIALLY_FILLED) {
                openOrders.add(res);
            }
        }
        return openOrders;
    }

    @Override
    public OrderResponse getOrderStatus(String clientOrderId) {
        return orderBook.get(clientOrderId);
    }

    @Override
    public OrderResponse placeOrder(OrderRequest request) {
        BigDecimal fillPrice = request.limitPrice() != null ? request.limitPrice() : BigDecimal.valueOf(100); // Simulate market price
        // Simulate slippage 0.1%
        fillPrice = fillPrice.multiply(new BigDecimal("1.001")).setScale(2, RoundingMode.HALF_UP);
        
        OrderResponse response = new OrderResponse(
            request.clientOrderId(),
            UUID.randomUUID().toString(),
            OrderStatus.FILLED,
            request.quantity(),
            fillPrice,
            "Simulated fill",
            Instant.now()
        );
        orderBook.put(request.clientOrderId(), response);
        
        // Update positions (simplified)
        BigDecimal currentQty = positions.getOrDefault(request.symbol(), BigDecimal.ZERO);
        if (request.side().name().equals("BUY")) {
            positions.put(request.symbol(), currentQty.add(request.quantity()));
        } else {
            positions.put(request.symbol(), currentQty.subtract(request.quantity()));
        }
        return response;
    }

    @Override
    public boolean cancelOrder(String clientOrderId) {
        OrderResponse order = orderBook.get(clientOrderId);
        if (order != null && (order.status() == OrderStatus.SUBMITTED || order.status() == OrderStatus.PARTIALLY_FILLED)) {
            orderBook.put(clientOrderId, new OrderResponse(
                order.clientOrderId(),
                order.brokerOrderId(),
                OrderStatus.CANCELLED,
                order.filledQuantity(),
                order.averageFillPrice(),
                "Cancelled",
                Instant.now()
            ));
            return true;
        }
        return false;
    }

    @Override
    public boolean isConnected() {
        return true;
    }
}
