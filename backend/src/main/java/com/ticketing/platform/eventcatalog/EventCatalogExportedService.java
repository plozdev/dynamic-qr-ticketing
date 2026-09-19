package com.ticketing.platform.eventcatalog;

import java.time.Instant;
import java.util.UUID;

/**
 * Public Boundary Interface (SPI) exposed by Event Catalog module.
 * Other bounded contexts (such as ticket-issuance or gate-validator)
 * invoke this contract to query event details without accessing internal module packages.
 */
public interface EventCatalogExportedService {

    boolean isEventActive(UUID eventId);

    void reserveTicket(UUID eventId);

    EventSummaryDto getEventSummary(UUID eventId);

    record EventSummaryDto(
            UUID eventId,
            String name,
            String venueName,
            String status,
            Instant startDateTime,
            Instant endDateTime,
            int checkInWindowMinutes,
            String bannerUrl
    ) {
        public EventSummaryDto(
                UUID eventId,
                String name,
                String venueName,
                String status
        ) {
            this(eventId, name, venueName, status, Instant.now(), Instant.now().plusSeconds(14400), 120, null);
        }
    }
}
