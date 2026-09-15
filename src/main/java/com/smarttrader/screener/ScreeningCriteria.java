package com.smarttrader.screener;

import com.smarttrader.entity.TradingProfile;
import java.math.BigDecimal;

public record ScreeningCriteria(
    BigDecimal minAvgVolume,
    BigDecimal minMarketCap,
    double minRsiValue,
    double maxRsiValue,
    double minRelativeVolume,
    double maxAtrPct,
    double minMlProbability,
    TradingProfile profile
) {
    public static ScreeningCriteria defaults(TradingProfile profile) {
        return new ScreeningCriteria(
            BigDecimal.valueOf(500_000),
            BigDecimal.valueOf(1_000_000_000L),
            30.0, 80.0, 1.2, 5.0, 0.55, profile
        );
    }
}
