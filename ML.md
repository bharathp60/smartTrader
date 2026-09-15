# ML Engine

The Machine Learning Engine is responsible for the heavy mathematical lifting before data reaches the AI.

## Model Abstraction
All models implement the `TradingModel` interface:
```java
public interface TradingModel {
    double predictReturn(FeatureVector features);
}
```

## SimpleMlModel
The baseline model is a Ridge Regression or LightGBM model predicting 5-day forward returns based on the feature vector.

## Feature Engineering
The `FeatureVector` contains 25 standardized features:
1-5: Momentum (1w, 1m, 3m, 6m, 12m)
6-10: Volatility (ATR, historical vol)
11-15: Valuation (P/E, P/B, EV/EBITDA)
16-20: Quality (ROE, ROA, Margins)
21-25: Technicals (RSI, MACD, Bollinger Band position)

## Model Registry and Deployment
Models are serialized (e.g., ONNX format) and loaded at runtime. The `ml` package handles inference.

## Training and Evaluation
Training happens offline or in a separate batch process. The metrics tracked are IC (Information Coefficient) and Rank IC.

## Look-Ahead Bias Prevention
All features are strictly lagged. Fundamental data is lagged by 45-90 days to simulate real-world SEC filing delays.

## Model Versioning
Models are versioned in the database. The system can shadow-test a V2 model while trading on V1.
