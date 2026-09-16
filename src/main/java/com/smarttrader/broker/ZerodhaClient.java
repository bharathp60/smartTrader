package com.smarttrader.broker;

import com.smarttrader.entity.OrderStatus;
import com.smarttrader.entity.TradingModeRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Profile;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
* Zerodha Kite Connect broker adapter.
* Active only when profile "live" is enabled AND live-trading-enabled=true.
*
* To activate:
*   - Run with --spring.profiles.active=live
*   - Set smart-trader.live-trading-enabled=true
*   - Set ZERODHA_API_KEY and ZERODHA_ACCESS_TOKEN env vars
*
* All orders still pass through RiskEngine before reaching this client.
*/
 * Zerodha Kite Connect v3 broker adapter.
 * Active only when the "live" Spring profile is enabled.
 *
 * Prerequisites:
 *   - Run with --spring.profiles.active=live
 *   - Set ZERODHA_API_KEY and ZERODHA_ACCESS_TOKEN environment variables
 *
 * All orders are still validated by RiskEngine before reaching this client.
 */
@Component
@Profile("live")
public class ZerodhaClient implements BrokerClient {

   private static final Logger log = LoggerFactory.getLogger(ZerodhaClient.class);
   private static final String KITE_BASE_URL = "https://api.kite.trade";
    private static final Logger log = LoggerFactory.getLogger(ZerodhaClient.class);
    private static final String KITE_BASE = "https://api.kite.trade";

   private final RestTemplate restTemplate;
   private final String apiKey;
   private final String accessToken;
    private final RestTemplate restTemplate;
    private final String apiKey;
    private final String accessToken;

   public ZerodhaClient(
           RestTemplateBuilder restTemplateBuilder,
           @Value("${ZERODHA_API_KEY:}") String apiKey,
           @Value("${ZERODHA_ACCESS_TOKEN:}") String accessToken) {
       this.restTemplate = restTemplateBuilder
           .connectTimeout(Duration.ofSeconds(5))
           .readTimeout(Duration.ofSeconds(10))
           .build();
       this.apiKey = apiKey;
       this.accessToken = accessToken;
   }
    public ZerodhaClient(
            @Value("${ZERODHA_API_KEY:}") String apiKey,
            @Value("${ZERODHA_ACCESS_TOKEN:}") String accessToken) {
        this.restTemplate  = new RestTemplate();
        this.apiKey        = apiKey;
        this.accessToken   = accessToken;
    }

   @Override
   public String getBrokerName() {
       return "Zerodha Kite";
   }
    @Override public String getBrokerName() { return "Zerodha Kite"; }
    @Override public TradingModeRecord getSupportedMode() { return TradingModeRecord.LIVE; }

   @Override
   public TradingModeRecord getSupportedMode() {
       return TradingModeRecord.LIVE;
   }
    @Override
    public boolean isConnected() {
        if (apiKey == null || apiKey.isBlank()) { log.warn("Zerodha API key not set"); return false; }
        try {
            ResponseEntity<Map> r = restTemplate.exchange(
                KITE_BASE + "/user/profile", HttpMethod.GET, new HttpEntity<>(headers()), Map.class);
            return r.getStatusCode().is2xxSuccessful();
        } catch (Exception e) { log.error("Zerodha connectivity check failed: {}", e.getMessage()); return false; }
    }

   @Override
   public boolean isConnected() {
       if (apiKey == null || apiKey.isBlank() || accessToken == null || accessToken.isBlank()) {
           log.warn("Zerodha credentials not configured — disconnected");
           return false;
       }
       try {
           ResponseEntity<Map> resp = restTemplate.exchange(
               KITE_BASE_URL + "/user/profile",
               HttpMethod.GET,
               new HttpEntity<>(buildHeaders()),
               Map.class
           );
           return resp.getStatusCode().is2xxSuccessful();
       } catch (Exception e) {
           log.error("Zerodha connectivity check failed: {}", e.getMessage());
           return false;
       }
   }
    @Override
    public AccountInfo getAccountInfo() {
        try {
            ResponseEntity<Map> r = restTemplate.exchange(
                KITE_BASE + "/user/margins", HttpMethod.GET, new HttpEntity<>(headers()), Map.class);
            if (r.getStatusCode().is2xxSuccessful() && r.getBody() != null) {
                Object dataObj = r.getBody().get("data");
                if (dataObj instanceof Map<?, ?> data) {
                    Object equityObj = data.get("equity");
                    if (equityObj instanceof Map<?, ?> eq) {
                        BigDecimal net       = parse(eq.get("net"));
                        Object availObj = eq.get("available");
                        BigDecimal available = availObj instanceof Map<?, ?> avail ? parse(((Map<?,?>) avail).get("cash")) : BigDecimal.ZERO;
                        BigDecimal used      = net.subtract(available);
                        return new AccountInfo(net, available, used, BigDecimal.ZERO, "INR");
                    }
                }
            }
        } catch (Exception e) { log.error("Failed to fetch Zerodha account info: {}", e.getMessage()); }
        return new AccountInfo(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, "INR");
    }

