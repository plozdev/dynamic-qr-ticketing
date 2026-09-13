package com.ticketing.platform.eventcatalog;

import com.ticketing.platform.shared.domain.DomainEvent;

import java.time.Instant;
import java.util.UUID;

/**
 * Integration Event published when an event status is transitioned to PUBLISHED.
 */
public record EventPublishedIntegrationEvent(
        UUID eventId,
        String eventName,
        Instant occurredAt
) implements DomainEvent {

    public EventPublishedIntegrationEvent(UUID eventId, String eventName) {
        this(eventId, eventName, Instant.now());
    }

    @Override
    public String eventType() {
        return "EVENT_PUBLISHED";
    }
}
