# Architecture

SMART TRADER is structured into 30 modules (packages) enforcing clean boundaries and strict responsibility assignment.

## Trading Pipeline

```text
[Market Data] -> (Indicators/Features) -> [ML Engine] -> (Signals) -> [Spring AI]
                                                                          |
                                                                    (Trade Proposal)
                                                                          |
                                                                          V
[Broker Interface] <- (Execution) <- [Order Management] <- (Validated) <- [Risk Engine]
```

## Module Descriptions
1. `config`: Spring Boot configuration, beans, properties.
2. `security`: JWT validation, role-based access control.
3. `controller`: REST APIs. No business logic here.
4. `service`: Core business services.
5. `repository`: Spring Data JPA interfaces.
6. `entity`: Domain models and DB tables.
7. `dto`: Data Transfer Objects.
8. `mapper`: MapStruct or manual mappers.
9. `exception`: Custom exceptions and global handlers.
10. `scheduler`: Cron jobs for market open/close, sync.
11. `websocket`: Real-time updates for the UI.
12. `audit`: Audit logging for regulatory compliance.
13. `notification`: Alerts (Slack, Email, SMS).
14. `marketdata`: Integrations with market data providers.
15. `indicators`: Technical indicators (RSI, MACD, etc.).
16. `features`: Feature engineering pipeline.
17. `fundamental`: Fundamental data processing.
18. `universe`: Stock screening and filtering.
19. `screener`: Real-time scanning.
20. `ranking`: Multi-factor ranking logic.
21. `regime`: Market regime detection (Bull/Bear/Volatile).
22. `ml`: Machine learning abstractions and implementations.
23. `ai`: Spring AI integration and prompt management.
24. `strategy`: High-level trading strategies.
25. `portfolio`: Portfolio tracking and allocation.
26. `risk`: The Risk Engine (The final authority).
27. `order`: Order state machine and routing.
28. `broker`: External broker integrations.
29. `backtest`: Simulation engine.
30. `learning`: Post-trade analysis and adaptation.
31. `performance`: Metrics calculation.
32. `system`: System-level controls (Kill Switch).

## Data Flow
Market data flows into the system, is enriched by the feature pipeline, and passed to the ML engine. Top candidates are sent to the AI via `AiDecisionDto` prompts. The AI generates a trade proposal. **The Risk Engine validates the proposal**. If approved, it moves to the broker.

## Why AI Cannot Bypass the Risk Engine
AI is probabilistic and prone to hallucinations. SMART TRADER treats AI output strictly as a *suggestion*. The Risk Engine is a deterministic, rule-based system coded in Java. It intercepts every order before it reaches the `BrokerClient`. If the AI suggests 100% portfolio allocation to a meme stock, the Risk Engine rejects it instantly.

## Redis Caching Strategy
Redis is used for:
- Intraday market data (fast retrieval, TTL = 1 day).
- Distributed locks for idempotency.
- Rate limiting for API clients.

## PostgreSQL Schema Overview
Entities extend a `BaseEntity` with `id` (UUID), `createdAt`, and `updatedAt`.
- `users`, `roles`
- `orders`, `trades`, `positions`
- `portfolio_snapshots`
- `learning_events`

## Security Architecture
Stateless JWT. Roles: `ADMIN` (can trigger kill switch), `TRADER` (can manually trade), `VIEWER` (read-only).

## Failure Modes and Fallbacks
If Spring AI is down, `deterministicFallbackEnabled` (if true) kicks in, using a simple ML model without AI ideation. If the DB is down, the system enters a HALT state and liquidates/stops new trades if possible.

## Distributed Lock Strategy
Using ShedLock or Redis lock to ensure cron jobs (like `MarketCloseJob`) only run on one node in a multi-instance deployment.
