package com.smarttrader.backtest;

import com.smarttrader.dto.CandleDto;
import com.smarttrader.entity.OrderSide;
import com.smarttrader.indicators.IndicatorResult;
import com.smarttrader.indicators.TechnicalIndicatorService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Event-driven historical backtest simulation engine.
 *
 * Signal Logic (RSI + MACD crossover):
 *   ENTRY: RSI(14) in 40-62 AND MACD histogram crosses from <=0 to >0 AND close > SMA(20).
 *          Fill at next-bar open with slippage.
 *   EXIT:  Stop-loss hit, take-profit hit, RSI > 72, or MACD flips negative while in profit.
 *
 * Position Sizing: fixed-fractional — risk riskPerTradePercent of equity per trade.
 * Transaction Costs: slippage applied adversely on every fill; commission on entry and exit.
 */
@Service
public class BacktestEngine {

    private static final int    MIN_CANDLES   = 35;
    private static final double DEFAULT_SLIP  = 0.0005;
    private static final double DEFAULT_COMM  = 0.0003;
    private static final double DEFAULT_STOP  = 1.5;
    private static final double DEFAULT_TAKE  = 3.0;
    private static final double DEFAULT_RISK  = 1.0;

    private final TechnicalIndicatorService indicators;

    public BacktestEngine(TechnicalIndicatorService indicators) {
        this.indicators = indicators;
    }

