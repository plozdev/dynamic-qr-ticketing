package com.ticketing.platform.gatevalidator.domain.model;

import java.util.UUID;

public record AccessGate(
        String gateId,
        UUID eventId,
        String zoneName
) {}
