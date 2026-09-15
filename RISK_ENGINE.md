# Risk Engine

The Risk Engine is the most critical component of SMART TRADER. **It is the final authority for all orders.** AI proposes, Risk Engine disposes.

## Risk Checks
Every order passes through 12 checks:
1. `MaxPositionSize`: Position cannot exceed X% of portfolio.
2. `MaxSectorExposure`: Sector cannot exceed Y% of portfolio.
3. `MaxDrawdownHalt`: If portfolio drops Z% in a day, halt trading.
4. `MinLiquidity`: Stock must have minimum daily volume.
5. `NoPennyStocks`: Price must be > $5.
6. `MaxLeverage`: Total gross exposure cannot exceed 1.0 (no margin).
7. `ValidStopLoss`: Every order MUST have a hard stop loss.
8. `RiskPerTrade`: Distance to stop loss * shares cannot exceed 1% of total equity.
9. `CorrelatedExposure`: Checks if we are overexposed to highly correlated assets.
10. `EarningsBlackout`: No new entries 3 days before earnings.
11. `MaxOpenOrders`: Limit on pending orders.
12. `WashSalePrevention`: Prevent re-entering a loss position within 30 days.

## RiskParameters
Defaults:
- Max Position Size: 10%
- Risk Per Trade: 1%
- Max Sector Exposure: 25%

## Position Sizing Formula
`Shares = (TotalEquity * RiskPerTrade) / (EntryPrice - StopLossPrice)`

## Why AI Cannot Bypass the Risk Engine
The Risk Engine intercepts all `OrderRequest` objects. It is a sealed, pure Java component. The AI has no API to modify Risk Engine parameters.

## Kill Switch Behavior
A global static/singleton flag. When triggered via API, the Risk Engine rejects all new orders immediately and triggers market sells for open positions.

## Risk Event Logging
Every rejection is logged with the specific rule that failed for audit purposes.

## Idempotency
Prevents duplicate orders using Redis distributed locks based on the `AiDecisionDto` hash.
