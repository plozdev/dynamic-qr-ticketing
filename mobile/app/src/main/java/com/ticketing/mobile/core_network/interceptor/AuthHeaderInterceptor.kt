package com.ticketing.mobile.core_network.interceptor

import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.ticketing.mobile.core_network.auth.AuthManager
import okhttp3.Interceptor
import okhttp3.Response

/**
 * OkHttp Interceptor tự động bổ sung các Header bảo mật và thông tin xác thực
 * vào mọi Request gửi từ ứng dụng lên Backend Spring Boot.
 * 
 * Mục đích:
 * - Đính kèm Firebase JWT ID Token (Authorization: Bearer <token>) để Backend xác thực.
 * - Đính kèm X-User-Id định danh người dùng.
 * - Đính kèm dấu thời gian của client (X-Client-Timestamp).
 * - Định danh nền tảng ứng dụng (Android).
 */
class AuthHeaderInterceptor(
    private val tokenProvider: () -> String? = {
        val cached = AuthManager.instance.getBearerToken()
        if (!cached.isNullOrBlank()) {
            cached
        } else {
            try {
                val firebaseUser = FirebaseAuth.getInstance().currentUser
                if (firebaseUser != null) {
                    val tokenResult = Tasks.await(firebaseUser.getIdToken(false))
                    val freshToken = tokenResult.token
                    if (!freshToken.isNullOrBlank()) {
                        AuthManager.instance.setBearerToken(freshToken)
                    }
                    freshToken
                } else null
            } catch (e: Exception) {
                null
            }
        }
    },
    private val userIdProvider: () -> String = {
        AuthManager.instance.getCurrentUserId()
    }
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
