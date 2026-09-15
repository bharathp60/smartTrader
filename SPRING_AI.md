# Spring AI Integration

SMART TRADER leverages Spring AI 2.0.1 for high-level trading analysis and ideation. 

## How Spring AI is Used
We use the `ChatClient` with structured output parsing. The AI is prompted with the current market regime, top-ranked stocks from the ML engine, and recent news. It is expected to return an `AiDecisionDto`.

## Tools Exposed
The AI is provided with a `MarketDataTool` function callback. If the AI needs to check a specific technical level (e.g., "What is the 200 SMA for AAPL?"), it can call this tool during its generation phase.

## AI Decision Schema (`AiDecisionDto`)
```java
public record AiDecisionDto(
    String symbol,
    Action action, // BUY, SELL, HOLD
    double confidenceScore, // 0.0 to 1.0
    double suggestedStopLoss,
    String reasoning
) {}
```

## Why AI is Not the Final Authority
AI models can hallucinate, ignore math, or suffer from recency bias. Therefore, the `AiDecisionDto` is purely a *request*. It is routed to the Risk Engine, which performs hard mathematical checks. If the AI suggests a stop loss that implies a 10% portfolio risk, the Risk Engine overrides or rejects it.

## AI Confidence Calibration
The `confidenceScore` is tracked historically. If the AI historically has 30% accuracy when its confidence is > 0.9, the Learning Engine will discount future high-confidence scores. 

## Failure Handling
If the LLM provider (e.g., OpenAI) is down or times out, the `deterministicFallbackEnabled` flag dictates behavior. If true, the system bypasses AI and trades purely on the ML multi-factor ranking.

## Prompt Design Principles
Prompts are strict and provide explicit context. We use few-shot prompting to show the AI examples of good and bad trades to guide its reasoning.
