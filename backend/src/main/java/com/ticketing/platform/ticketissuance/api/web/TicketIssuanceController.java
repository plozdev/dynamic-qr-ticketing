package com.ticketing.platform.ticketissuance.api.web;

import com.ticketing.platform.shared.security.SecurityUtils;
import com.ticketing.platform.shared.security.UserPrincipal;
import com.ticketing.platform.ticketissuance.api.dto.ClaimTicketRequest;
import com.ticketing.platform.ticketissuance.api.dto.ClaimTicketResponse;
import com.ticketing.platform.ticketissuance.api.dto.DynamicQrResponse;
import com.ticketing.platform.ticketissuance.api.dto.IssueTicketRequest;
import com.ticketing.platform.ticketissuance.api.dto.TicketDetailsResponse;
import com.ticketing.platform.ticketissuance.api.dto.TicketSyncResponse;
import com.ticketing.platform.ticketissuance.api.dto.UserTicketResponse;
import com.ticketing.platform.ticketissuance.application.dto.DynamicQrDto;
import com.ticketing.platform.ticketissuance.application.dto.TicketSyncDto;
import com.ticketing.platform.ticketissuance.application.port.in.ClaimTicketUseCase;
import com.ticketing.platform.ticketissuance.application.port.in.GetTicketDetailsUseCase;
import com.ticketing.platform.ticketissuance.application.port.in.GetUserTicketsUseCase;
import com.ticketing.platform.ticketissuance.application.port.in.GenerateDynamicQrUseCase;
import com.ticketing.platform.ticketissuance.application.port.in.IssueTicketUseCase;
import com.ticketing.platform.ticketissuance.application.port.in.SyncTicketUseCase;
import com.ticketing.platform.ticketissuance.infrastructure.sse.TicketSseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * REST Controller cho Ticket Issuance và luồng đẩy sự kiện thời gian thực (SSE) cho Mobile.
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
    private final TicketSseService ticketSseService;

    @GetMapping
    public ResponseEntity<List<UserTicketResponse>> getMyTickets(
            @RequestParam(value = "userId", required = false) UUID userIdParam) {
        UUID effectiveUserId = resolveUserId(userIdParam);
        return ResponseEntity.ok(getUserTicketsUseCase.getUserTickets(effectiveUserId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TicketDetailsResponse> getTicketDetails(
            @PathVariable UUID id,
            @RequestParam(value = "userId", required = false) UUID userIdParam) {
        UUID effectiveUserId = resolveUserId(userIdParam);
        return ResponseEntity.ok(getTicketDetailsUseCase.getTicketDetails(id, effectiveUserId));
    }

    @PostMapping("/issue")
    public ResponseEntity<Map<String, Object>> issueTicket(@Valid @RequestBody IssueTicketRequest request) {
        UUID ticketId = issueTicketUseCase.issueTicket(new com.ticketing.platform.ticketissuance.application.dto.IssueTicketCommand(
                request.eventId(), request.userId(), request.categoryName()));
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("ticketId", ticketId));
    }

    @GetMapping("/{id}/dynamic-qr")
    public ResponseEntity<DynamicQrResponse> getDynamicQr(
            @PathVariable UUID id,
            @RequestParam(value = "userId", required = false) UUID userIdParam) {
        UUID effectiveUserId = resolveUserId(userIdParam);
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
            @RequestParam(value = "userId", required = false) UUID userIdParam) {
        UUID effectiveUserId = resolveUserId(userIdParam);
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
            @RequestParam(value = "userId", required = false) UUID userIdParam,
            @RequestBody(required = false) ClaimTicketRequest request) {
        UUID effectiveUserId = resolveUserId(userIdParam);
        ClaimTicketRequest effectiveRequest = request != null ? request : new ClaimTicketRequest(null, null, null);
        ClaimTicketResponse response = claimTicketUseCase.claimTicket(effectiveRequest.toCommand(eventId, effectiveUserId));
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Kênh SSE đẩy trạng thái vé thời gian thực cho ứng dụng Mobile của người dùng.
     * Khi vé được soát thành công tại cổng, backend đẩy event 'ticket-update' để mobile
     * tự động làm mới trạng thái vé thành CHECKED_IN.
     */
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamUserTickets(
            @RequestParam(value = "userId", required = false) UUID userIdParam,
            @RequestParam(value = "ticketId", required = false) UUID ticketIdParam) {
        UUID effectiveUserId = resolveUserId(userIdParam);
        if (ticketIdParam != null) {
            getTicketDetailsUseCase.getTicketDetails(ticketIdParam, effectiveUserId);
        }
        return ticketSseService.createEmitter(effectiveUserId, ticketIdParam);
    }

    /**
     * Kênh SSE cho một vé cụ thể (khi mobile đang mở màn hình chi tiết vé / dynamic QR).
     */
    @GetMapping(value = "/{id}/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamSingleTicket(
            @PathVariable UUID id,
            @RequestParam(value = "userId", required = false) UUID userIdParam) {
        UUID effectiveUserId = resolveUserId(userIdParam);
        getTicketDetailsUseCase.getTicketDetails(id, effectiveUserId);
        return ticketSseService.createEmitter(effectiveUserId, id);
    }

    private UUID resolveUserId(UUID fallbackUserId) {
        UUID authenticatedUserId = SecurityUtils.getCurrentPrincipal()
                .map(UserPrincipal::userId)
                .orElseThrow(() -> new AccessDeniedException("Authentication required"));
        if (fallbackUserId != null && !fallbackUserId.equals(authenticatedUserId)) {
            throw new AccessDeniedException("Cannot access another user's tickets");
        }
        return authenticatedUserId;
    }
}
