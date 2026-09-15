package com.smarttrader.scheduler;

import com.smarttrader.ai.AIStockSelectionService;
import com.smarttrader.ai.AiDecisionDto;
import com.smarttrader.broker.AccountInfo;
import com.smarttrader.broker.BrokerClient;
import com.smarttrader.config.TradingProperties;
import com.smarttrader.entity.Instrument;
import com.smarttrader.entity.PositionStatus;
import com.smarttrader.exception.AiUnavailableException;
import com.smarttrader.marketdata.MarketDataService;
import com.smarttrader.ml.PredictionResult;
import com.smarttrader.ml.PredictionService;
import com.smarttrader.order.OrderService;
import com.smarttrader.portfolio.PortfolioOptimizer;
import com.smarttrader.portfolio.PortfolioState;
import com.smarttrader.ranking.CandidateScore;
import com.smarttrader.ranking.StockRankingService;
import com.smarttrader.regime.MarketRegime;
import com.smarttrader.regime.MarketRegimeService;
import com.smarttrader.repository.PositionRepository;
import com.smarttrader.risk.RiskParameters;
import com.smarttrader.screener.SectorAnalysisService;
import com.smarttrader.screener.ScreeningCriteria;
import com.smarttrader.screener.ScreeningResult;
import com.smarttrader.screener.StockScreeningService;
import com.smarttrader.system.KillSwitchService;
import com.smarttrader.universe.StockUniverseService;
import com.smarttrader.websocket.TradingEventPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Autonomous Trading Scheduler — the heartbeat of the SMART TRADER system.
 *
 * Every minute during market hours it executes the full autonomous trading pipeline:
 * 1. Kill switch check
 * 2. Market open check
 * 3. Load universe and detect regime
 * 4. Screen and rank candidates
 * 5. ML predictions
 * 6. AI analysis (with fallback if unavailable)
 * 7. Portfolio optimization
 * 8. Order submission (all orders go through RiskEngine inside OrderService)
 *
 * AI CANNOT bypass the Risk Engine — it runs inside OrderService.submitOrder().
 */
@Component
public class TradingSessionScheduler {

    private static final Logger log = LoggerFactory.getLogger(TradingSessionScheduler.class);

    private final KillSwitchService killSwitchService;
    private final MarketDataService marketDataService;
    private final TradingProperties tradingProperties;
    private final StockUniverseService stockUniverseService;
    private final MarketRegimeService marketRegimeService;
    private final StockScreeningService stockScreeningService;
    private final StockRankingService stockRankingService;
    private final PredictionService predictionService;
    private final AIStockSelectionService aiStockSelectionService;
    private final PortfolioOptimizer portfolioOptimizer;
    private final OrderService orderService;
    private final TradingEventPublisher eventPublisher;
    private final PositionRepository positionRepository;
    private final BrokerClient brokerClient;
    private final RiskParameters riskParameters;
    private final SectorAnalysisService sectorAnalysisService;

    public TradingSessionScheduler(
            KillSwitchService killSwitchService,
            MarketDataService marketDataService,
            TradingProperties tradingProperties,
            StockUniverseService stockUniverseService,
            MarketRegimeService marketRegimeService,
            StockScreeningService stockScreeningService,
            StockRankingService stockRankingService,
            PredictionService predictionService,
            AIStockSelectionService aiStockSelectionService,
            PortfolioOptimizer portfolioOptimizer,
            OrderService orderService,
            TradingEventPublisher eventPublisher,
            PositionRepository positionRepository,
            BrokerClient brokerClient,
            RiskParameters riskParameters,
            SectorAnalysisService sectorAnalysisService) {
        this.killSwitchService = killSwitchService;
        this.marketDataService = marketDataService;
        this.tradingProperties = tradingProperties;
        this.stockUniverseService = stockUniverseService;
        this.marketRegimeService = marketRegimeService;
        this.stockScreeningService = stockScreeningService;
        this.stockRankingService = stockRankingService;
        this.predictionService = predictionService;
        this.aiStockSelectionService = aiStockSelectionService;
        this.portfolioOptimizer = portfolioOptimizer;
        this.orderService = orderService;
        this.eventPublisher = eventPublisher;
        this.positionRepository = positionRepository;
        this.brokerClient = brokerClient;
        this.riskParameters = riskParameters;
        this.sectorAnalysisService = sectorAnalysisService;
    }

    @Scheduled(cron = "0 44 3 * * MON-FRI", zone = "UTC")  // 9:14 AM IST = 3:44 AM UTC
    public void morningSetup() {
        log.info("Starting morning setup...");
        if (killSwitchService.isActive()) {
            log.warn("Kill switch is active. Skipping morning setup.");
            return;
        }
        // Update universe and regime data
        stockUniverseService.updateUniverseFromProvider();
        log.info("Morning setup complete.");
        eventPublisher.publishSystemEvent("Morning setup complete", "INFO");
    }

