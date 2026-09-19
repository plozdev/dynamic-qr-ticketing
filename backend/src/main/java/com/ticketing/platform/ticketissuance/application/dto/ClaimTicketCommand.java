package com.ticketing.platform.ticketissuance.application.dto;

import java.util.UUID;

public record ClaimTicketCommand(
        UUID eventId,
        UUID userId,
        String categoryName,
        String seatNumber,
        String attendeeName
) {
}
