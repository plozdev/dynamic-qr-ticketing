package com.ticketing.mobile.events.domain.model

/**
 * Model đại diện cho một sự kiện trong danh mục sự kiện (Events Catalog).
 */
data class EventItem(
    val id: String,
    val title: String,
    val venue: String,
    val dateDisplay: String,
    val priceDisplay: String,
    val category: String,
    val remainingPercentage: Int,
    val isHotTrend: Boolean = false,
    val isDynamicPassSupported: Boolean = true,
    val gateOpensAtDisplay: String = "18:00"
)
