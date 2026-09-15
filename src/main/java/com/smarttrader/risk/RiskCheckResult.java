package com.smarttrader.risk;

import com.smarttrader.entity.RiskDecision;
import java.time.Instant;
import java.util.Map;

public record RiskCheckResult(
    RiskDecision decision,
    String reason,
    double riskScore,
    Map<String, Boolean> limitsChecked,
    Instant checkedAt
) {
    public boolean isApproved() { return decision == RiskDecision.APPROVED; }
    
    public static RiskCheckResult approved(double riskScore, Map<String, Boolean> limits) {
        return new RiskCheckResult(RiskDecision.APPROVED, null, riskScore, limits, Instant.now());
    }
    public static RiskCheckResult rejected(String reason, Map<String, Boolean> limits) {
        return new RiskCheckResult(RiskDecision.REJECTED, reason, 1.0, limits, Instant.now());
    }
}
