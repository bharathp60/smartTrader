package com.smarttrader.performance;

import com.smarttrader.entity.DailyPerformance;
import com.smarttrader.learning.TradeAnalysis;
import com.smarttrader.repository.DailyPerformanceRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
public class PerformanceEngine {

    private final DailyPerformanceRepository dailyPerformanceRepository;

    public PerformanceEngine(DailyPerformanceRepository dailyPerformanceRepository) {
        this.dailyPerformanceRepository = dailyPerformanceRepository;
    }

    public PerformanceMetrics calculateDailyMetrics(LocalDate date) {
        // Mocked full logic, needs DB lookups for accurate total lists
        return new PerformanceMetrics(
            date, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
            0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
            0, 0, 0, Map.of(), Map.of()
        );
    }

    public PerformanceMetrics calculateWeeklyMetrics(LocalDate weekEnding) {
        return calculateDailyMetrics(weekEnding);
    }

    public PerformanceMetrics calculateMonthlyMetrics(int year, int month) {
        return calculateDailyMetrics(LocalDate.of(year, month, 1));
    }

    public BigDecimal getDailyPnl(LocalDate date) {
        return dailyPerformanceRepository.findByPerformanceDate(date)
                .map(dp -> dp.getPnl())
                .orElse(BigDecimal.ZERO);
    }

    public double getWinRate(List<TradeAnalysis> analyses) {
        if (analyses.isEmpty()) return 0.0;
        long wins = analyses.stream().filter(a -> a.pnl().compareTo(BigDecimal.ZERO) > 0).count();
        return (double) wins / analyses.size();
    }

    public double getProfitFactor(List<TradeAnalysis> analyses) {
        BigDecimal grossProfit = BigDecimal.ZERO;
        BigDecimal grossLoss = BigDecimal.ZERO;
        
        for (TradeAnalysis a : analyses) {
            if (a.pnl().compareTo(BigDecimal.ZERO) > 0) {
                grossProfit = grossProfit.add(a.pnl());
            } else {
                grossLoss = grossLoss.add(a.pnl().abs());
            }
        }
        
        if (grossLoss.compareTo(BigDecimal.ZERO) == 0) return grossProfit.compareTo(BigDecimal.ZERO) > 0 ? 99.9 : 0.0;
        return grossProfit.divide(grossLoss, 4, java.math.RoundingMode.HALF_UP).doubleValue();
    }

    public double getSharpeRatio(List<Double> dailyReturns) {
        if (dailyReturns.isEmpty()) return 0.0;
        double mean = dailyReturns.stream().mapToDouble(d -> d).average().orElse(0.0);
        double variance = dailyReturns.stream().mapToDouble(d -> Math.pow(d - mean, 2)).average().orElse(0.0);
        double stdDev = Math.sqrt(variance);
        if (stdDev == 0) return 0.0;
        return (mean / stdDev) * Math.sqrt(252);
    }

    public double getSortinoRatio(List<Double> dailyReturns) {
        if (dailyReturns.isEmpty()) return 0.0;
        double mean = dailyReturns.stream().mapToDouble(d -> d).average().orElse(0.0);
        double variance = dailyReturns.stream().filter(d -> d < 0).mapToDouble(d -> Math.pow(d - mean, 2)).average().orElse(0.0);
        double stdDev = Math.sqrt(variance);
        if (stdDev == 0) return 0.0;
        return (mean / stdDev) * Math.sqrt(252);
    }

    public void persistDailyPerformance(PerformanceMetrics metrics) {
        DailyPerformance dp = new DailyPerformance();
        dp.setPerformanceDate(metrics.date());
        dp.setPnl(metrics.dailyPnl());
        dailyPerformanceRepository.save(dp);
    }
}
