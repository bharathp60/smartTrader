package com.smarttrader.broker;

import com.smarttrader.entity.OrderStatus;
import com.smarttrader.entity.TradingModeRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Alpaca Markets broker adapter for US equities.
 * Active only when profile "alpaca" is enabled.
 *
 * To activate:
 *   - Run with --spring.profiles.active=alpaca
 *   - Set ALPACA_API_KEY and ALPACA_SECRET_KEY env vars
 *   - Optionally set ALPACA_BASE_URL (defaults to paper trading URL)
 */
@Component
@Profile("alpaca")
public class AlpacaClient implements BrokerClient {

    private static final Logger log = LoggerFactory.getLogger(AlpacaClient.class);
    private static final String DEFAULT_BASE_URL = "https://paper-api.alpaca.markets/v2";

    private final RestTemplate restTemplate;
    private final String apiKey;
    private final String secretKey;
    private final String baseUrl;

    public AlpacaClient(
            @Value("${ALPACA_API_KEY:}") String apiKey,
            @Value("${ALPACA_SECRET_KEY:}") String secretKey,
            @Value("${ALPACA_BASE_URL:" + DEFAULT_BASE_URL + "}") String baseUrl) {
        this.restTemplate = new RestTemplate();
        this.apiKey       = apiKey;
        this.secretKey    = secretKey;
        this.baseUrl      = baseUrl;
    }

    @Override
    public String getBrokerName() { return "Alpaca"; }

    @Override
    public TradingModeRecord getSupportedMode() {
        return baseUrl.contains("paper") ? TradingModeRecord.PAPER : TradingModeRecord.LIVE;
    }

