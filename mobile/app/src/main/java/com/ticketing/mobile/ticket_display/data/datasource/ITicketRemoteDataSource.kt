package com.ticketing.mobile.ticket_display.data.datasource

import com.ticketing.mobile.core_network.model.NetworkResult
import com.ticketing.mobile.ticket_display.data.dto.TicketDto
import com.ticketing.mobile.ticket_display.domain.model.UserTicketItem

/**
 * Remote data source interface for ticket queries.
 */
interface ITicketRemoteDataSource {
    suspend fun fetchTicketById(ticketId: String): NetworkResult<TicketDto>

    suspend fun fetchMyTickets(userId: String = "11111111-2222-3333-4444-555555555555"): NetworkResult<List<UserTicketItem>>
}
