package com.ticketing.mobile.ticket_display.domain.model

/**
 * Trạng thái của vé trong danh sách vé của tôi.
 */
enum class UserTicketCheckInStatus {
    READY_TO_CHECK_IN, // Cổng đang mở, sẵn sàng check-in ngay (Dynamic QR đang xoay)
    NOT_YET_CHECK_IN,  // Chưa tới giờ check-in (Cổng chưa mở)
    CHECKED_IN,        // Đã check-in thành công vào cổng
    REVOKED            // Vé bị thu hồi hoặc hủy
}

/**
 * Model đại diện cho một vé của người dùng trong danh sách vé.
 */
data class UserTicketItem(
    val ticketId: String,
    val eventName: String,
    val venue: String,
    val dateDisplay: String,
    val seatNumber: String,
    val attendeeName: String,
    val tierName: String,
    val status: UserTicketCheckInStatus,
    val gateInfo: String = "CỔNG CHÍNH",
    val checkInNote: String = "",
    val checkInOpensAtEpochSeconds: Long = 0L,
    val isCheckInOpen: Boolean = false
)
