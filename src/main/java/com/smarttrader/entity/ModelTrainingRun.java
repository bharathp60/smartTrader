package com.smarttrader.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "model_training_runs")
public class ModelTrainingRun extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "model_version_id")
    ModelVersion modelVersion;

    @Column(nullable = false)
    String datasetRef;

    @Column(nullable = false)
    Instant startedAt;

    @Column
    Instant completedAt;

    @Column(columnDefinition = "jsonb")
    String metrics;

    @Column(nullable = false, length = 40)
    String status;

    public ModelVersion getModelVersion() { return modelVersion; }
    public void setModelVersion(ModelVersion modelVersion) { this.modelVersion = modelVersion; }
    public String getDatasetRef() { return datasetRef; }
    public void setDatasetRef(String datasetRef) { this.datasetRef = datasetRef; }
    public Instant getStartedAt() { return startedAt; }
    public void setStartedAt(Instant startedAt) { this.startedAt = startedAt; }
    public Instant getCompletedAt() { return completedAt; }
    public void setCompletedAt(Instant completedAt) { this.completedAt = completedAt; }
    public String getMetrics() { return metrics; }
    public void setMetrics(String metrics) { this.metrics = metrics; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
