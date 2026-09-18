package com.ticketing.platform.ticketissuance.infrastructure.persistence.repository;

import com.ticketing.platform.ticketissuance.infrastructure.persistence.entity.TicketJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SpringDataTicketRepository extends JpaRepository<TicketJpaEntity, UUID> {
    List<TicketJpaEntity> findByUserIdOrderByIssuedAtDesc(UUID userId);
}