   @Override
   public AccountInfo getAccountInfo() {
       try {
           ResponseEntity<Map> resp = restTemplate.exchange(
               KITE_BASE_URL + "/user/margins",
               HttpMethod.GET,
               new HttpEntity<>(buildHeaders()),
               Map.class
           );
           if (resp.getStatusCode().is2xxSuccessful() && resp.getBody() != null) {
               Map<?, ?> data = (Map<?, ?>) ((Map<?, ?>) resp.getBody()).get("data");
               Map<?, ?> equity = data != null ? (Map<?, ?>) data.get("equity") : null;
               if (equity != null) {
                   BigDecimal net = parseBigDecimal(equity.get("net"));
                   BigDecimal available = parseBigDecimal(((Map<?, ?>) equity.getOrDefault("available", Map.of())).get("cash"));
                   BigDecimal used = net.subtract(available);
                   return new AccountInfo(net, available, used, BigDecimal.ZERO, "INR");
               }
           }
       } catch (Exception e) {
           log.error("Failed to fetch Zerodha account info: {}", e.getMessage());
       }
       return new AccountInfo(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, "INR");
   }
    @Override
    public OrderResponse placeOrder(OrderRequest request) {
        log.info("Placing LIVE order on Zerodha: {} {} {}", request.side(), request.quantity(), request.symbol());
        try {
            HttpHeaders h = headers();
            h.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            String body = buildFormBody(request);

   @Override
   public OrderResponse placeOrder(OrderRequest request) {
       log.info("Placing LIVE order on Zerodha: {} {} {} @ {}",
           request.side(), request.quantity(), request.symbol(), request.limitPrice());
       try {
           HttpHeaders headers = buildHeaders();
           headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            ResponseEntity<Map> r = restTemplate.exchange(
                KITE_BASE + "/orders/regular", HttpMethod.POST, new HttpEntity<>(body, h), Map.class);

           String body = buildOrderBody(request);
           HttpEntity<String> entity = new HttpEntity<>(body, headers);
            if (r.getStatusCode().is2xxSuccessful() && r.getBody() != null) {
                Object dataObj = r.getBody().get("data");
                String zerodhaId = dataObj instanceof Map<?,?> d ? String.valueOf(d.get("order_id")) : "";
                return new OrderResponse(request.clientOrderId(), zerodhaId,
                    OrderStatus.SUBMITTED, BigDecimal.ZERO, request.limitPrice() != null ? request.limitPrice() : BigDecimal.ZERO,
                    "Order placed", Instant.now());
            }
        } catch (Exception e) { log.error("Zerodha order failed for {}: {}", request.symbol(), e.getMessage()); }
        return new OrderResponse(request.clientOrderId(), "", OrderStatus.REJECTED,
            BigDecimal.ZERO, BigDecimal.ZERO, "Placement failed", Instant.now());
    }

           ResponseEntity<Map> resp = restTemplate.exchange(
               KITE_BASE_URL + "/orders/regular",
               HttpMethod.POST,
               entity,
               Map.class
           );
    @Override
    public OrderResponse getOrderStatus(String clientOrderId) {
        try {
            ResponseEntity<Map> r = restTemplate.exchange(
                KITE_BASE + "/orders/" + clientOrderId, HttpMethod.GET, new HttpEntity<>(headers()), Map.class);
            if (r.getStatusCode().is2xxSuccessful() && r.getBody() != null) {
                Object dataObj = r.getBody().get("data");
                if (dataObj instanceof Map<?,?> data) {
                    OrderStatus status = mapStatus(String.valueOf(data.get("status")));
                    BigDecimal filled  = parse(data.get("filled_quantity"));
                    BigDecimal avgPrice = parse(data.get("average_price"));
                    return new OrderResponse(clientOrderId, String.valueOf(data.get("order_id")),
                        status, filled, avgPrice, "OK", Instant.now());
                }
            }
        } catch (Exception e) { log.error("Failed to fetch Zerodha order {}: {}", clientOrderId, e.getMessage()); }
        return null;
    }

           if (resp.getStatusCode().is2xxSuccessful() && resp.getBody() != null) {
               Map<?, ?> data = (Map<?, ?>) resp.getBody().get("data");
               String zerodhaOrderId = data != null ? String.valueOf(data.get("order_id")) : "";
               return new OrderResponse(
                   request.clientOrderId(),
                   zerodhaOrderId,
                   OrderStatus.SUBMITTED,
                   BigDecimal.ZERO,
                   request.limitPrice(),
                   "Order placed",
                   Instant.now()
               );
           }
       } catch (Exception e) {
           log.error("Zerodha order placement failed for {}: {}", request.symbol(), e.getMessage());
       }
       return new OrderResponse(
           request.clientOrderId(),
           "",
           OrderStatus.REJECTED,
           BigDecimal.ZERO,
           BigDecimal.ZERO,
           "Order placement failed",
           Instant.now()
       );
   }
    @Override
    public List<OrderResponse> getOpenOrders() {
        try {
            restTemplate.exchange(KITE_BASE + "/orders", HttpMethod.GET, new HttpEntity<>(headers()), Map.class);
        } catch (Exception e) { log.error("Failed to fetch Zerodha open orders: {}", e.getMessage()); }
        return List.of();
    }

