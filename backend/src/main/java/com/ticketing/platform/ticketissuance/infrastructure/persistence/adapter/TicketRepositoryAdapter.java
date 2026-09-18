package com.ticketing.platform.ticketissuance.infrastructure.persistence.adapter;

import com.ticketing.platform.ticketissuance.domain.model.Ticket;
import com.ticketing.platform.ticketissuance.domain.model.TicketId;
import com.ticketing.platform.ticketissuance.domain.model.TicketSecret;
import com.ticketing.platform.ticketissuance.domain.repository.TicketRepository;
import com.ticketing.platform.ticketissuance.infrastructure.persistence.entity.TicketJpaEntity;
import com.ticketing.platform.ticketissuance.infrastructure.persistence.repository.SpringDataTicketRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Khung sườn Adapter hiện thực TicketRepository Port bằng Spring Data JPA.
 */
@Component
@RequiredArgsConstructor 
public class TicketRepositoryAdapter implements TicketRepository {

    private final SpringDataTicketRepository jpaRepository;

    @Override
    public void save(Ticket ticket) {
        jpaRepository.save(toEntity(ticket));
    }

    @Override
    public Optional<Ticket> findById(TicketId id) {
        return jpaRepository.findById(id.value()).map(this::toDomain);
    }

    @Override
    public java.util.List<Ticket> findByUserId(java.util.UUID userId) {
        return jpaRepository.findByUserIdOrderByIssuedAtDesc(userId).stream()
                .map(this::toDomain)
                .toList();
    }

    private TicketJpaEntity toEntity(Ticket ticket) {
        return TicketJpaEntity.builder()
                .id(ticket.getId().value())
                .eventId(ticket.getEventId())
                .userId(ticket.getUserId())
                .categoryName(ticket.getCategoryName())
                .secretKey(ticket.getSecret().base64Key())
                .status(ticket.getStatus())
                .issuedAt(ticket.getIssuedAt())
                .usedAt(ticket.getUsedAt())
                .usedAtGateId(ticket.getUsedAtGateId())
                .seatNumber(ticket.getSeatNumber())
                .attendeeName(ticket.getAttendeeName())
                .gateInfo(ticket.getGateInfo())
                .build();
    }
    
    private Ticket toDomain(TicketJpaEntity t) {
        return new Ticket(
                TicketId.of(t.getId()),
                t.getEventId(),
                t.getUserId(),
                t.getCategoryName(),
                TicketSecret.of(t.getSecretKey()),
                t.getStatus(),
                t.getIssuedAt(),
                t.getUsedAt(),
                t.getUsedAtGateId(),
                t.getSeatNumber(),
                t.getAttendeeName(),
                t.getGateInfo()
        );
    }
}
