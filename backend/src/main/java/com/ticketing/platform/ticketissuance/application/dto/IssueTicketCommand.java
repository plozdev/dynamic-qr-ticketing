package com.ticketing.platform.ticketissuance.application.dto;

import java.util.UUID;

public record IssueTicketCommand(
        UUID eventId,
        UUID userId,
        String categoryName
) {}
