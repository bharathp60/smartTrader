package com.smarttrader.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "api_credentials")
public class ApiCredential extends BaseEntity {

    @Column(nullable = false, length = 80)
    String provider;

    @Column(nullable = false, length = 80)
    String credentialType;

    @Column(nullable = false)
    String encryptedPayload;

    @Column(nullable = false)
    boolean active = true;

    public String getProvider() { return provider; }
    public void setProvider(String provider) { this.provider = provider; }
    public String getCredentialType() { return credentialType; }
    public void setCredentialType(String credentialType) { this.credentialType = credentialType; }
    public String getEncryptedPayload() { return encryptedPayload; }
    public void setEncryptedPayload(String encryptedPayload) { this.encryptedPayload = encryptedPayload; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
