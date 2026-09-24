package com.ticketing.platform.ticketissuance.api.web;

import com.ticketing.platform.shared.security.SecurityUtils;
import com.ticketing.platform.ticketissuance.api.dto.ClaimTicketRequest;
import com.ticketing.platform.ticketissuance.api.dto.ClaimTicketResponse;
import com.ticketing.platform.ticketissuance.application.port.in.ClaimTicketUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * Controller chuyên biệt xử lý nhận vé / đặt vé trực tiếp từ chi tiết sự kiện
 * Route: POST /api/v1/events/{eventId}/claim
 */
@RestController
@RequestMapping("/api/v1/events")
@RequiredArgsConstructor
public class EventTicketClaimController {

    private final ClaimTicketUseCase claimTicketUseCase;

    @PostMapping("/{eventId}/claim")
    public ResponseEntity<ClaimTicketResponse> claimTicketFromEvent(
            @PathVariable UUID eventId,
            @RequestBody(required = false) ClaimTicketRequest request) {
        UUID effectiveUserId = SecurityUtils.getCurrentUserId();
        ClaimTicketRequest effectiveRequest = request != null ? request : new ClaimTicketRequest(null, null, null);
        ClaimTicketResponse response = claimTicketUseCase.claimTicket(effectiveRequest.toCommand(eventId, effectiveUserId));
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
