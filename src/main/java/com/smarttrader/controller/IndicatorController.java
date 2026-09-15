package com.smarttrader.controller;

import com.smarttrader.indicators.IndicatorResult;
import com.smarttrader.indicators.TechnicalIndicatorService;
import com.smarttrader.marketdata.CandleDto;
import com.smarttrader.marketdata.MarketDataService;
import com.smarttrader.marketdata.Timeframe;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/indicators")
public class IndicatorController {

    private final TechnicalIndicatorService indicatorService;
    private final MarketDataService marketDataService;

    public IndicatorController(TechnicalIndicatorService indicatorService, MarketDataService marketDataService) {
        this.indicatorService = indicatorService;
        this.marketDataService = marketDataService;
    }

    @GetMapping("/{symbol}")
    @PreAuthorize("hasAnyRole('VIEWER', 'TRADER', 'ADMIN')")
    public ResponseEntity<Map<String, Object>> computeIndicators(
            @PathVariable String symbol,
            @RequestParam(defaultValue = "1d") String timeframe) {

        Timeframe tf = Timeframe.fromCode(timeframe);
        Instant to = Instant.now();
        Instant from = to.minus(200, ChronoUnit.DAYS);
        List<CandleDto> candles = marketDataService.getHistoricalCandles(symbol, tf, from, to);

        if (candles.size() < 14) {
            return ResponseEntity.ok(Map.of("error", "Insufficient data — need at least 14 candles"));
        }

        double[] closes  = candles.stream().mapToDouble(c -> c.close().doubleValue()).toArray();
        double[] highs   = candles.stream().mapToDouble(c -> c.high().doubleValue()).toArray();
        double[] lows    = candles.stream().mapToDouble(c -> c.low().doubleValue()).toArray();
        double[] volumes = candles.stream().mapToDouble(c -> c.volume().doubleValue()).toArray();

        Map<String, Object> result = new HashMap<>();

        // RSI 14
        if (closes.length >= 15) {
            result.put("rsi14", indicatorService.rsi(closes, 14));
        }

        // SMA 20
        if (closes.length >= 20) {
            result.put("sma20", indicatorService.sma(closes, 20));
        }

        // MACD (12,26,9)
        if (closes.length >= 35) {
            IndicatorResult macd = indicatorService.macd(closes, 12, 26, 9);
            result.put("macd", macd.components());
        }

        // Bollinger Bands (20, 2)
        if (closes.length >= 20) {
            IndicatorResult bb = indicatorService.bollingerBands(closes, 20, 2.0);
            result.put("bollingerBands", bb.components());
        }

        // ATR 14
        if (closes.length >= 15) {
            result.put("atr14", indicatorService.atr(highs, lows, closes, 14));
        }

        // ADX 14
        if (closes.length >= 28) {
            IndicatorResult adx = indicatorService.adx(highs, lows, closes, 14);
            result.put("adx", adx.components());
        }

        // VWAP
        result.put("vwap", indicatorService.vwap(highs, lows, closes, volumes));

        // Relative Volume
        if (volumes.length >= 20) {
            result.put("relativeVolume", indicatorService.relativeVolume(volumes, 20));
        }

        result.put("symbol", symbol);
        result.put("timeframe", timeframe);
        result.put("candleCount", candles.size());

        return ResponseEntity.ok(result);
    }
}
