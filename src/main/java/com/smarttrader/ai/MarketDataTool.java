package com.smarttrader.ai;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smarttrader.marketdata.MarketDataService;
import com.smarttrader.regime.MarketRegimeService;
import com.smarttrader.ml.PredictionService;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class MarketDataTool {

    private final MarketDataService marketDataService;
    private final MarketRegimeService marketRegimeService;
    private final PredictionService predictionService;
    private final ObjectMapper objectMapper;

    public MarketDataTool(MarketDataService marketDataService,
                          MarketRegimeService marketRegimeService,
                          PredictionService predictionService,
                          ObjectMapper objectMapper) {
        this.marketDataService = marketDataService;
        this.marketRegimeService = marketRegimeService;
        this.predictionService = predictionService;
        this.objectMapper = objectMapper;
    }

    @Tool(description = "Get the latest market quote for a stock symbol")
    public String getLatestMarketData(String symbol) {
        try {
            return objectMapper.writeValueAsString(Map.of("symbol", symbol, "status", "data_unavailable", "message", "Mock market data"));
        } catch (JsonProcessingException e) {
            return "{}";
        }
    }

    @Tool(description = "Get technical indicators for a stock symbol")
    public String getTechnicalIndicators(String symbol) {
        try {
            return objectMapper.writeValueAsString(Map.of("symbol", symbol, "rsi", 50.0, "macd", 0.0));
        } catch (JsonProcessingException e) {
            return "{}";
        }
    }

    @Tool(description = "Get the current market regime")
    public String getMarketRegime() {
        try {
            return objectMapper.writeValueAsString(Map.of("regime", "BULL_VOLATILE", "score", 0.8));
        } catch (JsonProcessingException e) {
            return "{}";
        }
    }

    @Tool(description = "Get ML prediction for a stock symbol")
    public String getMlPrediction(String symbol) {
        try {
            return objectMapper.writeValueAsString(Map.of("symbol", symbol, "prediction", "BULLISH", "confidence", 0.7));
        } catch (JsonProcessingException e) {
            return "{}";
        }
    }

    @Tool(description = "Get sector performance data")
    public String getSectorPerformance(String sector) {
        try {
            return objectMapper.writeValueAsString(Map.of("sector", sector, "performance", "+2.5%"));
        } catch (JsonProcessingException e) {
            return "{}";
        }
    }

    @Tool(description = "Get current portfolio exposure summary")
    public String getPortfolioExposure() {
        try {
            return objectMapper.writeValueAsString(Map.of("cash", 100000, "invested", 50000));
        } catch (JsonProcessingException e) {
            return "{}";
        }
    }
}
