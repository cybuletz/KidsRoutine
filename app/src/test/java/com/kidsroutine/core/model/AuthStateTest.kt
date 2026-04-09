package com.kidsroutine.core.model

import org.junit.Assert.*
import org.junit.Test

class AuthStateTest {

    // ── Unauthenticated ─────────────────────────────────────────────

    @Test
    fun `Unauthenticated is AuthState`() {
        val state: AuthState = AuthState.Unauthenticated
        assertTrue(state is AuthState.Unauthenticated)
    }

    @Test
    fun `Unauthenticated is a data object singleton`() {
        assertSame(AuthState.Unauthenticated, AuthState.Unauthenticated)
    }

    // ── Loading ─────────────────────────────────────────────────────

    @Test
    fun `Loading is AuthState`() {
        val state: AuthState = AuthState.Loading
        assertTrue(state is AuthState.Loading)
    }

    @Test
    fun `Loading is a data object singleton`() {
        assertSame(AuthState.Loading, AuthState.Loading)
    }

    // ── Authenticated ───────────────────────────────────────────────

    @Test
    fun `Authenticated holds user`() {
        val user = UserModel(userId = "u1", displayName = "Alice")
        val state = AuthState.Authenticated(user)
        assertEquals("u1", state.user.userId)
        assertEquals("Alice", state.user.displayName)
    }

    @Test
    fun `Authenticated is AuthState`() {
        val state: AuthState = AuthState.Authenticated(UserModel())
        assertTrue(state is AuthState.Authenticated)
    }

    @Test
    fun `Authenticated equality based on user`() {
        val user = UserModel(userId = "u1")
        val a = AuthState.Authenticated(user)
        val b = AuthState.Authenticated(user)
        assertEquals(a, b)
    }

    @Test
    fun `Authenticated not equal with different users`() {
        val a = AuthState.Authenticated(UserModel(userId = "u1"))
        val b = AuthState.Authenticated(UserModel(userId = "u2"))
        assertNotEquals(a, b)
    }

    // ── Error ───────────────────────────────────────────────────────

    @Test
    fun `Error holds message`() {
        val state = AuthState.Error("Network failure")
        assertEquals("Network failure", state.message)
    }

    @Test
    fun `Error is AuthState`() {
        val state: AuthState = AuthState.Error("oops")
        assertTrue(state is AuthState.Error)
    }

    @Test
    fun `Error equality based on message`() {
        val a = AuthState.Error("msg")
        val b = AuthState.Error("msg")
        assertEquals(a, b)
    }

    @Test
    fun `Error not equal with different messages`() {
        val a = AuthState.Error("msg1")
        val b = AuthState.Error("msg2")
        assertNotEquals(a, b)
    }

    // ── when exhaustive ─────────────────────────────────────────────

    @Test
    fun `when covers all branches`() {
        val states = listOf(
            AuthState.Unauthenticated,
            AuthState.Loading,
            AuthState.Authenticated(UserModel()),
            AuthState.Error("err")
        )
        for (state in states) {
            val label = when (state) {
                is AuthState.Unauthenticated -> "unauth"
                is AuthState.Loading -> "loading"
                is AuthState.Authenticated -> "auth:${state.user.userId}"
                is AuthState.Error -> "error:${state.message}"
            }
            assertTrue(label.isNotEmpty())
        }
    }
}
