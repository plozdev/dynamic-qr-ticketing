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
    public Event save(Event event) {
        EventJpaEntity entity = toEntity(event);
        EventJpaEntity savedEntity = jpaRepository.save(entity);
        return toDomain(savedEntity);
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
        EventJpaEntity entity = new EventJpaEntity();
        entity.setId(event.getId());
        entity.setName(event.getName());
        entity.setDescription(event.getDescription());
        if (event.getVenue() != null) {
            entity.setVenueName(event.getVenue().name());
            entity.setVenueAddress(event.getVenue().address());
        }
        entity.setStartDateTime(event.getStartDateTime());
        entity.setEndDateTime(event.getEndDateTime());
        entity.setStatus(event.getStatus());
        return entity;
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
