CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE roles (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(40) NOT NULL UNIQUE,
    description TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    username VARCHAR(80) NOT NULL UNIQUE,
    email VARCHAR(160) NOT NULL UNIQUE,
    password_hash TEXT NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE user_roles (
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role_id UUID NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, role_id)
);

CREATE TABLE api_credentials (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    provider VARCHAR(80) NOT NULL,
    credential_type VARCHAR(80) NOT NULL,
    encrypted_payload TEXT NOT NULL,
    active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE instruments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    symbol VARCHAR(40) NOT NULL,
    exchange VARCHAR(40) NOT NULL,
    instrument_token VARCHAR(120),
    company_name VARCHAR(240),
    sector VARCHAR(120),
    industry VARCHAR(160),
    market_cap NUMERIC(20, 4),
    lot_size INTEGER NOT NULL DEFAULT 1,
    tick_size NUMERIC(18, 8) NOT NULL DEFAULT 0.01,
    average_daily_volume NUMERIC(24, 4),
    status VARCHAR(40) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_instruments_symbol_exchange UNIQUE (symbol, exchange)
);
CREATE INDEX idx_instruments_status ON instruments(status);
CREATE INDEX idx_instruments_sector ON instruments(sector);

CREATE TABLE market_data (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    instrument_id UUID NOT NULL REFERENCES instruments(id),
    symbol VARCHAR(40) NOT NULL,
    provider VARCHAR(80) NOT NULL,
    payload JSONB NOT NULL,
    received_at TIMESTAMPTZ NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_market_data_symbol_received_at ON market_data(symbol, received_at DESC);

CREATE TABLE candles (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    instrument_id UUID NOT NULL REFERENCES instruments(id),
    symbol VARCHAR(40) NOT NULL,
    timeframe VARCHAR(10) NOT NULL,
    opened_at TIMESTAMPTZ NOT NULL,
    open_price NUMERIC(20, 8) NOT NULL,
    high_price NUMERIC(20, 8) NOT NULL,
    low_price NUMERIC(20, 8) NOT NULL,
    close_price NUMERIC(20, 8) NOT NULL,
    volume NUMERIC(24, 4) NOT NULL DEFAULT 0,
    source VARCHAR(80) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_candles_symbol_timeframe_opened UNIQUE (symbol, timeframe, opened_at)
);
CREATE INDEX idx_candles_lookup ON candles(symbol, timeframe, opened_at DESC);

CREATE TABLE ticks (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    instrument_id UUID NOT NULL REFERENCES instruments(id),
    symbol VARCHAR(40) NOT NULL,
    traded_at TIMESTAMPTZ NOT NULL,
    last_price NUMERIC(20, 8) NOT NULL,
    bid_price NUMERIC(20, 8),
    ask_price NUMERIC(20, 8),
    volume NUMERIC(24, 4),
    source VARCHAR(80) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_ticks_symbol_traded_at ON ticks(symbol, traded_at DESC);

CREATE TABLE technical_indicators (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    instrument_id UUID NOT NULL REFERENCES instruments(id),
    symbol VARCHAR(40) NOT NULL,
    timeframe VARCHAR(10) NOT NULL,
    calculated_at TIMESTAMPTZ NOT NULL,
    indicators JSONB NOT NULL,
    indicator_version VARCHAR(40) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_indicators_symbol_timeframe_calculated UNIQUE (symbol, timeframe, calculated_at, indicator_version)
);
CREATE INDEX idx_technical_indicators_lookup ON technical_indicators(symbol, timeframe, calculated_at DESC);

CREATE TABLE fundamental_data (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    instrument_id UUID NOT NULL REFERENCES instruments(id),
    symbol VARCHAR(40) NOT NULL,
    fiscal_period VARCHAR(40),
    metrics JSONB,
    data_available BOOLEAN NOT NULL DEFAULT false,
    source VARCHAR(80),
    reported_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_fundamental_symbol_period ON fundamental_data(symbol, fiscal_period);

CREATE TABLE features (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    instrument_id UUID NOT NULL REFERENCES instruments(id),
    symbol VARCHAR(40) NOT NULL,
    feature_version VARCHAR(40) NOT NULL,
    timeframe VARCHAR(10) NOT NULL,
    generated_at TIMESTAMPTZ NOT NULL,
    values_json JSONB NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_features_symbol_generated_at ON features(symbol, generated_at DESC);

CREATE TABLE model_versions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    model_name VARCHAR(120) NOT NULL,
    model_version VARCHAR(80) NOT NULL,
    feature_version VARCHAR(40) NOT NULL,
    training_date TIMESTAMPTZ,
    metrics JSONB,
    deployment_status VARCHAR(40) NOT NULL DEFAULT 'CANDIDATE',
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_model_versions_name_version UNIQUE (model_name, model_version)
);

CREATE TABLE model_training_runs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    model_version_id UUID REFERENCES model_versions(id),
    dataset_ref TEXT NOT NULL,
    started_at TIMESTAMPTZ NOT NULL,
    completed_at TIMESTAMPTZ,
    metrics JSONB,
    status VARCHAR(40) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE ml_predictions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    model_version_id UUID REFERENCES model_versions(id),
    instrument_id UUID NOT NULL REFERENCES instruments(id),
    symbol VARCHAR(40) NOT NULL,
    predicted_at TIMESTAMPTZ NOT NULL,
    horizon VARCHAR(40) NOT NULL,
    probability_positive_return NUMERIC(8, 6),
    expected_return NUMERIC(12, 8),
    payload JSONB,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_ml_predictions_symbol_predicted_at ON ml_predictions(symbol, predicted_at DESC);

CREATE TABLE ai_decisions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    instrument_id UUID REFERENCES instruments(id),
    symbol VARCHAR(40),
    decision_type VARCHAR(60) NOT NULL,
    action VARCHAR(40) NOT NULL,
    confidence NUMERIC(8, 6),
    structured_output JSONB NOT NULL,
    validated BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_ai_decisions_symbol_created_at ON ai_decisions(symbol, created_at DESC);

CREATE TABLE strategies (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(120) NOT NULL UNIQUE,
    profile VARCHAR(40) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE strategy_versions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    strategy_id UUID NOT NULL REFERENCES strategies(id),
    version VARCHAR(40) NOT NULL,
    config JSONB NOT NULL,
    active BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_strategy_versions UNIQUE (strategy_id, version)
);

CREATE TABLE factor_scores (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    instrument_id UUID NOT NULL REFERENCES instruments(id),
    symbol VARCHAR(40) NOT NULL,
    profile VARCHAR(40) NOT NULL,
    calculated_at TIMESTAMPTZ NOT NULL,
    scores JSONB NOT NULL,
    total_score NUMERIC(10, 6) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_factor_scores_profile_score ON factor_scores(profile, total_score DESC);

CREATE TABLE stock_rankings (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    instrument_id UUID NOT NULL REFERENCES instruments(id),
    symbol VARCHAR(40) NOT NULL,
    profile VARCHAR(40) NOT NULL,
    rank_value INTEGER NOT NULL,
    opportunity_score NUMERIC(10, 6) NOT NULL,
    ranked_at TIMESTAMPTZ NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_stock_rankings_profile_ranked_at ON stock_rankings(profile, ranked_at DESC, rank_value);

CREATE TABLE orders (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    client_order_id VARCHAR(120) NOT NULL UNIQUE,
    broker_order_id VARCHAR(120),
    instrument_id UUID NOT NULL REFERENCES instruments(id),
    symbol VARCHAR(40) NOT NULL,
    side VARCHAR(20) NOT NULL,
    order_type VARCHAR(40) NOT NULL,
    quantity NUMERIC(24, 4) NOT NULL,
    limit_price NUMERIC(20, 8),
    stop_price NUMERIC(20, 8),
    status VARCHAR(40) NOT NULL,
    mode VARCHAR(20) NOT NULL,
    idempotency_key VARCHAR(160) NOT NULL UNIQUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_orders_symbol_status ON orders(symbol, status);

CREATE TABLE order_events (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    order_id UUID NOT NULL REFERENCES orders(id),
    event_type VARCHAR(60) NOT NULL,
    payload JSONB,
    occurred_at TIMESTAMPTZ NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_order_events_order_occurred_at ON order_events(order_id, occurred_at);

CREATE TABLE positions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    instrument_id UUID NOT NULL REFERENCES instruments(id),
    symbol VARCHAR(40) NOT NULL,
    quantity NUMERIC(24, 4) NOT NULL,
    average_price NUMERIC(20, 8) NOT NULL,
    stop_loss NUMERIC(20, 8),
    target_price NUMERIC(20, 8),
    status VARCHAR(40) NOT NULL,
    opened_at TIMESTAMPTZ NOT NULL,
    closed_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_positions_symbol_status ON positions(symbol, status);

CREATE TABLE trades (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    order_id UUID REFERENCES orders(id),
    position_id UUID REFERENCES positions(id),
    instrument_id UUID NOT NULL REFERENCES instruments(id),
    symbol VARCHAR(40) NOT NULL,
    side VARCHAR(20) NOT NULL,
    quantity NUMERIC(24, 4) NOT NULL,
    price NUMERIC(20, 8) NOT NULL,
    fees NUMERIC(20, 8) NOT NULL DEFAULT 0,
    executed_at TIMESTAMPTZ NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_trades_symbol_executed_at ON trades(symbol, executed_at DESC);

CREATE TABLE trade_results (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    position_id UUID REFERENCES positions(id),
    symbol VARCHAR(40) NOT NULL,
    pnl NUMERIC(20, 8) NOT NULL,
    return_pct NUMERIC(12, 8),
    classification VARCHAR(40),
    result_payload JSONB,
    closed_at TIMESTAMPTZ NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE risk_events (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    order_id UUID REFERENCES orders(id),
    symbol VARCHAR(40),
    decision VARCHAR(40) NOT NULL,
    reason TEXT NOT NULL,
    risk_score NUMERIC(10, 6),
    limits_checked JSONB NOT NULL,
    occurred_at TIMESTAMPTZ NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_risk_events_occurred_at ON risk_events(occurred_at DESC);

CREATE TABLE backtest_runs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    strategy_id UUID REFERENCES strategies(id),
    started_at TIMESTAMPTZ NOT NULL,
    completed_at TIMESTAMPTZ,
    input_config JSONB NOT NULL,
    metrics JSONB,
    status VARCHAR(40) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE backtest_trades (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    backtest_run_id UUID NOT NULL REFERENCES backtest_runs(id) ON DELETE CASCADE,
    symbol VARCHAR(40) NOT NULL,
    side VARCHAR(20) NOT NULL,
    quantity NUMERIC(24, 4) NOT NULL,
    entry_price NUMERIC(20, 8) NOT NULL,
    exit_price NUMERIC(20, 8),
    pnl NUMERIC(20, 8),
    executed_at TIMESTAMPTZ NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_backtest_trades_run ON backtest_trades(backtest_run_id);

CREATE TABLE daily_performance (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    performance_date DATE NOT NULL UNIQUE,
    pnl NUMERIC(20, 8) NOT NULL DEFAULT 0,
    metrics JSONB,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE trade_mistakes (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    trade_result_id UUID REFERENCES trade_results(id),
    symbol VARCHAR(40),
    taxonomy VARCHAR(80) NOT NULL,
    severity NUMERIC(8, 6) NOT NULL,
    financial_impact NUMERIC(20, 8),
    context JSONB,
    detected_at TIMESTAMPTZ NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_trade_mistakes_taxonomy_detected_at ON trade_mistakes(taxonomy, detected_at DESC);

CREATE TABLE daily_learning_reports (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    report_date DATE NOT NULL UNIQUE,
    market_summary TEXT,
    report JSONB NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE adaptation_proposals (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    proposal_type VARCHAR(80) NOT NULL,
    status VARCHAR(40) NOT NULL,
    old_value TEXT,
    new_value TEXT,
    reason TEXT NOT NULL,
    evidence JSONB,
    sample_size INTEGER,
    backtest_performance JSONB,
    out_of_sample_performance JSONB,
    risk_impact JSONB,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_adaptation_proposals_status ON adaptation_proposals(status);

CREATE TABLE configuration_versions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    version VARCHAR(80) NOT NULL UNIQUE,
    proposal_id UUID REFERENCES adaptation_proposals(id),
    config JSONB NOT NULL,
    active BOOLEAN NOT NULL DEFAULT false,
    deployed_at TIMESTAMPTZ,
    rolled_back_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE system_events (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    event_type VARCHAR(80) NOT NULL,
    severity VARCHAR(40) NOT NULL,
    message TEXT NOT NULL,
    payload JSONB,
    occurred_at TIMESTAMPTZ NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_system_events_type_occurred_at ON system_events(event_type, occurred_at DESC);

CREATE TABLE audit_logs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    actor VARCHAR(120),
    action VARCHAR(120) NOT NULL,
    entity_type VARCHAR(120),
    entity_id UUID,
    payload JSONB,
    occurred_at TIMESTAMPTZ NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_audit_logs_occurred_at ON audit_logs(occurred_at DESC);
