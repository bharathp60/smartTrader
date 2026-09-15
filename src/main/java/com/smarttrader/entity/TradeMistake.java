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
@Table(name = "trade_mistakes")
public class TradeMistake extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trade_result_id")
    TradeResult tradeResult;

    @Column(length = 40)
    String symbol;

    @Column(nullable = false, length = 80)
    String taxonomy;

    @Column(nullable = false, precision = 8, scale = 6)
    BigDecimal severity;

    @Column(precision = 20, scale = 8)
    BigDecimal financialImpact;

    @Column(columnDefinition = "jsonb")
    String context;

    @Column(nullable = false)
    Instant detectedAt;

    public TradeResult getTradeResult() { return tradeResult; }
    public void setTradeResult(TradeResult tradeResult) { this.tradeResult = tradeResult; }
    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }
    public String getTaxonomy() { return taxonomy; }
    public void setTaxonomy(String taxonomy) { this.taxonomy = taxonomy; }
    public BigDecimal getSeverity() { return severity; }
    public void setSeverity(BigDecimal severity) { this.severity = severity; }
    public BigDecimal getFinancialImpact() { return financialImpact; }
    public void setFinancialImpact(BigDecimal financialImpact) { this.financialImpact = financialImpact; }
    public String getContext() { return context; }
    public void setContext(String context) { this.context = context; }
    public Instant getDetectedAt() { return detectedAt; }
    public void setDetectedAt(Instant detectedAt) { this.detectedAt = detectedAt; }
}
