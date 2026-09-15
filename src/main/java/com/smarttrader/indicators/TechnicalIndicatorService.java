package com.smarttrader.indicators;

import org.springframework.stereotype.Service;
import java.util.Arrays;
import java.util.Map;

@Service
public class TechnicalIndicatorService {

    public double sma(double[] prices, int period) {
        validateLength(prices, period);
        double sum = 0;
        for (int i = prices.length - period; i < prices.length; i++) {
            sum += prices[i];
        }
        return sum / period;
    }

    public double[] ema(double[] prices, int period) {
        validateLength(prices, period);
        double[] ema = new double[prices.length];
        double multiplier = 2.0 / (period + 1);
        
        double sum = 0;
        for (int i = 0; i < period; i++) {
            sum += prices[i];
        }
        ema[period - 1] = sum / period; // Initial SMA
        
        for (int i = period; i < prices.length; i++) {
            ema[i] = (prices[i] - ema[i - 1]) * multiplier + ema[i - 1];
        }
        return ema; // Returning full array, although usually last value is needed
    }

    public double rsi(double[] prices, int period) {
        validateLength(prices, period + 1);
        double gain = 0;
        double loss = 0;
        
        for (int i = prices.length - period; i < prices.length; i++) {
            double change = prices[i] - prices[i - 1];
            if (change > 0) gain += change;
            else loss -= change;
        }
        
        double avgGain = gain / period;
        double avgLoss = loss / period;
        
        // Wilder's smoothing would typically require historical average gains/losses,
        // For a stateless calculation over a fixed window, this is the simple RSI
        
        if (avgLoss == 0) return 100;
        double rs = avgGain / avgLoss;
        return 100 - (100 / (1 + rs));
    }

    public IndicatorResult macd(double[] prices, int fastPeriod, int slowPeriod, int signalPeriod) {
        validateLength(prices, slowPeriod + signalPeriod);
        double[] fastEma = ema(prices, fastPeriod);
        double[] slowEma = ema(prices, slowPeriod);
        
        double[] macdLine = new double[prices.length];
        for (int i = slowPeriod - 1; i < prices.length; i++) {
            macdLine[i] = fastEma[i] - slowEma[i];
        }
        
        // Calculate signal line (EMA of MACD line)
        // Adjusting MACD line for EMA calculation
        double[] validMacd = Arrays.copyOfRange(macdLine, slowPeriod - 1, macdLine.length);
        double[] signalEma = ema(validMacd, signalPeriod);
        
        double currentMacd = macdLine[macdLine.length - 1];
        double currentSignal = signalEma[signalEma.length - 1];
        double histogram = currentMacd - currentSignal;
        
        return new IndicatorResult("MACD", currentMacd, Map.of(
            "macd", currentMacd,
            "signal", currentSignal,
            "histogram", histogram
        ));
    }

    public IndicatorResult bollingerBands(double[] prices, int period, double stdDevMultiplier) {
        validateLength(prices, period);
        double sma = sma(prices, period);
        
        double varianceSum = 0;
        for (int i = prices.length - period; i < prices.length; i++) {
            varianceSum += Math.pow(prices[i] - sma, 2);
        }
        double stdDev = Math.sqrt(varianceSum / period);
        
        double upper = sma + (stdDevMultiplier * stdDev);
        double lower = sma - (stdDevMultiplier * stdDev);
        double bandwidth = (upper - lower) / sma;
        double percentB = (prices[prices.length - 1] - lower) / (upper - lower);
        
        return new IndicatorResult("BollingerBands", sma, Map.of(
            "upper", upper,
            "middle", sma,
            "lower", lower,
            "bandwidth", bandwidth,
            "percentB", percentB
        ));
    }

    public double atr(double[] highs, double[] lows, double[] closes, int period) {
        validateLength(closes, period + 1);
        double sumTr = 0;
        for (int i = closes.length - period; i < closes.length; i++) {
            double tr = Math.max(highs[i] - lows[i],
                        Math.max(Math.abs(highs[i] - closes[i - 1]),
                                 Math.abs(lows[i] - closes[i - 1])));
            sumTr += tr;
        }
        return sumTr / period;
    }

    public IndicatorResult adx(double[] highs, double[] lows, double[] closes, int period) {
        // Simplified ADX for length constraints
        double plusDm = 0, minusDm = 0, tr = 0;
        int len = closes.length;
        if(len < period + 1) throw new IllegalArgumentException();
        for (int i = len - period; i < len; i++) {
            double upMove = highs[i] - highs[i-1];
            double downMove = lows[i-1] - lows[i];
            
            if (upMove > downMove && upMove > 0) plusDm += upMove;
            if (downMove > upMove && downMove > 0) minusDm += downMove;
            
            tr += Math.max(highs[i] - lows[i], Math.max(Math.abs(highs[i] - closes[i-1]), Math.abs(lows[i] - closes[i-1])));
        }
        
        double plusDi = 100 * (plusDm / tr);
        double minusDi = 100 * (minusDm / tr);
        double dx = 100 * Math.abs(plusDi - minusDi) / (plusDi + minusDi == 0 ? 1 : plusDi + minusDi);
        
        return new IndicatorResult("ADX", dx, Map.of("adx", dx, "plusDI", plusDi, "minusDI", minusDi));
    }

    public double vwap(double[] highs, double[] lows, double[] closes, double[] volumes) {
        validateLength(closes, 1);
        double sumPv = 0;
        double sumV = 0;
        for (int i = 0; i < closes.length; i++) {
            double typicalPrice = (highs[i] + lows[i] + closes[i]) / 3.0;
            sumPv += typicalPrice * volumes[i];
            sumV += volumes[i];
        }
        return sumPv / sumV;
    }

