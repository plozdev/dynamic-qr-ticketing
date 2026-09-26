package com.ticketing.platform.eventcatalog.application.port.in;

import com.ticketing.platform.eventcatalog.application.dto.CreateEventCommand;
import com.ticketing.platform.eventcatalog.application.dto.EventResponse;

public interface CreateEventUseCase {

    EventResponse createEvent(CreateEventCommand command);
}
