package com.ticketing.mobile.ticket_display.data.datasource

import com.ticketing.mobile.core_network.model.NetworkResult
import com.ticketing.mobile.ticket_display.data.dto.TicketDto

/**
 * Triển khai ITicketRemoteDataSource với cơ chế mô phỏng trả về dữ liệu vé từ API.
 */
class DefaultTicketRemoteDataSource : ITicketRemoteDataSource {

    override suspend fun fetchTicketById(ticketId: String): NetworkResult<TicketDto> {
        val dto = TicketDto(
            ticketId = ticketId,
            eventTitle = "Đêm Nhạc Hội Dynamic QR Tour",
            location = "Trung Tâm Triển Lãm & Hội Nghị SECC, TP.HCM",
            eventEpochSeconds = 1773513600L,
            seatCode = "ZONE-A-01",
            customerFullName = "Trần Thị Minh Anh",
            statusCode = "ACTIVE",
            secretKey = "sample_super_secure_offline_secret_key_2026"
        )
        return NetworkResult.Success(dto)
    }
}
