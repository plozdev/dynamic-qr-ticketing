package com.ticketing.platform.eventcatalog.domain.model;

import com.ticketing.platform.shared.domain.AggregateRoot;
import com.ticketing.platform.shared.exception.DomainException;

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
    private String bannerUrl;
    private String category;
    private java.math.BigDecimal basePrice;
    private int totalTickets;
    private int availableTickets;
    private boolean isHotTrend;
    private int checkInWindowMinutes;
    private boolean checkInEnabled;

    public Event(UUID id, String name, String description, Venue venue,
                 Instant startDateTime, Instant endDateTime, EventStatus status,
                 List<TicketCategory> categories) {
        this(id, name, description, venue, startDateTime, endDateTime, status, categories,
                null, "Âm nhạc & Concert", new java.math.BigDecimal("450000"), 1000, 850, false, 120);
    }

    public Event(UUID id, String name, String description, Venue venue,
                 Instant startDateTime, Instant endDateTime, EventStatus status,
                 List<TicketCategory> categories, String bannerUrl, String category,
                 java.math.BigDecimal basePrice, int totalTickets, int availableTickets,
                 boolean isHotTrend, int checkInWindowMinutes) {
        this.id = id != null ? id : UUID.randomUUID();
        this.name = name;
        this.description = description;
        this.venue = venue;
        this.startDateTime = startDateTime;
        this.endDateTime = endDateTime;
        this.status = status != null ? status : EventStatus.DRAFT;
        this.categories = categories != null ? new ArrayList<>(categories) : new ArrayList<>();
        this.bannerUrl = bannerUrl;
        this.category = category != null ? category : "Âm nhạc & Concert";
        this.basePrice = basePrice != null ? basePrice : new java.math.BigDecimal("450000");
        this.totalTickets = totalTickets > 0 ? totalTickets : 1000;
        this.availableTickets = availableTickets >= 0 ? availableTickets : 850;
        this.isHotTrend = isHotTrend;
        this.checkInWindowMinutes = checkInWindowMinutes > 0 ? checkInWindowMinutes : 120;
    }

    public void setCheckInEnabled(boolean enabled) {
        if (enabled && status != EventStatus.PUBLISHED) {
            throw new DomainException("Check-in requires a published event");
        }
        this.checkInEnabled = enabled;
    }

    public boolean isCheckInEnabled() {
        return checkInEnabled;
    }

    public static Event create(String name, String description, Venue venue,
                               Instant startDateTime, Instant endDateTime) {
        if (startDateTime.isAfter(endDateTime)) {
            throw new DomainException("Event date must precede end date");
        }
        return new Event(UUID.randomUUID(), name, description, venue, startDateTime, endDateTime,
                EventStatus.DRAFT, new ArrayList<>(), null, "Âm nhạc & Concert",
                new java.math.BigDecimal("450000"), 1000, 1000, false, 120);
    }

    public void configureListing(java.math.BigDecimal price, String imageUrl, Integer capacity) {
        if (status != EventStatus.DRAFT) throw new DomainException("Only draft events can be configured");
        if (price != null) {
            if (price.signum() < 0) throw new DomainException("Ticket price must not be negative");
            this.basePrice = price;
        }
        if (imageUrl != null) this.bannerUrl = imageUrl;
        if (capacity != null) {
            if (capacity < 1) throw new DomainException("Capacity must be positive");
            this.totalTickets = capacity;
            this.availableTickets = capacity;
        }
    }

    public void publish() {
        if (status != EventStatus.DRAFT) { throw new DomainException("Only DRAFT events can be published"); }
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

    public String getBannerUrl() {
        return bannerUrl;
    }

    public String getCategory() {
        return category;
    }

    public java.math.BigDecimal getBasePrice() {
        return basePrice;
    }

    public int getTotalTickets() {
        return totalTickets;
    }

    public int getAvailableTickets() {
        return availableTickets;
    }

    public int getRemainingPercentage() {
        if (totalTickets <= 0) return 0;
        return (int) Math.round(((double) availableTickets / totalTickets) * 100);
    }

    public boolean isHotTrend() {
        return isHotTrend;
    }

    public int getCheckInWindowMinutes() {
        return checkInWindowMinutes;
    }

    public Instant getCheckInOpensAt() {
        return startDateTime.minus(java.time.Duration.ofMinutes(checkInWindowMinutes));
    }

    public boolean isCheckInOpen(Instant now) {
        return status == EventStatus.PUBLISHED && checkInEnabled
                && !now.isAfter(endDateTime);
    }

    public void decrementAvailableTickets() {
        if (this.availableTickets <= 0) {
            throw new DomainException("No available tickets left for event: " + this.id);
        }
        this.availableTickets--;
    }
}
