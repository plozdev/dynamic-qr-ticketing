package com.ticketing.mobile.core_crypto.data

import com.ticketing.mobile.core_crypto.data.native_bridge.NativeCryptoBridge
import com.ticketing.mobile.core_crypto.domain.model.CryptoToken
import com.ticketing.mobile.core_crypto.domain.model.VerificationResult
import com.ticketing.mobile.core_crypto.domain.repository.ICryptoEngine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Triển khai interface ICryptoEngine trong Domain Layer bằng cách ủy quyền tính toán
 * cho thư viện C++ NDK (libdynamic_qr_crypto.so) thông qua NativeCryptoBridge.
 */
class NativeCryptoEngineImpl(
    private val bridge: NativeCryptoBridge = NativeCryptoBridge()
) : ICryptoEngine {

    /**
     * Sinh Dynamic Token bằng C++ NDK.
     */
    override suspend fun generateToken(
        ticketId: String,
        secretKey: String,
        epochSeconds: Long,
        intervalSeconds: Int
    ): Result<CryptoToken> = withContext(Dispatchers.Default) {
        try {
            if (!NativeCryptoBridge.isNativeAvailable()) {
                return@withContext Result.failure(IllegalStateException("Native crypto library not loaded"))
            }
            val rawToken = bridge.generateDynamicTotpToken(ticketId, secretKey, epochSeconds, intervalSeconds)
            val timeWindow = epochSeconds / intervalSeconds
            val expiresAt = (timeWindow + 1) * intervalSeconds

            Result.success(
                CryptoToken(
                    tokenValue = rawToken,
                    epochSeconds = epochSeconds,
                    expiresAtEpochSeconds = expiresAt,
                    intervalSeconds = intervalSeconds
                )
            )
        } catch (e: Throwable) {
            Result.failure(e)
        }
    }

    /**
     * Xác thực Dynamic Token tại máy quét bằng C++ NDK (hoạt động hoàn toàn Offline).
     */
    override suspend fun verifyToken(
        ticketId: String,
        secretKey: String,
        token: String,
        epochSeconds: Long,
        intervalSeconds: Int,
        allowedDriftSteps: Int
    ): VerificationResult = withContext(Dispatchers.Default) {
        try {
            if (!NativeCryptoBridge.isNativeAvailable()) {
                return@withContext VerificationResult.Error("Native crypto library not loaded")
            }
            val isValid = bridge.verifyDynamicTotpToken(
                ticketId, secretKey, token, epochSeconds, intervalSeconds, allowedDriftSteps
            )
            if (isValid) {
                VerificationResult.Valid
            } else {
                VerificationResult.InvalidToken
            }
        } catch (e: Throwable) {
            VerificationResult.Error(e.message ?: "Unknown native verification error", e)
        }
    }

    override fun getEngineVersion(): String {
        return try {
            bridge.getSecurityVersion()
        } catch (e: Throwable) {
            "unknown-native-unavailable"
        }
    }
}
