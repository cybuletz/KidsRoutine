package com.kidsroutine.feature.avatar.ui

import androidx.lifecycle.SavedStateHandle
import com.kidsroutine.feature.avatar.data.AvatarRepository
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AvatarShopViewModelTest {
    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var repository: AvatarRepository
    private lateinit var viewModel: AvatarShopViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = mockk(relaxed = true)
        viewModel = AvatarShopViewModel(repository, SavedStateHandle())
    }

    @After
    fun tearDown() { Dispatchers.resetMain() }

    @Test
    fun `initial state is default`() {
        assertFalse(viewModel.uiState.value.isLoading)
        assertEquals(0, viewModel.uiState.value.xp)
    }

    @Test
    fun `init loads shop data`() = runTest {
        coEvery { repository.getUserXp("u1") } returns 200
        coEvery { repository.getOwnedAvatarPacks("u1") } returns setOf("pack1")
        viewModel.init("u1")
        advanceUntilIdle()
        assertEquals(200, viewModel.uiState.value.xp)
        assertTrue(viewModel.uiState.value.ownedPackIds.contains("pack1"))
    }

    @Test
    fun `init error sets error message`() = runTest {
        coEvery { repository.getUserXp("u1") } throws RuntimeException("Network")
        viewModel.init("u1")
        advanceUntilIdle()
        assertNotNull(viewModel.uiState.value.errorMessage)
    }
}
