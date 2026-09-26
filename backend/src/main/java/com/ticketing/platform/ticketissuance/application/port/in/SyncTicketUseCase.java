package com.ticketing.platform.ticketissuance.application.port.in;

import com.ticketing.platform.ticketissuance.application.dto.TicketSyncDto;

import java.util.UUID;

public interface SyncTicketUseCase {

    TicketSyncDto syncTicket(UUID ticketId, UUID requesterUserId);
}
