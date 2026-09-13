package com.ticketing.platform.gatevalidator.application.dto;

import java.time.Instant;
import java.util.UUID;

public record GateValidationResultDto(
        UUID ticketId,
        boolean success,
        String status,
        String message,
        Instant validatedAt
) {}
