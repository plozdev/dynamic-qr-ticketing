package com.ticketing.platform.eventcatalog.infrastructure.persistence.repository;

import com.ticketing.platform.eventcatalog.infrastructure.persistence.entity.EventJpaEntity;
import com.ticketing.platform.eventcatalog.domain.model.EventStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SpringDataEventRepository extends JpaRepository<EventJpaEntity, UUID> {
    List<EventJpaEntity> findByStatusOrderByStartDateTimeAsc(EventStatus status);
    List<EventJpaEntity> findAllByOrderByStartDateTimeAsc();

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = "UPDATE events SET available_tickets = available_tickets - 1 " +
            "WHERE id = :eventId AND status = 'PUBLISHED' AND available_tickets > 0", nativeQuery = true)
    int reservePublishedTicket(@Param("eventId") UUID eventId);
}
