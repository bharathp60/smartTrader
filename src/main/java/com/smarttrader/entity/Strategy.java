package com.smarttrader.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

@Entity
@Table(name = "strategies")
public class Strategy extends BaseEntity {

    @Column(nullable = false, unique = true, length = 120)
    String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    TradingProfile profile;

    @Column(nullable = false)
    boolean active = true;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public TradingProfile getProfile() { return profile; }
    public void setProfile(TradingProfile profile) { this.profile = profile; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
