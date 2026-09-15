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
@Table(name = "ticks")
public class Tick extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "instrument_id", nullable = false)
    Instrument instrument;

    @Column(nullable = false, length = 40)
    String symbol;

    @Column(nullable = false)
    Instant tradedAt;

    @Column(nullable = false, precision = 20, scale = 8)
    BigDecimal lastPrice;

    @Column(precision = 20, scale = 8)
    BigDecimal bidPrice;

    @Column(precision = 20, scale = 8)
    BigDecimal askPrice;

    @Column(precision = 24, scale = 4)
    BigDecimal volume;

    @Column(nullable = false, length = 80)
    String source;

    public Instrument getInstrument() { return instrument; }
    public void setInstrument(Instrument instrument) { this.instrument = instrument; }
    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }
    public Instant getTradedAt() { return tradedAt; }
    public void setTradedAt(Instant tradedAt) { this.tradedAt = tradedAt; }
    public BigDecimal getLastPrice() { return lastPrice; }
    public void setLastPrice(BigDecimal lastPrice) { this.lastPrice = lastPrice; }
    public BigDecimal getBidPrice() { return bidPrice; }
    public void setBidPrice(BigDecimal bidPrice) { this.bidPrice = bidPrice; }
    public BigDecimal getAskPrice() { return askPrice; }
    public void setAskPrice(BigDecimal askPrice) { this.askPrice = askPrice; }
    public BigDecimal getVolume() { return volume; }
    public void setVolume(BigDecimal volume) { this.volume = volume; }
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
}
