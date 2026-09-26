package com.ticketing.mobile.core_network.auth

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
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
 * Lưu trữ trạng thái phiên đăng nhập bền vững qua SharedPreferences,
 * giúp người dùng không bị mất phiên khi đóng ứng dụng.
 */
class AuthManager private constructor() {

    private var prefs: SharedPreferences? = null

    private val _authState = MutableStateFlow<AuthState>(AuthState.Unauthenticated)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    fun init(context: Context) {
        if (prefs == null) {
            prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            loadSession()
        }
    }

    private fun loadSession() {
        val sp = prefs ?: return
        val isAuthenticated = sp.getBoolean(KEY_IS_AUTHENTICATED, false)
        if (isAuthenticated) {
            val userId = sp.getString(KEY_USER_ID, null)
            val email = sp.getString(KEY_EMAIL, "") ?: ""
            val displayName = sp.getString(KEY_DISPLAY_NAME, "") ?: ""
            val token = sp.getString(KEY_TOKEN, null)
            if (!userId.isNullOrBlank() && !token.isNullOrBlank()) {
                _authState.value = AuthState.Authenticated(
                    userId = userId,
                    email = email,
                    displayName = displayName,
                    token = token,
                    isDemo = false
                )
            } else {
                sp.edit { clear() }
            }
        }
    }

    fun login(userId: String, email: String, displayName: String, token: String) {
        _authState.value = AuthState.Authenticated(
            userId = userId,
            email = email,
            displayName = displayName,
            token = token,
            isDemo = false
        )
        prefs?.edit {
            putBoolean(KEY_IS_AUTHENTICATED, true)
            putString(KEY_USER_ID, userId)
            putString(KEY_EMAIL, email)
            putString(KEY_DISPLAY_NAME, displayName)
            putString(KEY_TOKEN, token)
            putBoolean(KEY_IS_DEMO, false)
        }
    }

    fun logout() {
        _authState.value = AuthState.Unauthenticated
        prefs?.edit { clear() }
    }

    fun getCurrentUserId(): String {
        return when (val state = _authState.value) {
            is AuthState.Authenticated -> state.userId
            else -> ""
        }
    }

    fun getBearerToken(): String? {
        return when (val state = _authState.value) {
            is AuthState.Authenticated -> state.token
            else -> null
        }
    }

    companion object {
        private const val PREFS_NAME = "secure_tix_auth_prefs"
        private const val KEY_IS_AUTHENTICATED = "is_authenticated"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_EMAIL = "email"
        private const val KEY_DISPLAY_NAME = "display_name"
        private const val KEY_TOKEN = "bearer_token"
        private const val KEY_IS_DEMO = "is_demo"

        val instance: AuthManager by lazy { AuthManager() }
    }
}
