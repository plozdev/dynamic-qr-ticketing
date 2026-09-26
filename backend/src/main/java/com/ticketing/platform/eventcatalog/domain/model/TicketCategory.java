package com.ticketing.platform.eventcatalog.domain.model;

import java.math.BigDecimal;
import java.util.UUID;

public record TicketCategory(
        UUID categoryId,
        String name,
        BigDecimal price,
        int totalQuota,
        int availableQuota
) {}
