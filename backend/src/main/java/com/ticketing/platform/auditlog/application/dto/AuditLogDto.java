package com.ticketing.platform.auditlog.application.dto;

import java.time.Instant;
import java.util.UUID;

public record AuditLogDto(
        UUID id,
        String eventType,
        String sourceModule,
        String principal,
        String details,
        Instant recordedAt
) {}