   @Override
   public OrderResponse getOrderStatus(String clientOrderId) {
       try {
           ResponseEntity<Map> resp = restTemplate.exchange(
               KITE_BASE_URL + "/orders/" + clientOrderId,
               HttpMethod.GET,
               new HttpEntity<>(buildHeaders()),
               Map.class
           );
           if (resp.getStatusCode().is2xxSuccessful() && resp.getBody() != null) {
               // Parse Zerodha order status response
               Map<?, ?> data = (Map<?, ?>) resp.getBody().get("data");
               if (data != null) {
                   String status = String.valueOf(data.get("status"));
                   OrderStatus orderStatus = mapZerodhaStatus(status);
                   BigDecimal filledQty = parseBigDecimal(data.get("filled_quantity"));
                   BigDecimal avgPrice = parseBigDecimal(data.get("average_price"));
                   return new OrderResponse(
                       clientOrderId,
                       String.valueOf(data.get("order_id")),
                       String.valueOf(data.get("tradingsymbol")),
                       orderStatus,
                       filledQty,
                       filledQty,
                       avgPrice,
                       Instant.now()
                   );
               }
           }
       } catch (Exception e) {
           log.error("Failed to fetch order status from Zerodha: {}", e.getMessage());
       }
       return null;
   }
    @Override
    public boolean cancelOrder(String clientOrderId) {
        try {
            ResponseEntity<Map> r = restTemplate.exchange(
                KITE_BASE + "/orders/regular/" + clientOrderId,
                HttpMethod.DELETE, new HttpEntity<>(headers()), Map.class);
            return r.getStatusCode().is2xxSuccessful();
        } catch (Exception e) { log.error("Failed to cancel Zerodha order {}: {}", clientOrderId, e.getMessage()); return false; }
    }

   @Override
   public List<OrderResponse> getOpenOrders() {
       try {
           ResponseEntity<Map> resp = restTemplate.exchange(
               KITE_BASE_URL + "/orders",
               HttpMethod.GET,
               new HttpEntity<>(buildHeaders()),
               Map.class
           );
           if (resp.getStatusCode().is2xxSuccessful()) {
               // In production: parse and return list
               return List.of();
           }
       } catch (Exception e) {
           log.error("Failed to fetch open orders from Zerodha: {}", e.getMessage());
       }
       return List.of();
   }
    // ── Helpers ───────────────────────────────────────────────────────────────

