package com.ticketing.platform.eventcatalog.application.port.in;

import com.ticketing.platform.eventcatalog.application.dto.EventResponse;

import java.util.UUID;

public interface GetEventQuery {

    EventResponse getEventById(UUID eventId);
}
