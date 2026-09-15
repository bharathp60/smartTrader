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
@Table(name = "stock_rankings")
public class StockRanking extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "instrument_id", nullable = false)
    Instrument instrument;

    @Column(nullable = false, length = 40)
    String symbol;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    TradingProfile profile;

    @Column(nullable = false)
    Integer rankValue;

    @Column(nullable = false, precision = 10, scale = 6)
    BigDecimal opportunityScore;

    @Column(nullable = false)
    Instant rankedAt;

    public Instrument getInstrument() {
        return instrument;
    }

    public void setInstrument(Instrument instrument) {
        this.instrument = instrument;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public TradingProfile getProfile() {
        return profile;
    }

    public void setProfile(TradingProfile profile) {
        this.profile = profile;
    }

    public Integer getRankValue() {
        return rankValue;
    }

    public void setRankValue(Integer rankValue) {
        this.rankValue = rankValue;
    }

    public BigDecimal getOpportunityScore() {
        return opportunityScore;
    }

    public void setOpportunityScore(BigDecimal opportunityScore) {
        this.opportunityScore = opportunityScore;
    }

    public Instant getRankedAt() {
        return rankedAt;
    }

    public void setRankedAt(Instant rankedAt) {
        this.rankedAt = rankedAt;
    }
}
