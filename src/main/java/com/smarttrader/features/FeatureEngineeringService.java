package com.smarttrader.features;

import com.smarttrader.dto.CandleDto;
import com.smarttrader.indicators.IndicatorResult;
import com.smarttrader.indicators.TechnicalIndicatorService;
import com.smarttrader.regime.MarketRegime;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Generates FeatureVector instances from historical candle data using real technical indicators.
 * All 25 fields are computed from RSI, MACD, ATR, ADX, VWAP, EMA/SMA distances,
 * Bollinger Bands, relative volume, volatility, and OBV.
 * Returns neutral values when fewer than 30 candles are available.
 */
@Service
public class FeatureEngineeringService {

    private static final int MIN_CANDLES = 30;

    private final TechnicalIndicatorService indicatorService;

    public FeatureEngineeringService(TechnicalIndicatorService indicatorService) {
        this.indicatorService = indicatorService;
    }

    public FeatureVector generateFeatures(String symbol, List<CandleDto> candles,
                                          MarketRegime marketRegime, double sectorStrength) {
        if (candles == null || candles.size() < MIN_CANDLES) {
            return neutral(symbol, marketRegime, sectorStrength);
        }

        double[] closes  = candles.stream().mapToDouble(c -> c.close().doubleValue()).toArray();
        double[] highs   = candles.stream().mapToDouble(c -> c.high().doubleValue()).toArray();
        double[] lows    = candles.stream().mapToDouble(c -> c.low().doubleValue()).toArray();
        double[] volumes = candles.stream().mapToDouble(c -> (double) c.volume()).toArray();

        int n    = closes.length;
        double last = closes[n - 1];

        // Returns
        double return1d    = n >= 2  ? pctChange(closes[n - 2], last) : 0;
        double return5d    = n >= 6  ? pctChange(closes[n - 6],  last) : 0;
        double return20d   = n >= 21 ? pctChange(closes[n - 21], last) : 0;
        double logReturn1d = n >= 2 && closes[n - 2] > 0 ? Math.log(last / closes[n - 2]) : 0;

        // RSI
        double rsi14 = n >= 15 ? safeDouble(() -> indicatorService.rsi(closes, 14), 50.0) : 50.0;

        // MACD
        double macdHistogram = 0, macdSignal = 0;
        if (n >= 35) {
            IndicatorResult macd = safeResult(() -> indicatorService.macd(closes, 12, 26, 9));
            if (macd != null) {
                macdHistogram = getComponent(macd, "histogram");
                macdSignal    = getComponent(macd, "signal");
            }
        }

        // ATR
        double atr14Pct = 0;
        if (n >= 15) {
            double atr = safeDouble(() -> indicatorService.atr(highs, lows, closes, 14), 0.0);
            atr14Pct = last > 0 ? atr / last : 0;
        }

        // ADX
        double adx14 = 0;
        if (n >= 28) {
            IndicatorResult adxResult = safeResult(() -> indicatorService.adx(highs, lows, closes, 14));
            if (adxResult != null) {
                adx14 = getComponent(adxResult, "adx");
            }
        }

        // VWAP distance
        double vwapDistance = 0;
        if (n >= 20) {
            double[] h20 = tail(highs, 20), l20 = tail(lows, 20),
                     c20 = tail(closes, 20), v20 = tail(volumes, 20);
            double vwap = safeDouble(() -> indicatorService.vwap(h20, l20, c20, v20), last);
            vwapDistance = vwap > 0 ? (last - vwap) / vwap : 0;
        }

        // EMA / SMA distances
        double ema9Distance   = emaDistance(closes, 9,  last);
        double ema21Distance  = emaDistance(closes, 21, last);
        double sma50Distance  = n >= 50
            ? safeDouble(() -> { double s = indicatorService.sma(closes, 50);  return (last - s) / s; }, 0.0)
            : 0;
        double sma200Distance = n >= 200
            ? safeDouble(() -> { double s = indicatorService.sma(closes, 200); return (last - s) / s; }, 0.0)
            : 0;

        // Bollinger %B
        double bbPercentB = 0.5;
        if (n >= 20) {
            IndicatorResult bb = safeResult(() -> indicatorService.bollingerBands(closes, 20, 2.0));
            if (bb != null) {
                double raw = getComponent(bb, "percentB");
                bbPercentB = Math.max(0.0, Math.min(1.0, raw));
            }
        }

        // Relative volume
        double relativeVolume = n >= 21
            ? safeDouble(() -> indicatorService.relativeVolume(volumes, 20), 1.0)
            : 1.0;

        // Volatility (annualised)
        double volatility20d = n >= 21
            ? safeDouble(() -> indicatorService.calculateVolatility(closes, 20), 0.2)
            : 0.2;

        // OBV direction signal
        double obvSignal = 0;
        if (n >= 10) {
            double[] obvArr = safeArr(() -> indicatorService.obv(closes, volumes));
            if (obvArr != null && obvArr.length >= 6) {
                double recent = obvArr[obvArr.length - 1];
                double prev   = obvArr[obvArr.length - 6];
                obvSignal = recent > prev ? 1.0 : (recent < prev ? -1.0 : 0.0);
            }
        }

        // Composite scores
        double momentumScore  = normalizeFeature(return5d * 0.6 + return20d * 0.4, -0.15, 0.15);
        double liquidityScore = normalizeFeature(relativeVolume, 0.5, 3.0);

        return new FeatureVector(
            symbol,
            FeatureVector.CURRENT_VERSION,
            Instant.now(),
            return1d, return5d, return20d, logReturn1d,
            rsi14,
            macdHistogram, macdSignal,
            atr14Pct, adx14,
            vwapDistance, ema9Distance, ema21Distance, sma50Distance, sma200Distance,
            bbPercentB,
            relativeVolume, volatility20d,
            obvSignal,
            sectorStrength,
            marketRegime.ordinal(),
            momentumScore, liquidityScore
        );
    }

