package com.ticketing.platform.ticketissuance.application.dto;

import java.util.UUID;

public record TicketSyncDto(
        UUID ticketId,
        UUID eventId,
        String categoryName,
        String secretKeyBase64,
        long serverTimeEpochSeconds
) {}
