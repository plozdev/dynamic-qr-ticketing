package com.ticketing.platform.eventcatalog.domain.model;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EventCheckInTest {

    @Test
    void adminCanOpenAndCloseCheckInBeforeScheduledWindow() {
        Instant now = Instant.now();
        Event event = new Event(UUID.randomUUID(), "Concert", "", null,
                now.plus(Duration.ofDays(7)), now.plus(Duration.ofDays(8)),
                EventStatus.PUBLISHED, List.of());

        assertFalse(event.isCheckInOpen(now));
        event.setCheckInEnabled(true);
        assertTrue(event.isCheckInOpen(now));
        event.setCheckInEnabled(false);
        assertFalse(event.isCheckInOpen(now));
    }

    @Test
    void checkInCannotStayOpenAfterEventEnds() {
        Instant now = Instant.now();
        Event event = new Event(UUID.randomUUID(), "Concert", "", null,
                now.minus(Duration.ofDays(2)), now.minus(Duration.ofDays(1)),
                EventStatus.PUBLISHED, List.of());

        event.setCheckInEnabled(true);
        assertFalse(event.isCheckInOpen(now));
    }
}
