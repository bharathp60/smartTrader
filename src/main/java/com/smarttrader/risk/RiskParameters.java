package com.smarttrader.risk;

public record RiskParameters(
    double maxDailyLossPercent,
    double maxTradeLossPercent,
    double maxPortfolioDrawdownPercent,
    double maxPositionSizePercent,
    double maxSectorExposurePercent,
    int maxOpenPositions,
    int maxConsecutiveLosses,
    double minRiskRewardRatio,
    double maxVolatilityPercent,
    double minLiquidityVolume,
    double maxSlippagePercent
) {
    public static RiskParameters defaults() {
        return new RiskParameters(2.0, 1.0, 5.0, 10.0, 25.0, 10, 5, 1.5, 6.0, 500_000.0, 0.5);
    }
}
