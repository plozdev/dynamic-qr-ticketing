package com.ticketing.mobile.core_network.auth

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

sealed interface AuthState {
    object Unauthenticated : AuthState
    object Loading : AuthState
    data class Authenticated(
        val userId: String,
        val email: String,
        val displayName: String,
        val token: String?,
        val isDemo: Boolean = false
    ) : AuthState
}

/**
 * Trình quản lý xác thực và danh tính người dùng trên thiết bị di động.
 * Hỗ trợ đồng thời:
 * 1. Đăng nhập nhanh Demo Account cho nhà phát triển / người chấm bài / kiểm thử.
 * 2. Xác thực Firebase Auth JWT ID Token khi cấu hình google-services.json.
 */
class AuthManager private constructor() {

    private val _authState = MutableStateFlow<AuthState>(
        // Khởi tạo mặc định ở trạng thái Demo Account đã sẵn sàng
        AuthState.Authenticated(
            userId = DEMO_USER_ID,
            email = "hoanglong@dynamic-qr.vn",
            displayName = "Nguyễn Hoàng Long",
            token = null,
            isDemo = true
        )
    )
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    fun loginWithDemoUser() {
        _authState.value = AuthState.Authenticated(
            userId = DEMO_USER_ID,
            email = "hoanglong@dynamic-qr.vn",
            displayName = "Nguyễn Hoàng Long",
            token = null,
            isDemo = true
        )
    }

    fun loginWithEmail(email: String, name: String) {
        val uid = java.util.UUID.randomUUID().toString()
        _authState.value = AuthState.Authenticated(
            userId = uid,
            email = email,
            displayName = name.ifBlank { email.substringBefore("@") },
            token = "jwt-mock-$uid",
            isDemo = false
        )
    }

    fun loginWithFirebase(userId: String, email: String, displayName: String, idToken: String) {
        _authState.value = AuthState.Authenticated(
            userId = userId,
            email = email,
            displayName = displayName,
            token = idToken,
            isDemo = false
        )
    }

    fun logout() {
        _authState.value = AuthState.Unauthenticated
    }

    fun getCurrentUserId(): String {
        return when (val state = _authState.value) {
            is AuthState.Authenticated -> state.userId
            else -> DEMO_USER_ID
        }
    }

    fun getBearerToken(): String? {
        return when (val state = _authState.value) {
            is AuthState.Authenticated -> state.token
            else -> null
        }
    }

    companion object {
        const val DEMO_USER_ID = "11111111-2222-3333-4444-555555555555"

        val instance: AuthManager by lazy { AuthManager() }
    }
}
