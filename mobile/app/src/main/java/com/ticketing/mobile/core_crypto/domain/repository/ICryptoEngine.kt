package com.ticketing.mobile.core_crypto.domain.repository

import com.ticketing.mobile.core_crypto.domain.model.CryptoToken
import com.ticketing.mobile.core_crypto.domain.model.VerificationResult

/**
 * Boundary Interface cho các thao tác mã hóa Native C++.
 * Pure Kotlin contract không phụ thuộc Android Framework.
 */
interface ICryptoEngine {

    /**
     * Sinh token động HMAC-SHA256 Base64Url cho vé dựa trên secretKey và epochSeconds.
     */
    suspend fun generateToken(
        ticketId: String,
        secretKey: String,
        epochSeconds: Long,
        intervalSeconds: Int = 30
    ): Result<CryptoToken>

    /**
     * Xác thực token động với cửa sổ trôi (Sliding Window Drift).
     */
    suspend fun verifyToken(
        ticketId: String,
        secretKey: String,
        token: String,
        epochSeconds: Long,
        intervalSeconds: Int = 30,
        allowedDriftSteps: Int = 1
    ): VerificationResult

    fun getEngineVersion(): String
}
