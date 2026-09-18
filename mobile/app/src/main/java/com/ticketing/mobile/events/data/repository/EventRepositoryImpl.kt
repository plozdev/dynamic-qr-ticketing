package com.ticketing.mobile.events.data.repository

import com.ticketing.mobile.core_network.model.NetworkResult
import com.ticketing.mobile.events.data.datasource.IEventRemoteDataSource
import com.ticketing.mobile.events.domain.model.EventItem
import com.ticketing.mobile.events.domain.repository.IEventRepository

class EventRepositoryImpl(
    private val remoteDataSource: IEventRemoteDataSource
) : IEventRepository {

    override suspend fun getEvents(category: String?): NetworkResult<List<EventItem>> {
        return remoteDataSource.getEvents(category)
    }
}
