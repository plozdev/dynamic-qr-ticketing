package com.ticketing.platform.ticketissuance.api.dto;

import com.ticketing.platform.ticketissuance.application.dto.IssueTicketCommand;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record IssueTicketRequest(
        @NotNull(message = "Event ID is required")
        UUID eventId,

        UUID userId,

        @NotBlank(message = "Ticket category is required")
        String categoryName
) {
    public IssueTicketCommand toCommand() {
        return new IssueTicketCommand(eventId, userId, categoryName);
    }
}
