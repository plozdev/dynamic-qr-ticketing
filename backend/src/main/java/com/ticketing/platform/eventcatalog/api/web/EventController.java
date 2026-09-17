package com.ticketing.platform.eventcatalog.api.web;

import com.ticketing.platform.eventcatalog.api.dto.CreateEventRequest;
import com.ticketing.platform.eventcatalog.application.dto.EventResponse;
import com.ticketing.platform.eventcatalog.application.port.in.CreateEventUseCase;
import com.ticketing.platform.eventcatalog.application.port.in.GetEventQuery;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
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
@RequiredArgsConstructor 
public class EventController {

    private final CreateEventUseCase createEventUseCase;
    private final GetEventQuery getEventQuery;

    @PostMapping
    public ResponseEntity<EventResponse> createEvent(@Valid @RequestBody CreateEventRequest request) {
        EventResponse res = createEventUseCase.createEvent(request.toCommand());
        return ResponseEntity.status(HttpStatus.CREATED).body(res);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EventResponse> getEventById(@PathVariable UUID id) {
        return ResponseEntity.status(HttpStatus.OK)
                            .body(getEventQuery.getEventById(id));
    }
}
