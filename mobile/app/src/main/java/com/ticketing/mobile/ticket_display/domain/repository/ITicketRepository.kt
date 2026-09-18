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
     * Continuously stream dynamic QR updates every rotation cycle.
     */
    fun observeDynamicQr(ticketId: String): Flow<DynamicQrData>
}