   @Override
   public boolean cancelOrder(String clientOrderId) {
       try {
           ResponseEntity<Map> resp = restTemplate.exchange(
               KITE_BASE_URL + "/orders/regular/" + clientOrderId,
               HttpMethod.DELETE,
               new HttpEntity<>(buildHeaders()),
               Map.class
           );
           return resp.getStatusCode().is2xxSuccessful();
       } catch (Exception e) {
           log.error("Failed to cancel order {} on Zerodha: {}", clientOrderId, e.getMessage());
           return false;
       }
   }
    private HttpHeaders headers() {
        HttpHeaders h = new HttpHeaders();
        h.set("X-Kite-Version", "3");
        h.set("Authorization", "token " + apiKey + ":" + accessToken);
        return h;
    }

   // ── Helpers ─────────────────────────────────────────────────────────────
    private String buildFormBody(OrderRequest req) {
        String orderType   = req.limitPrice() != null ? "LIMIT" : "MARKET";
        String transType   = req.side().name().equalsIgnoreCase("BUY") ? "BUY" : "SELL";
        StringBuilder sb   = new StringBuilder();
        sb.append("tradingsymbol=").append(req.symbol());
        sb.append("&exchange=NSE");
        sb.append("&transaction_type=").append(transType);
        sb.append("&order_type=").append(orderType);
        sb.append("&quantity=").append(req.quantity().toBigInteger());
        sb.append("&product=MIS");
        sb.append("&validity=DAY");
        if (req.limitPrice() != null) sb.append("&price=").append(req.limitPrice());
        sb.append("&tag=").append(req.clientOrderId().substring(0, Math.min(8, req.clientOrderId().length())));
        return sb.toString();
    }

   private HttpHeaders buildHeaders() {
       HttpHeaders headers = new HttpHeaders();
       headers.set("X-Kite-Version", "3");
       headers.set("Authorization", "token " + apiKey + ":" + accessToken);
       return headers;
   }
    private OrderStatus mapStatus(String s) {
        return switch (s.toUpperCase()) {
            case "COMPLETE"                  -> OrderStatus.FILLED;
            case "OPEN", "OPEN PENDING"      -> OrderStatus.SUBMITTED;
            case "PARTIAL"                   -> OrderStatus.PARTIALLY_FILLED;
            case "CANCELLED","CANCELLED AMO" -> OrderStatus.CANCELLED;
            case "REJECTED"                  -> OrderStatus.REJECTED;
            default                          -> OrderStatus.OPEN;
        };
    }

   private String buildOrderBody(OrderRequest request) {
       String orderType = request.limitPrice() != null ? "LIMIT" : "MARKET";
       String transType = "BUY".equalsIgnoreCase(String.valueOf(request.side())) ? "BUY" : "SELL";
       StringBuilder sb = new StringBuilder();
       sb.append("tradingsymbol=").append(request.symbol());
       sb.append("&exchange=NSE");
       sb.append("&transaction_type=").append(transType);
       sb.append("&order_type=").append(orderType);
       sb.append("&quantity=").append(request.quantity().toBigInteger());
       sb.append("&product=MIS"); // Intraday by default — change to CNC for positional
       sb.append("&validity=DAY");
       if (request.limitPrice() != null) {
           sb.append("&price=").append(request.limitPrice());
       }
       sb.append("&tag=").append(request.clientOrderId().substring(0, Math.min(8, request.clientOrderId().length())));
       return sb.toString();
   }

   private OrderStatus mapZerodhaStatus(String status) {
       return switch (status.toUpperCase()) {
           case "COMPLETE" -> OrderStatus.FILLED;
           case "OPEN", "OPEN PENDING" -> OrderStatus.SUBMITTED;
           case "PARTIAL" -> OrderStatus.PARTIALLY_FILLED;
           case "CANCELLED", "CANCELLED AMO" -> OrderStatus.CANCELLED;
           case "REJECTED" -> OrderStatus.REJECTED;
           default -> OrderStatus.OPEN;
       };
   }

   private BigDecimal parseBigDecimal(Object value) {
       if (value == null) return BigDecimal.ZERO;
       try { return new BigDecimal(String.valueOf(value)); }
       catch (NumberFormatException e) { return BigDecimal.ZERO; }
   }
    private BigDecimal parse(Object v) {
        if (v == null) return BigDecimal.ZERO;
        try { return new BigDecimal(String.valueOf(v)); } catch (NumberFormatException e) { return BigDecimal.ZERO; }
    }
}
