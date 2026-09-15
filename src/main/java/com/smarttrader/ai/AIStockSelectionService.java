package com.smarttrader.ai;

import com.smarttrader.ml.PredictionResult;
import com.smarttrader.ranking.CandidateScore;
import com.smarttrader.regime.MarketRegime;
import com.smarttrader.exception.AiUnavailableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AIStockSelectionService {

    private static final Logger log = LoggerFactory.getLogger(AIStockSelectionService.class);

    private final AIAnalysisService aiAnalysisService;

    public AIStockSelectionService(AIAnalysisService aiAnalysisService) {
        this.aiAnalysisService = aiAnalysisService;
    }

    public List<AiDecisionDto> selectTopCandidates(List<CandidateScore> candidates, Map<String, PredictionResult> predictions, MarketRegime regime) {
        List<AiDecisionDto> results = new ArrayList<>();

        // Limit to top 10
        List<CandidateScore> topCandidates = candidates.stream()
                .limit(10)
                .collect(Collectors.toList());

        for (CandidateScore candidate : topCandidates) {
            try {
                PredictionResult prediction = predictions.get(candidate.symbol());
                AiDecisionDto decision = aiAnalysisService.analyzeStock(candidate.symbol(), candidate, prediction, regime);

                if ("TOP_OPPORTUNITY".equals(decision.opportunityRating()) || "SECONDARY_OPPORTUNITY".equals(decision.opportunityRating())) {
                    results.add(decision);
                }
            } catch (AiUnavailableException e) {
                log.error("AI Unavailable for symbol {}, returning empty list as fallback is not permitted.", candidate.symbol(), e);
                return new ArrayList<>(); // Return empty list on AI failure
            } catch (Exception e) {
                log.error("Error analyzing symbol {}", candidate.symbol(), e);
            }
        }

        return results;
    }
}
