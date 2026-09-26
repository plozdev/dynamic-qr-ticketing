package com.ticketing.platform.gatevalidator.api.dto;

import java.util.UUID;

/**
 * Bản ghi thông tin vé trong danh sách đồng bộ ngoại tuyến cho máy quét (Offline Scanner Roster).
 */
public record TicketRosterItem(
        UUID ticketId,
        String secretKeyBase64,
        String categoryName,
        String status
) {}