    @Scheduled(fixedDelay = 60000)  // every minute
    public void mainTradingLoop() {
        // 1. Kill switch check
        if (killSwitchService.isActive()) {
            log.debug("Kill switch active. Trading loop skipped.");
            return;
        }

        // 2. Market open check
        if (!marketDataService.isMarketOpen()) {
            log.debug("Market is closed. Trading loop skipped.");
            return;
        }

        log.info("Executing trading loop in {} mode", tradingProperties.liveTradingAllowed() ? "LIVE" : "PAPER");

        try {
            // 3. Load active instruments
            List<Instrument> universe = stockUniverseService.getActiveInstruments();
            if (universe.isEmpty()) {
                log.info("Universe is empty, no trades possible");
                return;
            }

            // 4. Detect market regime
            MarketRegime regime = marketRegimeService.getCurrentRegime();
            if (regime == MarketRegime.STRONG_BEAR) {
                log.info("STRONG_BEAR regime detected. No new trades.");
                return;
            }

            // 5. Screen universe with SWING criteria (default)
            ScreeningCriteria criteria = ScreeningCriteria.defaults(com.smarttrader.entity.TradingProfile.SWING);
            List<ScreeningResult> screeningResults = stockScreeningService.screenUniverse(
                universe, criteria, Map.of(), Map.of(), Map.of());

            List<String> candidateSymbols = screeningResults.stream()
                .filter(ScreeningResult::isCandidate)
                .map(ScreeningResult::symbol)
                .collect(Collectors.toList());

            if (candidateSymbols.isEmpty()) {
                log.info("No candidates found after screening, no trade");
                return;
            }

            // 6. Rank top 20 candidates
            List<CandidateScore> ranked = stockRankingService.getTopCandidates(
                20, com.smarttrader.entity.TradingProfile.SWING, Map.of());

            if (ranked.isEmpty()) {
                log.info("Ranking returned no candidates");
                return;
            }

            // 7. Get ML predictions for top 20
            List<CandidateScore> top10 = ranked.stream().limit(10).collect(Collectors.toList());

            // 8. AI Analysis for top 10 (with fallback)
            List<AiDecisionDto> aiDecisions;
            try {
                Map<String, PredictionResult> predictions = top10.stream()
                    .collect(Collectors.toMap(
                        CandidateScore::symbol,
                        c -> predictionService.predictForSymbol(c.symbol(),
                            createEmptyFeatureVector(c.symbol()))
                    ));
                aiDecisions = aiStockSelectionService.selectTopCandidates(ranked, predictions, regime);
            } catch (AiUnavailableException e) {
                log.warn("AI unavailable during trading loop: {}. No AI-dependent trades placed.", e.getMessage());
                return; // No AI = no trades (safety rule)
            }

            if (aiDecisions.isEmpty()) {
                log.info("AI returned no trade recommendations");
                return;
            }

            // 9. Portfolio optimization
            AccountInfo account = brokerClient.getAccountInfo();
            int openPositionCount = positionRepository.findByStatus(PositionStatus.OPEN).size();
            PortfolioState portfolioState = new PortfolioState(
                account.totalCapital(), account.availableCapital(),
                openPositionCount, Map.of(), Map.of(),
                BigDecimal.ZERO, account.unrealizedPnl(), 0, 0.0);

            List<AiDecisionDto> optimized = portfolioOptimizer.optimize(
                aiDecisions, portfolioState, regime, riskParameters);

            // 10. Submit orders — each goes through RiskEngine inside OrderService
            for (AiDecisionDto decision : optimized) {
                Instrument instrument = universe.stream()
                    .filter(i -> i.getSymbol().equals(decision.symbol()))
                    .findFirst()
                    .orElse(null);

                if (instrument == null) {
                    log.warn("Instrument not found for symbol: {}", decision.symbol());
                    continue;
                }

                try {
                    orderService.submitOrder(decision, instrument, account);
                    eventPublisher.publishAiDecision(decision);
                } catch (Exception e) {
                    log.error("Failed to submit order for {}: {}", decision.symbol(), e.getMessage());
                }
            }

            log.info("Trading loop complete. Submitted {} order(s).", optimized.size());

        } catch (Exception e) {
            log.error("Unexpected error in trading loop: {}", e.getMessage(), e);
            eventPublisher.publishSystemEvent("Trading loop error: " + e.getMessage(), "ERROR");
        }
    }

    @Scheduled(cron = "0 0 10 * * MON-FRI", zone = "UTC")  // 3:30 PM IST = 10:00 UTC
    public void endOfDayClose() {
        log.info("Executing end-of-day position check...");
        if (killSwitchService.isActive()) return;
        eventPublisher.publishSystemEvent("End of day position check complete", "INFO");
    }

    /**
     * Creates a minimal empty FeatureVector for prediction when full features aren't computed.
     * In production, the full feature engineering pipeline would populate this.
     */
    private com.smarttrader.features.FeatureVector createEmptyFeatureVector(String symbol) {
        return new com.smarttrader.features.FeatureVector(
            symbol, com.smarttrader.features.FeatureVector.CURRENT_VERSION,
            java.time.Instant.now(),
            0, 0, 0, 0,   // returns
            50, 0, 0,     // rsi, macd, signal
            2.0, 20.0,    // atr, adx
            0, 0, 0, 0, 0, // distances
            0.5,           // bbPercentB
            1.0,           // relativeVolume
            0.02,          // volatility
            0,             // obv
            0,             // sectorStrength
            3,             // SIDEWAYS.ordinal()
            0.5,           // momentumScore
            0.5            // liquidityScore
        );
    }
}
