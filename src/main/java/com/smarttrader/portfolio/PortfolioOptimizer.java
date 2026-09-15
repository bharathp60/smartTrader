package com.smarttrader.portfolio;

import com.smarttrader.ai.AiDecisionDto;
import com.smarttrader.regime.MarketRegime;
import com.smarttrader.risk.RiskParameters;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Optimizes the candidate list from AI analysis into the final set of orders.
 *
 * Principles:
 * - In STRONG_BEAR: return empty list (no new trades).
 * - In BEAR: halve the available slot count.
 * - Max 2 stocks per market segment (using holdingPeriod as a proxy).
 * - Sort by confidence × expectedRiskReward descending.
 * - Respect maxOpenPositions limit.
 */
@Service
public class PortfolioOptimizer {

    /**
     * Return the optimal subset of AI-approved candidates.
     *
     * @param candidates     AI-approved AiDecisionDto list
     * @param portfolioState current portfolio state
     * @param marketRegime   current market regime
     * @param riskParams     risk configuration
     * @return optimal subset to actually submit
     */
    public List<AiDecisionDto> optimize(
            List<AiDecisionDto> candidates,
            PortfolioState portfolioState,
            MarketRegime marketRegime,
            RiskParameters riskParams) {

        // In STRONG_BEAR: no new trades at all
        if (marketRegime == MarketRegime.STRONG_BEAR) {
            return List.of();
        }

        // Calculate optimal slot count
        int optimalCount    = calculateOptimalPositionCount(marketRegime, riskParams);
        int slotsAvailable  = Math.max(0, optimalCount - portfolioState.openPositionCount());

        // In BEAR: reduce positions by half
        if (marketRegime == MarketRegime.BEAR) {
            slotsAvailable = Math.max(0, slotsAvailable / 2);
        }

        if (slotsAvailable == 0) {
            return List.of();
        }

        // Sort by (confidence × expectedRiskReward) descending
        List<AiDecisionDto> sorted = candidates.stream()
            .filter(c -> c.isValid() && c.isTradeRecommended())
            .sorted((a, b) -> Double.compare(
                b.confidence() * b.expectedRiskReward(),
                a.confidence() * a.expectedRiskReward()))
            .collect(Collectors.toList());

        // Select top candidates, max 2 per holding period type
        List<AiDecisionDto> selected = new ArrayList<>();
        for (AiDecisionDto candidate : sorted) {
            if (selected.size() >= slotsAvailable) break;
            long holdingTypeCount = selected.stream()
                .filter(s -> s.holdingPeriod().equals(candidate.holdingPeriod()))
                .count();
            if (holdingTypeCount < 2) {
                selected.add(candidate);
            }
        }

        return selected;
    }

    public boolean canAddPosition(PortfolioState portfolioState, RiskParameters riskParams) {
        return portfolioState.openPositionCount() < riskParams.maxOpenPositions();
    }

    public int calculateOptimalPositionCount(MarketRegime marketRegime, RiskParameters riskParams) {
        if (marketRegime == null) return riskParams.maxOpenPositions();
        return switch (marketRegime) {
            case STRONG_BULL -> riskParams.maxOpenPositions();
            case BULL        -> (int) (riskParams.maxOpenPositions() * 0.8);
            case SIDEWAYS    -> (int) (riskParams.maxOpenPositions() * 0.5);
            case VOLATILE    -> (int) (riskParams.maxOpenPositions() * 0.3);
            case BEAR        -> (int) (riskParams.maxOpenPositions() * 0.2);
            case STRONG_BEAR -> 0;
        };
    }
}
