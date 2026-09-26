package com.ticketing.platform.ticketissuance.infrastructure.persistence.repository;

import com.ticketing.platform.ticketissuance.infrastructure.persistence.entity.TicketJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface SpringDataTicketRepository extends JpaRepository<TicketJpaEntity, UUID> {
    List<TicketJpaEntity> findByUserIdOrderByIssuedAtDesc(UUID userId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = "UPDATE tickets SET status = 'USED', used_at = :usedAt, used_at_gate_id = :gateId " +
            "WHERE id = :ticketId AND status = 'ACTIVE'", nativeQuery = true)
    int markActiveAsUsed(@Param("ticketId") UUID ticketId,
                         @Param("gateId") String gateId,
                         @Param("usedAt") Instant usedAt);
}
