package com.smarttrader.marketdata;

import com.smarttrader.config.TradingProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Component
@ConditionalOnProperty(prefix = "trading", name = "live-trading-enabled", havingValue = "true", matchIfMissing = true)
public class MarketDataScheduler {

    private final MarketDataService marketDataService;
    private final TradingProperties tradingProperties;
    
    private static final List<String> NIFTY50 = List.of(
        "RELIANCE", "TCS", "HDFCBANK", "INFY", "ICICIBANK" // truncated for brevity
    );

    public MarketDataScheduler(MarketDataService marketDataService, TradingProperties tradingProperties) {
        this.marketDataService = marketDataService;
        this.tradingProperties = tradingProperties;
    }

    @Scheduled(fixedRate = 30000)
    public void refreshTopStocksQuotes() {
        if (!marketDataService.isMarketOpen()) {
            return;
        }
        marketDataService.refreshQuotes(NIFTY50);
    }

    @Scheduled(cron = "0 * * * * *")
    public void persistRecentCandles() {
        if (!marketDataService.isMarketOpen()) {
            return;
        }
        Instant to = Instant.now();
        Instant from = to.minus(1, ChronoUnit.MINUTES);
        
        for (String symbol : NIFTY50) {
            List<CandleDto> candles = marketDataService.getHistoricalCandles(symbol, Timeframe.ONE_MINUTE, from, to);
            for (CandleDto candle : candles) {
                marketDataService.persistCandle(candle);
            }
        }
    }
}
