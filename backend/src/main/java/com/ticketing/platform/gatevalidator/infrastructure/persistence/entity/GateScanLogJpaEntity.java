package com.ticketing.platform.gatevalidator.infrastructure.persistence.entity;

import com.ticketing.platform.gatevalidator.domain.model.ValidationStatus;
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
@Table(name = "gate_scan_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GateScanLogJpaEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "ticket_id")
    private UUID ticketId;

    @Column(name = "gate_id", nullable = false)
    private String gateId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ValidationStatus status;

    @Column(name = "message")
    private String message;

    @Column(name = "scanned_at", nullable = false)
    private Instant scannedAt;
}
