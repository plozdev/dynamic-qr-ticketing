package com.ticketing.platform.ticketissuance.api.dto;

import java.time.Instant;
import java.util.UUID;

public record ClaimTicketResponse(
        UUID ticketId,
        UUID eventId,
        String eventName,
        String venueName,
        Instant startDateTime,
        String categoryName,
        String seatNumber,
        String attendeeName,
        String gateInfo,
        String secretKeyBase64,
        String status,
        long checkInOpensAtEpochSeconds
) {
}
