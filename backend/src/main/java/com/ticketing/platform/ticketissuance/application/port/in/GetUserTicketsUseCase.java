package com.ticketing.platform.ticketissuance.application.port.in;

import com.ticketing.platform.ticketissuance.api.dto.UserTicketResponse;

import java.util.List;
import java.util.UUID;

public interface GetUserTicketsUseCase {

    List<UserTicketResponse> getUserTickets(UUID userId);
}
