package com.ticketing.platform.eventcatalog.domain.model;

import java.util.List;
import java.util.UUID;

public record Venue(
        UUID venueId,
        String name,
        String address,
        List<String> entryGates
) {}
