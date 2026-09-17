package com.ticketing.platform.ticketissuance.application.dto;

import java.util.UUID;

public record DynamicQrDto(
        UUID ticketId,
        String dynamicPayload,
        long expiresAtEpochSeconds,
        int refreshIntervalSeconds
) {}
