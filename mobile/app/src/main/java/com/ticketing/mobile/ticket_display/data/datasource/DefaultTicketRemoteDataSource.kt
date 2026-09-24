package com.ticketing.mobile.ticket_display.data.datasource

import com.ticketing.mobile.core_network.client.IApiClient
import com.ticketing.mobile.core_network.client.OkHttpApiClient
import com.ticketing.mobile.core_network.model.NetworkResult
import com.ticketing.mobile.ticket_display.data.dto.EventItemDto
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
    private val apiClient: IApiClient = OkHttpApiClient()
) : ITicketRemoteDataSource {

    private val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy • HH:mm")
        .withZone(ZoneId.of("Asia/Ho_Chi_Minh"))

    override suspend fun fetchTicketById(ticketId: String): NetworkResult<TicketDto> {
        return apiClient.get(
            endpoint = "/tickets/$ticketId"
        ) { jsonStr ->
            val obj = org.json.JSONObject(jsonStr)
            TicketDto(
                ticketId = obj.optString("ticketId", ticketId),
                eventTitle = obj.optString("eventTitle", "Sự Kiện"),
                location = obj.optString("location", "Chưa xác định"),
                eventEpochSeconds = obj.optLong("eventEpochSeconds", System.currentTimeMillis() / 1000),
                seatCode = obj.optString("seatCode", "GA-01"),
                customerFullName = obj.optString("customerFullName", "Khán Giả"),
                statusCode = obj.optString("statusCode", "ACTIVE"),
                secretKey = obj.optString("secretKey").takeIf { it.isNotBlank() }
            )
        }
    }

    override suspend fun fetchMyTickets(userId: String): NetworkResult<List<UserTicketItem>> {
        return apiClient.get(
            endpoint = "/tickets"
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
    }

    override suspend fun claimTicket(
        eventId: String,
        categoryName: String?,
        attendeeName: String?
    ): NetworkResult<TicketDto> {
        val payload = org.json.JSONObject().apply {
            if (!categoryName.isNullOrBlank()) put("categoryName", categoryName)
            if (!attendeeName.isNullOrBlank()) put("attendeeName", attendeeName)
        }

        return apiClient.post(
            endpoint = "/events/$eventId/claim",
            bodyJson = payload.toString()
        ) { jsonStr ->
            val obj = org.json.JSONObject(jsonStr)
            val ticketId = obj.optString("ticketId")
            TicketDto(
                ticketId = ticketId,
                eventTitle = obj.optString("eventName", "Sự Kiện"),
                location = obj.optString("venueName", "Chưa xác định"),
                eventEpochSeconds = try {
                    Instant.parse(obj.optString("startDateTime")).epochSecond
                } catch (e: Exception) {
                    System.currentTimeMillis() / 1000
                },
                seatCode = obj.optString("seatNumber", "GA-01"),
                customerFullName = obj.optString("attendeeName", "Khán Giả"),
                statusCode = obj.optString("status", "ACTIVE"),
                secretKey = obj.optString("secretKeyBase64").takeIf { it.isNotBlank() }
            )
        }
    }

    override suspend fun fetchEvents(): NetworkResult<List<EventItemDto>> {
        return apiClient.get(endpoint = "/events") { jsonStr ->
            val jsonArray = JSONArray(jsonStr)
            val events = mutableListOf<EventItemDto>()
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                events.add(
                    EventItemDto(
                        id = obj.optString("id"),
                        name = obj.optString("name", "Sự Kiện"),
                        description = obj.optString("description"),
                        venueName = obj.optString("venueName"),
                        category = obj.optString("category", "Âm nhạc & Concert"),
                        basePrice = obj.optDouble("basePrice", 450000.0),
                        totalTickets = obj.optInt("totalTickets", 1000),
                        availableTickets = obj.optInt("availableTickets", 850),
                        bannerUrl = obj.optString("bannerUrl").takeIf { it.isNotBlank() },
                        isHotTrend = obj.optBoolean("isHotTrend", false)
                    )
                )
            }
            events
        }
    }
}
