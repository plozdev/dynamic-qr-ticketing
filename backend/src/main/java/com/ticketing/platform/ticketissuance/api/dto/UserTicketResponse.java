package com.ticketing.platform.ticketissuance.api.dto;

import java.time.Instant;
import java.util.UUID;

/**
 * DTO trả về thông tin chi tiết vé kèm metadata sự kiện và trạng thái check-in cho Mobile App.
 */
public record UserTicketResponse(
        UUID ticketId,
        UUID eventId,
        String eventName,
        String venueName,
        Instant startDateTime,
        Instant endDateTime,
        String categoryName,
        String seatNumber,
        String attendeeName,
        String gateInfo,
        String status, // READY_TO_CHECK_IN, NOT_YET_CHECK_IN, CHECKED_IN, REVOKED
        long checkInOpensAtEpochSeconds,
        boolean isCheckInOpen,
        String checkInNote
) {}
