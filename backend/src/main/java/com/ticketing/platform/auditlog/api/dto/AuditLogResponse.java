package com.ticketing.platform.auditlog.api.dto;

import java.time.Instant;
import java.util.UUID;

public record AuditLogResponse(
        UUID id,
        String eventType,
        String sourceModule,
        String principal,
        String details,
        Instant recordedAt
) {}
