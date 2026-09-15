package com.smarttrader.learning;

import com.smarttrader.regime.MarketRegime;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class MistakeDetectionService {

    public List<MistakeTaxonomy> detectMistakes(TradeAnalysis analysis, double entryVsIntendedEntry, double exitSlippage, double riskRewardAtEntry, boolean wasBreakoutEntry, boolean tookBuy) {
        List<MistakeTaxonomy> mistakes = new ArrayList<>();

        if (entryVsIntendedEntry > 0.005) { // > 0.5%
            mistakes.add(MistakeTaxonomy.LATE_ENTRY);
        }

        if (analysis.mlProbability() < 0.55) {
            mistakes.add(MistakeTaxonomy.EARLY_ENTRY);
        }

        if (analysis.pnl().compareTo(BigDecimal.ZERO) < 0 && wasBreakoutEntry) {
            mistakes.add(MistakeTaxonomy.FALSE_BREAKOUT);
        }

        boolean isBearish = analysis.regimeAtEntry() == MarketRegime.BEAR || analysis.regimeAtEntry() == MarketRegime.STRONG_BEAR;
        if (analysis.pnl().compareTo(BigDecimal.ZERO) < 0 && isBearish && tookBuy) {
            mistakes.add(MistakeTaxonomy.TREND_MISREAD);
        }

        if (riskRewardAtEntry < 1.5) {
            mistakes.add(MistakeTaxonomy.POOR_RISK_REWARD);
        }

        if (exitSlippage > 0.005) { // > 0.5%
            mistakes.add(MistakeTaxonomy.HIGH_SLIPPAGE);
        }

        if (tookBuy && isBearish) {
            if (!mistakes.contains(MistakeTaxonomy.WRONG_MARKET_REGIME)) {
                mistakes.add(MistakeTaxonomy.WRONG_MARKET_REGIME);
            }
        }

        if (analysis.mlProbability() >= 0.65 && analysis.pnl().compareTo(BigDecimal.ZERO) < 0) {
            mistakes.add(MistakeTaxonomy.ML_FALSE_POSITIVE);
        }

        if (analysis.aiConfidence() >= 0.85 && analysis.returnPct() < -0.01) {
            mistakes.add(MistakeTaxonomy.AI_OVERCONFIDENCE);
        }

        return mistakes;
    }
}
