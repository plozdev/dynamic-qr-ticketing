package com.ticketing.mobile.ticket_display.data.datasource

import com.ticketing.mobile.core_network.model.NetworkResult
import com.ticketing.mobile.ticket_display.data.dto.TicketDto

/**
 * Remote data source interface for ticket queries.
 */
interface ITicketRemoteDataSource {
    suspend fun fetchTicketById(ticketId: String): NetworkResult<TicketDto>
}
