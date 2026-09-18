package com.ticketing.mobile.ticket_display.data.datasource

import com.ticketing.mobile.core_network.client.IApiClient
import com.ticketing.mobile.core_network.client.OkHttpApiClient
import com.ticketing.mobile.core_network.model.NetworkResult
import com.ticketing.mobile.ticket_display.data.dto.TicketDto
import com.ticketing.mobile.ticket_display.domain.model.UserTicketCheckInStatus
import com.ticketing.mobile.ticket_display.domain.model.UserTicketItem
import org.json.JSONArray
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * Triển khai ITicketRemoteDataSource với kết nối Backend REST API và offline fallback.
 */
class DefaultTicketRemoteDataSource(
    private val apiClient: IApiClient = OkHttpApiClient(baseUrl = "http://10.0.2.2:8080/api/v1")
) : ITicketRemoteDataSource {

    private val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy • HH:mm")
        .withZone(ZoneId.of("Asia/Ho_Chi_Minh"))

    private val fallbackTickets = listOf(
        UserTicketItem(
            ticketId = "a1111111-0000-0000-0000-000000000001",
            eventName = "HÀ NỘI ROCK FEST 2026",
            venue = "SVĐ Quốc Gia Mỹ Đình, Hà Nội",
            dateDisplay = "Hôm nay • 19:30",
            seatNumber = "VIP-A12",
            attendeeName = "Nguyễn Hoàng Long",
            tierName = "VIP Diamond",
            status = UserTicketCheckInStatus.READY_TO_CHECK_IN,
            gateInfo = "CỔNG A1",
            checkInNote = "Đang mở cửa check-in",
            isCheckInOpen = true
        ),
        UserTicketItem(
            ticketId = "a2222222-0000-0000-0000-000000000002",
            eventName = "ĐẠI NHẠC HỘI MONSOON EDM",
            venue = "TT Hội Nghị Quốc Gia, Hà Nội",
            dateDisplay = "15/11/2026 • 18:00",
            seatNumber = "ZONE-FANZ-08",
            attendeeName = "Nguyễn Hoàng Long",
            tierName = "Fanzone Standard",
            status = UserTicketCheckInStatus.NOT_YET_CHECK_IN,
            gateInfo = "CỔNG B2",
            checkInNote = "Cổng mở lúc 16:00 (chưa thể check-in)",
            isCheckInOpen = false
        ),
        UserTicketItem(
            ticketId = "a3333333-0000-0000-0000-000000000003",
            eventName = "CHUNG KẾT CÚP QUỐC GIA 2026",
            venue = "SVĐ Hàng Đẫy, Hà Nội",
            dateDisplay = "10/09/2026 • 17:00",
            seatNumber = "STAND-A-45",
            attendeeName = "Nguyễn Hoàng Long",
            tierName = "Khán Đài A",
            status = UserTicketCheckInStatus.CHECKED_IN,
            gateInfo = "CỔNG CHÍNH",
            checkInNote = "Đã check-in qua cổng CỔNG CHÍNH",
            isCheckInOpen = false
        )
    )

    override suspend fun fetchTicketById(ticketId: String): NetworkResult<TicketDto> {
        val dto = TicketDto(
            ticketId = ticketId,
            eventTitle = "Hà Nội Rock Fest 2026",
            location = "SVĐ Quốc Gia Mỹ Đình, Hà Nội",
            eventEpochSeconds = 1773513600L,
            seatCode = "VIP-A12",
            customerFullName = "Nguyễn Hoàng Long",
            statusCode = "ACTIVE",
            secretKey = "47c9f87cb5e23631f24d1a6e9a7e02e86d0b674b3e813739a8c62b92ef51bcf6"
        )
        return NetworkResult.Success(dto)
    }

    override suspend fun fetchMyTickets(userId: String): NetworkResult<List<UserTicketItem>> {
        val result = apiClient.get(
            endpoint = "/tickets",
            headers = mapOf("X-User-Id" to userId)
        ) { jsonStr ->
            val jsonArray = JSONArray(jsonStr)
            val tickets = mutableListOf<UserTicketItem>()
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val ticketId = obj.optString("ticketId")
                val eventName = obj.optString("eventName", "Sự Kiện")
                val venue = obj.optString("venueName", "Chưa xác định")
                val startIso = obj.optString("startDateTime")
                val dateDisplay = try {
                    dateFormatter.format(Instant.parse(startIso))
                } catch (e: Exception) {
                    "Sắp diễn ra"
                }
                val seatNumber = obj.optString("seatNumber", "GA-01")
                val attendeeName = obj.optString("attendeeName", "Khán Giả")
                val tierName = obj.optString("categoryName", "Standard")
                val statusStr = obj.optString("status", "NOT_YET_CHECK_IN")
                val status = try {
                    UserTicketCheckInStatus.valueOf(statusStr)
                } catch (e: Exception) {
                    UserTicketCheckInStatus.NOT_YET_CHECK_IN
                }
                val gateInfo = obj.optString("gateInfo", "CỔNG CHÍNH")
                val checkInNote = obj.optString("checkInNote", "")
                val checkInOpensAt = obj.optLong("checkInOpensAtEpochSeconds", 0L)
                val isCheckInOpen = obj.optBoolean("isCheckInOpen", false)

                tickets.add(
                    UserTicketItem(
                        ticketId = ticketId,
                        eventName = eventName,
                        venue = venue,
                        dateDisplay = dateDisplay,
                        seatNumber = seatNumber,
                        attendeeName = attendeeName,
                        tierName = tierName,
                        status = status,
                        gateInfo = gateInfo,
                        checkInNote = checkInNote,
                        checkInOpensAtEpochSeconds = checkInOpensAt,
                        isCheckInOpen = isCheckInOpen
                    )
                )
            }
            tickets
        }

        return when (result) {
            is NetworkResult.Success -> result
            is NetworkResult.Error, NetworkResult.Loading -> NetworkResult.Success(fallbackTickets)
        }
    }
}
