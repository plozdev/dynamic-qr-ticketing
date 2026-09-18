package com.ticketing.platform.ticketissuance.application.port.in;

import com.ticketing.platform.ticketissuance.api.dto.TicketDetailsResponse;

import java.util.UUID;

public interface GetTicketDetailsUseCase {
    TicketDetailsResponse getTicketDetails(UUID ticketId, UUID requesterUserId);
}
