package com.smarttrader.ranking;

import com.smarttrader.entity.TradingProfile;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Service
public class StockRankingService {

    public List<CandidateScore> rankCandidates(List<String> symbols, TradingProfile profile, Map<String, Map<String, Double>> factorValues) {
        ScoringProfile sp = ScoringProfile.forProfile(profile);
        
        return symbols.stream().map(sym -> {
            Map<String, Double> factors = factorValues.getOrDefault(sym, Map.of());
            
            double tech = factors.getOrDefault("technical", 0.0);
            double mom = factors.getOrDefault("momentum", 0.0);
            double vol = factors.getOrDefault("volume", 0.0);
            double sect = factors.getOrDefault("sector", 0.0);
            double reg = factors.getOrDefault("regime", 0.0);
            double fund = factors.getOrDefault("fundamental", 0.0);
            double ml = factors.getOrDefault("mlScore", 0.0);
            double rr = factors.getOrDefault("riskReward", 0.0);
            double liq = factors.getOrDefault("liquidity", 0.0);
            double ai = factors.getOrDefault("aiContext", 0.0);
            
            double totalScore = tech * sp.getWeight("technical") +
                                mom * sp.getWeight("momentum") +
                                vol * sp.getWeight("volume") +
                                sect * sp.getWeight("sector") +
                                reg * sp.getWeight("regime") +
                                fund * sp.getWeight("fundamental") +
                                ml * sp.getWeight("mlScore") +
                                rr * sp.getWeight("riskReward");
            
            return new CandidateScore(sym, profile, tech, mom, vol, sect, reg, fund, ml, rr, liq, ai, totalScore, Instant.now());
        })
        .sorted((a, b) -> Double.compare(b.totalScore(), a.totalScore()))
        .toList();
    }

    public double calculateTechnicalScore(double rsi, double macdHistogram, double bbPercentB) {
        double rsiScore = (rsi > 40 && rsi < 70) ? 0.8 : 0.2;
        double macdScore = macdHistogram > 0 ? 1.0 : 0.0;
        double bbScore = (bbPercentB > 0.2 && bbPercentB < 0.8) ? 0.8 : 0.2;
        return (rsiScore * 0.4) + (macdScore * 0.4) + (bbScore * 0.2);
    }

    public double calculateMomentumScore(double roc10, double roc20, double priceVsEma20) {
        double roc10Score = roc10 > 0 ? 1.0 : 0.0;
        double roc20Score = roc20 > 0 ? 1.0 : 0.0;
        double emaScore = priceVsEma20 > 0 ? 1.0 : 0.0;
        return (roc10Score * 0.3) + (roc20Score * 0.3) + (emaScore * 0.4);
    }

    public double calculateVolumeScore(double relativeVolume, double obvTrend) {
        double rvScore = Math.min(relativeVolume / 3.0, 1.0);
        double obvScore = obvTrend > 0 ? 1.0 : 0.0;
        return (rvScore * 0.6) + (obvScore * 0.4);
    }

    public List<CandidateScore> getTopCandidates(int n, TradingProfile profile, Map<String, Map<String, Double>> factorValues) {
        List<String> symbols = factorValues.keySet().stream().toList();
        return rankCandidates(symbols, profile, factorValues).stream()
                .limit(n)
                .toList();
    }
}
