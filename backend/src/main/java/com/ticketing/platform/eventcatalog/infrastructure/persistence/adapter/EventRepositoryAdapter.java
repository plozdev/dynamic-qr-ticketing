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
    public boolean existsById(UUID id) {
        return jpaRepository.existsById(id);
    }

    private EventJpaEntity toEntity(Event event) {
        Venue v = event.getVenue();
        return EventJpaEntity.builder()
                    .id(event.getId())
                    .name(event.getName())
                    .description(event.getDescription())
                    .venueName(v != null ? v.name() : null)
                    .venueAddress(v != null ? v.address() : null)
                    .startDateTime(event.getStartDateTime())
                    .endDateTime(event.getEndDateTime())
                    .status(event.getStatus())
                    .build();
    }

    private Event toDomain(EventJpaEntity entity) {
        Venue venue = null;
        if (entity.getVenueName() != null || entity.getVenueAddress() != null) {
            venue = new Venue(null, entity.getVenueName(), entity.getVenueAddress(), Collections.emptyList());
        }
        return new Event(
                entity.getId(),
                entity.getName(),
                entity.getDescription(),
                venue,
                entity.getStartDateTime(),
                entity.getEndDateTime(),
                entity.getStatus(),
                Collections.emptyList()
        );
    }
}
