package com.ticketing.platform.gatevalidator;

import com.ticketing.platform.shared.domain.DomainEvent;

import java.time.Instant;
import java.util.UUID;

/**
 * Integration Event published when a gate check-in attempt is processed.
 * Listened to by the Audit Log module.
 */
public record TicketValidatedIntegrationEvent(
        UUID ticketId,
        String gateId,
        String validationStatus,
        String reason,
        Instant occurredAt
) implements DomainEvent {

    public TicketValidatedIntegrationEvent(UUID ticketId, String gateId, String validationStatus, String reason) {
        this(ticketId, gateId, validationStatus, reason, Instant.now());
    }

    @Override
    public String eventType() {
        return "TICKET_VALIDATED";
    }
}
