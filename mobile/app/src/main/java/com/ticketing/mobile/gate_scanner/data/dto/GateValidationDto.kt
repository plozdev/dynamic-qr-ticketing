package com.ticketing.mobile.gate_scanner.data.dto

/**
 * Request DTO sent to gate validation endpoint.
 */
data class GateValidationRequestDto(
    val rawPayload: String,
    val gateId: String,
    val scanTimestamp: Long
)

/**
 * Response DTO received from gate validation endpoint.
 */
data class GateValidationResponseDto(
    val isAllowed: Boolean,
    val ticketId: String?,
    val attendeeName: String?,
    val seatNumber: String?,
    val checkInTimestamp: Long?,
    val rejectionReasonCode: String?,
    val message: String?
)
