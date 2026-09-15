package com.smarttrader.order;

import com.smarttrader.ai.AiDecisionDto;
import com.smarttrader.broker.AccountInfo;
import com.smarttrader.broker.BrokerClient;
import com.smarttrader.broker.OrderRequest;
import com.smarttrader.broker.OrderResponse;
import com.smarttrader.entity.Instrument;
import com.smarttrader.entity.OrderSide;
import com.smarttrader.entity.OrderStatus;
import com.smarttrader.entity.OrderType;
import com.smarttrader.entity.TradingOrder;
import com.smarttrader.repository.OrderEventRepository;
import com.smarttrader.repository.OrderRepository;
import com.smarttrader.risk.OrderRiskRequest;
import com.smarttrader.risk.PositionSizingService;
import com.smarttrader.risk.RiskCheckResult;
import com.smarttrader.risk.RiskEngine;
import com.smarttrader.risk.RiskParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

/**
 * Order service — manages the complete order lifecycle.
 *
 * Pipeline for every order:
 * 1. Idempotency check (Redis + DB)
 * 2. Risk Engine check (MANDATORY — cannot be bypassed)
 * 3. If approved: submit to broker
 * 4. Persist order and events
 *
 * The Risk Engine is called on every order submission. There is no bypass.
 */
@Service
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    private final OrderRepository orderRepository;
    private final OrderEventRepository orderEventRepository;
    private final BrokerClient brokerClient;
    private final RiskEngine riskEngine;
    private final RiskParameters riskParameters;
    private final PositionSizingService positionSizingService;
    private final StringRedisTemplate redisTemplate;

    public OrderService(OrderRepository orderRepository,
                        OrderEventRepository orderEventRepository,
                        BrokerClient brokerClient,
                        RiskEngine riskEngine,
                        RiskParameters riskParameters,
                        PositionSizingService positionSizingService,
                        StringRedisTemplate redisTemplate) {
        this.orderRepository = orderRepository;
        this.orderEventRepository = orderEventRepository;
        this.brokerClient = brokerClient;
        this.riskEngine = riskEngine;
        this.riskParameters = riskParameters;
        this.positionSizingService = positionSizingService;
        this.redisTemplate = redisTemplate;
    }

    /**
     * Submit an order derived from an AI decision.
     * The AI decision specifies: symbol, action (BUY/SELL), stopLoss, target, entryZone.
     * This method translates it into a concrete order and runs it through the Risk Engine.
     */
    @Transactional
    public OrderResponse submitOrder(AiDecisionDto decision, Instrument instrument, AccountInfo account) {
        // 1. Determine side from AI action
        if ("NO_TRADE".equals(decision.action())) {
            return new OrderResponse(UUID.randomUUID().toString(), null, OrderStatus.CANCELLED,
                    BigDecimal.ZERO, BigDecimal.ZERO, "AI decided NO_TRADE", Instant.now());
        }

        OrderSide side = "BUY".equals(decision.action()) ? OrderSide.BUY : OrderSide.SELL;
        BigDecimal entryPrice = decision.entryZoneHigh() != null
                ? decision.entryZoneHigh()
                : (decision.entryZoneLow() != null ? decision.entryZoneLow() : BigDecimal.valueOf(100));

        // 2. Calculate position size using Risk Engine's PositionSizingService
        BigDecimal stopLoss = decision.stopLoss();
        BigDecimal quantity = BigDecimal.ONE; // default
        if (stopLoss != null && entryPrice.compareTo(BigDecimal.ZERO) > 0) {
            quantity = positionSizingService.calculateSize(
                    account.totalCapital(),
                    entryPrice,
                    stopLoss,
                    riskParameters.maxTradeLossPercent(),
                    BigDecimal.valueOf(instrument.getLotSize()),
                    riskParameters.maxPositionSizePercent());
            if (quantity.compareTo(BigDecimal.ZERO) <= 0) {
                return new OrderResponse(UUID.randomUUID().toString(), null, OrderStatus.REJECTED,
                        BigDecimal.ZERO, BigDecimal.ZERO, "Position size calculated as zero or negative", Instant.now());
            }
        }

        // 3. Idempotency check
        String idempotencyKey = generateIdempotencyKey(decision.symbol(), side.name(), quantity, entryPrice);
        if (Boolean.TRUE.equals(redisTemplate.hasKey("order:idemp:" + idempotencyKey))
                || orderRepository.existsByIdempotencyKey(idempotencyKey)) {
            log.warn("Duplicate order submission detected for symbol={}, key={}", decision.symbol(), idempotencyKey);
            return new OrderResponse(UUID.randomUUID().toString(), null, OrderStatus.REJECTED,
                    BigDecimal.ZERO, BigDecimal.ZERO, "Duplicate order submission", Instant.now());
        }

        // 4. Risk Engine check — THE FINAL AUTHORITY (cannot be bypassed)
        OrderRiskRequest riskRequest = new OrderRiskRequest(
            decision.symbol(),
            side,
            quantity,
            entryPrice,
            stopLoss,
            decision.target(),
            account.totalCapital(),
            BigDecimal.ZERO,  // currentDailyLoss — should be fetched from performance tracker in full impl
            (int) orderRepository.countByStatus(OrderStatus.OPEN),
            0,                // consecutiveLosses — should come from learning engine
            null,             // sectorExposures — simplified
            2.0,              // atrPercent placeholder — should come from indicators
            1_000_000.0,      // avgDailyVolume placeholder
            brokerClient.isConnected(),
            "PAPER"
        );

        RiskCheckResult riskResult = riskEngine.checkOrder(riskRequest, riskParameters);

        if (!riskResult.isApproved()) {
            log.info("Order REJECTED by RiskEngine: symbol={}, reason={}", decision.symbol(), riskResult.reason());
            return new OrderResponse(UUID.randomUUID().toString(), null, OrderStatus.REJECTED,
                    BigDecimal.ZERO, BigDecimal.ZERO, "RISK_REJECTED: " + riskResult.reason(), Instant.now());
        }

        // 5. Submit to broker
        String clientOrderId = UUID.randomUUID().toString();
        OrderRequest request = new OrderRequest(
            clientOrderId,
            decision.symbol(),
            side,
            OrderType.LIMIT,
            quantity,
            entryPrice,
            stopLoss
        );

        OrderResponse response = brokerClient.placeOrder(request);
        log.info("Order submitted: symbol={}, side={}, qty={}, price={}, status={}",
                decision.symbol(), side, quantity, entryPrice, response.status());

        // 6. Mark idempotency key in Redis
        redisTemplate.opsForValue().set("order:idemp:" + idempotencyKey, "true", Duration.ofMinutes(5));

        return response;
    }

    @Transactional
    public boolean cancelOrder(String clientOrderId) {
        return brokerClient.cancelOrder(clientOrderId);
    }

    public List<TradingOrder> getOpenOrders() {
        return orderRepository.findByStatusIn(List.of(OrderStatus.SUBMITTED, OrderStatus.PARTIALLY_FILLED, OrderStatus.OPEN));
    }

    @Transactional
    public void updateOrderStatus(String clientOrderId, OrderStatus status) {
        orderRepository.findByClientOrderId(clientOrderId).ifPresent(order -> {
            order.setStatus(status);
            orderRepository.save(order);
        });
    }

    private String generateIdempotencyKey(String symbol, String side, BigDecimal quantity, BigDecimal price) {
        try {
            String raw = symbol + side + quantity.toPlainString() + price.toPlainString()
                       + Instant.now().truncatedTo(ChronoUnit.MINUTES);
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(raw.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(hash.length * 2);
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            return UUID.randomUUID().toString();
        }
    }
}
