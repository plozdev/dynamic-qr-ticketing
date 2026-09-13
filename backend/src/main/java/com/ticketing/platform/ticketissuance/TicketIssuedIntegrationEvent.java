package com.ticketing.platform.ticketissuance;

import com.ticketing.platform.shared.domain.DomainEvent;

import java.time.Instant;
import java.util.UUID;

/**
 * Integration Event published when a new ticket has been issued.
 * Listened to by the Audit Log module.
 */
public record TicketIssuedIntegrationEvent(
        UUID ticketId,
        UUID eventId,
        UUID userId,
        String ticketCategory,
        Instant occurredAt
) implements DomainEvent {

    public TicketIssuedIntegrationEvent(UUID ticketId, UUID eventId, UUID userId, String ticketCategory) {
        this(ticketId, eventId, userId, ticketCategory, Instant.now());
    }

    @Override
    public String eventType() {
        return "TICKET_ISSUED";
    }
}
