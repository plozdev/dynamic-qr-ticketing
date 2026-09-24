package com.ticketing.platform.eventcatalog.infrastructure.persistence.entity;

import com.ticketing.platform.eventcatalog.domain.model.EventStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "events")
@Getter
@Setter
@Builder 
@NoArgsConstructor
@AllArgsConstructor
public class EventJpaEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "venue_name")
    private String venueName;

    @Column(name = "venue_address")
    private String venueAddress;

    @Column(name = "start_date_time", nullable = false)
    private Instant startDateTime;

    @Column(name = "end_date_time", nullable = false)
    private Instant endDateTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private EventStatus status;

    @Column(name = "banner_url")
    private String bannerUrl;

    @Column(name = "category", nullable = false)
    private String category;

    @Column(name = "base_price", nullable = false)
    private java.math.BigDecimal basePrice;

    @Column(name = "total_tickets", nullable = false)
    private int totalTickets;

    @Column(name = "available_tickets", nullable = false)
    private int availableTickets;

    @Column(name = "is_hot_trend", nullable = false)
    private boolean isHotTrend;

    @Column(name = "check_in_window_minutes", nullable = false)
    private int checkInWindowMinutes;

    @Column(name = "check_in_enabled", nullable = false)
    private boolean checkInEnabled;
}
