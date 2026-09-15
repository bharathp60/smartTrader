# API Reference

All API requests must include the JWT token in the `Authorization: Bearer <token>` header, except `/api/auth/login`.

## Authentication

### POST `/api/auth/login`
Authenticates a user and returns a JWT.
**Request:**
```json
{
  "username": "admin",
  "password": "password"
}
```
**Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1...",
  "role": "ADMIN"
}
```

## Portfolio Management

### GET `/api/portfolio`
Returns current portfolio state.
**Role:** TRADER, VIEWER, ADMIN
**Response:**
```json
{
  "totalEquity": 150000.0,
  "availableCash": 25000.0,
  "positions": [
    {
      "symbol": "AAPL",
      "quantity": 50,
      "currentPrice": 150.0,
      "unrealizedPnl": 500.0
    }
  ]
}
```

## System Controls

### POST `/api/system/kill-switch`
Immediately halts all new orders and attempts to liquidate open positions.
**Role:** ADMIN
**Response:**
```json
{
  "status": "HALTED",
  "message": "Kill switch engaged. System is liquidating."
}
```

## WebSockets
Connect to `/ws` with JWT in the connection payload.
- **Topic:** `/topic/portfolio` - Real-time equity updates.
- **Topic:** `/topic/orders` - Order state changes (FILLED, REJECTED).
- **Event Schema:**
```json
{
  "type": "ORDER_UPDATE",
  "timestamp": "2026-09-05T09:30:30Z",
  "data": {
    "orderId": "uuid",
    "status": "FILLED"
  }
}
```

## Error Handling
Standard RFC 7807 Problem Details response:
```json
{
  "type": "about:blank",
  "title": "RiskEngineRejection",
  "status": 400,
  "detail": "Order exceeds max position size."
}
```

## Rate Limiting
APIs are rate-limited per user to 100 requests / minute. HTTP 429 is returned if exceeded.


## Prompt
'''
SMART TRADER — AUTONOMOUS AI/ML ALGORITHMIC TRADING PLATFORM
You are the lead software architect, senior Java 17 developer, Spring Boot developer, Spring AI engineer, quantitative developer, ML engineer, financial-data engineer, DevOps engineer, database architect and automated trading-system engineer.
Your task is to BUILD the complete application described below inside this Codex workspace.
Do not merely provide examples or pseudocode.
Inspect the existing workspace first and then create/modify the actual files.
The application name is:
SMART TRADER
The goal is to build a highly intelligent autonomous stock-selection and algorithmic trading platform.
============================================================ 0. CORE OBJECTIVE
Build an autonomous system that can:
- Collect market data
- Maintain a stock universe
- Screen thousands of stocks
- Analyze technical indicators
- Analyze fundamentals when reliable data is available
- Analyze sectors
- Detect market regime
- Generate quantitative features
- Use machine learning
- Use Spring AI + OpenAI for contextual reasoning
- Rank stocks by opportunity quality
- Select the best trading opportunities
- Determine entry/stop-loss/target
- Determine position size
- Optimize portfolio exposure
- Execute trades
- Monitor positions
- Exit automatically
- Record every decision
- Evaluate every trade
- Learn from mistakes
- Learn from successful decisions
- Adapt to changing market conditions
- Retrain/evaluate models
- Backtest strategies
- Support paper trading
- Support live trading through a broker adapter
- Operate autonomously during configured trading hours
  The system must be designed so that normal daily trading does NOT require human intervention.
  However, it must have strict deterministic safety controls.
  AI must NEVER bypass the Risk Engine.
  ============================================================
1. MANDATORY TECHNOLOGY
   ============================================================
Use:
Java 17
Spring Boot 3.x compatible with Java 17
Maven
Spring AI
OpenAI API through Spring AI
PostgreSQL
Redis
Spring Data JPA
Flyway
Spring Security
JWT
REST APIs
WebSocket
Docker
Docker Compose
JUnit 5
Mockito
Testcontainers where appropriate
Spring Boot Actuator
Java 17 is mandatory.
DO NOT use Java 21-specific language features or APIs.
Keep dependencies compatible with Java 17.
Use the latest stable compatible Spring Boot/Spring AI versions available in the development environment.
Before choosing versions, inspect the current Maven/project environment.
============================================================ 2. IMPORTANT ARCHITECTURAL PRINCIPLE
NEVER build this:
AI → BUY → Broker
Build this:
Market Data
↓
Quantitative Analysis
↓
Technical Analysis
↓
Fundamental Analysis
↓
Feature Engineering
↓
ML Prediction
↓
Market Regime
↓
Stock Ranking
↓
Spring AI Analysis
↓
Portfolio Optimization
↓
Risk Engine
↓
Order Validation
↓
Broker
↓
Trade Result
↓
Learning Engine
↓
Validated Adaptation
↓
Next Trading Session
AI is not the final authority.
The Risk Engine is the final authority for trade authorization.
============================================================ 3. DEVELOPMENT RULE
FIRST inspect the existing workspace.
Do not overwrite existing working code unnecessarily.
Determine:
- existing project structure
- Java version
- Spring Boot version
- Maven configuration
- existing dependencies
- existing source code
- existing tests
- existing configuration
- existing Docker configuration
  Then build incrementally.
  Do NOT attempt to generate everything blindly in one huge operation.
  Work in phases.
  After each phase:
1. Compile.
2. Run tests.
3. Fix compilation errors.
4. Fix failing tests.
5. Inspect the changed files.
6. Ensure architecture remains clean.
7. Provide a concise summary.
8. Continue to the next phase unless a blocking issue requires clarification.
   The final objective is a fully working application, not merely scaffolding.
   ============================================================ 4. TARGET ARCHITECTURE
   Use a modular package architecture:
   com.smarttrader
   config
   security
   controller
   service
   repository
   entity
   dto
   mapper
   exception
   scheduler
   websocket
   audit
   notification
   marketdata
   indicators
   features
   fundamental
   universe
   screener
   ranking
   regime
   ml
   ai
   strategy
   portfolio
   risk
   order
   broker
   backtest
   learning
   performance
   system
   Keep business logic out of controllers.
   Use constructor injection.
   Use interfaces for replaceable infrastructure.
   Use DTOs between API and domain layers.
   Use transactions where required.
   Avoid God classes.
   ============================================================ 5. APPLICATION MODES
   The application must support:
   BACKTEST
   PAPER
   LIVE
   Default mode:
   PAPER
   Live trading must be disabled by default.
   Configuration:
   TRADING_MODE=PAPER
   LIVE_TRADING_ENABLED=false
   Never activate live trading automatically during development.
   ============================================================ 6. MARKET DATA ENGINE
   Create a provider abstraction.
   Example:
   MarketDataProvider
   Methods for:
   historical candles
   latest quote
   ticks
   market status
   subscriptions
   Support:
   1m
   5m
   15m
   30m
   1h
   1d
   Handle:
   missing data
   duplicate data
   out-of-order data
   network errors
   timeouts
   market holidays
   market closed periods
   stale prices
   Create:
   MarketDataService
   MarketDataScheduler
   MarketDataCacheService
   Use Redis for suitable real-time/cache data.
   PostgreSQL remains the source of persistent trading history.
   ============================================================ 7. STOCK UNIVERSE
   Create:
   StockUniverseService
   Maintain eligible instruments.
   Store:
   symbol
   exchange
   instrument token
   company name
   sector
   industry
   market cap where available
   lot size
   tick size
   liquidity information
   status
   Support filtering:
   active instruments
   liquid instruments
   suspended instruments
   invalid instruments
   The system must be able to analyze a large universe efficiently.
   Do not send thousands of stocks to the LLM.
   Use deterministic/ML screening first.
   ============================================================ 8. STOCK SCREENING ENGINE
   Create:
   StockScreeningService
   The system should progressively reduce the universe.
   Example:
   2000 stocks
   ↓
   eligible stocks
   ↓
   liquid stocks
   ↓
   technical candidates
   ↓
   ML candidates
   ↓
   high-quality candidates
   ↓
   AI analysis
   ↓
   portfolio candidates
   Screen using:
   liquidity
   volume
   volatility
   trend
   momentum
   breakout
   relative strength
   sector strength
   technical quality
   ML probability
   risk/reward
   Make thresholds configurable.
   ============================================================ 9. TECHNICAL ANALYSIS
   Implement deterministic calculations for:
   SMA
   EMA
   RSI
   MACD
   Bollinger Bands
   ATR
   ADX
   VWAP
   OBV
   Stochastic
   ROC
   Momentum
   Volume moving average
   Relative volume
   Volatility
   Support/resistance
   Breakouts
   Trend strength
   Gap detection
   Candlestick patterns
   Do NOT use the LLM to calculate mathematical indicators.
   Create:
   TechnicalIndicatorService
   All indicator calculations must have unit tests.
   ============================================================ 10. FUNDAMENTAL ANALYSIS
   Create a FundamentalDataProvider abstraction.
   Where reliable data is available, support:
   revenue growth
   profit growth
   EPS
   P/E
   P/B
   ROE
   ROCE
   debt/equity
   operating margin
   profit margin
   cash flow
   earnings growth
   valuation metrics
   Do not fabricate missing fundamental data.
   If data is unavailable:
   mark it unavailable.
   Do not ask the LLM to invent fundamentals.
   ============================================================ 11. SECTOR ANALYSIS
   Create:
   SectorAnalysisService
   Analyze:
   sector momentum
   sector relative strength
   sector trend
   sector volatility
   sector breadth where available
   sector performance versus benchmark
   Use sector strength as one factor in stock ranking.
   ============================================================ 12. MARKET REGIME ENGINE
   Create:
   MarketRegimeService
   Detect:
   STRONG_BULL
   BULL
   SIDEWAYS
   VOLATILE
   BEAR
   STRONG_BEAR
   Use:
   index trend
   breadth
   volatility
   momentum
   volume
   sector rotation
   market structure
   Strategies must adapt to market regime.
   ============================================================ 13. FEATURE ENGINEERING
   Create:
   FeatureEngineeringService
   Generate features including:
   returns
   log returns
   momentum
   RSI
   MACD
   ATR
   ADX
   VWAP distance
   EMA relationships
   SMA relationships
   Bollinger position
   volume ratio
   volatility
   trend strength
   breakout distance
   support distance
   resistance distance
   sector strength
   market regime
   relative strength
   liquidity
   Feature definitions must be versioned.
   ============================================================ 14. MACHINE LEARNING
   Create an ML abstraction.
   TradingModel
   PredictionService
   ModelTrainer
   ModelEvaluator
   ModelRegistry
   ModelDeploymentService
   Possible predictions:
   probability of positive return
   expected return
   probability target is reached
   probability stop-loss is hit
   expected volatility
   trend probability
   Store:
   model version
   training date
   features version
   training dataset
   metrics
   deployment status
   Never blindly deploy a newly trained model.
   Use:
   training
   validation
   out-of-sample evaluation
   Prevent:
   look-ahead bias
   data leakage
   overfitting
   ============================================================ 15. STOCK OPPORTUNITY SCORE
   Create a multi-factor ranking system.
   Example:
   Opportunity Score =
   Technical Score
- Momentum Score
- Volume Score
- Sector Score
- Market Regime Score
- Fundamental Score
- ML Score
- Risk/Reward Score
- Liquidity Score
- AI Context Score
  Weights must be configurable.
  Create:
  Factor
  FactorScore
  FactorWeight
  ScoringProfile
  Support profiles:
  INTRADAY
  SWING
  POSITIONAL
  Do not hardcode all weights permanently.
  ============================================================ 16. SPRING AI
  Spring AI is mandatory.
  Use Spring AI as the AI orchestration layer.
  Create:
  AIAnalysisService
  AIStockSelectionService
  AITradingDecisionService
  AIPortfolioAnalysisService
  AITradeReviewService
  AIResearchService
  Use Spring AI's structured output/function/tool calling capabilities where supported.
  Centralize model configuration.
  Do not scatter direct OpenAI HTTP requests throughout the application.
  ============================================================ 17. SPRING AI TOOLS
  Expose safe application tools to the AI.
  Examples:
  getLatestMarketData
  getHistoricalCandles
  getTechnicalIndicators
  getFundamentalMetrics
  getSectorPerformance
  getMarketRegime
  getStockLiquidity
  getCurrentPositions
  getPortfolioExposure
  getRiskStatus
  getMLPrediction
  getStrategyPerformance
  getPreviousTrades
  getRecentTradeMistakes
  The AI must NOT directly query PostgreSQL.
  The AI must NOT directly call the broker.
  ============================================================ 18. AI STOCK SELECTION
  The AI should receive only the top candidates after deterministic screening.
  Example:
  Candidate A
  Candidate B
  Candidate C
  Candidate D
  Candidate E
  Analyze:
  trend
  technical confirmation
  fundamentals
  sector
  market regime
  ML probability
  risk/reward
  liquidity
  recent performance
  potential catalysts
  warnings
  Return structured output.
  Possible result:
  TOP_OPPORTUNITY
  SECONDARY_OPPORTUNITY
  WATCHLIST
  NO_TRADE
  The system MUST allow NO_TRADE.
  ============================================================ 19. AI DECISION SCHEMA
  Use a strict Java DTO/schema.
  Example fields:
  symbol
  action
  confidence
  marketView
  trendAssessment
  riskAssessment
  entryZone
  stopLoss
  target
  expectedRiskReward
  holdingPeriod
  warnings
  reasoningSummary
  AI output must be validated before entering the trading pipeline.
  Never use arbitrary natural-language parsing to place orders.
  ============================================================ 20. AI CONFIDENCE CALIBRATION
  Do not blindly trust AI confidence.
  Track:
  AI confidence
  actual outcome
  Calculate performance by confidence range.
  Example:
  0.60–0.70
  0.70–0.80
  0.80–0.90
  0.90+
  Use historical calibration to determine whether confidence is meaningful.
  ============================================================ 21. PORTFOLIO OPTIMIZATION
  Create:
  PortfolioOptimizer
  Consider:
  current positions
  sector exposure
  correlation
  capital
  risk
  drawdown
  existing exposure
  number of positions
  market regime
  If 10 stocks generate BUY signals from the same sector, do not blindly buy all 10.
  Select the best risk-adjusted combination.
  ============================================================ 22. RISK ENGINE
  Create:
  RiskEngine
  Every order MUST pass through it.
  Check:
  maximum daily loss
  maximum trade loss
  maximum portfolio drawdown
  maximum position size
  maximum exposure
  maximum sector exposure
  maximum number of positions
  maximum consecutive losses
  minimum liquidity
  maximum volatility
  minimum risk/reward
  maximum slippage
  duplicate order
  duplicate position
  market status
  broker status
  stale market data
  Return:
  APPROVED
  REJECTED
  with:
  reason
  riskScore
  limitsChecked
  AI cannot override RiskEngine.
  ============================================================ 23. POSITION SIZING
  Implement deterministic risk-based sizing.
  Concept:
  Risk Amount =
  Capital × Risk Percentage
  Position Size =
  Risk Amount / Stop Loss Distance
  Apply:
  maximum quantity
  maximum order value
  maximum exposure
  lot size
  tick size
  All calculations must be unit tested.
  ============================================================ 24. ORDER ENGINE
  Create:
  OrderService
  Lifecycle:
  SIGNAL_GENERATED
  RISK_CHECK
  APPROVED
  SUBMITTED
  OPEN
  PARTIALLY_FILLED
  FILLED
  CANCELLED
  REJECTED
  EXITED
  Implement idempotency.
  Retries must never create duplicate orders.
  Use unique client order IDs.
  ============================================================ 25. BROKER ABSTRACTION
  Create:
  BrokerClient
  Methods:
  getAccountInfo
  getPositions
  getOrders
  placeOrder
  cancelOrder
  getOrderStatus
  Implement:
  PaperBrokerClient
  Initially.
  Create a clean extension point for:
  ZerodhaBrokerClient
  Do not couple trading strategy code to Zerodha.
  ============================================================ 26. PAPER TRADING
  Paper trading must simulate:
  orders
  fills
  slippage
  brokerage
  positions
  P&L
  stop-loss
  target
  trailing stop where configured
  The same trading logic must be used for PAPER and LIVE.
  Only the broker implementation changes.
  ============================================================ 27. AUTONOMOUS TRADING LOOP
  Implement:
  Market Open
  ↓
  Check System Health
  ↓
  Check Market Status
  ↓
  Load Stock Universe
  ↓
  Market Regime
  ↓
  Screen Stocks
  ↓
  Technical Analysis
  ↓
  Feature Engineering
  ↓
  ML Predictions
  ↓
  Rank Candidates
  ↓
  Spring AI Analysis
  ↓
  Portfolio Optimization
  ↓
  Risk Engine
  ↓
  Order Validation
  ↓
  Broker
  ↓
  Position Monitoring
  ↓
  Exit
  ↓
  Trade Result
  ↓
  Learning
  Do not call the LLM for every tick.
  Use:
  tick level:
  deterministic processing
  candle level:
  technical + ML
  candidate level:
  AI
  portfolio level:
  AI when necessary
  end-of-day:
  AI review
  ============================================================ 28. TRADE MANAGEMENT
  Monitor open positions automatically.
  Evaluate:
  stop-loss
  target
  trailing stop
  trend deterioration
  opposite signal
  market regime change
  risk exposure
  volatility
  Create:
  PositionMonitoringService
  No manual intervention should be required for normal exits.
  ============================================================ 29. LEARNING FROM MISTAKES
  This is a core requirement.
  Create:
  DailyLearningService
  MistakeDetectionService
  TradeReviewService
  AdaptationEngine
  LearningEvaluationService
  At market close:
  collect trades
  ↓
  classify trades
  ↓
  detect mistakes
  ↓
  analyze performance
  ↓
  analyze ML
  ↓
  analyze AI
  ↓
  analyze stock selection
  ↓
  analyze strategies
  ↓
  analyze market regime
  ↓
  identify recurring patterns
  ↓
  generate adaptation proposals
  ↓
  validate
  ↓
  backtest
  ↓
  out-of-sample test
  ↓
  approve/reject
  ↓
  create new configuration version
  ============================================================ 30. TRADE CLASSIFICATION
  Classify completed trades as:
  GOOD_WIN
  GOOD_LOSS
  BAD_WIN
  BAD_LOSS
  A losing trade is not automatically a mistake.
  A winning trade is not automatically a good decision.
  This distinction must be implemented.
  ============================================================ 31. MISTAKE TAXONOMY
  Support:
  LATE_ENTRY
  EARLY_ENTRY
  FALSE_BREAKOUT
  TREND_MISREAD
  REVERSAL_MISSED
  POOR_RISK_REWARD
  STOP_TOO_TIGHT
  STOP_TOO_WIDE
  TARGET_TOO_CLOSE
  TARGET_TOO_FAR
  OVERTRADING
  LOW_LIQUIDITY
  HIGH_SLIPPAGE
  WRONG_MARKET_REGIME
  SECTOR_WEAKNESS
  CONFLICTING_INDICATORS
  ML_FALSE_POSITIVE
  ML_FALSE_NEGATIVE
  AI_OVERCONFIDENCE
  AI_UNDERCONFIDENCE
  BAD_POSITION_SIZE
  EXCESSIVE_EXPOSURE
  DUPLICATE_SIGNAL
  BAD_EXIT
  MISSED_EXIT
  UNEXPECTED_VOLATILITY
  Track:
  frequency
  severity
  financial impact
  market regime
  strategy
  symbol/sector
  time period
  ============================================================ 32. DAILY AI TRADE REVIEW
  Use Spring AI to review the day's trades.
  Provide:
  market summary
  successful decisions
  failed decisions
  mistakes
  recurring patterns
  strategy observations
  ML observations
  AI observations
  risk observations
  possible adaptations
  Return structured data.
  Do not allow the AI to directly change production configuration.
  ============================================================ 33. ADAPTATION ENGINE
  Create:
  AdaptationEngine
  AI proposal:
  AI Recommendation
  ↓
  Statistical Validation
  ↓
  Backtest
  ↓
  Out-of-Sample Test
  ↓
  Risk Validation
  ↓
  Adaptation Score
  ↓
  Approve / Reject
  Only validated adaptations can be deployed.
  ============================================================ 34. ADAPTIVE PARAMETERS
  Allow controlled adaptation of:
  technical thresholds
  minimum volume ratio
  minimum ML confidence
  minimum AI confidence
  strategy weights
  factor weights
  ranking weights
  market-regime strategy weights
  target parameters
  trailing-stop parameters
  candidate thresholds
  Every parameter must have:
  default
  current
  minimum
  maximum
  version
  reason
  updatedAt
  ============================================================ 35. PARAMETERS THAT MUST NEVER AUTO-ADAPT
  Never automatically change:
  maximum daily loss
  maximum account exposure
  emergency kill switch
  security settings
  broker credentials
  duplicate-order protection
  core safety validation
  Safety limits remain deterministic.
  ============================================================ 36. RECENCY + LONG-TERM LEARNING
  Use both:
  long-term performance
  recent performance
  Example:
  long-term = 70%
  recent = 30%
  Make weights configurable.
  Never allow one bad trading day to completely change the system.
  Require minimum sample sizes.
  ============================================================ 37. ADAPTATION VERSIONING
  Every adaptation must create a version.
  Store:
  old value
  new value
  reason
  evidence
  sample size
  backtest performance
  out-of-sample performance
  risk impact
  expected improvement
  actual improvement
  date
  strategy version
  model version
  Support rollback.
  ============================================================ 38. AUTOMATIC ROLLBACK
  If an adaptation performs significantly worse than the previous validated configuration:
  automatically rollback.
  Use configurable degradation thresholds.
  ============================================================ 39. BACKTESTING
  Create a full backtesting engine.
  Input:
  instrument
  date range
  timeframe
  strategy
  capital
  fees
  slippage
  risk configuration
  Output:
  total return
  CAGR
  win rate
  profit factor
  maximum drawdown
  Sharpe
  Sortino
  trade count
  average win
  average loss
  largest win
  largest loss
  expectancy
  equity curve
  Prevent:
  look-ahead bias
  future-data leakage
  incorrect order sequencing
  ============================================================ 40. PERFORMANCE ENGINE
  Track:
  daily P&L
  weekly P&L
  monthly P&L
  strategy performance
  stock selection performance
  sector performance
  ML performance
  AI performance
  entry performance
  exit performance
  risk performance
  Calculate:
  win rate
  profit factor
  expectancy
  drawdown
  Sharpe
  Sortino
  ============================================================ 41. DATABASE
  Use PostgreSQL + Flyway.
  Create tables for at least:
  users
  roles
  api_credentials
  instruments
  market_data
  candles
  ticks
  technical_indicators
  fundamental_data
  features
  ml_predictions
  ai_decisions
  strategies
  strategy_versions
  factor_scores
  stock_rankings
  orders
  order_events
  positions
  trades
  trade_results
  risk_events
  model_versions
  model_training_runs
  backtest_runs
  backtest_trades
  daily_performance
  trade_mistakes
  daily_learning_reports
  adaptation_proposals
  configuration_versions
  system_events
  audit_logs
  Add proper indexes.
  Use UUIDs where appropriate.
  Do not store secrets in plain text.
  ============================================================ 42. REDIS
  Use Redis for:
  latest prices
  latest market state
  latest indicators
  temporary signals
  distributed locks
  idempotency
  short-lived state
  Prevent multiple application instances from processing the same trading event simultaneously.
  ============================================================ 43. REST APIs
  Implement:
  GET /api/health
  GET /api/stocks
  GET /api/screener
  GET /api/rankings
  GET /api/market-data/{symbol}
  GET /api/indicators/{symbol}
  GET /api/signals
  GET /api/ai/decisions
  GET /api/ml/predictions
  GET /api/orders
  GET /api/positions
  GET /api/trades
  GET /api/portfolio
  GET /api/risk
  GET /api/performance
  GET /api/learning/daily
  GET /api/learning/mistakes
  GET /api/learning/adaptations
  GET /api/models
  POST /api/backtest
  POST /api/paper/start
  POST /api/paper/stop
  POST /api/live/start
  POST /api/live/stop
  POST /api/system/kill-switch
  GET /api/system/status
  Secure sensitive APIs.
  ============================================================ 44. WEBSOCKET
  Provide real-time events:
  market prices
  signals
  AI decisions
  ML predictions
  orders
  fills
  positions
  P&L
  risk alerts
  system status
  ============================================================ 45. SECURITY
  Implement Spring Security + JWT.
  Roles:
  ADMIN
  TRADER
  VIEWER
  Never expose:
  OpenAI API key
  broker API secret
  database password
  Use environment variables.
  Example:
  OPENAI_API_KEY
  BROKER_API_KEY
  BROKER_API_SECRET
  DB_USERNAME
  DB_PASSWORD
  ============================================================ 46. ERROR HANDLING
  Handle:
  broker timeout
  market data timeout
  database failure
  Redis failure
  OpenAI failure
  invalid AI response
  stale market data
  insufficient funds
  invalid instrument
  duplicate order
  network failure
  If AI is unavailable:
  DO NOT place an AI-dependent trade.
  Only allow a deterministic fallback strategy if it is explicitly configured and validated.
  ============================================================ 47. KILL SWITCH
  Implement:
  POST /api/system/kill-switch
  When activated:
  stop new entries
  cancel eligible pending orders
  continue monitoring existing positions
  record emergency event
  Also support safe mode.
  ============================================================ 48. OBSERVABILITY
  Use Spring Boot Actuator.
  Health checks:
  database
  Redis
  broker
  market data
  AI service
  Use structured logging.
  Never log secrets.
  ============================================================ 49. DOCUMENTATION
  Create:
  README.md
  ARCHITECTURE.md
  API.md
  TRADING_ENGINE.md
  SPRING_AI.md
  ML.md
  LEARNING_ENGINE.md
  RISK_ENGINE.md
  BACKTESTING.md
  BROKER_INTEGRATION.md
  DEPLOYMENT.md
  SECURITY.md
  ============================================================ 50. TESTING
  Create extensive automated tests.
  Unit tests:
  indicator calculations
  feature engineering
  stock screening
  ranking
  position sizing
  risk engine
  AI DTO validation
  trade classification
  mistake detection
  adaptation validation
  backtesting
  Integration tests:
  PostgreSQL
  Redis
  REST APIs
  Paper Broker
  Order lifecycle
  Learning pipeline
  Test critical failure cases.
  ============================================================ 51. DOCKER
  Create:
  Dockerfile
  docker-compose.yml
  Services:
  smart-trader
  postgres
  redis
  Use Java 17.
  Use environment variables.
  ============================================================ 52. DAILY AUTONOMOUS LEARNING
  At the end of every trading day:
1. Close/settle trade records.
2. Calculate performance.
3. Analyze mistakes.
4. Analyze successful decisions.
5. Analyze stock selection.
6. Analyze sectors.
7. Analyze market regime.
8. Evaluate ML predictions.
9. Evaluate AI decisions.
10. Identify recurring patterns.
11. Generate adaptation candidates.
12. Validate candidates statistically.
13. Backtest.
14. Run out-of-sample validation.
15. Reject unsafe/worse changes.
16. Approve validated improvements.
17. Version configuration.
18. Prepare next trading session.
    The next trading session must use the latest validated configuration.
    ============================================================ 53. INTELLIGENCE PRINCIPLE
    The objective is NOT:
    "Trade as much as possible."
    The objective is:
    "Select the best risk-adjusted opportunities while avoiding unnecessary trades and preserving capital."
    The system must be capable of saying:
    NO_TRADE
    even when the market is open.
    ============================================================ 54. NO HALLUCINATED DATA
    This is mandatory.
    Never invent:
    prices
    fundamentals
    news
    financial results
    market data
    broker responses
    technical indicators
    If data is unavailable:
    mark it unavailable.
    AI must reason only from supplied/verified data.
    ============================================================ 55. NO UNCONTROLLED SELF-MODIFICATION
    The system can learn and adapt, but only through:
    data
    → analysis
    → hypothesis
    → validation
    → backtest
    → out-of-sample test
    → risk validation
    → versioning
    → deployment
    Never allow an LLM response to directly modify Java source code, database schema, broker credentials or safety controls.
    ============================================================ 56. PHASED IMPLEMENTATION
    Implement in these phases:
    PHASE 1
    Project foundation
    PHASE 2
    Database + Flyway + entities
    PHASE 3
    Market data
    PHASE 4
    Technical indicators
    PHASE 5
    Fundamental data abstraction
    PHASE 6
    Stock universe
    PHASE 7
    Stock screener
    PHASE 8
    Multi-factor ranking
    PHASE 9
    Market regime
    PHASE 10
    Feature engineering
    PHASE 11
    ML engine
    PHASE 12
    Spring AI integration
    PHASE 13
    AI stock selection
    PHASE 14
    Strategy engine
    PHASE 15
    Portfolio optimizer
    PHASE 16
    Risk engine
    PHASE 17
    Paper broker
    PHASE 18
    Order management
    PHASE 19
    Position management
    PHASE 20
    Backtesting
    PHASE 21
    Learning engine
    PHASE 22
    Daily adaptation
    PHASE 23
    Performance engine
    PHASE 24
    REST APIs
    PHASE 25
    WebSocket
    PHASE 26
    Security
    PHASE 27
    Docker
    PHASE 28
    Live broker adapter
    PHASE 29
    End-to-end integration
    PHASE 30
    Production hardening
    ============================================================ 57. IMPORTANT — START NOW
    Start with PHASE 1.
    First inspect the existing workspace.
    Then create/configure:
    pom.xml
    Spring Boot main application
    package structure
    application.yml
    application-dev.yml
    application-test.yml
    application-paper.yml
    application-live.yml
    .env.example
    .gitignore
    Dockerfile
    docker-compose.yml
    README.md
    Use Java 17.
    Configure the foundation for:
    Spring Boot
    PostgreSQL
    Redis
    Flyway
    Spring Security
    Spring AI
    Do not implement trading logic in Phase 1.
    After implementation:
1. Run java -version.
2. Run mvn clean test.
3. Fix all compilation errors.
4. Fix all test failures.
5. Verify Docker configuration.
6. Show the final project tree.
7. Explain what was created.
8. STOP after Phase 1.
   Do not ask me to manually create files if you can create them in the workspace.
   Do not just give me code snippets.
   Actually modify the workspace.
   Proceed carefully and maintain a production-quality architecture throughout the project.
'''