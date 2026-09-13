package com.ticketing.platform.gatevalidator.api.dto;

import java.util.List;
import java.util.UUID;

/**
 * DTO phản hồi danh sách vé hợp lệ trước giờ G cho thiết bị Scanner (Offline Gate Sync).
 * Cho phép thiết bị chạy chế độ Offline Verification khi mạng chập chờn tại cửa soát vé.
 */
public record GateRosterSyncResponse(
        String gateId,
        UUID eventId,
        long syncedAtEpochSeconds,
        int totalTickets,
        List<TicketRosterItem> roster
) {}
