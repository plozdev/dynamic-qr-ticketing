package com.ticketing.mobile.ticket_display.domain.model

/**
 * Domain entity đại diện cho payload mã Dynamic QR đang hiển thị trên màn hình.
 */
data class DynamicQrData(
    val ticketId: String,
    val qrPayload: String,
    val validUntilEpochSeconds: Long,
    val totalIntervalSeconds: Int,
    val remainingSeconds: Int
)
