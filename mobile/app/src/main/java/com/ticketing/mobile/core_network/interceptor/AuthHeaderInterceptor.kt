package com.ticketing.mobile.core_network.interceptor

import okhttp3.Interceptor
import okhttp3.Response

/**
 * Khung sườn Interceptor tự động tiêm Token xác thực và Fingerprint thiết bị vào Request.
 */
class AuthHeaderInterceptor(
    private val tokenProvider: () -> String? = { null }
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        // TODO: [Giai đoạn 2] Tự triển khai logic thêm các Header bảo mật:
        // - Authorization: Bearer <tokenProvider()>
        // - X-App-Platform: Android
        // - X-Client-Timestamp: System.currentTimeMillis()
        val originalRequest = chain.request()
        val builder = originalRequest.newBuilder()
        return chain.proceed(builder.build())
    }
}
