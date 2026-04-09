package com.kidsroutine.feature.auth.domain

import com.kidsroutine.core.model.UserModel
import com.kidsroutine.feature.auth.data.AuthRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class SignInAnonymouslyUseCaseTest {

    private lateinit var authRepository: AuthRepository
    private lateinit var useCase: SignInAnonymouslyUseCase

    @Before
    fun setUp() {
        authRepository = mockk()
        useCase = SignInAnonymouslyUseCase(authRepository)
    }

    @Test
    fun `invoke delegates to authRepository signInAnonymously`() = runTest {
        val expected = UserModel(userId = "anon_123", displayName = "Anonymous")
        coEvery { authRepository.signInAnonymously() } returns expected

        val result = useCase()

        assertEquals("anon_123", result.userId)
        assertEquals("Anonymous", result.displayName)
        coVerify(exactly = 1) { authRepository.signInAnonymously() }
    }

    @Test
    fun `returns UserModel from repository`() = runTest {
        val user = UserModel(userId = "u1", role = com.kidsroutine.core.model.Role.CHILD)
        coEvery { authRepository.signInAnonymously() } returns user

        val result = useCase()
        assertEquals(com.kidsroutine.core.model.Role.CHILD, result.role)
    }

    @Test
    fun `propagates exception from repository`() = runTest {
        coEvery { authRepository.signInAnonymously() } throws RuntimeException("Auth failed")

        try {
            useCase()
            fail("Expected RuntimeException")
        } catch (e: RuntimeException) {
            assertEquals("Auth failed", e.message)
        }
    }
}
