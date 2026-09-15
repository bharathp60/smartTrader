package com.smarttrader.ml;

import com.smarttrader.features.FeatureVector;
import java.util.List;

public interface TradingModel {
    String getModelName();
    String getModelVersion();
    String getFeatureVersion();
    PredictionResult predict(FeatureVector features);
    List<PredictionResult> predictBatch(List<FeatureVector> features);
    ModelMetrics evaluate(List<FeatureVector> features, List<Double> actualReturns);
    boolean isDeployed();
}
