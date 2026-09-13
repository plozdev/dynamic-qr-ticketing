package com.ticketing.platform.ticketissuance.infrastructure.persistence.adapter;

import com.ticketing.platform.ticketissuance.domain.model.Ticket;
import com.ticketing.platform.ticketissuance.domain.model.TicketId;
import com.ticketing.platform.ticketissuance.domain.repository.TicketRepository;
import com.ticketing.platform.ticketissuance.infrastructure.persistence.repository.SpringDataTicketRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Khung sườn Adapter hiện thực TicketRepository Port bằng Spring Data JPA.
 */
@Component
public class TicketRepositoryAdapter implements TicketRepository {

    private final SpringDataTicketRepository jpaRepository;

    public TicketRepositoryAdapter(SpringDataTicketRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Ticket save(Ticket ticket) {
        // TODO: Chuyển Ticket domain model sang TicketJpaEntity và lưu vào DB
        throw new UnsupportedOperationException("TODO: Bạn tự triển khai save()");
    }

    @Override
    public Optional<Ticket> findById(TicketId id) {
        // TODO: Tìm kiếm TicketJpaEntity theo id và map sang Ticket domain model
        throw new UnsupportedOperationException("TODO: Bạn tự triển khai findById()");
    }
}
