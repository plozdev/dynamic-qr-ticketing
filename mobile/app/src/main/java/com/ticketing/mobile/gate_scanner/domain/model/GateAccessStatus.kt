package com.ticketing.mobile.gate_scanner.domain.model

/**
 * Quyết định ra vào tại cổng soát vé.
 */
sealed interface GateAccessStatus {
    data class Granted(
        val ticketId: String,
        val attendeeName: String,
        val seatNumber: String,
        val checkInTimestamp: Long
    ) : GateAccessStatus

    data class Denied(
        val reason: DenyReason,
        val message: String
    ) : GateAccessStatus
}

enum class DenyReason {
    INVALID_SIGNATURE,
    EXPIRED_TIMESTAMP,
    ALREADY_CHECKED_IN,
    TICKET_NOT_FOUND,
    DEVICE_MISMATCH,
    SERVER_REJECTED
}
