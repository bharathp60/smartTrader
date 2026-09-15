package com.smarttrader.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "configuration_versions")
public class ConfigurationVersion extends BaseEntity {

    @Column(nullable = false, unique = true, length = 80)
    String version;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proposal_id")
    AdaptationProposal proposal;

    @Column(nullable = false, columnDefinition = "jsonb")
    String config;

    @Column(nullable = false)
    boolean active = false;

    @Column
    Instant deployedAt;

    @Column
    Instant rolledBackAt;

    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }
    public AdaptationProposal getProposal() { return proposal; }
    public void setProposal(AdaptationProposal proposal) { this.proposal = proposal; }
    public String getConfig() { return config; }
    public void setConfig(String config) { this.config = config; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public Instant getDeployedAt() { return deployedAt; }
    public void setDeployedAt(Instant deployedAt) { this.deployedAt = deployedAt; }
    public Instant getRolledBackAt() { return rolledBackAt; }
    public void setRolledBackAt(Instant rolledBackAt) { this.rolledBackAt = rolledBackAt; }
}
