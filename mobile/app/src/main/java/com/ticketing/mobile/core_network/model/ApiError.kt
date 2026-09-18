package com.ticketing.mobile.core_network.model

/**
 * Standardized network and HTTP errors.
 */
sealed class ApiError(val messageText: String, val causeThrowable: Throwable? = null) {
    data class HttpError(val code: Int, val rawBody: String?) : 
        ApiError("HTTP $code error occurred")
    data class NetworkConnection(val ex: Throwable?) : 
        ApiError("No internet connection or timeout", ex)
    data class Unauthorized(val reason: String = "Session expired") : 
        ApiError(reason)
    data class SerializationError(val ex: Throwable?) : 
        ApiError("Failed to parse response payload", ex)
    data class Unknown(val ex: Throwable?) : 
        ApiError("An unexpected error occurred", ex)
}
