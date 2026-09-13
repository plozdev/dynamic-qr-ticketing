package com.ticketing.platform.eventcatalog.api.web;

import com.ticketing.platform.eventcatalog.api.dto.CreateEventRequest;
import com.ticketing.platform.eventcatalog.application.dto.EventResponse;
import com.ticketing.platform.eventcatalog.application.port.in.CreateEventUseCase;
import com.ticketing.platform.eventcatalog.application.port.in.GetEventQuery;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * Khung sườn REST Controller cho Event Catalog.
 */
@RestController
@RequestMapping("/api/v1/events")
public class EventController {

    private final CreateEventUseCase createEventUseCase;
    private final GetEventQuery getEventQuery;

    public EventController(CreateEventUseCase createEventUseCase, GetEventQuery getEventQuery) {
        this.createEventUseCase = createEventUseCase;
        this.getEventQuery = getEventQuery;
    }

    @PostMapping
    public ResponseEntity<EventResponse> createEvent(@Valid @RequestBody CreateEventRequest request) {
        // TODO: Chuyển CreateEventRequest sang CreateEventCommand, gọi createEventUseCase và trả về 201 Created
        throw new UnsupportedOperationException("TODO: Bạn tự triển khai endpoint POST /api/v1/events");
    }

    @GetMapping("/{id}")
    public ResponseEntity<EventResponse> getEventById(@PathVariable UUID id) {
        // TODO: Gọi getEventQuery và trả về 200 OK
        throw new UnsupportedOperationException("TODO: Bạn tự triển khai endpoint GET /api/v1/events/{id}");
    }
}
