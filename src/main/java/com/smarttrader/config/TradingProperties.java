package com.smarttrader.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Top-level trading configuration bound from application.yml prefix "smart-trader".
 * <p>
 * Defaults:
 * <ul>
 *   <li>mode — PAPER (safe default)</li>
 *   <li>liveTradingEnabled — false (must be explicitly enabled via env var)</li>
 *   <li>deterministicFallbackEnabled — false</li>
 * </ul>
 */
@ConfigurationProperties(prefix = "smart-trader")
public record TradingProperties(
        TradingMode mode,
        boolean liveTradingEnabled,
        boolean deterministicFallbackEnabled,
        SecurityProperties security
) {

    /**
     * Compact constructor: apply default for null mode.
     * In Java 17 records the compact constructor cannot reassign, so we
     * canonicalize in the full canonical form instead.
     */
    public TradingProperties(
            TradingMode mode,
            boolean liveTradingEnabled,
            boolean deterministicFallbackEnabled,
            SecurityProperties security) {
        this.mode = (mode != null) ? mode : TradingMode.PAPER;
        this.liveTradingEnabled = liveTradingEnabled;
        this.deterministicFallbackEnabled = deterministicFallbackEnabled;
        this.security = (security != null) ? security : new SecurityProperties("smart-trader", "change-me-in-secure-env");
    }

    /** Live trading is allowed only when both flag AND mode agree. */
    public boolean liveTradingAllowed() {
        return mode == TradingMode.LIVE && liveTradingEnabled;
    }

    /** Nested record for JWT security properties. */
    public record SecurityProperties(String issuer, String secret) {
    }
}
