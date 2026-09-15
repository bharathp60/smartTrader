package com.smarttrader.features;

import java.time.Instant;

public record FeatureVector(
    String symbol,
    String featureVersion,
    Instant generatedAt,
    double return1d,
    double return5d,
    double return20d,
    double logReturn1d,
    double rsi14,
    double macdHistogram,
    double macdSignal,
    double atr14Pct,
    double adx14,
    double vwapDistance,
    double ema9Distance,
    double ema21Distance,
    double sma50Distance,
    double sma200Distance,
    double bbPercentB,
    double relativeVolume,
    double volatility20d,
    double obv,
    double sectorStrength,
    int marketRegimeOrdinal,
    double momentumScore,
    double liquidityScore
) {
    public static final String CURRENT_VERSION = "v1.0";
}
