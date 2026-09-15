package com.smarttrader.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "technical_indicators")
public class TechnicalIndicatorSnapshot extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "instrument_id", nullable = false)
    Instrument instrument;

    @Column(nullable = false, length = 40)
    String symbol;

    @Column(nullable = false, length = 10)
    String timeframe;

    @Column(nullable = false)
    Instant calculatedAt;

    @Column(nullable = false, columnDefinition = "jsonb")
    String indicators;

    @Column(nullable = false, length = 40)
    String indicatorVersion;

    public Instrument getInstrument() { return instrument; }
    public void setInstrument(Instrument instrument) { this.instrument = instrument; }
    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }
    public String getTimeframe() { return timeframe; }
    public void setTimeframe(String timeframe) { this.timeframe = timeframe; }
    public Instant getCalculatedAt() { return calculatedAt; }
    public void setCalculatedAt(Instant calculatedAt) { this.calculatedAt = calculatedAt; }
    public String getIndicators() { return indicators; }
    public void setIndicators(String indicators) { this.indicators = indicators; }
    public String getIndicatorVersion() { return indicatorVersion; }
    public void setIndicatorVersion(String indicatorVersion) { this.indicatorVersion = indicatorVersion; }
}
