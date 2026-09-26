package com.ticketing.platform.ticketissuance.domain.repository;

import com.ticketing.platform.ticketissuance.domain.model.Ticket;
import com.ticketing.platform.ticketissuance.domain.model.TicketId;

import java.util.Optional;
import java.time.Instant;
import java.util.UUID;

/**
 * Domain Outbound Port for Ticket persistence.
 */
public interface TicketRepository {

    void save(Ticket ticket);

    Optional<Ticket> findById(TicketId id);

    boolean markActiveAsUsed(UUID ticketId, String gateId, Instant usedAt);

    java.util.List<Ticket> findByUserId(java.util.UUID userId);

    java.util.List<Ticket> findAll();
}