    public BacktestResult runBacktest(BacktestRequest req) {
        List<CandleDto> candles = req.candles();
        if (candles == null || candles.size() < MIN_CANDLES) {
            return empty(req);
        }

        double slippage   = req.slippagePercent()    <= 0 ? DEFAULT_SLIP : req.slippagePercent()    / 100.0;
        double commission = req.commissionPercent()   <= 0 ? DEFAULT_COMM : req.commissionPercent()  / 100.0;
        double stopPct    = req.stopLossPercent()     <= 0 ? DEFAULT_STOP : req.stopLossPercent();
        double takePct    = req.takeProfitPercent()   <= 0 ? DEFAULT_TAKE : req.takeProfitPercent();
        double riskPct    = req.riskPerTradePercent() <= 0 ? DEFAULT_RISK : req.riskPerTradePercent();

        double[] closes  = candles.stream().mapToDouble(c -> c.close().doubleValue()).toArray();
        double[] highs   = candles.stream().mapToDouble(c -> c.high().doubleValue()).toArray();
        double[] lows    = candles.stream().mapToDouble(c -> c.low().doubleValue()).toArray();

        double[] rsiArr   = computeRsiSeries(closes);
        double[] histoArr = computeMacdHistoSeries(closes);
        double[] sma20Arr = computeSma20Series(closes);

        BigDecimal equity     = req.initialCapital();
        BigDecimal peakEquity = equity;
        BigDecimal prevEquity = equity;
        double maxDrawdown = 0;
        double sumDailyRet = 0, sumDailyRetSq = 0;
        int dailyCount = 0;

        List<BigDecimal> equityCurve = new ArrayList<>();
        List<BacktestTradeSummary> trades = new ArrayList<>();
        equityCurve.add(equity);

        boolean inTrade = false;
        double entryPrice = 0, stopPrice = 0, takePrice = 0, quantity = 0;
        int entryBar = 0;

        for (int i = MIN_CANDLES; i < candles.size(); i++) {
            double close     = closes[i];
            double rsi       = rsiArr[i];
            double histo     = histoArr[i];
            double prevHisto = histoArr[i - 1];
            double sma20     = sma20Arr[i];

            if (!inTrade) {
                boolean rsiOk     = rsi >= 40 && rsi <= 62;
                boolean macdCross = histo > 0 && prevHisto <= 0;
                boolean aboveSma  = close > sma20;

                if (rsiOk && macdCross && aboveSma && i + 1 < candles.size()) {
                    double open = candles.get(i + 1).open().doubleValue();
                    double fill = open * (1 + slippage);
                    double risk = fill * (stopPct / 100.0);
                    double qty  = Math.floor((equity.doubleValue() * riskPct / 100.0) / risk);
                    if (qty < 1) { i++; continue; }

                    double entryCost = fill * qty * commission;
                    equity     = equity.subtract(BigDecimal.valueOf(fill * qty + entryCost));
                    entryPrice = fill;
                    stopPrice  = fill * (1 - stopPct / 100.0);
                    takePrice  = fill * (1 + takePct / 100.0);
                    entryBar   = i + 1;
                    quantity   = qty;
                    inTrade    = true;
                    i++;
                }

            } else {
                boolean stopHit  = lows[i] <= stopPrice;
                boolean takeHit  = highs[i] >= takePrice;
                boolean rsiOB    = rsi > 72;
                boolean macdFlip = histo < 0 && prevHisto >= 0 && close > entryPrice;

                String exitReason = null;
                double exitPrice  = 0;

                if (stopHit) {
                    exitReason = "STOP_LOSS";
                    exitPrice  = stopPrice * (1 - slippage);
                } else if (takeHit) {
                    exitReason = "TAKE_PROFIT";
                    exitPrice  = takePrice * (1 - slippage);
                } else if (rsiOB) {
                    exitReason = "RSI_OVERBOUGHT";
                    exitPrice  = close * (1 - slippage);
                } else if (macdFlip) {
                    exitReason = "MACD_REVERSAL";
                    exitPrice  = close * (1 - slippage);
                }

                if (exitReason != null) {
                    double exitCost = exitPrice * quantity * commission;
                    equity = equity.add(BigDecimal.valueOf(exitPrice * quantity - exitCost));

                    BigDecimal pnl = BigDecimal.valueOf((exitPrice - entryPrice) * quantity - exitCost)
                                               .setScale(2, RoundingMode.HALF_UP);
                    double returnPct = (exitPrice - entryPrice) / entryPrice;

                    trades.add(new BacktestTradeSummary(
                        req.symbol(),
                        OrderSide.BUY,
                        BigDecimal.valueOf(quantity),
                        BigDecimal.valueOf(entryPrice).setScale(4, RoundingMode.HALF_UP),
                        BigDecimal.valueOf(exitPrice).setScale(4, RoundingMode.HALF_UP),
                        pnl,
                        returnPct,
                        candles.get(entryBar).timestamp(),
                        candles.get(i).timestamp(),
                        exitReason
                    ));
                    inTrade = false;
                }
            }

            equityCurve.add(equity);
            if (equity.compareTo(peakEquity) > 0) peakEquity = equity;
            if (peakEquity.compareTo(BigDecimal.ZERO) > 0) {
                double dd = peakEquity.subtract(equity)
                    .divide(peakEquity, 8, RoundingMode.HALF_UP).doubleValue();
                if (dd > maxDrawdown) maxDrawdown = dd;
            }
            if (prevEquity.compareTo(BigDecimal.ZERO) > 0) {
                double dr = equity.subtract(prevEquity)
                    .divide(prevEquity, 8, RoundingMode.HALF_UP).doubleValue();
                sumDailyRet   += dr;
                sumDailyRetSq += dr * dr;
                dailyCount++;
            }
            prevEquity = equity;
        }

        int total  = trades.size();
        int wins   = (int) trades.stream().filter(t -> t.pnl().signum() > 0).count();
        int losses = total - wins;

        BigDecimal totalWin  = trades.stream().filter(t -> t.pnl().signum() > 0)
            .map(BacktestTradeSummary::pnl).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalLoss = trades.stream().filter(t -> t.pnl().signum() < 0)
            .map(BacktestTradeSummary::pnl).reduce(BigDecimal.ZERO, BigDecimal::add).abs();

        double winRate      = total == 0 ? 0 : (double) wins / total;
        double profitFactor = totalLoss.signum() == 0
            ? (totalWin.signum() > 0 ? 99.0 : 0.0)
            : totalWin.divide(totalLoss, 4, RoundingMode.HALF_UP).doubleValue();

        BigDecimal avgWin  = wins   > 0 ? totalWin.divide(BigDecimal.valueOf(wins),   2, RoundingMode.HALF_UP) : BigDecimal.ZERO;
        BigDecimal avgLoss = losses > 0 ? totalLoss.divide(BigDecimal.valueOf(losses), 2, RoundingMode.HALF_UP).negate() : BigDecimal.ZERO;

        BigDecimal largestWin  = trades.stream().map(BacktestTradeSummary::pnl)
            .filter(p -> p.signum() > 0).max(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
        BigDecimal largestLoss = trades.stream().map(BacktestTradeSummary::pnl)
            .filter(p -> p.signum() < 0).min(BigDecimal::compareTo).orElse(BigDecimal.ZERO);

        BigDecimal totalReturn = equity.subtract(req.initialCapital());
        double totalReturnPct  = req.initialCapital().signum() > 0
            ? totalReturn.divide(req.initialCapital(), 6, RoundingMode.HALF_UP).doubleValue() : 0;

        double sharpe = 0;
        if (dailyCount > 1) {
            double avgR = sumDailyRet / dailyCount;
            double var  = (sumDailyRetSq / dailyCount) - avgR * avgR;
            double std  = var > 0 ? Math.sqrt(var) : 0;
            sharpe = std > 0 ? (avgR / std) * Math.sqrt(252) : 0;
        }

        double expectancy = total == 0 ? 0
            : winRate * avgWin.doubleValue() + (1 - winRate) * avgLoss.doubleValue();

        long daysBetween = req.startDate().until(req.endDate(), java.time.temporal.ChronoUnit.DAYS);
        double years = daysBetween / 365.25;
        double cagr  = years > 0 && req.initialCapital().signum() > 0
            ? Math.pow(1 + totalReturnPct, 1.0 / years) - 1 : 0;

        return new BacktestResult(
            req.strategyName(), req.symbol(), req.startDate(), req.endDate(),
            req.initialCapital(), equity,
            totalReturn, totalReturnPct, cagr,
            winRate, profitFactor, maxDrawdown, sharpe, sharpe * 0.9,
            total, wins, losses,
            avgWin, avgLoss, largestWin, largestLoss,
            expectancy, trades, equityCurve
        );
    }

    // --- Indicator series helpers ---

    private double[] computeRsiSeries(double[] closes) {
        double[] rsi = new double[closes.length];
        Arrays.fill(rsi, 50.0);
        for (int i = 15; i < closes.length; i++) {
            double[] w = Arrays.copyOfRange(closes, i - 14, i + 1);
            try { rsi[i] = indicators.rsi(w, 14); } catch (Exception e) { rsi[i] = 50.0; }
        }
        return rsi;
    }

    private double[] computeMacdHistoSeries(double[] closes) {
        double[] histo = new double[closes.length];
        for (int i = 35; i < closes.length; i++) {
            double[] w = Arrays.copyOfRange(closes, 0, i + 1);
            try {
                IndicatorResult r = indicators.macd(w, 12, 26, 9);
                Object raw = r.components().get("histogram");
                histo[i] = raw instanceof Double ? (Double) raw : 0.0;
            } catch (Exception e) { histo[i] = 0.0; }
        }
        return histo;
    }

    private double[] computeSma20Series(double[] closes) {
        double[] sma = new double[closes.length];
        for (int i = 20; i < closes.length; i++) {
            double sum = 0;
            for (int j = i - 20; j < i; j++) sum += closes[j];
            sma[i] = sum / 20.0;
        }
        return sma;
    }

    private BacktestResult empty(BacktestRequest req) {
        return new BacktestResult(
            req.strategyName(), req.symbol(), req.startDate(), req.endDate(),
            req.initialCapital(), req.initialCapital(),
            BigDecimal.ZERO, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0,
            BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
            0, List.of(), List.of(req.initialCapital())
        );
    }
}
