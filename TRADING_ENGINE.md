# Trading Engine

The Trading Engine is the core loop of SMART TRADER.

## Autonomous Trading Loop
1. **Sync Market Data**: Fetches the latest OHLCV data.
2. **Regime Detection**: Assesses overall market state (Bull, Bear, Volatile).
3. **Screen & Rank**: Filters the universe and ranks top N stocks.
4. **AI Ideation**: Asks the AI for trade logic.
5. **Risk Check**: Risk Engine validates the idea.
6. **Execution**: Sends order to broker.

## Market Regime Detection
Uses SPY/NIFTY50 moving averages (50 vs 200 SMA) and VIX to determine the regime. Trading aggressiveness is scaled down in Bear/Volatile regimes.

## Stock Screening Pipeline
Filters out penny stocks, illiquid stocks (< $10M daily volume), and heavily shorted names.

## Multi-Factor Ranking
Stocks are ranked based on a composite score:
- **Value (20%)**: P/E, P/B.
- **Quality (30%)**: ROE, Debt/Equity.
- **Momentum (40%)**: 6-month price return, RSI.
- **Volatility (10%)**: ATR, Beta.

## Position Sizing Formula
Uses Kelly Criterion scaled down by half (Half-Kelly) or Volatility Targeting (e.g., risk 1% of total equity per trade based on ATR).

## Trade Management
- **Stop-Loss**: Trailing stop based on 2x ATR.
- **Target**: Initial target based on resistance levels or 3x ATR.
- **Exit Logic**: Time-based exit (e.g., end of week) or technical breakdown.

## Trading Session Schedule
Configured for IST:
- 09:00 - Pre-market data sync
- 09:15 - Market Open, begin execution
- 15:15 - Intraday square-off
- 15:30 - Market Close, daily learning run

## Kill Switch Behavior
If activated:
1. Rejects all incoming signals.
2. Cancels all open limit/stop orders.
3. Sends MARKET orders to close all open positions.
4. Requires manual database flag reset to resume.
