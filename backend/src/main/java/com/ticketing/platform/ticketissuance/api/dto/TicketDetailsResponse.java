package com.ticketing.platform.ticketissuance.api.dto;

import java.util.UUID;

/**
 * Chi tiết vé đầy đủ trả về cho Mobile Client, bao gồm Secret Key để sinh Dynamic QR Offline.
 */
public record TicketDetailsResponse(
        UUID ticketId,
        String eventTitle,
        String location,
        long eventEpochSeconds,
        String seatCode,
        String customerFullName,
        String statusCode,
        String secretKey
) {}
