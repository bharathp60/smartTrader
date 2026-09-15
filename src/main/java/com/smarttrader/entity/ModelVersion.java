package com.smarttrader.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "model_versions")
public class ModelVersion extends BaseEntity {

    @Column(nullable = false, length = 120)
    String modelName;

    @Column(nullable = false, length = 80)
    String modelVersion;

    @Column(nullable = false, length = 40)
    String featureVersion;

    @Column
    Instant trainingDate;

    @Column(columnDefinition = "jsonb")
    String metrics;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    DeploymentStatus deploymentStatus = DeploymentStatus.CANDIDATE;

    public String getModelName() { return modelName; }
    public void setModelName(String modelName) { this.modelName = modelName; }
    public String getModelVersion() { return modelVersion; }
    public void setModelVersion(String modelVersion) { this.modelVersion = modelVersion; }
    public String getFeatureVersion() { return featureVersion; }
    public void setFeatureVersion(String featureVersion) { this.featureVersion = featureVersion; }
    public Instant getTrainingDate() { return trainingDate; }
    public void setTrainingDate(Instant trainingDate) { this.trainingDate = trainingDate; }
    public String getMetrics() { return metrics; }
    public void setMetrics(String metrics) { this.metrics = metrics; }
    public DeploymentStatus getDeploymentStatus() { return deploymentStatus; }
    public void setDeploymentStatus(DeploymentStatus deploymentStatus) { this.deploymentStatus = deploymentStatus; }
}
