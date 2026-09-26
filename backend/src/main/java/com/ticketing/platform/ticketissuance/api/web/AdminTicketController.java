package com.ticketing.platform.ticketissuance.api.web;

import com.ticketing.platform.ticketissuance.api.dto.ClaimTicketResponse;
import com.ticketing.platform.ticketissuance.api.dto.UserTicketResponse;
import com.ticketing.platform.ticketissuance.application.dto.ClaimTicketCommand;
import com.ticketing.platform.ticketissuance.application.port.in.ClaimTicketUseCase;
import com.ticketing.platform.ticketissuance.application.port.in.GetUserTicketsUseCase;
import com.ticketing.platform.user.UserExportedService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/tickets")
@RequiredArgsConstructor
public class AdminTicketController {
    private final UserExportedService users;
    private final ClaimTicketUseCase claimTickets;
    private final GetUserTicketsUseCase getUserTickets;

    @PostMapping
    public ResponseEntity<ClaimTicketResponse> assign(
            @Valid @RequestBody AssignTicketRequest request) {
        if (users.getUserById(request.userId()).isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }
        ClaimTicketResponse result = claimTickets.claimTicket(new ClaimTicketCommand(
                request.eventId(), request.userId(), request.categoryName(), request.seatNumber(), request.attendeeName()));
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @GetMapping("/users/{userId}")
    public List<UserTicketResponse> listForUser(
            @PathVariable UUID userId) {
        if (users.getUserById(userId).isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }
        return getUserTickets.getUserTickets(userId);
    }

    public record AssignTicketRequest(@NotNull UUID eventId, @NotNull UUID userId,
                                      @NotBlank String categoryName, String seatNumber, String attendeeName) {}
}
