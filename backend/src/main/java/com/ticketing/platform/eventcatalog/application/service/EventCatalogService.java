package com.ticketing.platform.eventcatalog.application.service;

import com.ticketing.platform.eventcatalog.EventCatalogExportedService;
import com.ticketing.platform.eventcatalog.application.dto.CreateEventCommand;
import com.ticketing.platform.eventcatalog.application.dto.EventResponse;
import com.ticketing.platform.eventcatalog.application.port.in.CreateEventUseCase;
import com.ticketing.platform.eventcatalog.application.port.in.GetEventQuery;
import com.ticketing.platform.eventcatalog.EventPublishedIntegrationEvent;
import com.ticketing.platform.eventcatalog.EventCheckInChangedIntegrationEvent;
import com.ticketing.platform.eventcatalog.domain.repository.EventRepository;

import com.ticketing.platform.eventcatalog.domain.model.Event;
import com.ticketing.platform.eventcatalog.domain.model.EventStatus;
import com.ticketing.platform.eventcatalog.domain.model.Venue;
import com.ticketing.platform.shared.exception.DomainException;
import com.ticketing.platform.shared.exception.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.UUID;
import java.time.Instant;

/**
 * Khung sườn Application Service cho Event Catalog.
 * Hiện thực Use Cases và Boundary Interface (SPI) để module khác giao tiếp.
 */
@Service
@Transactional
@RequiredArgsConstructor
public class EventCatalogService implements CreateEventUseCase, GetEventQuery, EventCatalogExportedService {

    private final EventRepository eventRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public EventResponse createEvent(CreateEventCommand command) {
        Venue venue = new Venue(
                UUID.randomUUID(),
                command.venueName(),
                command.venueAddress(),
                command.venueGates() != null ? command.venueGates() : Collections.emptyList()
        );

        Event event = Event.create(
                command.name(),
                command.description(),
                venue,
                command.startDateTime(),
                command.endDateTime()
        );

        event.publish();
        eventRepository.save(event);
        eventPublisher.publishEvent(new EventPublishedIntegrationEvent(event.getId(), event.getName(), java.time.Instant.now()));
        return toResponse(event);
    }

    @Override
    @Transactional(readOnly = true)
    public EventResponse getEventById(UUID eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EntityNotFoundException("Event", eventId));
        return toResponse(event);
    }

    public EventResponse setCheckInEnabled(UUID eventId, boolean enabled) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EntityNotFoundException("Event", eventId));
        if (enabled && Instant.now().isAfter(event.getEndDateTime())) {
            throw new DomainException("Cannot open check-in after event ends");
        }
        event.setCheckInEnabled(enabled);
        eventRepository.save(event);
        eventPublisher.publishEvent(new EventCheckInChangedIntegrationEvent(eventId, enabled, Instant.now()));
        return toResponse(event);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isCheckInOpen(UUID eventId, Instant now) {
        return eventRepository.findById(eventId)
                .map(event -> event.isCheckInOpen(now))
                .orElse(false);
    }

    @Override
    @Transactional(readOnly = true)
    public java.util.List<EventResponse> getEvents(String category, EventStatus status) {
        java.util.List<Event> events;
        if (status != null) {
            events = eventRepository.findAllByStatus(status);
        } else {
            events = eventRepository.findAllByStatus(EventStatus.PUBLISHED);
        }

        if (category != null && !category.isBlank() && !category.equalsIgnoreCase("Tất cả")) {
            events = events.stream()
                    .filter(e -> category.equalsIgnoreCase(e.getCategory()))
                    .toList();
        }

        return events.stream()
                .map(this::toResponse)
                .toList();
    }

    // --- Boundary Service (EventCatalogExportedService implementation) ---

    @Override
    @Transactional(readOnly = true)
    public boolean isEventActive(UUID eventId) {
        return eventRepository.findById(eventId)
                .map(event -> event.getStatus() == EventStatus.PUBLISHED)
                .orElse(false);
    }

    @Override
    public void reserveTicket(UUID eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EntityNotFoundException("Event", eventId));
        if (event.getStatus() != EventStatus.PUBLISHED) {
            throw new DomainException("Cannot reserve ticket for non-published event: " + eventId);
        }
        event.decrementAvailableTickets();
        eventRepository.save(event);
    }

    @Override
    @Transactional(readOnly = true)
    public EventSummaryDto getEventSummary(UUID eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EntityNotFoundException("Event", eventId));
        String venueName = event.getVenue() != null ? event.getVenue().name() : null;
        return new EventSummaryDto(
                event.getId(),
                event.getName(),
                venueName,
                event.getStatus().name(),
                event.getStartDateTime(),
                event.getEndDateTime(),
                event.getCheckInWindowMinutes(),
                event.getBannerUrl(),
                event.isCheckInEnabled()
        );
    }

    private EventResponse toResponse(Event event) {
        return new EventResponse(
                event.getId(),
                event.getName(),
                event.getDescription(),
                event.getVenue() != null ? event.getVenue().name() : null,
                event.getStartDateTime(),
                event.getEndDateTime(),
                event.getStatus().name(),
                event.getBannerUrl(),
                event.getCategory(),
                event.getBasePrice(),
                event.getTotalTickets(),
                event.getAvailableTickets(),
                event.getRemainingPercentage(),
                event.isHotTrend(),
                event.getCheckInWindowMinutes(),
                event.isCheckInEnabled()
        );
    }
}
