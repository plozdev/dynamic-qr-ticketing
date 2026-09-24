package com.ticketing.platform.eventcatalog;

import com.ticketing.platform.shared.domain.DomainEvent;
import java.time.Instant;
import java.util.UUID;

public record EventCheckInChangedIntegrationEvent(UUID eventId, boolean enabled, Instant occurredAt)
        implements DomainEvent {
    @Override
    public String eventType() {
        return "EVENT_CHECK_IN_CHANGED";
    }
}
