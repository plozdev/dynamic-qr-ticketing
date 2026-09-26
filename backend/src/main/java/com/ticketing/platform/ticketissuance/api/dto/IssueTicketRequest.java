package com.ticketing.platform.ticketissuance.api.dto;

import com.ticketing.platform.ticketissuance.application.dto.IssueTicketCommand;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import java.util.UUID;

public record IssueTicketRequest(
        @NotNull(message = "Event ID is required")
        UUID eventId,

        @NotNull(message = "User ID is required") UUID userId,

        @NotBlank(message = "Ticket category is required")
        String categoryName,
        @Min(1) @Max(10) Integer quantity
) {
    public int effectiveQuantity() { return quantity == null ? 1 : quantity; }
    public IssueTicketCommand toCommand() {
        return new IssueTicketCommand(eventId, userId, categoryName);
    }
}
