package com.smarttrader.ml;

import java.time.Instant;

public record ModelMetrics(
    double accuracy,
    double precision,
    double recall,
    double f1Score,
    double auc,
    double sharpeRatio,
    double profitFactor,
    int sampleSize,
    Instant evaluatedAt
) {}
