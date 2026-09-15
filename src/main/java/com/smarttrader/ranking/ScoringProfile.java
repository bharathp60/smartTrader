package com.smarttrader.ranking;

import com.smarttrader.entity.TradingProfile;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ScoringProfile {
    private final TradingProfile profile;
    private final List<FactorWeight> weights;
    private final Map<String, FactorWeight> weightMap;

    private ScoringProfile(TradingProfile profile, List<FactorWeight> weights) {
        this.profile = profile;
        this.weights = weights;
        this.weightMap = weights.stream().collect(Collectors.toMap(FactorWeight::factorName, w -> w));
    }

    public static ScoringProfile forProfile(TradingProfile profile) {
        return switch (profile) {
            case INTRADAY -> new ScoringProfile(profile, List.of(
                new FactorWeight("momentum", 0.25, true),
                new FactorWeight("volume", 0.20, true),
                new FactorWeight("technical", 0.20, true),
                new FactorWeight("mlScore", 0.15, true),
                new FactorWeight("regime", 0.10, true),
                new FactorWeight("riskReward", 0.10, true)
            ));
            case SWING -> new ScoringProfile(profile, List.of(
                new FactorWeight("technical", 0.25, true),
                new FactorWeight("mlScore", 0.20, true),
                new FactorWeight("momentum", 0.15, true),
                new FactorWeight("fundamental", 0.15, true),
                new FactorWeight("regime", 0.10, true),
                new FactorWeight("volume", 0.10, true),
                new FactorWeight("riskReward", 0.05, true)
            ));
            case POSITIONAL -> new ScoringProfile(profile, List.of(
                new FactorWeight("fundamental", 0.25, true),
                new FactorWeight("technical", 0.20, true),
                new FactorWeight("mlScore", 0.20, true),
                new FactorWeight("regime", 0.15, true),
                new FactorWeight("momentum", 0.10, true),
                new FactorWeight("volume", 0.10, true)
            ));
        };
    }

    public double getWeight(String factorName) {
        FactorWeight fw = weightMap.get(factorName);
        return (fw != null && fw.enabled()) ? fw.weight() : 0.0;
    }
    
    public TradingProfile getProfile() {
        return profile;
    }
}
