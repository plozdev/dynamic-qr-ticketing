package com.ticketing.mobile.ticket_display.data.datasource

import com.ticketing.mobile.ticket_display.data.dto.TicketDto
import java.util.concurrent.ConcurrentHashMap

/**
 * Triển khai ITicketLocalDataSource lưu trữ trong RAM (In-Memory Cache).
 * Có sẵn dữ liệu mẫu để thử nghiệm tính năng Dynamic QR Offline ngay lập tức.
 */
class InMemoryTicketLocalDataSource : ITicketLocalDataSource {

    private val cache = ConcurrentHashMap<String, TicketDto>()

    init {
        // Khởi tạo sẵn các vé mẫu để test offline đầy đủ các trạng thái
        val sampleTicket1 = TicketDto(
            ticketId = "TKT-VN-2026-9901",
            eventTitle = "HÀ NỘI ROCK FEST 2026",
            location = "SVĐ Quốc Gia Mỹ Đình, Hà Nội",
            eventEpochSeconds = 1773513600L,
            seatCode = "VIP-A12",
            customerFullName = "Nguyễn Hoàng Long",
            statusCode = "ACTIVE",
            secretKey = "sample_super_secure_offline_secret_key_2026"
        )
        val sampleTicket2 = TicketDto(
            ticketId = "TKT-VN-2026-8802",
            eventTitle = "ĐẠI NHẠC HỘI MONSOON EDM",
            location = "TT Hội Nghị Quốc Gia, Hà Nội",
            eventEpochSeconds = 1773513600L,
            seatCode = "ZONE-FANZ-08",
            customerFullName = "Nguyễn Hoàng Long",
            statusCode = "EXPIRED", // Chưa tới giờ mở cổng check-in
            secretKey = "sample_super_secure_offline_secret_key_monsoon"
        )
        val sampleTicket3 = TicketDto(
            ticketId = "TKT-VN-2026-7703",
            eventTitle = "CHUNG KẾT CÚP QUỐC GIA 2026",
            location = "SVĐ Hàng Đẫy, Hà Nội",
            eventEpochSeconds = 1773513600L,
            seatCode = "STAND-A-45",
            customerFullName = "Nguyễn Hoàng Long",
            statusCode = "CHECKED_IN", // Đã quét qua cổng
            secretKey = "sample_super_secure_offline_secret_key_cup"
        )
        cache[sampleTicket1.ticketId] = sampleTicket1
        cache[sampleTicket2.ticketId] = sampleTicket2
        cache[sampleTicket3.ticketId] = sampleTicket3
    }

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
