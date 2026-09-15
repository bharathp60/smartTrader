package com.smarttrader.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

@Entity
@Table(name = "adaptation_proposals")
public class AdaptationProposal extends BaseEntity {

    @Column(nullable = false, length = 80)
    String proposalType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    AdaptationStatus status;

    @Column
    String oldValue;

    @Column
    String newValue;

    @Column(nullable = false)
    String reason;

    @Column(columnDefinition = "jsonb")
    String evidence;

    @Column
    Integer sampleSize;

    @Column(columnDefinition = "jsonb")
    String backtestPerformance;

    @Column(columnDefinition = "jsonb")
    String outOfSamplePerformance;

    @Column(columnDefinition = "jsonb")
    String riskImpact;

    public String getProposalType() { return proposalType; }
    public void setProposalType(String proposalType) { this.proposalType = proposalType; }
    public AdaptationStatus getStatus() { return status; }
    public void setStatus(AdaptationStatus status) { this.status = status; }
    public String getOldValue() { return oldValue; }
    public void setOldValue(String oldValue) { this.oldValue = oldValue; }
    public String getNewValue() { return newValue; }
    public void setNewValue(String newValue) { this.newValue = newValue; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public String getEvidence() { return evidence; }
    public void setEvidence(String evidence) { this.evidence = evidence; }
    public Integer getSampleSize() { return sampleSize; }
    public void setSampleSize(Integer sampleSize) { this.sampleSize = sampleSize; }
    public String getBacktestPerformance() { return backtestPerformance; }
    public void setBacktestPerformance(String s) { this.backtestPerformance = s; }
    public String getOutOfSamplePerformance() { return outOfSamplePerformance; }
    public void setOutOfSamplePerformance(String s) { this.outOfSamplePerformance = s; }
    public String getRiskImpact() { return riskImpact; }
    public void setRiskImpact(String riskImpact) { this.riskImpact = riskImpact; }
}
