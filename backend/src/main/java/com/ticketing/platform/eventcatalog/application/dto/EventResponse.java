package com.ticketing.platform.eventcatalog.application.dto;

import java.time.Instant;
import java.util.UUID;

public record EventResponse(
        UUID id,
        String name,
        String description,
        String venueName,
        Instant startDateTime,
        Instant endDateTime,
        String status
) {}
