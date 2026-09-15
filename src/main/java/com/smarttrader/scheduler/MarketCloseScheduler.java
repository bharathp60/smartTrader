package com.smarttrader.scheduler;

import com.smarttrader.learning.DailyLearningService;
import com.smarttrader.performance.PerformanceEngine;
import com.smarttrader.websocket.TradingEventPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class MarketCloseScheduler {

    private static final Logger log = LoggerFactory.getLogger(MarketCloseScheduler.class);

    private final DailyLearningService dailyLearningService;
    private final PerformanceEngine performanceEngine;
    private final TradingEventPublisher eventPublisher;

    public MarketCloseScheduler(
            DailyLearningService dailyLearningService,
            PerformanceEngine performanceEngine,
            TradingEventPublisher eventPublisher) {
        this.dailyLearningService = dailyLearningService;
        this.performanceEngine = performanceEngine;
        this.eventPublisher = eventPublisher;
    }

    @Scheduled(cron = "0 10 10 * * MON-FRI", zone = "UTC")  // 3:40 PM IST = 10:10 AM UTC
    public void endOfDayLearning() {
        log.info("Starting end-of-day learning and metrics calculation...");

        try {
            LocalDate today = LocalDate.now();
            performanceEngine.calculateDailyMetrics(today);
            dailyLearningService.runDailyLearning(today);

            eventPublisher.publishSystemEvent("End of day learning and metrics calculated successfully", "INFO");
        } catch (Exception e) {
            log.error("Failed to run end-of-day routines: {}", e.getMessage());
            eventPublisher.publishSystemEvent("End of day learning failed: " + e.getMessage(), "ERROR");
        }

        log.info("End-of-day routines completed.");
    }
}
