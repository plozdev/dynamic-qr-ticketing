package com.ticketing.mobile.ticket_display.domain.usecase

import com.ticketing.mobile.ticket_display.data.dto.EventItemDto
import com.ticketing.mobile.ticket_display.domain.repository.ITicketRepository

class GetEventsUseCase(
    private val ticketRepository: ITicketRepository
) {
    suspend operator fun invoke(): Result<List<EventItemDto>> {
        return ticketRepository.getEvents()
    }
}
