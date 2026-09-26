package com.ticketing.platform.auditlog.api.web;

import com.ticketing.platform.auditlog.api.dto.AuditLogResponse;
import com.ticketing.platform.auditlog.application.dto.AuditLogDto;
import com.ticketing.platform.auditlog.application.port.in.RecordAuditLogUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST Controller cho Audit Log.
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
        int queryLimit = (limit > 0 && limit <= 200) ? limit : 50;
        List<AuditLogDto> dtos = recordAuditLogUseCase.getRecentLogs(queryLimit);
        List<AuditLogResponse> responses = dtos.stream()
                .map(dto -> new AuditLogResponse(
                        dto.id(),
                        dto.eventType(),
                        dto.sourceModule(),
                        dto.principal(),
                        dto.details(),
                        dto.recordedAt()
                ))
                .toList();
        return ResponseEntity.ok(responses);
    }
}

