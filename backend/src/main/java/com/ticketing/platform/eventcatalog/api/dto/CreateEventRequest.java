package com.ticketing.platform.eventcatalog.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.List;

import com.ticketing.platform.eventcatalog.application.dto.CreateEventCommand;

public record CreateEventRequest(
        @NotBlank(message = "Event name is required")
        String name,

        String description,

        @NotBlank(message = "Venue name is required")
        String venueName,

        String venueAddress,

        List<String> venueGates,

        @NotNull(message = "Start date time is required")
        Instant startDateTime,

        @NotNull(message = "End date time is required")
        Instant endDateTime
) {
        public CreateEventCommand toCommand() {
                return new CreateEventCommand(
                        this.name,
                        this.description,
                        this.venueName,
                        this.venueAddress,
                        this.venueGates,
                        this.startDateTime,
                        this.endDateTime
                );
        }
}
