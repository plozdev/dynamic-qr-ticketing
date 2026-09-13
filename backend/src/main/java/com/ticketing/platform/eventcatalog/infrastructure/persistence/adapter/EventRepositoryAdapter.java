package com.ticketing.platform.eventcatalog.infrastructure.persistence.adapter;

import com.ticketing.platform.eventcatalog.domain.model.Event;
import com.ticketing.platform.eventcatalog.domain.repository.EventRepository;
import com.ticketing.platform.eventcatalog.infrastructure.persistence.repository.SpringDataEventRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

/**
 * Khung sườn Adapter hiện thực Domain Outbound Port EventRepository sử dụng Spring Data JPA.
 */
@Component
public class EventRepositoryAdapter implements EventRepository {

    private final SpringDataEventRepository jpaRepository;

    public EventRepositoryAdapter(SpringDataEventRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Event save(Event event) {
        // TODO: Ánh xạ từ Domain model Event sang EventJpaEntity, lưu qua jpaRepository và map ngược lại
        throw new UnsupportedOperationException("TODO: Bạn tự triển khai save()");
    }

    @Override
    public Optional<Event> findById(UUID id) {
        // TODO: Tìm kiếm theo ID và map sang Domain model Event
        throw new UnsupportedOperationException("TODO: Bạn tự triển khai findById()");
    }

    @Override
    public boolean existsById(UUID id) {
        return jpaRepository.existsById(id);
    }
}
