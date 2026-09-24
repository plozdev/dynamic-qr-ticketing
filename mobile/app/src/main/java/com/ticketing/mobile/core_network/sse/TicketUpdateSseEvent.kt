package com.ticketing.mobile.core_network.sse

/**
 * Model biểu diễn sự kiện Server-Sent Events (SSE) đẩy từ Backend
 * khi vé được soát tại cổng (TicketValidatedIntegrationEvent).
 */
data class TicketUpdateSseEvent(
    val eventType: String,
    val ticketId: String,
    val userId: String,
    val status: String,
    val gateId: String,
    val timestamp: Long
)
