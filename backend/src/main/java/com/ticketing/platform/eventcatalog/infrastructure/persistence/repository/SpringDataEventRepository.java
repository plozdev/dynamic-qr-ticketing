package com.ticketing.platform.eventcatalog.infrastructure.persistence.repository;

import com.ticketing.platform.eventcatalog.infrastructure.persistence.entity.EventJpaEntity;
import com.ticketing.platform.eventcatalog.domain.model.EventStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SpringDataEventRepository extends JpaRepository<EventJpaEntity, UUID> {
    List<EventJpaEntity> findByStatusOrderByStartDateTimeAsc(EventStatus status);
    List<EventJpaEntity> findAllByOrderByStartDateTimeAsc();
}
