# Broker Integration

The system communicates with external brokers via the `BrokerClient` interface.

## BrokerClient Interface
```java
public interface BrokerClient {
    String placeOrder(Order order);
    void cancelOrder(String orderId);
    List<Position> getPositions();
    double getAccountBalance();
}
```

## PaperBrokerClient
The default implementation. It simulates order fills based on real-time market data ticks. Slippage is modeled mathematically.

## Implementing a Live Broker
To implement a broker like Zerodha (Kite API):
1. Implement `BrokerClient` as `ZerodhaBrokerClient`.
2. Handle OAuth / Session token refresh.
3. Map internal `Order` models to broker-specific API structures.
4. Handle WebSocket streaming for order updates.

## Idempotency
To prevent double ordering, every order has a unique UUID `client_order_id`. The broker integration must map this to the broker's tagging system to ensure if a network timeout occurs, a retry doesn't result in 2x the position size.

## Configuration for Live Trading
You must explicitly set `LIVE_TRADING_ENABLED=true` in production environments. By default, the system will not initialize a live broker bean unless this is true.

## Safety Checklist
Before enabling LIVE mode:
- [ ] Run strategy in PAPER mode for at least 30 days.
- [ ] Validate Risk Engine parameters.
- [ ] Ensure API keys are injected via secure secrets manager.
- [ ] Test the Kill Switch endpoint in a staging environment.
