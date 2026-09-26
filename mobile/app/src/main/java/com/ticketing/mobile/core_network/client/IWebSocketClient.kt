package com.ticketing.mobile.core_network.client

import kotlinx.coroutines.flow.Flow

/**
 * Boundary interface for Real-Time Gate / Event updates via WebSocket.
 */
interface IWebSocketClient {

    /**
     * Connect to real-time gate channel.
     */
    fun connect(url: String)

    /**
     * Observe incoming event stream.
     */
    fun observeEvents(): Flow<String>

    /**
     * Send outgoing message/ping.
     */
    fun send(message: String): Boolean

    /**
     * Disconnect connection.
     */
    fun disconnect()
}
