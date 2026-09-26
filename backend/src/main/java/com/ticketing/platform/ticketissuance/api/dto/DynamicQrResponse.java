package com.ticketing.platform.ticketissuance.api.dto;

import java.util.UUID;

public record DynamicQrResponse(
        UUID ticketId,
        String dynamicPayload,
        long expiresAtEpochSeconds,
        int refreshIntervalSeconds
) {}
