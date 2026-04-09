package com.kidsroutine.feature.daily.ui

import com.kidsroutine.core.model.EntitlementsRepository
import com.kidsroutine.core.model.UserModel
import com.kidsroutine.feature.generation.data.GenerationRepository
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AiSuggestionViewModelTest {
    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var generationRepository: GenerationRepository
    private lateinit var entitlementsRepository: EntitlementsRepository
    private lateinit var viewModel: AiSuggestionViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        generationRepository = mockk(relaxed = true)
        entitlementsRepository = mockk(relaxed = true)
        viewModel = AiSuggestionViewModel(generationRepository, entitlementsRepository)
    }

    @After
    fun tearDown() { Dispatchers.resetMain() }

    @Test
    fun `initial state is default`() {
        assertFalse(viewModel.uiState.value.isLoading)
        assertTrue(viewModel.uiState.value.suggestions.isEmpty())
        assertFalse(viewModel.uiState.value.dismissed)
    }

    @Test
    fun `dismiss sets dismissed true`() {
        viewModel.dismiss()
        assertTrue(viewModel.uiState.value.dismissed)
    }

    @Test
    fun `loadSuggestions while already loading is skipped`() = runTest {
        // First trigger loading
        val child = UserModel(userId = "u1", familyId = "fam1")
        coEvery { entitlementsRepository.getEntitlements(any(), any()) } returns com.kidsroutine.core.model.UserEntitlements()
        coEvery { generationRepository.generateTasks(any(), any(), any(), any(), any(), any()) } returns Result.success(
            com.kidsroutine.feature.generation.data.GeneratedTasksResponse(tasks = emptyList(), quotaRemaining = 1, cached = false)
        )
        viewModel.loadSuggestions(child)
        advanceUntilIdle()
        // Second call with existing suggestions should be skipped
        viewModel.loadSuggestions(child)
        coVerify(exactly = 1) { generationRepository.generateTasks(any(), any(), any(), any(), any(), any()) }
    }
}
