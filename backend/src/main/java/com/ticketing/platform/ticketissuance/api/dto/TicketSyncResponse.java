package com.ticketing.platform.ticketissuance.api.dto;

import java.util.UUID;

/**
 * DTO dành riêng cho Mobile Client (Android Compose + C++ NDK) trong luồng Key Provisioning.
 * Cung cấp khóa bí mật (Secret Seed) và thời gian máy chủ để Mobile tự sinh Dynamic QR offline
 * và tính toán độ lệch đồng hồ (Clock Drift).
 */
public record TicketSyncResponse(
        UUID ticketId,
        UUID eventId,
        String categoryName,
        String secretKeyBase64,
        long serverTimeEpochSeconds
) {}
