package com.ticketing.platform.auditlog.infrastructure.persistence.entity;

import com.ticketing.platform.auditlog.domain.model.AuditEventType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "audit_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogJpaEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false)
    private AuditEventType eventType;

    @Column(name = "source_module", nullable = false)
    private String sourceModule;

    @Column(name = "principal", nullable = false)
    private String principal;

    @Column(name = "details", length = 2048)
    private String details;

    @Column(name = "recorded_at", nullable = false)
    private Instant recordedAt;
}
