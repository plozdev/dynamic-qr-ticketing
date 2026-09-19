package com.ticketing.mobile.core_network.interceptor

import com.ticketing.mobile.core_network.auth.AuthManager
import okhttp3.Interceptor
import okhttp3.Response

/**
 * OkHttp Interceptor tự động bổ sung các Header bảo mật và thông tin xác thực
 * vào mọi Request gửi từ ứng dụng lên Backend Spring Boot.
 * 
 * Mục đích:
 * - Đính kèm JWT Access Token (Bearer) nếu người dùng đã đăng nhập Firebase.
 * - Đính kèm X-User-Id định danh người dùng.
 * - Đính kèm dấu thời gian của client (X-Client-Timestamp) để Backend phát hiện các cuộc tấn công phát lại (Replay Attacks).
 * - Định danh nền tảng ứng dụng (Android).
 */
class AuthHeaderInterceptor(
    private val tokenProvider: () -> String? = { AuthManager.instance.getBearerToken() },
    private val userIdProvider: () -> String = { AuthManager.instance.getCurrentUserId() }
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val builder = originalRequest.newBuilder()

        val token = tokenProvider()
        if (!token.isNullOrBlank()) {
            builder.header("Authorization", "Bearer $token")
        }
        val userId = userIdProvider()
        if (userId.isNotBlank()) {
            builder.header("X-User-Id", userId)
        }
        builder.header("X-App-Platform", "Android")
        builder.header("X-Client-Timestamp", (System.currentTimeMillis() / 1000).toString())
        builder.header("Accept", "application/json")

        return chain.proceed(builder.build())
    }
}
