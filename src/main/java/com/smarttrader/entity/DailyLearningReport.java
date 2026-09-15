package com.smarttrader.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.LocalDate;

@Entity
@Table(name = "daily_learning_reports")
public class DailyLearningReport extends BaseEntity {

    @Column(nullable = false, unique = true)
    LocalDate reportDate;

    @Column
    String marketSummary;

    @Column(nullable = false, columnDefinition = "jsonb")
    String report;

    public LocalDate getReportDate() { return reportDate; }
    public void setReportDate(LocalDate reportDate) { this.reportDate = reportDate; }
    public String getMarketSummary() { return marketSummary; }
    public void setMarketSummary(String marketSummary) { this.marketSummary = marketSummary; }
    public String getReport() { return report; }
    public void setReport(String report) { this.report = report; }
}
