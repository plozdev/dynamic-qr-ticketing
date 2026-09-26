package com.ticketing.platform.ticketissuance.application.service;

import com.ticketing.platform.eventcatalog.EventCatalogExportedService;
import com.ticketing.platform.shared.exception.DomainException;
import com.ticketing.platform.shared.exception.EntityNotFoundException;
import com.ticketing.platform.ticketissuance.TicketIssuedIntegrationEvent;
import com.ticketing.platform.ticketissuance.TicketVerificationExportedService;
import com.ticketing.platform.ticketissuance.application.dto.DynamicQrDto;
import com.ticketing.platform.ticketissuance.application.dto.IssueTicketCommand;
import com.ticketing.platform.ticketissuance.application.dto.TicketSyncDto;
import com.ticketing.platform.ticketissuance.api.dto.UserTicketResponse;
import com.ticketing.platform.ticketissuance.api.dto.TicketDetailsResponse;
import com.ticketing.platform.ticketissuance.application.port.in.GetTicketDetailsUseCase;
import com.ticketing.platform.ticketissuance.application.port.in.GetUserTicketsUseCase;
import com.ticketing.platform.ticketissuance.application.port.in.GenerateDynamicQrUseCase;
import com.ticketing.platform.ticketissuance.application.port.in.IssueTicketUseCase;
import com.ticketing.platform.ticketissuance.application.port.in.SyncTicketUseCase;
import com.ticketing.platform.ticketissuance.application.port.out.QrCryptoPort;
import com.ticketing.platform.ticketissuance.domain.model.Ticket;
import com.ticketing.platform.ticketissuance.domain.model.TicketId;
import com.ticketing.platform.ticketissuance.domain.model.TicketStatus;
import com.ticketing.platform.ticketissuance.domain.repository.TicketRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import com.ticketing.platform.ticketissuance.api.dto.ClaimTicketResponse;
import com.ticketing.platform.ticketissuance.application.dto.ClaimTicketCommand;
import com.ticketing.platform.ticketissuance.application.port.in.ClaimTicketUseCase;
import com.ticketing.platform.user.UserExportedService;

/**
 * Application Service cho Ticket Issuance.
 */
@Service
@Transactional
@RequiredArgsConstructor 
public class TicketIssuanceService implements IssueTicketUseCase, ClaimTicketUseCase, GenerateDynamicQrUseCase, SyncTicketUseCase, GetUserTicketsUseCase, GetTicketDetailsUseCase, TicketVerificationExportedService {

    private final TicketRepository ticketRepository;
    private final EventCatalogExportedService eventCatalogService;
    private final UserExportedService userExportedService;
    private final QrCryptoPort qrCryptoPort;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public UUID issueTicket(IssueTicketCommand command) {
        return issueTickets(command, 1).getFirst();
    }

    @Override
    public List<UUID> issueTickets(IssueTicketCommand command, int quantity) {
        if (quantity < 1 || quantity > 10) {
            throw new DomainException("Quantity must be between 1 and 10");
        }
        if (userExportedService.getUserById(command.userId()).isEmpty()) {
            throw new EntityNotFoundException("User", command.userId());
        }
        List<UUID> ids = new java.util.ArrayList<>(quantity);
        for (int index = 0; index < quantity; index++) {
            ids.add(claimTicket(new ClaimTicketCommand(command.eventId(), command.userId(),
                    command.categoryName(), null, null)).ticketId());
        }
        return ids;
    }

