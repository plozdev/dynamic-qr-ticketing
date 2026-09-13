package com.ticketing.platform.ticketissuance.domain.model;

import com.ticketing.platform.shared.domain.AggregateRoot;

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
        // TODO: Khởi tạo vé mới với secret ngẫu nhiên bảo mật và trạng thái ACTIVE
        return new Ticket(TicketId.generate(), eventId, userId, categoryName,
                TicketSecret.generate(), TicketStatus.ACTIVE, Instant.now(), null, null);
    }

    public void markAsUsed(String gateId) {
        // TODO: Kiểm tra điều kiện: chỉ vé ở trạng thái ACTIVE mới được check-in, sau đó chuyển sang USED
        this.status = TicketStatus.USED;
        this.usedAt = Instant.now();
        this.usedAtGateId = gateId;
    }

    public void revoke() {
        // TODO: Kiểm tra điều kiện: vé đã USED thì không thể thu hồi
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
