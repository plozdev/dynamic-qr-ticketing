package com.ticketing.platform.auditlog.domain.repository;

import com.ticketing.platform.auditlog.domain.model.AuditLogEntry;

import java.util.List;
import java.util.UUID;

/**
 * Domain Outbound Port for immutable Audit Log persistence.
 */
public interface AuditLogRepository {

    AuditLogEntry save(AuditLogEntry entry);

    List<AuditLogEntry> findRecent(int limit);
}
