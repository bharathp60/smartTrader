package com.smarttrader.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "backtest_runs")
public class BacktestRun extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "strategy_id")
    Strategy strategy;

    @Column(nullable = false)
    Instant startedAt;

    @Column
    Instant completedAt;

    @Column(nullable = false, columnDefinition = "jsonb")
    String inputConfig;

    @Column(columnDefinition = "jsonb")
    String metrics;

    @Column(nullable = false, length = 40)
    String status;

    public Strategy getStrategy() { return strategy; }
    public void setStrategy(Strategy strategy) { this.strategy = strategy; }
    public Instant getStartedAt() { return startedAt; }
    public void setStartedAt(Instant startedAt) { this.startedAt = startedAt; }
    public Instant getCompletedAt() { return completedAt; }
    public void setCompletedAt(Instant completedAt) { this.completedAt = completedAt; }
    public String getInputConfig() { return inputConfig; }
    public void setInputConfig(String inputConfig) { this.inputConfig = inputConfig; }
    public String getMetrics() { return metrics; }
    public void setMetrics(String metrics) { this.metrics = metrics; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
