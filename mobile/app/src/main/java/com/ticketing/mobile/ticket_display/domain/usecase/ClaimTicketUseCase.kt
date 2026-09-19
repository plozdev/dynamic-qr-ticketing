package com.ticketing.mobile.ticket_display.domain.usecase

import com.ticketing.mobile.ticket_display.domain.model.Ticket
import com.ticketing.mobile.ticket_display.domain.repository.ITicketRepository

class ClaimTicketUseCase(
    private val ticketRepository: ITicketRepository
) {
    suspend operator fun invoke(
        eventId: String,
        categoryName: String? = null,
        attendeeName: String? = null
    ): Result<Ticket> {
        return ticketRepository.claimTicket(eventId, categoryName, attendeeName)
    }
}
