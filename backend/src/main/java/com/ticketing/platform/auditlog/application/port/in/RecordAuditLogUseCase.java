package com.ticketing.platform.auditlog.application.port.in;

import com.ticketing.platform.auditlog.application.dto.AuditLogDto;
import com.ticketing.platform.auditlog.domain.model.AuditEventType;

import java.util.List;

public interface RecordAuditLogUseCase {

    void record(AuditEventType eventType, String sourceModule, String principal, String details);

    List<AuditLogDto> getRecentLogs(int limit);
}
