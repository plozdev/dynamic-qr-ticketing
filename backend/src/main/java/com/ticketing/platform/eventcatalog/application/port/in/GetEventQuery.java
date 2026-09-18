package com.ticketing.platform.eventcatalog.application.port.in;

import com.ticketing.platform.eventcatalog.application.dto.EventResponse;

import com.ticketing.platform.eventcatalog.domain.model.EventStatus;

import java.util.List;
import java.util.UUID;

public interface GetEventQuery {

    EventResponse getEventById(UUID eventId);

    List<EventResponse> getEvents(String category, EventStatus status);
}
