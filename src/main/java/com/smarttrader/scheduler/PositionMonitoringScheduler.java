package com.smarttrader.scheduler;

import com.smarttrader.entity.Position;
import com.smarttrader.entity.PositionStatus;
import com.smarttrader.marketdata.MarketDataCacheService;
import com.smarttrader.marketdata.MarketDataService;
import com.smarttrader.marketdata.QuoteDto;
import com.smarttrader.order.OrderService;
import com.smarttrader.repository.PositionRepository;
import com.smarttrader.websocket.TradingEventPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Monitors open positions every 30 seconds and triggers exits on:
 * - Stop-loss breach: currentPrice ≤ stopLoss
 * - Target hit: currentPrice ≥ targetPrice
 * Prices are read from the Redis cache (populated by MarketDataScheduler).
 */
@Component
public class PositionMonitoringScheduler {

    private static final Logger log = LoggerFactory.getLogger(PositionMonitoringScheduler.class);

    private final PositionRepository positionRepository;
    private final MarketDataCacheService marketDataCacheService;
    private final MarketDataService marketDataService;
    private final OrderService orderService;
    private final TradingEventPublisher eventPublisher;

    public PositionMonitoringScheduler(
            PositionRepository positionRepository,
            MarketDataCacheService marketDataCacheService,
            MarketDataService marketDataService,
            OrderService orderService,
            TradingEventPublisher eventPublisher) {
        this.positionRepository = positionRepository;
        this.marketDataCacheService = marketDataCacheService;
        this.marketDataService = marketDataService;
        this.orderService = orderService;
        this.eventPublisher = eventPublisher;
    }

    @Scheduled(fixedDelay = 30000)  // every 30 seconds
    public void monitorPositions() {
        if (!marketDataService.isMarketOpen()) {
            return;
        }

        List<Position> openPositions = positionRepository.findByStatus(PositionStatus.OPEN);
        if (openPositions.isEmpty()) {
            return;
        }

        for (Position position : openPositions) {
            try {
                // Fetch current price from Redis cache
                Optional<QuoteDto> quoteOpt = marketDataCacheService.getCachedQuote(position.getSymbol());
                if (quoteOpt.isEmpty()) {
                    continue;
                }

                BigDecimal currentPrice = quoteOpt.get().lastPrice();
                if (currentPrice == null) {
                    continue;
                }

                boolean shouldExit = false;
                String exitReason = "";

                // Stop-loss check
                if (position.getStopLoss() != null
                        && currentPrice.compareTo(position.getStopLoss()) <= 0) {
                    shouldExit = true;
                    exitReason = "Stop-loss hit at " + currentPrice;
                }
                // Target check
                else if (position.getTargetPrice() != null
                        && currentPrice.compareTo(position.getTargetPrice()) >= 0) {
                    shouldExit = true;
                    exitReason = "Target hit at " + currentPrice;
                }

                if (shouldExit) {
                    log.info("Triggering exit for {}: {}", position.getSymbol(), exitReason);
                    eventPublisher.publishSystemEvent(
                        "Exit triggered for " + position.getSymbol() + ": " + exitReason, "WARN");
                    // Note: Full exit order implementation requires building a SELL AiDecisionDto
                    // and calling orderService.submitOrder(). This is a placeholder for Phase 22+.
                }

            } catch (Exception e) {
                log.error("Error monitoring position {}: {}", position.getSymbol(), e.getMessage());
            }
        }
    }
}
