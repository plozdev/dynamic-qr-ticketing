package com.ticketing.mobile.ticket_display.data.dto

data class EventItemDto(
    val id: String,
    val name: String,
    val description: String? = null,
    val venueName: String? = null,
    val category: String? = "Âm nhạc & Concert",
    val basePrice: Double = 450000.0,
    val totalTickets: Int = 1000,
    val availableTickets: Int = 850,
    val bannerUrl: String? = null,
    val isHotTrend: Boolean = false
)
