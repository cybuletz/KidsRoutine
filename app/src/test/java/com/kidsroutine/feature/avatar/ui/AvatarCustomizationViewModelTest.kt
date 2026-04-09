package com.kidsroutine.feature.avatar.ui

import androidx.lifecycle.SavedStateHandle
import com.kidsroutine.core.model.AvatarState
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
class AvatarCustomizationViewModelTest {
    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var repository: AvatarRepository
    private lateinit var viewModel: AvatarCustomizationViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = mockk(relaxed = true)
        viewModel = AvatarCustomizationViewModel(repository, SavedStateHandle())
    }

    @After
    fun tearDown() { Dispatchers.resetMain() }

    @Test
    fun `initial state is default`() {
        assertFalse(viewModel.uiState.value.isLoading)
        assertFalse(viewModel.uiState.value.isSaving)
    }

    @Test
    fun `initWithUserId loads avatar data`() = runTest {
        coEvery { repository.getAvatar("u1") } returns AvatarState(userId = "u1")
        coEvery { repository.getCoins("u1") } returns 50
        coEvery { repository.getPlayerName("u1") } returns "Alice"
        coEvery { repository.getUnlockedItemIds("u1") } returns setOf("item1")
        viewModel.initWithUserId("u1")
        advanceUntilIdle()
        assertFalse(viewModel.uiState.value.isLoading)
        assertEquals(50, viewModel.uiState.value.coins)
    }

    @Test
    fun `initWithUserId error sets error message`() = runTest {
        coEvery { repository.getAvatar("u1") } throws RuntimeException("Fail")
        viewModel.initWithUserId("u1")
        advanceUntilIdle()
        assertNotNull(viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `hasUnsavedChanges is false initially`() {
        assertFalse(viewModel.uiState.value.hasUnsavedChanges)
    }
}
