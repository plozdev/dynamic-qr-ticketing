package com.ticketing.platform.shared.domain;

import java.time.Instant;

/**
 * Base contract for all Domain and Integration Events across bounded contexts.
 */
public interface DomainEvent {

    Instant occurredAt();

    String eventType();
}
