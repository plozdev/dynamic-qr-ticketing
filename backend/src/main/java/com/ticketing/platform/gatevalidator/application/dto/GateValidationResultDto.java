package com.ticketing.platform.gatevalidator.application.dto;

import java.time.Instant;
import java.util.UUID;

public record GateValidationResultDto(
        UUID ticketId,
        boolean success,
        String status,
        String message,
        Instant validatedAt
) {
    public static GateValidationResultDto granted(UUID ticketId, String message) {
        return new GateValidationResultDto(ticketId, true, "GRANTED", message, Instant.now());
    }

    public static GateValidationResultDto denied(UUID ticketId, String status, String message) {
        return new GateValidationResultDto(ticketId, false, status, message, Instant.now());
    }
}

