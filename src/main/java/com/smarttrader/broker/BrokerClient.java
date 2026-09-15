package com.smarttrader.broker;

import com.smarttrader.entity.TradingModeRecord;
import java.util.List;

public interface BrokerClient {
    String getBrokerName();
    TradingModeRecord getSupportedMode();
    AccountInfo getAccountInfo();
    List<OrderResponse> getOpenOrders();
    OrderResponse getOrderStatus(String clientOrderId);
    OrderResponse placeOrder(OrderRequest request);
    boolean cancelOrder(String clientOrderId);
    boolean isConnected();
}
