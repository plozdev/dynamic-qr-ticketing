package com.ticketing.platform.auditlog.api.web;

import com.ticketing.platform.auditlog.api.dto.AuditLogResponse;
import com.ticketing.platform.auditlog.application.port.in.RecordAuditLogUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Khung sườn REST Controller cho Audit Log.
 */
@RestController
@RequestMapping("/api/v1/audit-logs")
public class AuditLogController {

    private final RecordAuditLogUseCase recordAuditLogUseCase;

    public AuditLogController(RecordAuditLogUseCase recordAuditLogUseCase) {
        this.recordAuditLogUseCase = recordAuditLogUseCase;
    }

    @GetMapping
    public ResponseEntity<List<AuditLogResponse>> getRecentAuditLogs(
            @RequestParam(defaultValue = "50") int limit) {
        // TODO: Gọi recordAuditLogUseCase.getRecentLogs(limit) và trả về HTTP 200 OK
        throw new UnsupportedOperationException("TODO: Bạn tự triển khai endpoint GET /api/v1/audit-logs");
    }
}
