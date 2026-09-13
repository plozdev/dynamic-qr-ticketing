package com.ticketing.mobile.core_crypto.domain.model

/**
 * Domain entity đại diện cho token động sinh bởi thuật toán mã hóa C++ NDK.
 */
data class CryptoToken(
    val tokenValue: String,
    val epochSeconds: Long,
    val expiresAtEpochSeconds: Long,
    val intervalSeconds: Int
)
