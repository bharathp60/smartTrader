package com.smarttrader.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "strategy_versions")
public class StrategyVersion extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "strategy_id", nullable = false)
    Strategy strategy;

    @Column(nullable = false, length = 40)
    String version;

    @Column(nullable = false, columnDefinition = "jsonb")
    String config;

    @Column(nullable = false)
    boolean active = false;

    public Strategy getStrategy() { return strategy; }
    public void setStrategy(Strategy strategy) { this.strategy = strategy; }
    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }
    public String getConfig() { return config; }
    public void setConfig(String config) { this.config = config; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
