package com.smarttrader.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;

@Entity
@Table(name = "ai_decisions")
public class AiDecision extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "instrument_id")
    Instrument instrument;

    @Column(length = 40)
    String symbol;

    @Column(nullable = false, length = 60)
    String decisionType;

    @Column(nullable = false, length = 40)
    String action;

    @Column(precision = 8, scale = 6)
    BigDecimal confidence;

    @Column(nullable = false, columnDefinition = "jsonb")
    String structuredOutput;

    @Column(nullable = false)
    boolean validated = false;

    public Instrument getInstrument() { return instrument; }
    public void setInstrument(Instrument instrument) { this.instrument = instrument; }
    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }
    public String getDecisionType() { return decisionType; }
    public void setDecisionType(String decisionType) { this.decisionType = decisionType; }
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
    public BigDecimal getConfidence() { return confidence; }
    public void setConfidence(BigDecimal confidence) { this.confidence = confidence; }
    public String getStructuredOutput() { return structuredOutput; }
    public void setStructuredOutput(String structuredOutput) { this.structuredOutput = structuredOutput; }
    public boolean isValidated() { return validated; }
    public void setValidated(boolean validated) { this.validated = validated; }
}
