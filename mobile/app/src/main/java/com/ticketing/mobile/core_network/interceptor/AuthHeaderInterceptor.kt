package com.ticketing.mobile.core_network.interceptor

import com.ticketing.mobile.core_network.auth.AuthManager
import okhttp3.Interceptor
import okhttp3.Response

/**
 * OkHttp Interceptor tự động bổ sung các Header bảo mật và thông tin xác thực
 * vào mọi Request gửi từ ứng dụng lên Backend Spring Boot.
 */
class AuthHeaderInterceptor(
    private val tokenProvider: () -> String? = {
        AuthManager.instance.getBearerToken()
    }
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val builder = originalRequest.newBuilder()

        val token = tokenProvider()
        if (!token.isNullOrBlank()) {
            builder.header("Authorization", "Bearer $token")
        }
        builder.header("X-App-Platform", "Android")
        builder.header("X-Client-Timestamp", (System.currentTimeMillis() / 1000).toString())
        if (originalRequest.header("Accept").isNullOrBlank()) {
            builder.header("Accept", "application/json")
        }

        return chain.proceed(builder.build())
    }
}