    @Override
    public boolean isConnected() {
        if (apiKey == null || apiKey.isBlank()) {
            log.warn("Alpaca credentials not configured");
            return false;
        }
        try {
            ResponseEntity<Map> resp = restTemplate.exchange(
                baseUrl + "/account", HttpMethod.GET,
                new HttpEntity<>(buildHeaders()), Map.class);
            return resp.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            log.error("Alpaca connectivity check failed: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public AccountInfo getAccountInfo() {
        try {
            ResponseEntity<Map> resp = restTemplate.exchange(
                baseUrl + "/account", HttpMethod.GET,
                new HttpEntity<>(buildHeaders()), Map.class);
            if (resp.getStatusCode().is2xxSuccessful() && resp.getBody() != null) {
                Map<?, ?> body = resp.getBody();
                BigDecimal equity       = parse(body.get("equity"));
                BigDecimal buyingPower  = parse(body.get("buying_power"));
                BigDecimal usedMargin   = equity.subtract(buyingPower.min(equity));
                BigDecimal unrealizedPnl = parse(body.get("unrealized_pl"));
                return new AccountInfo(equity, buyingPower, usedMargin, unrealizedPnl, "USD");
            }
        } catch (Exception e) {
            log.error("Failed to fetch Alpaca account info: {}", e.getMessage());
        }
        return new AccountInfo(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, "USD");
    }

    @Override
    public OrderResponse placeOrder(OrderRequest request) {
        log.info("Placing order on Alpaca: {} {} {} @ {}",
            request.side(), request.quantity(), request.symbol(), request.limitPrice());
        try {
            String orderType = request.limitPrice() != null ? "limit" : "market";
            String side = request.side().name().equalsIgnoreCase("BUY") ? "buy" : "sell";

            Map<String, Object> body = new java.util.LinkedHashMap<>();
            body.put("symbol", request.symbol());
            body.put("qty",    request.quantity().toPlainString());
            body.put("side",   side);
            body.put("type",   orderType);
            body.put("time_in_force", "day");
            body.put("client_order_id", request.clientOrderId());
            if (request.limitPrice() != null) body.put("limit_price", request.limitPrice().toPlainString());
            if (request.stopPrice()  != null) body.put("stop_price",  request.stopPrice().toPlainString());

            HttpHeaders headers = buildHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            ResponseEntity<Map> resp = restTemplate.exchange(
                baseUrl + "/orders", HttpMethod.POST,
                new HttpEntity<>(body, headers), Map.class);

            if (resp.getStatusCode().is2xxSuccessful() && resp.getBody() != null) {
                Map<?, ?> data = resp.getBody();
                return new OrderResponse(
                    request.clientOrderId(),
                    String.valueOf(data.get("id")),
                    mapStatus(String.valueOf(data.get("status"))),
                    parse(data.get("filled_qty")),
                    parse(data.get("filled_avg_price")),
                    "Order placed",
                    Instant.now()
                );
            }
        } catch (Exception e) {
            log.error("Alpaca order placement failed for {}: {}", request.symbol(), e.getMessage());
        }
        return new OrderResponse(request.clientOrderId(), "", OrderStatus.REJECTED,
            BigDecimal.ZERO, BigDecimal.ZERO, "Placement failed", Instant.now());
    }

    @Override
    public OrderResponse getOrderStatus(String clientOrderId) {
        try {
            ResponseEntity<Map> resp = restTemplate.exchange(
                baseUrl + "/orders:by_client_order_id?client_order_id=" + clientOrderId,
                HttpMethod.GET, new HttpEntity<>(buildHeaders()), Map.class);
            if (resp.getStatusCode().is2xxSuccessful() && resp.getBody() != null) {
                Map<?, ?> data = resp.getBody();
                return new OrderResponse(
                    clientOrderId,
                    String.valueOf(data.get("id")),
                    mapStatus(String.valueOf(data.get("status"))),
                    parse(data.get("filled_qty")),
                    parse(data.get("filled_avg_price")),
                    "OK",
                    Instant.now()
                );
            }
        } catch (Exception e) {
            log.error("Failed to fetch Alpaca order {}: {}", clientOrderId, e.getMessage());
        }
        return null;
    }

    @Override
    public List<OrderResponse> getOpenOrders() {
        try {
            restTemplate.exchange(baseUrl + "/orders?status=open",
                HttpMethod.GET, new HttpEntity<>(buildHeaders()), List.class);
        } catch (Exception e) {
            log.error("Failed to fetch Alpaca open orders: {}", e.getMessage());
        }
        return List.of();
    }

    @Override
    public boolean cancelOrder(String clientOrderId) {
        try {
            OrderResponse status = getOrderStatus(clientOrderId);
            if (status == null || status.brokerOrderId() == null) return false;
            ResponseEntity<Void> resp = restTemplate.exchange(
                baseUrl + "/orders/" + status.brokerOrderId(),
                HttpMethod.DELETE, new HttpEntity<>(buildHeaders()), Void.class);
            return resp.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            log.error("Failed to cancel Alpaca order {}: {}", clientOrderId, e.getMessage());
            return false;
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private HttpHeaders buildHeaders() {
        HttpHeaders h = new HttpHeaders();
        h.set("APCA-API-KEY-ID", apiKey);
        h.set("APCA-API-SECRET-KEY", secretKey);
        return h;
    }

    private OrderStatus mapStatus(String s) {
        return switch (s.toLowerCase()) {
            case "filled"                         -> OrderStatus.FILLED;
            case "partially_filled"               -> OrderStatus.PARTIALLY_FILLED;
            case "new","accepted","pending_new"   -> OrderStatus.SUBMITTED;
            case "canceled","expired","done_for_day" -> OrderStatus.CANCELLED;
            case "rejected"                       -> OrderStatus.REJECTED;
            default                               -> OrderStatus.OPEN;
        };
    }

    private BigDecimal parse(Object v) {
        if (v == null) return BigDecimal.ZERO;
        try { return new BigDecimal(String.valueOf(v)); }
        catch (NumberFormatException e) { return BigDecimal.ZERO; }
    }
}
