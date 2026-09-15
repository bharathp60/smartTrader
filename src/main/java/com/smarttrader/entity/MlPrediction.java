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
@Table(name = "ml_predictions")
public class MlPrediction extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "model_version_id")
    ModelVersion modelVersion;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "instrument_id", nullable = false)
    Instrument instrument;

    @Column(nullable = false, length = 40)
    String symbol;

    @Column(nullable = false)
    Instant predictedAt;

    @Column(nullable = false, length = 40)
    String horizon;

    @Column(precision = 8, scale = 6)
    BigDecimal probabilityPositiveReturn;

    @Column(precision = 12, scale = 8)
    BigDecimal expectedReturn;

    @Column(columnDefinition = "jsonb")
    String payload;

    public ModelVersion getModelVersion() { return modelVersion; }
    public void setModelVersion(ModelVersion modelVersion) { this.modelVersion = modelVersion; }
    public Instrument getInstrument() { return instrument; }
    public void setInstrument(Instrument instrument) { this.instrument = instrument; }
    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }
    public Instant getPredictedAt() { return predictedAt; }
    public void setPredictedAt(Instant predictedAt) { this.predictedAt = predictedAt; }
    public String getHorizon() { return horizon; }
    public void setHorizon(String horizon) { this.horizon = horizon; }
    public BigDecimal getProbabilityPositiveReturn() { return probabilityPositiveReturn; }
    public void setProbabilityPositiveReturn(BigDecimal probabilityPositiveReturn) { this.probabilityPositiveReturn = probabilityPositiveReturn; }
    public BigDecimal getExpectedReturn() { return expectedReturn; }
    public void setExpectedReturn(BigDecimal expectedReturn) { this.expectedReturn = expectedReturn; }
    public String getPayload() { return payload; }
    public void setPayload(String payload) { this.payload = payload; }
}
