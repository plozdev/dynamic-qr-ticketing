package com.ticketing.platform.ticketissuance.api.web;

import com.ticketing.platform.ticketissuance.api.dto.DynamicQrResponse;
import com.ticketing.platform.ticketissuance.api.dto.IssueTicketRequest;
import com.ticketing.platform.ticketissuance.api.dto.TicketSyncResponse;
import com.ticketing.platform.ticketissuance.application.dto.DynamicQrDto;
import com.ticketing.platform.ticketissuance.application.dto.TicketSyncDto;
import com.ticketing.platform.ticketissuance.api.dto.UserTicketResponse;
import com.ticketing.platform.ticketissuance.application.port.in.GetUserTicketsUseCase;
import com.ticketing.platform.ticketissuance.application.port.in.GenerateDynamicQrUseCase;
import com.ticketing.platform.ticketissuance.application.port.in.IssueTicketUseCase;
import com.ticketing.platform.ticketissuance.application.port.in.SyncTicketUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.ticketing.platform.shared.security.SecurityUtils;
import com.ticketing.platform.ticketissuance.api.dto.ClaimTicketRequest;
import com.ticketing.platform.ticketissuance.api.dto.ClaimTicketResponse;
import com.ticketing.platform.ticketissuance.api.dto.TicketDetailsResponse;
import com.ticketing.platform.ticketissuance.application.port.in.ClaimTicketUseCase;
import com.ticketing.platform.ticketissuance.application.port.in.GetTicketDetailsUseCase;

/**
 * REST Controller cho Ticket Issuance.
 */
@RestController
@RequestMapping("/api/v1/tickets")
@RequiredArgsConstructor 
public class TicketIssuanceController {

    private final IssueTicketUseCase issueTicketUseCase;
    private final ClaimTicketUseCase claimTicketUseCase;
    private final GenerateDynamicQrUseCase generateDynamicQrUseCase;
    private final SyncTicketUseCase syncTicketUseCase;
    private final GetUserTicketsUseCase getUserTicketsUseCase;
    private final GetTicketDetailsUseCase getTicketDetailsUseCase;

    @GetMapping
    public ResponseEntity<List<UserTicketResponse>> getMyTickets(
            @RequestHeader(value = "X-User-Id", required = false) UUID userIdHeader,
            @RequestParam(value = "userId", required = false) UUID userIdParam) {
        UUID effectiveUserId = SecurityUtils.getEffectiveUserId(userIdHeader != null ? userIdHeader : userIdParam);
        return ResponseEntity.ok(getUserTicketsUseCase.getUserTickets(effectiveUserId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TicketDetailsResponse> getTicketDetails(
            @PathVariable UUID id,
            @RequestHeader(value = "X-User-Id", required = false) UUID userIdHeader,
            @RequestParam(value = "userId", required = false) UUID userIdParam) {
        UUID effectiveUserId = SecurityUtils.getEffectiveUserId(userIdHeader != null ? userIdHeader : userIdParam);
        return ResponseEntity.ok(getTicketDetailsUseCase.getTicketDetails(id, effectiveUserId));
    }

    @PostMapping("/issue")
    public ResponseEntity<Map<String, Object>> issueTicket(@Valid @RequestBody IssueTicketRequest request) {
        UUID ticketId = issueTicketUseCase.issueTicket(request.toCommand());
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("ticketId", ticketId));
    }

    @GetMapping("/{id}/dynamic-qr")
    public ResponseEntity<DynamicQrResponse> getDynamicQr(
            @PathVariable UUID id,
            @RequestHeader(value = "X-User-Id", required = false) UUID userIdHeader,
            @RequestParam(value = "userId", required = false) UUID userIdParam) {
        UUID effectiveUserId = SecurityUtils.getEffectiveUserId(userIdHeader != null ? userIdHeader : userIdParam);
        DynamicQrDto dto = generateDynamicQrUseCase.generateDynamicQr(id, effectiveUserId);
        return ResponseEntity.ok(new DynamicQrResponse(
                dto.ticketId(),
                dto.dynamicPayload(),
                dto.expiresAtEpochSeconds(),
                dto.refreshIntervalSeconds()
        ));
    }

    @GetMapping("/{id}/sync")
    public ResponseEntity<TicketSyncResponse> syncTicket(
            @PathVariable UUID id,
            @RequestHeader(value = "X-User-Id", required = false) UUID userIdHeader,
            @RequestParam(value = "userId", required = false) UUID userIdParam) {
        UUID effectiveUserId = SecurityUtils.getEffectiveUserId(userIdHeader != null ? userIdHeader : userIdParam);
        TicketSyncDto dto = syncTicketUseCase.syncTicket(id, effectiveUserId);
        return ResponseEntity.ok(new TicketSyncResponse(
                dto.ticketId(),
                dto.eventId(),
                dto.categoryName(),
                dto.secretKeyBase64(),
                dto.serverTimeEpochSeconds()
        ));
    }

    @PostMapping("/claim")
    public ResponseEntity<ClaimTicketResponse> claimTicket(
            @RequestParam UUID eventId,
            @RequestBody(required = false) ClaimTicketRequest request,
            @RequestHeader(value = "X-User-Id", required = false) UUID userIdHeader,
            @RequestParam(value = "userId", required = false) UUID userIdParam) {
        UUID effectiveUserId = SecurityUtils.getEffectiveUserId(userIdHeader != null ? userIdHeader : userIdParam);
        ClaimTicketRequest effectiveRequest = request != null ? request : new ClaimTicketRequest(null, null, null);
        ClaimTicketResponse response = claimTicketUseCase.claimTicket(effectiveRequest.toCommand(eventId, effectiveUserId));
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
