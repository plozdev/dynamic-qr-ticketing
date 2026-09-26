package com.ticketing.mobile.core_network.model

/**
 * Generic sealed class representing network response outcomes.
 */
sealed interface NetworkResult<out T> {
    data class Success<T>(val data: T, val statusCode: Int = 200) : NetworkResult<T>
    data class Error(val error: ApiError) : NetworkResult<Nothing>
    data object Loading : NetworkResult<Nothing>
}
