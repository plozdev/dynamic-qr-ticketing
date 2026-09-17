package com.ticketing.platform.ticketissuance.domain.model;

import com.ticketing.platform.shared.domain.AggregateRoot;
import com.ticketing.platform.shared.exception.DomainException;

import java.time.Instant;
import java.util.UUID;

/**
 * Khung sườn Aggregate Root Ticket trong Ticket Issuance Bounded Context.
 * Bạn tự triển khai các quy tắc nghiệp vụ (invariants, state transitions) tại đây.
 */
public class Ticket implements AggregateRoot<TicketId> {

    private final TicketId id;
    private final UUID eventId;
    private final UUID userId;
    private final String categoryName;
    private final TicketSecret secret;
    private TicketStatus status;
    private final Instant issuedAt;
    private Instant usedAt;
    private String usedAtGateId;

    public Ticket(TicketId id, UUID eventId, UUID userId, String categoryName,
                  TicketSecret secret, TicketStatus status, Instant issuedAt,
                  Instant usedAt, String usedAtGateId) {
        this.id = id != null ? id : TicketId.generate();
        this.eventId = eventId;
        this.userId = userId;
        this.categoryName = categoryName;
        this.secret = secret != null ? secret : TicketSecret.generate();
        this.status = status != null ? status : TicketStatus.ACTIVE;
        this.issuedAt = issuedAt != null ? issuedAt : Instant.now();
        this.usedAt = usedAt;
        this.usedAtGateId = usedAtGateId;
    }

    public static Ticket issue(UUID eventId, UUID userId, String categoryName) {
        return new Ticket(TicketId.generate(), eventId, userId, categoryName,
                TicketSecret.generate(), TicketStatus.ACTIVE, Instant.now(), null, null);
    }

    public void markAsUsed(String gateId) {
        if (this.status != TicketStatus.ACTIVE) {
            throw new DomainException("Ticket cannot be marked as used because it is not active: " + this.status);
        }
        this.status = TicketStatus.USED;
        this.usedAt = Instant.now();
        this.usedAtGateId = gateId;
    }

    public void revoke() {
        if (this.status == TicketStatus.USED) {
            throw new DomainException("Cannot revoke a ticket that has already been used");
        }
        this.status = TicketStatus.REVOKED;
    }

    @Override
    public TicketId getId() {
        return id;
    }

    public UUID getEventId() {
        return eventId;
    }

    public UUID getUserId() {
        return userId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public TicketSecret getSecret() {
        return secret;
    }

    public TicketStatus getStatus() {
        return status;
    }

    public Instant getIssuedAt() {
        return issuedAt;
    }

    public Instant getUsedAt() {
        return usedAt;
    }

    public String getUsedAtGateId() {
        return usedAtGateId;
    }
}
