package com.ticketing.platform.ticketissuance.application.port.in;

import com.ticketing.platform.ticketissuance.api.dto.ClaimTicketResponse;
import com.ticketing.platform.ticketissuance.application.dto.ClaimTicketCommand;

public interface ClaimTicketUseCase {

    ClaimTicketResponse claimTicket(ClaimTicketCommand command);
}
