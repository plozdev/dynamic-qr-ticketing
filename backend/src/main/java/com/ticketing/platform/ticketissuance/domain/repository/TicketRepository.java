package com.ticketing.platform.ticketissuance.domain.repository;

import com.ticketing.platform.ticketissuance.domain.model.Ticket;
import com.ticketing.platform.ticketissuance.domain.model.TicketId;

import java.util.Optional;

/**
 * Domain Outbound Port for Ticket persistence.
 */
public interface TicketRepository {

    void save(Ticket ticket);

    Optional<Ticket> findById(TicketId id);

    java.util.List<Ticket> findByUserId(java.util.UUID userId);
}
