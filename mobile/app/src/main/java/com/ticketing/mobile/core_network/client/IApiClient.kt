package com.ticketing.mobile.core_network.client

import com.ticketing.mobile.core_network.model.NetworkResult

/**
 * Boundary interface for HTTP communication.
 */
interface IApiClient {

    /**
     * Perform HTTP GET request.
     */
    suspend fun <T> get(
        endpoint: String,
        headers: Map<String, String> = emptyMap(),
        deserializer: (String) -> T
    ): NetworkResult<T>

    /**
     * Perform HTTP POST request.
     */
    suspend fun <T> post(
        endpoint: String,
        bodyJson: String,
        headers: Map<String, String> = emptyMap(),
        deserializer: (String) -> T
    ): NetworkResult<T>
}
