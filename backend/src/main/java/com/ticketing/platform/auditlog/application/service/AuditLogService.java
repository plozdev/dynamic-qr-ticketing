package com.ticketing.platform.auditlog.application.service;

import com.ticketing.platform.auditlog.application.dto.AuditLogDto;
import com.ticketing.platform.auditlog.application.port.in.RecordAuditLogUseCase;
import com.ticketing.platform.auditlog.domain.model.AuditEventType;
import com.ticketing.platform.auditlog.domain.model.AuditLogEntry;
import com.ticketing.platform.auditlog.domain.repository.AuditLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Application Service ghi nhận nhật ký kiểm toán hệ thống.
 */
@Service
@Transactional
public class AuditLogService implements RecordAuditLogUseCase {

    private final AuditLogRepository auditLogRepository;

    public AuditLogService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Override
    public void record(AuditEventType eventType, String sourceModule, String principal, String details) {
        if (eventType == null) {
            throw new IllegalArgumentException("eventType không được null");
        }
        if (sourceModule == null || sourceModule.isBlank()) {
            throw new IllegalArgumentException("sourceModule không được để trống");
        }
        String safePrincipal = (principal != null && !principal.isBlank()) ? principal : "SYSTEM";
        String safeDetails = (details != null) ? details : "";

        AuditLogEntry entry = AuditLogEntry.record(eventType, sourceModule, safePrincipal, safeDetails);
        auditLogRepository.save(entry);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AuditLogDto> getRecentLogs(int limit) {
        int queryLimit = (limit > 0 && limit <= 200) ? limit : 50;
        List<AuditLogEntry> entries = auditLogRepository.findRecent(queryLimit);
        return entries.stream()
                .map(entry -> new AuditLogDto(
                        entry.getId(),
                        entry.getEventType().name(),
                        entry.getSourceModule(),
                        entry.getPrincipal(),
                        entry.getDetails(),
                        entry.getRecordedAt()
                ))
                .toList();
    }
}

