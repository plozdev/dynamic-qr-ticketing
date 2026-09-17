package com.ticketing.platform.eventcatalog.infrastructure.persistence.repository;

import com.ticketing.platform.eventcatalog.infrastructure.persistence.entity.EventJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface SpringDataEventRepository extends JpaRepository<EventJpaEntity, UUID> {
}
