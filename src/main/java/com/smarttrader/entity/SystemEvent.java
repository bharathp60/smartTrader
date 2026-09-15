package com.smarttrader.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "system_events")
public class SystemEvent extends BaseEntity {

    @Column(nullable = false, length = 80)
    String eventType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    SystemSeverity severity;

    @Column(nullable = false)
    String message;

    @Column(columnDefinition = "jsonb")
    String payload;

    @Column(nullable = false)
    Instant occurredAt;

    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }
    public SystemSeverity getSeverity() { return severity; }
    public void setSeverity(SystemSeverity severity) { this.severity = severity; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getPayload() { return payload; }
    public void setPayload(String payload) { this.payload = payload; }
    public Instant getOccurredAt() { return occurredAt; }
    public void setOccurredAt(Instant occurredAt) { this.occurredAt = occurredAt; }
}
