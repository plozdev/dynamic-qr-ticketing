package com.ticketing.mobile.core_crypto.data

import com.ticketing.mobile.core_crypto.data.native_bridge.NativeCryptoBridge
import com.ticketing.mobile.core_crypto.domain.model.CryptoToken
import com.ticketing.mobile.core_crypto.domain.model.VerificationResult
import com.ticketing.mobile.core_crypto.domain.repository.ICryptoEngine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.nio.charset.StandardCharsets
import java.util.Base64
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

/**
 * Triển khai interface ICryptoEngine trong Domain Layer:
 * Ưu tiên ủy quyền cho C++ NDK (libdynamic_qr_crypto.so),
 * tự động fallback sang Java HMAC-SHA256 chuẩn để đảm bảo tính năng Dynamic QR hoạt động 100% trên mọi thiết bị.
 */
class NativeCryptoEngineImpl(
    private val bridge: NativeCryptoBridge = NativeCryptoBridge()
) : ICryptoEngine {

    /**
     * Sinh Dynamic Token: Ưu tiên C++ NDK, tự động fallback sang Java HMAC nếu thư viện C++ chưa sẵn sàng.
     */
    override suspend fun generateToken(
        ticketId: String,
        secretKey: String,
        epochSeconds: Long,
        intervalSeconds: Int
    ): Result<CryptoToken> = withContext(Dispatchers.Default) {
        try {
            val rawToken = if (NativeCryptoBridge.isNativeAvailable()) {
                try {
                    bridge.generateDynamicTotpToken(ticketId, secretKey, epochSeconds, intervalSeconds)
                } catch (t: Throwable) {
                    android.util.Log.w("NativeCryptoEngine", "Native bridge error, falling back to Java HMAC: ${t.message}")
                    computeJavaHmacTotp(ticketId, secretKey, epochSeconds, intervalSeconds)
                }
            } else {
                computeJavaHmacTotp(ticketId, secretKey, epochSeconds, intervalSeconds)
            }
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
     * Xác thực Dynamic Token tại máy quét (hoạt động hoàn toàn Offline).
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
            if (NativeCryptoBridge.isNativeAvailable()) {
                try {
                    val isValid = bridge.verifyDynamicTotpToken(
                        ticketId, secretKey, token, epochSeconds, intervalSeconds, allowedDriftSteps
                    )
                    return@withContext if (isValid) VerificationResult.Valid else VerificationResult.InvalidToken
                } catch (t: Throwable) {
                    // Fallback to Java HMAC verification
                }
            }

            val currentWindow = epochSeconds / intervalSeconds
            for (drift in -allowedDriftSteps..allowedDriftSteps) {
                val checkTime = (currentWindow + drift) * intervalSeconds
                val expected = computeJavaHmacTotp(ticketId, secretKey, checkTime, intervalSeconds)
                if (expected == token) {
                    return@withContext VerificationResult.Valid
                }
            }
            VerificationResult.InvalidToken
        } catch (e: Throwable) {
            VerificationResult.Error(e.message ?: "Unknown verification error", e)
        }
    }

    override fun getEngineVersion(): String {
        return if (NativeCryptoBridge.isNativeAvailable()) {
            try {
                bridge.getSecurityVersion()
            } catch (e: Throwable) {
                "java-hmac-sha256-fallback"
            }
        } else {
            "java-hmac-sha256-fallback"
        }
    }

    private fun computeJavaHmacTotp(
        ticketId: String,
        secretKey: String,
        epochSeconds: Long,
        intervalSeconds: Int
    ): String {
        val interval = if (intervalSeconds <= 0) 30 else intervalSeconds
        val timeWindow = epochSeconds / interval
        val message = "$ticketId:$timeWindow"

        val keyBytes = try {
            Base64.getUrlDecoder().decode(secretKey)
        } catch (e: Exception) {
            try {
                Base64.getDecoder().decode(secretKey)
            } catch (e2: Exception) {
                secretKey.toByteArray(StandardCharsets.UTF_8)
            }
        }

        val mac = Mac.getInstance("HmacSHA256")
        mac.init(SecretKeySpec(keyBytes, "HmacSHA256"))
        val rawHmac = mac.doFinal(message.toByteArray(StandardCharsets.UTF_8))
        return Base64.getUrlEncoder().withoutPadding().encodeToString(rawHmac)
    }
}
