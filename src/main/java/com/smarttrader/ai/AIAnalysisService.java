package com.smarttrader.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smarttrader.exception.AiUnavailableException;
import com.smarttrader.ml.PredictionResult;
import com.smarttrader.regime.MarketRegime;
import com.smarttrader.ranking.CandidateScore;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.stereotype.Service;

@Service
public class AIAnalysisService {

    private final ChatClient chatClient;
    private final ObjectMapper objectMapper;

    public AIAnalysisService(ChatClient chatClient, ObjectMapper objectMapper) {
        this.chatClient = chatClient;
        this.objectMapper = objectMapper;
    }

    public AiDecisionDto analyzeStock(String symbol, CandidateScore score, PredictionResult prediction, MarketRegime regime) {
        try {
            BeanOutputConverter<AiDecisionDto> converter = new BeanOutputConverter<>(AiDecisionDto.class);
            String format = converter.getFormat();

            String promptText = String.format("""
                Analyze the following stock for a trading opportunity.
                Symbol: %s
                Candidate Score: %f
                ML Probability (Bullish): %f
                Expected Return: %f
                Market Regime: %s
                
                Provide a highly structured decision.
                %s
                """, symbol, 
                score != null ? score.totalScore() : 0.0, 
                prediction != null ? prediction.probabilityPositiveReturn() : 0.0,
                prediction != null ? prediction.expectedReturn() : 0.0,
                regime != null ? regime.name() : "UNKNOWN",
                format);

            AiDecisionDto response = chatClient.prompt()
                    .user(promptText)
                    .call()
                    .entity(converter);
                    
            if (response == null || !response.isValid()) {
                throw new AiUnavailableException("Invalid response from AI");
            }

            return response;
        } catch (Exception e) {
            throw new AiUnavailableException("Failed to analyze stock: " + symbol, e);
        }
    }
}
