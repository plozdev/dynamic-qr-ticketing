package com.ticketing.mobile.core_crypto.domain.model

/**
 * Domain representation of cryptographic validation result.
 */
sealed interface VerificationResult {
    data object Valid : VerificationResult
    data object InvalidToken : VerificationResult
    data object ExpiredTimeWindow : VerificationResult
    data class Error(val message: String, val cause: Throwable? = null) : VerificationResult
}
