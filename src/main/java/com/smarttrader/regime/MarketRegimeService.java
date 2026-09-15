package com.smarttrader.regime;

import org.springframework.stereotype.Service;

@Service
public class MarketRegimeService {
    
    private MarketRegime currentRegime = MarketRegime.SIDEWAYS;

    public MarketRegime detectRegime(RegimeIndicators indicators) {
        if (indicators.indexRsi() > 65 && indicators.indexMacdHistogram() > 0 
                && indicators.breadthRatio() > 0.6 && indicators.indexAbove200Sma() > 0) {
            return MarketRegime.STRONG_BULL;
        }
        if (indicators.indexRsi() > 55 && indicators.indexMacdHistogram() > 0 
                && indicators.breadthRatio() > 0.5) {
            return MarketRegime.BULL;
        }
        if (indicators.volatilityPercent() > 3.0 && Math.abs(indicators.momentum20d()) < 2) {
            return MarketRegime.VOLATILE;
        }
        if (indicators.indexRsi() < 40 && indicators.indexMacdHistogram() < 0 
                && indicators.breadthRatio() < 0.35 && indicators.indexAbove200Sma() == 0) {
            return MarketRegime.STRONG_BEAR;
        }
        if (indicators.indexRsi() < 45 && indicators.indexMacdHistogram() < 0 
                && indicators.breadthRatio() < 0.45) {
            return MarketRegime.BEAR;
        }
        return MarketRegime.SIDEWAYS;
    }

    public MarketRegime getCurrentRegime() {
        return currentRegime;
    }
    
    public void setCurrentRegime(MarketRegime currentRegime) {
        this.currentRegime = currentRegime;
    }

    public double getRegimeStrategyMultiplier(MarketRegime regime) {
        return switch (regime) {
            case STRONG_BULL -> 1.5;
            case BULL -> 1.2;
            case SIDEWAYS -> 0.8;
            case VOLATILE -> 0.5;
            case BEAR -> 0.3;
            case STRONG_BEAR -> 0.0;
        };
    }
}