    public Map<String, FeatureVector> generateFeaturesForUniverse(
            List<String> symbols,
            Map<String, List<CandleDto>> candleMap,
            MarketRegime regime,
            Map<String, Double> sectorStrengths) {
        Map<String, FeatureVector> result = new HashMap<>();
        for (String sym : symbols) {
            List<CandleDto> candles = candleMap.getOrDefault(sym, List.of());
            double strength = sectorStrengths.getOrDefault(sym, 0.5);
            result.put(sym, generateFeatures(sym, candles, regime, strength));
        }
        return result;
    }

    public double normalizeFeature(double value, double min, double max) {
        if (max == min) return 0.5;
        return Math.max(0.0, Math.min(1.0, (value - min) / (max - min)));
    }

    // --- Helpers ---

    private FeatureVector neutral(String symbol, MarketRegime regime, double sectorStrength) {
        return new FeatureVector(
            symbol, FeatureVector.CURRENT_VERSION, Instant.now(),
            0, 0, 0, 0,
            50.0, 0, 0,
            0, 0,
            0, 0, 0, 0, 0,
            0.5,
            1.0, 0.2,
            0,
            sectorStrength,
            regime.ordinal(),
            0.5, 0.5
        );
    }

    private double emaDistance(double[] closes, int period, double last) {
        if (closes.length < period) return 0;
        double[] arr = safeArr(() -> indicatorService.ema(closes, period));
        if (arr == null || arr.length == 0) return 0;
        double emaLast = arr[arr.length - 1];
        return emaLast > 0 ? (last - emaLast) / emaLast : 0;
    }

    private double pctChange(double from, double to) {
        return from == 0 ? 0 : (to - from) / from;
    }

    private double[] tail(double[] arr, int n) {
        if (arr.length <= n) return arr;
        double[] r = new double[n];
        System.arraycopy(arr, arr.length - n, r, 0, n);
        return r;
    }

    /** Safe getter for IndicatorResult.components() — returns 0.0 if key missing */
    private double getComponent(IndicatorResult r, String key) {
        Double val = r.components().get(key);
        return val != null ? val : 0.0;
    }

    @FunctionalInterface private interface Dbl { double get() throws Exception; }
    @FunctionalInterface private interface Res { IndicatorResult get() throws Exception; }
    @FunctionalInterface private interface Arr { double[] get() throws Exception; }

    private double         safeDouble(Dbl s, double def) { try { return s.get(); } catch (Exception e) { return def; } }
    private IndicatorResult safeResult(Res s)             { try { return s.get(); } catch (Exception e) { return null; } }
    private double[]        safeArr(Arr s)                { try { return s.get(); } catch (Exception e) { return null; } }
}
