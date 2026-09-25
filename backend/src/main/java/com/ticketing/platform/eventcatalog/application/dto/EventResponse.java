package com.ticketing.platform.eventcatalog.application.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record EventResponse(
        UUID id,
        String name,
        String description,
        String venueName,
        java.util.List<String> venueGates,
        Instant startDateTime,
        Instant endDateTime,
        String status,
        String bannerUrl,
        String category,
        BigDecimal basePrice,
        int totalTickets,
        int availableTickets,
        int remainingPercentage,
        boolean isHotTrend,
        int checkInWindowMinutes,
        boolean checkInEnabled
) {
    public EventResponse(
            UUID id,
            String name,
            String description,
            String venueName,
            Instant startDateTime,
            Instant endDateTime,
            String status
    ) {
        this(id, name, description, venueName, java.util.List.of(), startDateTime, endDateTime, status,
                null, "Âm nhạc & Concert", new BigDecimal("450000"), 1000, 850, 85, false, 120, false);
    }
}
