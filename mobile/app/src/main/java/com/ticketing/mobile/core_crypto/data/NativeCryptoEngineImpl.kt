package com.ticketing.mobile.core_crypto.data

import com.ticketing.mobile.core_crypto.data.native_bridge.NativeCryptoBridge
import com.ticketing.mobile.core_crypto.domain.model.CryptoToken
import com.ticketing.mobile.core_crypto.domain.model.VerificationResult
import com.ticketing.mobile.core_crypto.domain.repository.ICryptoEngine

/**
 * Khung sườn triển khai ICryptoEngine ủy quyền tính toán cho C++ NDK qua NativeCryptoBridge.
 */
class NativeCryptoEngineImpl(
    private val bridge: NativeCryptoBridge = NativeCryptoBridge()
) : ICryptoEngine {

    override suspend fun generateToken(
        ticketId: String,
        secretKey: String,
        epochSeconds: Long,
        intervalSeconds: Int
    ): Result<CryptoToken> {
        // TODO: [Công đoạn 1] Gọi bridge.generateDynamicTotpToken() trên Dispatchers.Default
        // và đóng gói trả về Result.success(CryptoToken(...))
        TODO("Tự triển khai logic gọi JNI để sinh Dynamic Token")
    }

    override suspend fun verifyToken(
        ticketId: String,
        secretKey: String,
        token: String,
        epochSeconds: Long,
        intervalSeconds: Int,
        allowedDriftSteps: Int
    ): VerificationResult {
        // TODO: [Công đoạn 1] Gọi bridge.verifyDynamicTotpToken() và ánh xạ kết quả boolean sang VerificationResult
        TODO("Tự triển khai logic gọi JNI để xác thực Token")
    }

    override fun getEngineVersion(): String {
        return try {
            bridge.getSecurityVersion()
        } catch (e: Throwable) {
            "unknown-native-unavailable"
        }
    }
}
