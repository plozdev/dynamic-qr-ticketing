package com.ticketing.mobile.ticket_display.data.datasource

import com.ticketing.mobile.ticket_display.data.dto.TicketDto
import java.util.concurrent.ConcurrentHashMap

/**
 * Triển khai ITicketLocalDataSource lưu trữ trong RAM (In-Memory Cache).
 * Có sẵn dữ liệu mẫu để thử nghiệm tính năng Dynamic QR Offline ngay lập tức.
 */
class InMemoryTicketLocalDataSource : ITicketLocalDataSource {

    private val cache = ConcurrentHashMap<String, TicketDto>()

    override suspend fun getCachedTicket(ticketId: String): TicketDto? {
        return cache[ticketId]
    }

    override suspend fun getSecretKey(ticketId: String): String? {
        return cache[ticketId]?.secretKey
    }

    override suspend fun saveTicket(ticket: TicketDto) {
        cache[ticket.ticketId] = ticket
    }

    override suspend fun clearCache() {
        cache.clear()
    }
}