    @Override
    public ClaimTicketResponse claimTicket(ClaimTicketCommand command) {
        if (!eventCatalogService.isEventActive(command.eventId())) {
            throw new DomainException("Cannot claim ticket because event is not active: " + command.eventId());
        }

        // 1. Reserve ticket in Event Catalog (decrements available tickets)
        eventCatalogService.reserveTicket(command.eventId());

        // 2. Resolve attendee name
        String attendeeName = command.attendeeName();
        if (attendeeName == null || attendeeName.isBlank()) {
            attendeeName = userExportedService.getUserById(command.userId())
                    .map(UserExportedService.UserDto::displayName)
                    .orElse("Khán Giả");
        }

        // 3. Resolve category, seat and gate
        var eventSummary = eventCatalogService.getEventSummary(command.eventId());
        String categoryName = command.categoryName() != null && !command.categoryName().isBlank()
                ? command.categoryName()
                : "Standard";

        String seatNumber = command.seatNumber() != null && !command.seatNumber().isBlank()
                ? command.seatNumber()
                : "GA-" + String.format("%03d", (int) (Math.random() * 900 + 100));

        String gateInfo = "CỔNG CHÍNH";

        // 4. Create and save ticket with 256-bit cryptographically secure seed
        Ticket ticket = Ticket.issue(command.eventId(), command.userId(), categoryName, seatNumber, attendeeName, gateInfo);
        ticketRepository.save(ticket);

        // 5. Publish integration event
        eventPublisher.publishEvent(new TicketIssuedIntegrationEvent(
                ticket.getId().value(),
                ticket.getEventId(),
                ticket.getUserId(),
                ticket.getCategoryName()
        ));

        Instant startDateTime = eventSummary.startDateTime();
        int checkInWindowMinutes = eventSummary.checkInWindowMinutes();
        Instant checkInOpensAt = startDateTime.minus(Duration.ofMinutes(checkInWindowMinutes));

        return new ClaimTicketResponse(
                ticket.getId().value(),
                ticket.getEventId(),
                eventSummary.name(),
                eventSummary.venueName(),
                startDateTime,
                ticket.getCategoryName(),
                ticket.getSeatNumber(),
                ticket.getAttendeeName(),
                ticket.getGateInfo(),
                ticket.getSecret().base64Key(),
                ticket.getStatus().name(),
                checkInOpensAt.getEpochSecond()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public DynamicQrDto generateDynamicQr(UUID ticketId, UUID requesterUserId) {
        Ticket ticket = ticketRepository.findById(TicketId.of(ticketId))
                .orElseThrow(() -> new EntityNotFoundException("Ticket", ticketId));

        if (!ticket.getUserId().equals(requesterUserId)) {
            throw new DomainException("Requester is not the owner of this ticket");
        }

        if (ticket.getStatus() != TicketStatus.ACTIVE) {
            throw new DomainException("Ticket is not active: " + ticket.getStatus());
        }

        if (!eventCatalogService.isCheckInOpen(ticket.getEventId(), Instant.now())) {
            throw new DomainException("Check-in is not open for this event");
        }

        long now = Instant.now().getEpochSecond();
        int interval = qrCryptoPort.getValiditySeconds();
        long timeWindow = now / interval;
        long expiresAt = (timeWindow + 1) * interval;

        String token = qrCryptoPort.computeTotpToken(ticket.getId().value(), ticket.getSecret().base64Key(), now);
        String payload = "TICKETING:" + ticket.getId().value() + ":" + expiresAt + ":" + token;

        return new DynamicQrDto(ticket.getId().value(), payload, expiresAt, interval);
    }

    @Override
    @Transactional(readOnly = true)
    public TicketSyncDto syncTicket(UUID ticketId, UUID requesterUserId) {
        Ticket ticket = ticketRepository.findById(TicketId.of(ticketId))
                .orElseThrow(() -> new EntityNotFoundException("Ticket", ticketId));

        if (!ticket.getUserId().equals(requesterUserId)) {
            throw new DomainException("Requester is not the owner of this ticket");
        }

        return new TicketSyncDto(
                ticket.getId().value(),
                ticket.getEventId(),
                ticket.getCategoryName(),
                ticket.getSecret().base64Key(),
                Instant.now().getEpochSecond()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public TicketDetailsResponse getTicketDetails(UUID ticketId, UUID requesterUserId) {
        Ticket ticket = ticketRepository.findById(TicketId.of(ticketId))
                .orElseThrow(() -> new EntityNotFoundException("Ticket", ticketId));

        if (!ticket.getUserId().equals(requesterUserId)) {
            throw new DomainException("Requester is not the owner of this ticket");
        }

        var eventSummary = eventCatalogService.getEventSummary(ticket.getEventId());
        return new TicketDetailsResponse(
                ticket.getId().value(),
                eventSummary.name(),
                eventSummary.venueName() != null ? eventSummary.venueName() : "Chưa xác định",
                eventSummary.startDateTime().getEpochSecond(),
                ticket.getSeatNumber(),
                ticket.getAttendeeName(),
                ticket.getStatus().name(),
                ticket.getSecret().base64Key()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserTicketResponse> getUserTickets(UUID userId) {
        List<Ticket> tickets = ticketRepository.findByUserId(userId);
        Instant now = Instant.now();
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm").withZone(ZoneId.of("Asia/Ho_Chi_Minh"));

        return tickets.stream().map(ticket -> {
            var eventSummary = eventCatalogService.getEventSummary(ticket.getEventId());
            Instant startDateTime = eventSummary.startDateTime();
            Instant endDateTime = eventSummary.endDateTime();
            int checkInWindowMinutes = eventSummary.checkInWindowMinutes();
            Instant checkInOpensAt = startDateTime.minus(Duration.ofMinutes(checkInWindowMinutes));

            boolean isCheckInOpen = eventCatalogService.isCheckInOpen(ticket.getEventId(), now);

            String status;
            String checkInNote;

            if (ticket.getStatus() == TicketStatus.USED) {
                status = "CHECKED_IN";
                checkInNote = "Đã check-in qua cổng " + (ticket.getUsedAtGateId() != null ? ticket.getUsedAtGateId() : ticket.getGateInfo());
            } else if (ticket.getStatus() == TicketStatus.REVOKED) {
                status = "REVOKED";
                checkInNote = "Vé đã bị thu hồi hoặc hủy";
            } else if (isCheckInOpen) {
                status = "READY_TO_CHECK_IN";
                checkInNote = "Đang mở cửa check-in";
            } else if (now.isAfter(endDateTime)) {
                status = "REVOKED";
                checkInNote = "Sự kiện đã kết thúc";
            } else {
                status = "NOT_YET_CHECK_IN";
                checkInNote = !eventSummary.checkInEnabled()
                        ? "Ban tổ chức chưa mở check-in"
                        : "Cổng mở lúc " + timeFormatter.format(checkInOpensAt) + " (chưa thể check-in)";
            }

            return new UserTicketResponse(
                    ticket.getId().value(),
                    ticket.getEventId(),
                    eventSummary.name(),
                    eventSummary.venueName(),
                    startDateTime,
                    endDateTime,
                    ticket.getCategoryName(),
                    ticket.getSeatNumber(),
                    ticket.getAttendeeName(),
                    ticket.getGateInfo(),
                    status,
                    checkInOpensAt.getEpochSecond(),
                    isCheckInOpen,
                    checkInNote
            );
        }).toList();
    }

    // --- Boundary Service (TicketVerificationExportedService implementation) ---

    @Override
    @Transactional(readOnly = true)
    public Optional<TicketVerificationData> getTicketForValidation(UUID ticketId) {
        return ticketRepository.findById(TicketId.of(ticketId))
                .map(ticket -> new TicketVerificationData(
                        ticket.getId().value(),
                        ticket.getUserId(),
                        ticket.getEventId(),
                        ticket.getStatus().name(),
                        ticket.getSecret().base64Key(),
                        ticket.getIssuedAt().getEpochSecond()
                ));
    }

    @Override
    public boolean markTicketAsUsed(UUID ticketId, String gateId) {
        return ticketRepository.markActiveAsUsed(ticketId, gateId, Instant.now());
    }
}
