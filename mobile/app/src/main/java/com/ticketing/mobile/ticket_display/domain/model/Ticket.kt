package com.ticketing.mobile.ticket_display.domain.model

/**
 * Domain entity representing a concert/transit ticket.
 */
data class Ticket(
    val id: String,
    val eventName: String,
    val venue: String,
    val eventTimestamp: Long,
    val seatNumber: String,
    val ticketHolderName: String,
    val status: TicketStatus
)

enum class TicketStatus {
    ACTIVE,
    CHECKED_IN,
    EXPIRED,
    REVOKED
}
