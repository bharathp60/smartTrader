# Learning Engine

The Learning Engine runs daily post-market to analyze performance and adapt the system.

## Daily Learning Pipeline
The pipeline consists of 17 steps, primarily:
1. Fetch all closed trades for the day.
2. Compare entry/exit vs optimal theoretical entry/exit.
3. Classify trade outcome.
4. Identify mistakes.
5. Propose parameter adaptations (e.g., widen stop loss).
6. Statistical validation of proposal.
7. Apply or reject adaptation.

## Trade Classification
- **GOOD_WIN**: Profit hit target, risk managed.
- **GOOD_LOSS**: Stopped out properly, thesis invalidated.
- **BAD_WIN**: Made money, but broke rules (e.g., held through earnings).
- **BAD_LOSS**: Lost money due to rule breaking or poor execution.

## Mistake Taxonomy
There are 26 defined mistake types, including:
- `PREMATURE_EXIT`: Exited before target or trailing stop hit.
- `STOP_TOO_TIGHT`: Stopped out on normal volatility noise.
- `LATE_ENTRY`: Entered after the bulk of the move.
- `EARNINGS_HOLD`: Held an active trading position through an earnings announcement unexpectedly.

## Adaptation Proposal Workflow
If the engine detects consistent `STOP_TOO_TIGHT` mistakes, it proposes increasing the ATR multiplier for stops.

## Statistical Validation
An adaptation is only applied if the mistake has occurred with statistical significance (e.g., p-value < 0.05 across last 100 trades).

## Auto-Rollback
If a parameter is changed and performance drops over the next 5 days, the engine automatically rolls back to the previous configuration.

## What Cannot Be Auto-Adapted
Safety limits (like `MAX_PORTFOLIO_RISK = 2%`) are hardcoded in the Risk Engine and cannot be changed by the Learning Engine.
