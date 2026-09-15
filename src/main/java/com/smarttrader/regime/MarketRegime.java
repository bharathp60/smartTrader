package com.smarttrader.regime;

public enum MarketRegime {
    STRONG_BULL("Strong uptrend, broad participation"),
    BULL("Uptrend with moderate breadth"),
    SIDEWAYS("Range-bound, no clear direction"),
    VOLATILE("High volatility, unclear direction"),
    BEAR("Downtrend with moderate breadth"),
    STRONG_BEAR("Strong downtrend, broad selling");
    
    private final String description;
    MarketRegime(String d) { this.description = d; }
    
    public String getDescription() { return description; }
    public boolean isBullish() { return this == STRONG_BULL || this == BULL; }
    public boolean isBearish() { return this == BEAR || this == STRONG_BEAR; }
    public boolean isTradeAllowed() { return this != STRONG_BEAR; }
}
