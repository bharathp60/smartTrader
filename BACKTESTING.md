# Backtesting

SMART TRADER includes a robust backtesting engine to simulate strategies against historical data.

## BacktestRequest
Parameters for a backtest run:
```json
{
  "startDate": "2020-01-01",
  "endDate": "2023-01-01",
  "initialCapital": 100000.0,
  "symbols": ["AAPL", "MSFT", "SPY"],
  "strategy": "MOMENTUM_AI"
}
```

## Look-Ahead Bias Prevention
The engine steps through time day-by-day. At `T=0`, only data up to `T-1` is visible to the ML features and AI prompt.

## Strategy Description
Strategies map to implementations of the `TradingStrategy` interface. They define the criteria for entry and exit.

## Output Metrics Explained
- **CAGR**: Compound Annual Growth Rate.
- **Sharpe Ratio**: Risk-adjusted return (using 3% risk-free rate).
- **Sortino Ratio**: Focuses only on downside deviation.
- **Max Drawdown**: Largest peak-to-trough drop.
- **Win Rate**: % of profitable trades.

## Commission and Slippage Modeling
- **Commission**: Assumed $0.005 per share.
- **Slippage**: MODELED dynamically based on stock liquidity (e.g., higher slippage for lower volume stocks).

## Result Interpretation
Backtests are stored in the database. A Sharpe > 1.5 is required before moving a strategy to PAPER trading.
