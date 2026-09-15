package com.smarttrader.controller;

import com.smarttrader.marketdata.CandleDto;
import com.smarttrader.marketdata.MarketDataService;
import com.smarttrader.marketdata.QuoteDto;
import com.smarttrader.marketdata.Timeframe;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/market-data")
public class MarketDataController {

    private final MarketDataService marketDataService;

    public MarketDataController(MarketDataService marketDataService) {
        this.marketDataService = marketDataService;
    }

    @GetMapping("/{symbol}")
    @PreAuthorize("hasAnyRole('VIEWER', 'TRADER', 'ADMIN')")
    public ResponseEntity<QuoteDto> getLatestQuote(@PathVariable String symbol) {
        Optional<QuoteDto> quote = marketDataService.getLatestQuote(symbol);
        return quote.map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{symbol}/candles")
    @PreAuthorize("hasAnyRole('VIEWER', 'TRADER', 'ADMIN')")
    public ResponseEntity<List<CandleDto>> getHistoricalCandles(
            @PathVariable String symbol,
            @RequestParam(defaultValue = "1d") String timeframe) {
        Timeframe tf = Timeframe.fromCode(timeframe);
        Instant to = Instant.now();
        Instant from = to.minus(365, ChronoUnit.DAYS);
        List<CandleDto> candles = marketDataService.getHistoricalCandles(symbol, tf, from, to);
        return ResponseEntity.ok(candles);
    }

    @GetMapping("/status")
    @PreAuthorize("hasAnyRole('VIEWER', 'TRADER', 'ADMIN')")
    public ResponseEntity<Map<String, Boolean>> getMarketStatus() {
        return ResponseEntity.ok(Map.of("isOpen", marketDataService.isMarketOpen()));
    }
}
