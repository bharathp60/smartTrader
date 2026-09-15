package com.smarttrader.system;

import com.smarttrader.entity.SystemEvent;
import com.smarttrader.entity.SystemSeverity;
import com.smarttrader.order.OrderService;
import com.smarttrader.repository.SystemEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;

/**
 * Kill switch service — emergency halt for all new trading activity.
 * When activated:
 * - Sets a Redis flag (TTL 24h)
 * - Cancels all eligible pending orders
 * - Logs a CRITICAL system event
 * AI cannot override this service.
 */
@Service
public class KillSwitchService {

    private static final Logger log = LoggerFactory.getLogger(KillSwitchService.class);
    private static final String KILL_SWITCH_KEY = "kill-switch:active";

    private final StringRedisTemplate redisTemplate;
    private final SystemEventRepository systemEventRepository;
    private final OrderService orderService;

    public KillSwitchService(
            StringRedisTemplate redisTemplate,
            SystemEventRepository systemEventRepository,
            OrderService orderService) {
        this.redisTemplate = redisTemplate;
        this.systemEventRepository = systemEventRepository;
        this.orderService = orderService;
    }

    @Transactional
    public void activate(String reason, String actor) {
        redisTemplate.opsForValue().set(KILL_SWITCH_KEY, "true", Duration.ofHours(24));
        log.warn("KILL SWITCH ACTIVATED by {} — Reason: {}", actor, reason);

        // Cancel all eligible pending orders
        orderService.getOpenOrders().forEach(order -> {
            try {
                orderService.cancelOrder(order.getClientOrderId());
            } catch (Exception e) {
                log.error("Failed to cancel order {} during kill switch: {}", order.getClientOrderId(), e.getMessage());
            }
        });

        persistEvent("KILL_SWITCH_ACTIVATED", SystemSeverity.CRITICAL,
                "Kill switch activated by " + actor + ". Reason: " + reason);
    }

    public boolean isActive() {
        return Boolean.TRUE.equals(redisTemplate.hasKey(KILL_SWITCH_KEY));
    }

    @Transactional
    public void deactivate(String actor) {
        redisTemplate.delete(KILL_SWITCH_KEY);
        log.info("Kill switch deactivated by {}", actor);
        persistEvent("KILL_SWITCH_DEACTIVATED", SystemSeverity.INFO,
                "Kill switch deactivated by " + actor);
    }

    private void persistEvent(String eventType, SystemSeverity severity, String message) {
        SystemEvent event = new SystemEvent();
        event.setEventType(eventType);
        event.setSeverity(severity);
        event.setMessage(message);
        event.setOccurredAt(Instant.now());
        systemEventRepository.save(event);
    }
}