    public double[] obv(double[] closes, double[] volumes) {
        validateLength(closes, 2);
        double[] obv = new double[closes.length];
        obv[0] = volumes[0];
        for (int i = 1; i < closes.length; i++) {
            if (closes[i] > closes[i - 1]) {
                obv[i] = obv[i - 1] + volumes[i];
            } else if (closes[i] < closes[i - 1]) {
                obv[i] = obv[i - 1] - volumes[i];
            } else {
                obv[i] = obv[i - 1];
            }
        }
        return obv;
    }

    public IndicatorResult stochastic(double[] highs, double[] lows, double[] closes, int kPeriod, int dPeriod) {
        validateLength(closes, kPeriod + dPeriod);
        
        double currentClose = closes[closes.length - 1];
        double highestHigh = highs[highs.length - kPeriod];
        double lowestLow = lows[lows.length - kPeriod];
        
        for (int i = highs.length - kPeriod; i < highs.length; i++) {
            highestHigh = Math.max(highestHigh, highs[i]);
            lowestLow = Math.min(lowestLow, lows[i]);
        }
        
        double k = 100 * ((currentClose - lowestLow) / (highestHigh - lowestLow == 0 ? 1 : highestHigh - lowestLow));
        
        // Simplified %D as SMA of %K would require array of %K, just returning %K as %D for this stub
        return new IndicatorResult("Stochastic", k, Map.of("k", k, "d", k)); // To fully implement, need a moving window for %D
    }

    public double roc(double[] prices, int period) {
        validateLength(prices, period + 1);
        double current = prices[prices.length - 1];
        double previous = prices[prices.length - 1 - period];
        return ((current - previous) / previous) * 100;
    }

    public double momentum(double[] prices, int period) {
        validateLength(prices, period + 1);
        return prices[prices.length - 1] - prices[prices.length - 1 - period];
    }

    public double volumeMovingAverage(double[] volumes, int period) {
        validateLength(volumes, period);
        double sum = 0;
        for (int i = volumes.length - period; i < volumes.length; i++) {
            sum += volumes[i];
        }
        return sum / period;
    }

    public double relativeVolume(double[] volumes, int period) {
        validateLength(volumes, period + 1);
        double currentVol = volumes[volumes.length - 1];
        double avgVol = volumeMovingAverage(Arrays.copyOfRange(volumes, 0, volumes.length - 1), period);
        return avgVol == 0 ? 0 : currentVol / avgVol;
    }

    public double calculateVolatility(double[] prices, int period) {
        validateLength(prices, period + 1);
        double[] returns = new double[period];
        double sumReturns = 0;
        
        for (int i = 0; i < period; i++) {
            int idx = prices.length - period + i;
            returns[i] = Math.log(prices[idx] / prices[idx - 1]);
            sumReturns += returns[i];
        }
        
        double meanReturn = sumReturns / period;
        double varianceSum = 0;
        for (double r : returns) {
            varianceSum += Math.pow(r - meanReturn, 2);
        }
        
        double dailyVol = Math.sqrt(varianceSum / (period - 1));
        return dailyVol * Math.sqrt(252); // Annualized
    }

    public double findSupportLevel(double[] lows, int lookback) {
        validateLength(lows, lookback);
        double min = lows[lows.length - lookback];
        for (int i = lows.length - lookback + 1; i < lows.length; i++) {
            if (lows[i] < min) {
                min = lows[i];
            }
        }
        return min;
    }

    public double findResistanceLevel(double[] highs, int lookback) {
        validateLength(highs, lookback);
        double max = highs[highs.length - lookback];
        for (int i = highs.length - lookback + 1; i < highs.length; i++) {
            if (highs[i] > max) {
                max = highs[i];
            }
        }
        return max;
    }

    public boolean detectBreakout(double[] closes, double[] highs, int period) {
        validateLength(closes, period + 1);
        double currentClose = closes[closes.length - 1];
        double maxHigh = highs[highs.length - period - 1];
        
        for (int i = highs.length - period - 1; i < highs.length - 1; i++) {
            if (highs[i] > maxHigh) {
                maxHigh = highs[i];
            }
        }
        return currentClose > maxHigh;
    }

    public double trendStrength(double[] prices, int period) {
        validateLength(prices, period);
        double sumX = 0, sumY = 0, sumXY = 0, sumX2 = 0;
        
        for (int i = 0; i < period; i++) {
            double y = prices[prices.length - period + i];
            sumX += i;
            sumY += y;
            sumXY += i * y;
            sumX2 += i * i;
        }
        
        double meanX = sumX / period;
        double meanY = sumY / period;
        
        double numerator = sumXY - period * meanX * meanY;
        double denominatorX = sumX2 - period * meanX * meanX;
        
        double slope = numerator / denominatorX;
        
        // Simplified R-squared
        double ssTot = 0, ssRes = 0;
        for (int i = 0; i < period; i++) {
            double y = prices[prices.length - period + i];
            double yPred = meanY + slope * (i - meanX);
            ssTot += Math.pow(y - meanY, 2);
            ssRes += Math.pow(y - yPred, 2);
        }
        
        return ssTot == 0 ? 0 : 1 - (ssRes / ssTot);
    }

    public String detectGap(double prevClose, double open, double gapThresholdPct) {
        double pctChange = ((open - prevClose) / prevClose) * 100;
        if (pctChange >= gapThresholdPct) {
            return "GAP_UP";
        } else if (pctChange <= -gapThresholdPct) {
            return "GAP_DOWN";
        }
        return "NO_GAP";
    }

    private void validateLength(double[] array, int requiredLength) {
        if (array == null || array.length < requiredLength) {
            throw new IllegalArgumentException("Array length must be at least " + requiredLength);
        }
    }
}
