package com.ticketing.mobile.ticket_display.domain.usecase

import com.ticketing.mobile.ticket_display.domain.model.UserTicketItem
import com.ticketing.mobile.ticket_display.domain.repository.ITicketRepository

/**
 * UseCase lấy danh sách vé của người dùng từ Repository.
 */
class GetMyTicketsUseCase(
    private val repository: ITicketRepository
) {
    suspend operator fun invoke(userId: String = "11111111-2222-3333-4444-555555555555"): Result<List<UserTicketItem>> {
        return repository.getMyTickets(userId)
    }
}
