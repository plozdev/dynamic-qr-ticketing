package com.ticketing.platform.eventcatalog.application.dto;

import java.time.Instant;
import java.util.List;

public record CreateEventCommand(
        String name,
        String description,
        String venueName,
        String venueAddress,
        List<String> venueGates,
        Instant startDateTime,
        Instant endDateTime
) {}
