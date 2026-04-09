package com.kidsroutine.feature.auth.ui

import com.kidsroutine.core.database.dao.UserDao
import com.kidsroutine.core.model.AuthState
import com.kidsroutine.core.model.UserModel
import com.kidsroutine.feature.auth.data.AuthRepository
import com.kidsroutine.feature.auth.domain.SignInAnonymouslyUseCase
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelTest {
    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var signInAnonymously: SignInAnonymouslyUseCase
    private lateinit var authRepository: AuthRepository
    private lateinit var userDao: UserDao
    private lateinit var viewModel: AuthViewModel

    private val testUser = UserModel(userId = "u1", displayName = "Test")

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        signInAnonymously = mockk(relaxed = true)
        authRepository = mockk(relaxed = true)
        userDao = mockk(relaxed = true)
        viewModel = AuthViewModel(signInAnonymously, authRepository, userDao)
    }

    @After
    fun tearDown() { Dispatchers.resetMain() }

    @Test
    fun `initial state is Unauthenticated`() {
        // FirebaseAuth.getInstance().currentUser returns null with returnDefaultValues
        assertTrue(viewModel.authState.value is AuthState.Unauthenticated)
    }

    @Test
    fun `signInAsGuest success sets Authenticated`() = runTest {
        coEvery { signInAnonymously() } returns testUser
        viewModel.signInAsGuest()
        advanceUntilIdle()
        val state = viewModel.authState.value
        assertTrue(state is AuthState.Authenticated)
        assertEquals("u1", (state as AuthState.Authenticated).user.userId)
    }

    @Test
    fun `signInAsGuest error sets Error state`() = runTest {
        coEvery { signInAnonymously() } throws RuntimeException("Network error")
        viewModel.signInAsGuest()
        advanceUntilIdle()
        assertTrue(viewModel.authState.value is AuthState.Error)
    }

    @Test
    fun `loginWithEmail success sets Authenticated`() = runTest {
        coEvery { authRepository.signInWithEmail("a@b.com", "pass") } returns testUser
        viewModel.loginWithEmail("a@b.com", "pass")
        advanceUntilIdle()
        assertTrue(viewModel.authState.value is AuthState.Authenticated)
    }

    @Test
    fun `loginWithEmail error sets Error state`() = runTest {
        coEvery { authRepository.signInWithEmail(any(), any()) } throws RuntimeException("Invalid")
        viewModel.loginWithEmail("bad", "bad")
        advanceUntilIdle()
        assertTrue(viewModel.authState.value is AuthState.Error)
    }

    @Test
    fun `logout calls repository signOut`() = runTest {
        viewModel.logout()
        advanceUntilIdle()
        coVerify { authRepository.signOut() }
    }
}
