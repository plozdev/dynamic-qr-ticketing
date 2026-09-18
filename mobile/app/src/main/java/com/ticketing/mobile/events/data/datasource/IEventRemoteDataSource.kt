package com.ticketing.mobile.events.data.datasource

import com.ticketing.mobile.core_network.model.NetworkResult
import com.ticketing.mobile.events.domain.model.EventItem

interface IEventRemoteDataSource {
    suspend fun getEvents(category: String? = null): NetworkResult<List<EventItem>>
}
