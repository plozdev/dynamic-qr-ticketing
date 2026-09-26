package com.ticketing.platform.ticketissuance.domain.model;

import java.util.UUID;

public record TicketId(UUID value) {

    public static TicketId generate() {
        return new TicketId(UUID.randomUUID());
    }

    public static TicketId of(UUID value) {
        return new TicketId(value);
    }
}
