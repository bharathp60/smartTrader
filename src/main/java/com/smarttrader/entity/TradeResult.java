package com.smarttrader.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "trade_results")
public class TradeResult extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "position_id")
    Position position;

    @Column(nullable = false, length = 40)
    String symbol;

    @Column(nullable = false, precision = 20, scale = 8)
    BigDecimal pnl;

    @Column(precision = 12, scale = 8)
    BigDecimal returnPct;

    @Enumerated(EnumType.STRING)
    @Column(length = 40)
    TradeClassification classification;

    @Column(columnDefinition = "jsonb")
    String resultPayload;

    @Column(nullable = false)
    Instant closedAt;

    public Position getPosition() { return position; }
    public void setPosition(Position position) { this.position = position; }
    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }
    public BigDecimal getPnl() { return pnl; }
    public void setPnl(BigDecimal pnl) { this.pnl = pnl; }
    public BigDecimal getReturnPct() { return returnPct; }
    public void setReturnPct(BigDecimal returnPct) { this.returnPct = returnPct; }
    public TradeClassification getClassification() { return classification; }
    public void setClassification(TradeClassification classification) { this.classification = classification; }
    public String getResultPayload() { return resultPayload; }
    public void setResultPayload(String resultPayload) { this.resultPayload = resultPayload; }
    public Instant getClosedAt() { return closedAt; }
    public void setClosedAt(Instant closedAt) { this.closedAt = closedAt; }
}
