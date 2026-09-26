package com.ticketing.mobile.ticket_display.data.dto

/**
 * Data Transfer Object đại diện cho vé nhận từ Backend API.
 * Chứa secretKey được Backend provision riêng cho vé này.
 */
data class TicketDto(
    val ticketId: String,
    val eventTitle: String,
    val location: String,
    val eventEpochSeconds: Long,
    val seatCode: String,
    val customerFullName: String,
    val statusCode: String,
    val secretKey: String? = null
)
