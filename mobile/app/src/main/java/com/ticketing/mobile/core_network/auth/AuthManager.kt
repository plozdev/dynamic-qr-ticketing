package com.ticketing.mobile.core_network.auth

import com.google.firebase.auth.FirebaseAuth
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
 * Tích hợp chặt chẽ với Firebase Authentication.
 */
class AuthManager private constructor() {

    private val _authState = MutableStateFlow<AuthState>(initInitialAuthState())
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private fun initInitialAuthState(): AuthState {
        return try {
            val user = FirebaseAuth.getInstance().currentUser
            if (user != null) {
                AuthState.Authenticated(
                    userId = user.uid,
                    email = user.email ?: "${user.uid}@dynamic-qr.vn",
                    displayName = user.displayName ?: "Khán Giả",
                    token = null,
                    isDemo = false
                )
            } else {
                AuthState.Unauthenticated
            }
        } catch (e: Exception) {
            AuthState.Unauthenticated
        }
    }

    fun loginWithDemoUser() {
        _authState.value = AuthState.Authenticated(
            userId = DEMO_USER_ID,
            email = "demo@dynamic-qr.vn",
            displayName = "Khán Giả (Demo)",
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

    fun setBearerToken(token: String) {
        val current = _authState.value
        if (current is AuthState.Authenticated) {
            _authState.value = current.copy(token = token)
        }
    }

    fun logout() {
        try {
            FirebaseAuth.getInstance().signOut()
        } catch (ignored: Exception) {}
        _authState.value = AuthState.Unauthenticated
    }

    fun getCurrentUserId(): String {
        return when (val state = _authState.value) {
            is AuthState.Authenticated -> state.userId
            else -> try {
                FirebaseAuth.getInstance().currentUser?.uid ?: DEMO_USER_ID
            } catch (e: Exception) {
                DEMO_USER_ID
            }
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
