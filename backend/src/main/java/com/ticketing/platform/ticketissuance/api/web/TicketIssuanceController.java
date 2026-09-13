package com.ticketing.platform.ticketissuance.api.web;

import com.ticketing.platform.ticketissuance.api.dto.DynamicQrResponse;
import com.ticketing.platform.ticketissuance.api.dto.IssueTicketRequest;
import com.ticketing.platform.ticketissuance.application.port.in.GenerateDynamicQrUseCase;
import com.ticketing.platform.ticketissuance.application.port.in.IssueTicketUseCase;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

/**
 * Khung sườn REST Controller cho Ticket Issuance.
 */
@RestController
@RequestMapping("/api/v1/tickets")
public class TicketIssuanceController {

    private final IssueTicketUseCase issueTicketUseCase;
    private final GenerateDynamicQrUseCase generateDynamicQrUseCase;

    public TicketIssuanceController(IssueTicketUseCase issueTicketUseCase,
                                    GenerateDynamicQrUseCase generateDynamicQrUseCase) {
        this.issueTicketUseCase = issueTicketUseCase;
        this.generateDynamicQrUseCase = generateDynamicQrUseCase;
    }

    @PostMapping("/issue")
    public ResponseEntity<Map<String, Object>> issueTicket(@Valid @RequestBody IssueTicketRequest request) {
        // TODO: Chuyển request sang IssueTicketCommand, gọi issueTicketUseCase và trả về 201 Created
        throw new UnsupportedOperationException("TODO: Bạn tự triển khai endpoint POST /api/v1/tickets/issue");
    }

    @GetMapping("/{id}/dynamic-qr")
    public ResponseEntity<DynamicQrResponse> getDynamicQr(
            @PathVariable UUID id,
            @RequestHeader("X-User-Id") UUID requesterUserId) {
        // TODO: Phương án phụ (Web Client Fallback): Gọi generateDynamicQrUseCase và trả về DynamicQrResponse
        throw new UnsupportedOperationException("TODO: Bạn tự triển khai endpoint GET /api/v1/tickets/{id}/dynamic-qr");
    }

    @GetMapping("/{id}/sync")
    public ResponseEntity<com.ticketing.platform.ticketissuance.api.dto.TicketSyncResponse> syncTicket(
            @PathVariable UUID id,
            @RequestHeader("X-User-Id") UUID requesterUserId) {
        // TODO: Key Provisioning cho Mobile (Android Compose + C++ NDK):
        // 1. Kiểm tra requesterUserId có đúng là chủ sở hữu vé id không
        // 2. Trả về ticketId, eventId, categoryName, secretKeyBase64, và serverTimeEpochSeconds
        // 3. Mobile sẽ lưu secretKey an toàn và tự sinh Dynamic QR offline (Zero network call mỗi 30s)
        throw new UnsupportedOperationException("TODO: Bạn tự triển khai endpoint GET /api/v1/tickets/{id}/sync");
    }
}
