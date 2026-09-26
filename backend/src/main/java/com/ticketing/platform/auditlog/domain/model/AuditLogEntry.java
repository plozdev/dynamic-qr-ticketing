package com.ticketing.platform.auditlog.domain.model;

import com.ticketing.platform.shared.domain.AggregateRoot;

import java.time.Instant;
import java.util.UUID;

/**
 * Audit Log Aggregate Root within Audit Log Bounded Context.
 */
public class AuditLogEntry implements AggregateRoot<UUID> {

    private final UUID id;
    private final AuditEventType eventType;
    private final String sourceModule;
    private final String principal;
    private final String details;
    private final Instant recordedAt;

    public AuditLogEntry(UUID id, AuditEventType eventType, String sourceModule,
                         String principal, String details, Instant recordedAt) {
        this.id = id != null ? id : UUID.randomUUID();
        this.eventType = eventType;
        this.sourceModule = sourceModule;
        this.principal = principal;
        this.details = details;
        this.recordedAt = recordedAt != null ? recordedAt : Instant.now();
    }

    public static AuditLogEntry record(AuditEventType type, String sourceModule, String principal, String details) {
        return new AuditLogEntry(UUID.randomUUID(), type, sourceModule, principal, details, Instant.now());
    }

    @Override
    public UUID getId() {
        return id;
    }

    public AuditEventType getEventType() {
        return eventType;
    }

    public String getSourceModule() {
        return sourceModule;
    }

    public String getPrincipal() {
        return principal;
    }

    public String getDetails() {
        return details;
    }

    public Instant getRecordedAt() {
        return recordedAt;
    }
}
