package com.ticketing.platform.gatevalidator.domain.model;

import java.time.Instant;
import java.util.UUID;

public record ValidationResult(
        UUID ticketId,
        ValidationStatus status,
        String message,
        Instant validatedAt
) {
    public static ValidationResult granted(UUID ticketId) {
        return new ValidationResult(ticketId, ValidationStatus.GRANTED, "Access granted", Instant.now());
    }

    public static ValidationResult denied(UUID ticketId, ValidationStatus status, String message) {
        return new ValidationResult(ticketId, status, message, Instant.now());
    }

    public boolean isSuccess() {
        return status == ValidationStatus.GRANTED;
    }
}
