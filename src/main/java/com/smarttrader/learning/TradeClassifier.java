package com.smarttrader.learning;

import com.smarttrader.entity.TradeClassification;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class TradeClassifier {

    public TradeClassification classifyTrade(BigDecimal pnl, double aiConfidence, double mlProbability, double riskRewardAtEntry, boolean wasStopTriggered) {
        boolean isWin = pnl.compareTo(BigDecimal.ZERO) > 0;

        if (isWin) {
            if (aiConfidence >= 0.65 && mlProbability >= 0.55 && riskRewardAtEntry >= 1.5) {
                return TradeClassification.GOOD_WIN;
            } else {
                return TradeClassification.BAD_WIN;
            }
        } else {
            if (wasStopTriggered && aiConfidence >= 0.65 && riskRewardAtEntry >= 1.5) {
                return TradeClassification.GOOD_LOSS;
            } else {
                return TradeClassification.BAD_LOSS;
            }
        }
    }
}
