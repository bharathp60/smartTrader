package com.smarttrader.backtest;

import com.smarttrader.dto.CandleDto;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Request payload for running a historical backtest simulation.
 *
 * <p>The caller must supply the ordered list of OHLCV candles (ascending by timestamp)
 * that covers the {@code startDate}–{@code endDate} window for the given {@code symbol}.
 * The engine will use these candles to replay market events, compute signals and execute
 * simulated fills.</p>
 *
 * <p>Default slippage of 0.05 % and commission of 0.03 % are assumed when the
 * corresponding fields are left at 0; the engine clamps negative values to 0.</p>
 */
public record BacktestRequest(

        /** Human-readable name for the strategy being tested. */
        @NotBlank String strategyName,

        /** Ticker / instrument symbol, e.g. {@code RELIANCE}, {@code BTCUSDT}. */
        @NotBlank String symbol,

        /** Candle timeframe label, e.g. {@code 1d}, {@code 1h}, {@code 15m}. */
        @NotBlank String timeframe,

        /** Inclusive simulation start date. */
        @NotNull LocalDate startDate,

        /** Inclusive simulation end date. */
        @NotNull LocalDate endDate,

        /**
         * Starting portfolio cash in the account's base currency.
         * Must be strictly positive.
         */
        @NotNull @DecimalMin("1.00") BigDecimal initialCapital,

        /**
         * Round-trip commission as a percentage of trade value (e.g. {@code 0.03} = 0.03 %).
         * Clamped to 0 when negative.  Defaults to 0.03 when zero is supplied.
         */
        double commissionPercent,

        /**
         * Simulated market-impact / slippage as a percentage of fill price
         * (e.g. {@code 0.05} = 0.05 %).  Clamped to 0 when negative.
         * Defaults to 0.05 when zero is supplied.
         */
        double slippagePercent,

        /**
         * Fraction of current equity risked per trade (e.g. {@code 1.0} = 1 %).
         * Controls position sizing via fixed-fractional risk model.
         * Clamped to 0.1 when zero or negative.
         */
        double riskPerTradePercent,

        /**
         * Stop-loss distance as a percentage below the entry price
         * (e.g. {@code 1.5} = 1.5 % below entry).
         * Defaults to {@code 1.5} when zero or negative.
         */
        double stopLossPercent,

        /**
         * Take-profit distance as a percentage above the entry price
         * (e.g. {@code 3.0} = 3 % above entry, i.e. risk-reward ratio 2:1).
         * Defaults to {@code 3.0} when zero or negative.
         */
        double takeProfitPercent,

        /**
         * Ordered list of OHLCV candles (ascending by {@code timestamp}) used as
         * the market data feed for the simulation.  Must not be empty.
         */
        @NotNull @NotEmpty List<CandleDto> candles

) {}
