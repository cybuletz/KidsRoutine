package com.kidsroutine.core.network

import org.junit.Assert.*
import org.junit.Test

class ResultTest {

    // ── Result.Success ──────────────────────────────────────────────

    @Test
    fun `Success wraps data`() {
        val result = Result.Success("hello")
        assertEquals("hello", result.data)
    }

    @Test
    fun `Success is Result instance`() {
        val result: Result<String> = Result.Success("test")
        assertTrue(result is Result.Success)
    }

    // ── Result.Error ────────────────────────────────────────────────

    @Test
    fun `Error stores message`() {
        val result = Result.Error("something went wrong")
        assertEquals("something went wrong", result.message)
    }

    @Test
    fun `Error stores optional cause`() {
        val exception = RuntimeException("boom")
        val result = Result.Error("failed", exception)
        assertEquals(exception, result.cause)
    }

    @Test
    fun `Error cause defaults to null`() {
        val result = Result.Error("failed")
        assertNull(result.cause)
    }

    @Test
    fun `Error is Result instance`() {
        val result: Result<String> = Result.Error("fail")
        assertTrue(result is Result.Error)
    }

    // ── Result.Loading ──────────────────────────────────────────────

    @Test
    fun `Loading is singleton`() {
        val a = Result.Loading
        val b = Result.Loading
        assertSame(a, b)
    }

    @Test
    fun `Loading is Result instance`() {
        val result: Result<String> = Result.Loading
        assertTrue(result is Result.Loading)
    }

    // ── onSuccess extension ─────────────────────────────────────────

    @Test
    fun `onSuccess executes action for Success`() {
        var captured = ""
        Result.Success("data").onSuccess { captured = it }
        assertEquals("data", captured)
    }

    @Test
    fun `onSuccess does not execute for Error`() {
        var called = false
        Result.Error("fail").onSuccess { called = true }
        assertFalse(called)
    }

    @Test
    fun `onSuccess does not execute for Loading`() {
        var called = false
        Result.Loading.onSuccess { called = true }
        assertFalse(called)
    }

    @Test
    fun `onSuccess returns same result`() {
        val original = Result.Success(42)
        val returned = original.onSuccess { }
        assertSame(original, returned)
    }

    // ── onError extension ───────────────────────────────────────────

    @Test
    fun `onError executes action for Error`() {
        var capturedMsg = ""
        var capturedCause: Throwable? = null
        val exception = RuntimeException("inner")
        Result.Error("fail", exception).onError { msg, cause ->
            capturedMsg = msg
            capturedCause = cause
        }
        assertEquals("fail", capturedMsg)
        assertEquals(exception, capturedCause)
    }

    @Test
    fun `onError does not execute for Success`() {
        var called = false
        Result.Success("data").onError { _, _ -> called = true }
        assertFalse(called)
    }

    @Test
    fun `onError does not execute for Loading`() {
        var called = false
        Result.Loading.onError { _, _ -> called = true }
        assertFalse(called)
    }

    @Test
    fun `onError returns same result`() {
        val original = Result.Error("fail")
        val returned = original.onError { _, _ -> }
        assertSame(original, returned)
    }

    // ── Chaining ────────────────────────────────────────────────────

    @Test
    fun `onSuccess and onError can be chained`() {
        var successCalled = false
        var errorCalled = false
        Result.Success("ok")
            .onSuccess { successCalled = true }
            .onError { _, _ -> errorCalled = true }
        assertTrue(successCalled)
        assertFalse(errorCalled)
    }

    @Test
    fun `chained Error triggers onError not onSuccess`() {
        var successCalled = false
        var errorCalled = false
        Result.Error("fail")
            .onSuccess { successCalled = true }
            .onError { _, _ -> errorCalled = true }
        assertFalse(successCalled)
        assertTrue(errorCalled)
    }
}
