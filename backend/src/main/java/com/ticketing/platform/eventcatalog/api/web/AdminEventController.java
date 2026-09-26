package com.ticketing.platform.eventcatalog.api.web;

import com.ticketing.platform.eventcatalog.application.dto.EventResponse;
import com.ticketing.platform.eventcatalog.application.service.EventCatalogService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/events")
@RequiredArgsConstructor
public class AdminEventController {
    private final EventCatalogService events;

    @GetMapping
    public List<EventResponse> listAll() {
        return events.getAllEvents();
    }
}
