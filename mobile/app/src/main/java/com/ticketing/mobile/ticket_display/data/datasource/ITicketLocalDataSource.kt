package com.ticketing.mobile.ticket_display.data.datasource

import com.ticketing.mobile.ticket_display.data.dto.TicketDto

/**
 * Interface cho Local Storage an toàn (RoomDB / EncryptedSharedPreferences).
 */
interface ITicketLocalDataSource {
    suspend fun getCachedTicket(ticketId: String): TicketDto?
    suspend fun getSecretKey(ticketId: String): String?
    suspend fun saveTicket(ticket: TicketDto)
    suspend fun clearCache()
}
