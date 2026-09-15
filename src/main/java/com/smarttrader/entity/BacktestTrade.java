package com.smarttrader.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "backtest_trades")
public class BacktestTrade extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "backtest_run_id", nullable = false)
    BacktestRun backtestRun;

    @Column(nullable = false, length = 40)
    String symbol;

    @Column(nullable = false, length = 20)
    String side;

    @Column(nullable = false, precision = 24, scale = 4)
    BigDecimal quantity;

    @Column(nullable = false, precision = 20, scale = 8)
    BigDecimal entryPrice;

    @Column(precision = 20, scale = 8)
    BigDecimal exitPrice;

    @Column(precision = 20, scale = 8)
    BigDecimal pnl;

    @Column(nullable = false)
    Instant executedAt;

    public BacktestRun getBacktestRun() { return backtestRun; }
    public void setBacktestRun(BacktestRun backtestRun) { this.backtestRun = backtestRun; }
    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }
    public String getSide() { return side; }
    public void setSide(String side) { this.side = side; }
    public BigDecimal getQuantity() { return quantity; }
    public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }
    public BigDecimal getEntryPrice() { return entryPrice; }
    public void setEntryPrice(BigDecimal entryPrice) { this.entryPrice = entryPrice; }
    public BigDecimal getExitPrice() { return exitPrice; }
    public void setExitPrice(BigDecimal exitPrice) { this.exitPrice = exitPrice; }
    public BigDecimal getPnl() { return pnl; }
    public void setPnl(BigDecimal pnl) { this.pnl = pnl; }
    public Instant getExecutedAt() { return executedAt; }
    public void setExecutedAt(Instant executedAt) { this.executedAt = executedAt; }
}
