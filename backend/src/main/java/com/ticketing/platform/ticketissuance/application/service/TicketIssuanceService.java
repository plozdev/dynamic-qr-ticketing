package com.ticketing.platform.ticketissuance.application.service;

import com.ticketing.platform.eventcatalog.EventCatalogExportedService;
import com.ticketing.platform.shared.exception.DomainException;
import com.ticketing.platform.shared.exception.EntityNotFoundException;
import com.ticketing.platform.ticketissuance.TicketIssuedIntegrationEvent;
import com.ticketing.platform.ticketissuance.TicketVerificationExportedService;
import com.ticketing.platform.ticketissuance.application.dto.DynamicQrDto;
import com.ticketing.platform.ticketissuance.application.dto.IssueTicketCommand;
import com.ticketing.platform.ticketissuance.application.dto.TicketSyncDto;
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

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

/**
 * Khung sườn Application Service cho Ticket Issuance.
 * Bạn tự triển khai logic cấp phát vé, sinh mã động và liên kết sự kiện.
 */
@Service
@Transactional
@RequiredArgsConstructor 
public class TicketIssuanceService implements IssueTicketUseCase, GenerateDynamicQrUseCase, SyncTicketUseCase, TicketVerificationExportedService {

    private final TicketRepository ticketRepository;
    private final EventCatalogExportedService eventCatalogService;
    private final QrCryptoPort qrCryptoPort;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public UUID issueTicket(IssueTicketCommand command) {
        if (!eventCatalogService.isEventActive(command.eventId())) {
            throw new DomainException("Cannot issue ticket because event is not active: " + command.eventId());
        }

        Ticket ticket = Ticket.issue(command.eventId(), command.userId(), command.categoryName());
        ticketRepository.save(ticket);

        eventPublisher.publishEvent(new TicketIssuedIntegrationEvent(
                ticket.getId().value(),
                ticket.getEventId(),
                ticket.getUserId(),
                ticket.getCategoryName()
        ));

        return ticket.getId().value();
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

    // --- Boundary Service (TicketVerificationExportedService implementation) ---

    @Override
    @Transactional(readOnly = true)
    public Optional<TicketVerificationData> getTicketForValidation(UUID ticketId) {
        return ticketRepository.findById(TicketId.of(ticketId))
                .map(ticket -> new TicketVerificationData(
                        ticket.getId().value(),
                        ticket.getEventId(),
                        ticket.getStatus().name(),
                        ticket.getSecret().base64Key(),
                        ticket.getIssuedAt().getEpochSecond()
                ));
    }

    @Override
    public void markTicketAsUsed(UUID ticketId, String gateId) {
        Ticket ticket = ticketRepository.findById(TicketId.of(ticketId))
                .orElseThrow(() -> new EntityNotFoundException("Ticket", ticketId));

        ticket.markAsUsed(gateId);
        ticketRepository.save(ticket);
    }
}
