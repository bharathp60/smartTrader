package com.smarttrader.regime;

public record RegimeIndicators(
    double indexRsi,
    double indexMacdHistogram,
    double volatilityPercent,
    double breadthRatio,
    double indexAbove200Sma,
    double momentum20d
) {}
