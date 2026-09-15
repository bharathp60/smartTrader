package com.smarttrader.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "daily_performance")
public class DailyPerformance extends BaseEntity {

    @Column(nullable = false, unique = true)
    LocalDate performanceDate;

    @Column(nullable = false, precision = 20, scale = 8)
    BigDecimal pnl = BigDecimal.ZERO;

    @Column(columnDefinition = "jsonb")
    String metrics;

    public LocalDate getPerformanceDate() { return performanceDate; }
    public void setPerformanceDate(LocalDate performanceDate) { this.performanceDate = performanceDate; }
    public BigDecimal getPnl() { return pnl; }
    public void setPnl(BigDecimal pnl) { this.pnl = pnl; }
    public String getMetrics() { return metrics; }
    public void setMetrics(String metrics) { this.metrics = metrics; }
}
