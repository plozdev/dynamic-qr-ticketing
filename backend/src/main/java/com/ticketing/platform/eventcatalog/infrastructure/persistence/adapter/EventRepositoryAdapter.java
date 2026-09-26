package com.ticketing.platform.eventcatalog.infrastructure.persistence.adapter;

import com.ticketing.platform.eventcatalog.domain.model.Event;
import com.ticketing.platform.eventcatalog.domain.model.Venue;
import com.ticketing.platform.eventcatalog.domain.repository.EventRepository;
import com.ticketing.platform.eventcatalog.infrastructure.persistence.entity.EventJpaEntity;
import com.ticketing.platform.eventcatalog.infrastructure.persistence.repository.SpringDataEventRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

/**
 * Khung sườn Adapter hiện thực Domain Outbound Port EventRepository sử dụng Spring Data JPA.
 */
@Component
@RequiredArgsConstructor 
public class EventRepositoryAdapter implements EventRepository {

    private final SpringDataEventRepository jpaRepository;

    @Override
    public void save(Event event) {
        jpaRepository.save(toEntity(event));
    }

    @Override
    public Optional<Event> findById(UUID id) {
        return jpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public boolean reservePublishedTicket(UUID eventId) {
        return jpaRepository.reservePublishedTicket(eventId) == 1;
    }

    @Override
    public boolean existsById(UUID id) {
        return jpaRepository.existsById(id);
    }

    @Override
    public java.util.List<Event> findAll() {
        return jpaRepository.findAllByOrderByStartDateTimeAsc().stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public java.util.List<Event> findAllByStatus(com.ticketing.platform.eventcatalog.domain.model.EventStatus status) {
        return jpaRepository.findByStatusOrderByStartDateTimeAsc(status).stream()
                .map(this::toDomain)
                .toList();
    }

    private EventJpaEntity toEntity(Event event) {
        Venue v = event.getVenue();
        return EventJpaEntity.builder()
                    .id(event.getId())
                    .name(event.getName())
                    .description(event.getDescription())
                    .venueName(v != null ? v.name() : null)
                    .venueAddress(v != null ? v.address() : null)
                    .venueGates(v != null && v.entryGates() != null ? v.entryGates() : Collections.emptyList())
                    .startDateTime(event.getStartDateTime())
                    .endDateTime(event.getEndDateTime())
                    .status(event.getStatus())
                    .bannerUrl(event.getBannerUrl())
                    .category(event.getCategory())
                    .basePrice(event.getBasePrice())
                    .totalTickets(event.getTotalTickets())
                    .availableTickets(event.getAvailableTickets())
                    .isHotTrend(event.isHotTrend())
                    .checkInWindowMinutes(event.getCheckInWindowMinutes())
                    .checkInEnabled(event.isCheckInEnabled())
                    .build();
    }

    private Event toDomain(EventJpaEntity entity) {
        Venue venue = null;
        if (entity.getVenueName() != null || entity.getVenueAddress() != null) {
            venue = new Venue(null, entity.getVenueName(), entity.getVenueAddress(),
                    entity.getVenueGates() != null ? entity.getVenueGates() : Collections.emptyList());
        }
        Event event = new Event(
                entity.getId(),
                entity.getName(),
                entity.getDescription(),
                venue,
                entity.getStartDateTime(),
                entity.getEndDateTime(),
                entity.getStatus(),
                Collections.emptyList(),
                entity.getBannerUrl(),
                entity.getCategory(),
                entity.getBasePrice(),
                entity.getTotalTickets(),
                entity.getAvailableTickets(),
                entity.isHotTrend(),
                entity.getCheckInWindowMinutes()
        );
        event.setCheckInEnabled(entity.isCheckInEnabled());
        return event;
    }
}
