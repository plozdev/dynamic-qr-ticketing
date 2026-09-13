package com.ticketing.platform.auditlog.application.service;

import com.ticketing.platform.auditlog.application.dto.AuditLogDto;
import com.ticketing.platform.auditlog.application.port.in.RecordAuditLogUseCase;
import com.ticketing.platform.auditlog.domain.model.AuditEventType;
import com.ticketing.platform.auditlog.domain.repository.AuditLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Khung sườn Application Service ghi nhận nhật ký kiểm toán hệ thống.
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
        // TODO: Tạo AuditLogEntry và lưu vào auditLogRepository
        throw new UnsupportedOperationException("TODO: Bạn tự triển khai record()");
    }

    @Override
    @Transactional(readOnly = true)
    public List<AuditLogDto> getRecentLogs(int limit) {
        // TODO: Lấy danh sách log gần nhất qua auditLogRepository và chuyển thành AuditLogDto
        throw new UnsupportedOperationException("TODO: Bạn tự triển khai getRecentLogs()");
    }
}
