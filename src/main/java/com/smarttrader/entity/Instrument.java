package com.smarttrader.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.math.BigDecimal;

@Entity
@Table(name = "instruments")
public class Instrument extends BaseEntity {

    @Column(nullable = false, length = 40)
    String symbol;

    @Column(nullable = false, length = 40)
    String exchange;

    @Column(length = 120)
    String instrumentToken;

    @Column(length = 240)
    String companyName;

    @Column(length = 120)
    String sector;

    @Column(length = 160)
    String industry;

    @Column(precision = 20, scale = 4)
    BigDecimal marketCap;

    @Column(nullable = false)
    Integer lotSize = 1;

    @Column(nullable = false, precision = 18, scale = 8)
    BigDecimal tickSize = BigDecimal.valueOf(0.01);

    @Column(precision = 24, scale = 4)
    BigDecimal averageDailyVolume;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    InstrumentStatus status = InstrumentStatus.ACTIVE;

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public String getExchange() {
        return exchange;
    }

    public void setExchange(String exchange) {
        this.exchange = exchange;
    }

    public String getInstrumentToken() {
        return instrumentToken;
    }

    public void setInstrumentToken(String instrumentToken) {
        this.instrumentToken = instrumentToken;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getSector() {
        return sector;
    }

    public void setSector(String sector) {
        this.sector = sector;
    }

    public String getIndustry() {
        return industry;
    }

    public void setIndustry(String industry) {
        this.industry = industry;
    }

    public BigDecimal getMarketCap() {
        return marketCap;
    }

    public void setMarketCap(BigDecimal marketCap) {
        this.marketCap = marketCap;
    }

    public Integer getLotSize() {
        return lotSize;
    }

    public void setLotSize(Integer lotSize) {
        this.lotSize = lotSize;
    }

    public BigDecimal getTickSize() {
        return tickSize;
    }

    public void setTickSize(BigDecimal tickSize) {
        this.tickSize = tickSize;
    }

    public BigDecimal getAverageDailyVolume() {
        return averageDailyVolume;
    }

    public void setAverageDailyVolume(BigDecimal averageDailyVolume) {
        this.averageDailyVolume = averageDailyVolume;
    }

    public InstrumentStatus getStatus() {
        return status;
    }

    public void setStatus(InstrumentStatus status) {
        this.status = status;
    }
}
