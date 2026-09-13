package com.ticketing.platform.eventcatalog.domain.model;

import com.ticketing.platform.shared.domain.AggregateRoot;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Khung sườn Aggregate Root cho Event trong Event Catalog Bounded Context.
 * Bạn tự triển khai các quy tắc nghiệp vụ (invariants, business rules) tại đây.
 */
public class Event implements AggregateRoot<UUID> {

    private final UUID id;
    private String name;
    private String description;
    private Venue venue;
    private Instant startDateTime;
    private Instant endDateTime;
    private EventStatus status;
    private final List<TicketCategory> categories;

    public Event(UUID id, String name, String description, Venue venue,
                 Instant startDateTime, Instant endDateTime, EventStatus status,
                 List<TicketCategory> categories) {
        this.id = id != null ? id : UUID.randomUUID();
        this.name = name;
        this.description = description;
        this.venue = venue;
        this.startDateTime = startDateTime;
        this.endDateTime = endDateTime;
        this.status = status != null ? status : EventStatus.DRAFT;
        this.categories = categories != null ? new ArrayList<>(categories) : new ArrayList<>();
    }

    public static Event create(String name, String description, Venue venue,
                               Instant startDateTime, Instant endDateTime) {
        // TODO: Kiểm tra quy tắc nghiệp vụ: startDateTime phải trước endDateTime
        return new Event(UUID.randomUUID(), name, description, venue, startDateTime, endDateTime, EventStatus.DRAFT, new ArrayList<>());
    }

    public void publish() {
        // TODO: Kiểm tra điều kiện xuất bản (chỉ sự kiện ở trạng thái DRAFT mới được xuất bản)
        this.status = EventStatus.PUBLISHED;
    }

    @Override
    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Venue getVenue() {
        return venue;
    }

    public Instant getStartDateTime() {
        return startDateTime;
    }

    public Instant getEndDateTime() {
        return endDateTime;
    }

    public EventStatus getStatus() {
        return status;
    }

    public List<TicketCategory> getCategories() {
        return Collections.unmodifiableList(categories);
    }
}
