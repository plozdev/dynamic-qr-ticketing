package com.ticketing.platform.shared.event;

import com.ticketing.platform.shared.domain.DomainEvent;

import java.time.Instant;
import java.util.UUID;

/**
 * Integration Event published when a gate check-in attempt is processed.
 * Dispatched across module boundaries to Audit Log and Ticket Issuance (for Mobile SSE push).
 */
public record TicketValidatedIntegrationEvent(
        UUID ticketId,
        UUID userId,
        String gateId,
        String validationStatus,
        String reason,
        Instant occurredAt
) implements DomainEvent {

    public TicketValidatedIntegrationEvent(UUID ticketId, UUID userId, String gateId, String validationStatus, String reason) {
        this(ticketId, userId, gateId, validationStatus, reason, Instant.now());
    }

    public TicketValidatedIntegrationEvent(UUID ticketId, String gateId, String validationStatus, String reason) {
        this(ticketId, null, gateId, validationStatus, reason, Instant.now());
    }

    @Override
    public String eventType() {
        return "TICKET_VALIDATED";
    }
}
