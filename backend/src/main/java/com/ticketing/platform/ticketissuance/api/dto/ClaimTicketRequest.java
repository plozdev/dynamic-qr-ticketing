package com.ticketing.platform.ticketissuance.api.dto;

import com.ticketing.platform.ticketissuance.application.dto.ClaimTicketCommand;

import java.util.UUID;

public record ClaimTicketRequest(
        String categoryName,
        String seatNumber,
        String attendeeName
) {
    public ClaimTicketCommand toCommand(UUID eventId, UUID userId) {
        return new ClaimTicketCommand(eventId, userId, categoryName, seatNumber, attendeeName);
    }
}
