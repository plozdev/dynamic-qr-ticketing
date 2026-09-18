package com.ticketing.mobile.ticket_display.domain.repository

import com.ticketing.mobile.ticket_display.domain.model.DynamicQrData
import com.ticketing.mobile.ticket_display.domain.model.Ticket
import kotlinx.coroutines.flow.Flow

/**
 * Boundary repository interface for ticket display operations.
 */
interface ITicketRepository {

    /**
     * Retrieve ticket detail by ticket ID.
     */
    suspend fun getTicket(ticketId: String): Result<Ticket>

    /**
     * Generate dynamic QR payload using native crypto engine.
     */
    suspend fun getDynamicQr(ticketId: String): Result<DynamicQrData>

    /**
     * Retrieve all tickets belonging to the current user.
     */
    suspend fun getMyTickets(userId: String = "11111111-2222-3333-4444-555555555555"): Result<List<com.ticketing.mobile.ticket_display.domain.model.UserTicketItem>>

    /**
     * Continuously stream dynamic QR updates every rotation cycle.
     */
    fun observeDynamicQr(ticketId: String): Flow<DynamicQrData>
}
