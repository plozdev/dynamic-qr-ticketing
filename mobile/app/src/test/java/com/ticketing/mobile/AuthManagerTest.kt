package com.ticketing.mobile

import com.ticketing.mobile.core_network.auth.AuthManager
import com.ticketing.mobile.core_network.auth.AuthState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AuthManagerTest {

    @Test
    fun testDemoUserLogin() {
        val authManager = AuthManager.instance
        authManager.loginWithDemoUser()

        val state = authManager.authState.value
        assertTrue(state is AuthState.Authenticated)
        val auth = state as AuthState.Authenticated
        assertEquals(AuthManager.DEMO_USER_ID, auth.userId)
        assertTrue(auth.isDemo)
    }

    @Test
    fun testEmailLogin() {
        val authManager = AuthManager.instance
        authManager.loginWithEmail("test@example.com", "Nguyễn Test")

        val state = authManager.authState.value
        assertTrue(state is AuthState.Authenticated)
        val auth = state as AuthState.Authenticated
        assertEquals("test@example.com", auth.email)
        assertEquals("Nguyễn Test", auth.displayName)
    }

    @Test
    fun testLogout() {
        val authManager = AuthManager.instance
        authManager.logout()

        val state = authManager.authState.value
        assertTrue(state is AuthState.Unauthenticated)
    }
}
