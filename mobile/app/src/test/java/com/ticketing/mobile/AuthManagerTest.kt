package com.ticketing.mobile

import com.ticketing.mobile.core_network.auth.AuthManager
import com.ticketing.mobile.core_network.auth.AuthState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AuthManagerTest {

    @Test
    fun testLoginStoresBackendSession() {
        val authManager = AuthManager.instance
        authManager.login("user-123", "test@example.com", "Test User", "session-token")

        val state = authManager.authState.value
        assertTrue(state is AuthState.Authenticated)
        val auth = state as AuthState.Authenticated
        assertEquals("user-123", auth.userId)
        assertEquals("test@example.com", auth.email)
        assertEquals("Test User", auth.displayName)
        assertEquals("session-token", authManager.getBearerToken())
        assertEquals(false, auth.isDemo)
    }

    @Test
    fun testLogout() {
        val authManager = AuthManager.instance
        authManager.logout()

        val state = authManager.authState.value
        assertTrue(state is AuthState.Unauthenticated)
        assertEquals(null, authManager.getBearerToken())
    }
}
