package com.smarttrader.ai;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;
import java.util.List;

public record AiDecisionDto(
    @NotBlank String symbol,
    @NotBlank String action,            // "BUY", "SELL", "NO_TRADE"
    @NotBlank String opportunityRating, // "TOP_OPPORTUNITY", "SECONDARY_OPPORTUNITY", "WATCHLIST", "NO_TRADE"
    @DecimalMin("0.0") @DecimalMax("1.0") double confidence,
    @NotBlank String marketView,
    @NotBlank String trendAssessment,
    @NotBlank String riskAssessment,
    BigDecimal entryZoneLow,
    BigDecimal entryZoneHigh,
    BigDecimal stopLoss,
    BigDecimal target,
    double expectedRiskReward,
    @NotBlank String holdingPeriod,     // "INTRADAY", "2-5 DAYS", "2-4 WEEKS"
    List<String> warnings,
    @NotBlank String reasoningSummary
) {
    public boolean isTradeRecommended() { 
        return "BUY".equals(action) || "SELL".equals(action); 
    }
    public boolean isHighConfidence() { return confidence >= 0.75; }
    public boolean isValid() {
        return symbol != null && !symbol.isBlank()
            && action != null && (action.equals("BUY") || action.equals("SELL") || action.equals("NO_TRADE"))
            && confidence >= 0.0 && confidence <= 1.0
            && (stopLoss == null || target == null || target.compareTo(stopLoss) > 0);
    }
}
