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
@Table(name = "risk_events")
public class RiskEvent extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    TradingOrder order;

    @Column(length = 40)
    String symbol;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    RiskDecision decision;

    @Column(nullable = false)
    String reason;

    @Column(precision = 10, scale = 6)
    BigDecimal riskScore;

    @Column(nullable = false, columnDefinition = "jsonb")
    String limitsChecked;

    @Column(nullable = false)
    Instant occurredAt;

    public TradingOrder getOrder() { return order; }
    public void setOrder(TradingOrder order) { this.order = order; }
    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }
    public RiskDecision getDecision() { return decision; }
    public void setDecision(RiskDecision decision) { this.decision = decision; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public BigDecimal getRiskScore() { return riskScore; }
    public void setRiskScore(BigDecimal riskScore) { this.riskScore = riskScore; }
    public String getLimitsChecked() { return limitsChecked; }
    public void setLimitsChecked(String limitsChecked) { this.limitsChecked = limitsChecked; }
    public Instant getOccurredAt() { return occurredAt; }
    public void setOccurredAt(Instant occurredAt) { this.occurredAt = occurredAt; }
}
