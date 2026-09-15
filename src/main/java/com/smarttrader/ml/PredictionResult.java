package com.smarttrader.ml;

import java.time.Instant;
import java.util.Map;

public record PredictionResult(
    String symbol,
    String modelName,
    String modelVersion,
    Instant predictedAt,
    String horizon,                     // "1d", "5d", "20d"
    double probabilityPositiveReturn,
    double expectedReturn,
    double probabilityTargetHit,
    double probabilityStopHit,
    double expectedVolatility,
    double trendProbability,
    double confidence,
    Map<String, Double> featureImportances
) {
    public boolean isHighConfidence() { return confidence >= 0.70; }
    public boolean isBullish() { return probabilityPositiveReturn >= 0.60; }
}
