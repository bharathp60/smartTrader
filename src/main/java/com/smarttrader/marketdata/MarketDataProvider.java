package com.smarttrader.marketdata;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface MarketDataProvider {
    String getProviderName();
    List<CandleDto> getHistoricalCandles(String symbol, Timeframe timeframe, Instant from, Instant to);
    Optional<QuoteDto> getLatestQuote(String symbol);
    boolean isMarketOpen();
    Set<String> getSupportedSymbols();
}
