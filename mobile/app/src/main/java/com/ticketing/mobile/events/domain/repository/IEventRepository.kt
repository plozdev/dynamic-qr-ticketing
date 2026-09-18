package com.ticketing.mobile.events.domain.repository

import com.ticketing.mobile.core_network.model.NetworkResult
import com.ticketing.mobile.events.domain.model.EventItem

interface IEventRepository {
    suspend fun getEvents(category: String? = null): NetworkResult<List<EventItem>>
}
