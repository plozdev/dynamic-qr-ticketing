package com.ticketing.platform.eventcatalog.domain.repository;

import com.ticketing.platform.eventcatalog.domain.model.Event;

import java.util.Optional;
import java.util.UUID;

/**
 * Domain Outbound Port for Event persistence.
 */
public interface EventRepository {

    void save(Event event);

    Optional<Event> findById(UUID id);

    boolean existsById(UUID id);

    java.util.List<Event> findAll();

    java.util.List<Event> findAllByStatus(com.ticketing.platform.eventcatalog.domain.model.EventStatus status);
}
