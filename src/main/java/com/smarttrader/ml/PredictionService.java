package com.smarttrader.ml;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smarttrader.entity.Instrument;
import com.smarttrader.entity.MlPrediction;
import com.smarttrader.features.FeatureVector;
import com.smarttrader.repository.MlPredictionRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class PredictionService {

    private final ModelRegistry modelRegistry;
    private final MlPredictionRepository mlPredictionRepository;
    private final ObjectMapper objectMapper;

    public PredictionService(ModelRegistry modelRegistry, MlPredictionRepository mlPredictionRepository, ObjectMapper objectMapper) {
        this.modelRegistry = modelRegistry;
        this.mlPredictionRepository = mlPredictionRepository;
        this.objectMapper = objectMapper;
    }

    public PredictionResult predictForSymbol(String symbol, FeatureVector featureVector) {
        TradingModel model = modelRegistry.getDeployedModel();
        return model.predict(featureVector);
    }

    public Map<String, PredictionResult> predictForCandidates(Map<String, FeatureVector> candidates) {
        TradingModel model = modelRegistry.getDeployedModel();
        return candidates.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> model.predict(e.getValue())));
    }

    public double getProbabilityThreshold() {
        return 0.60;
    }

    public boolean isHighConfidence(PredictionResult predictionResult) {
        return predictionResult.isHighConfidence();
    }

    public void persistPrediction(PredictionResult result, Instrument instrument) {
        MlPrediction prediction = new MlPrediction();
        prediction.setInstrument(instrument);
        prediction.setSymbol(result.symbol());
        prediction.setPredictedAt(result.predictedAt());
        prediction.setHorizon(result.horizon());
        prediction.setProbabilityPositiveReturn(BigDecimal.valueOf(result.probabilityPositiveReturn()));
        prediction.setExpectedReturn(BigDecimal.valueOf(result.expectedReturn()));
        
        try {
            prediction.setPayload(objectMapper.writeValueAsString(result));
        } catch (JsonProcessingException e) {
            prediction.setPayload("{}");
        }

        mlPredictionRepository.save(prediction);
    }
}
