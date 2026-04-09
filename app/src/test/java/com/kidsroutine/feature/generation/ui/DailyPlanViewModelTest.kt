package com.kidsroutine.feature.generation.ui

import com.kidsroutine.core.model.UserModel
import com.kidsroutine.feature.generation.data.DayMood
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
class DailyPlanViewModelTest {
    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var repository: GenerationRepository
    private lateinit var viewModel: DailyPlanViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = mockk(relaxed = true)
        viewModel = DailyPlanViewModel(repository)
    }

    @After
    fun tearDown() { Dispatchers.resetMain() }

    @Test
    fun `initial state is default`() {
        assertFalse(viewModel.uiState.value.isLoading)
        assertEquals(DayMood.NORMAL, viewModel.uiState.value.selectedMood)
        assertNull(viewModel.uiState.value.plan)
    }

    @Test
    fun `selectMood updates mood and resets plan`() {
        viewModel.selectMood(DayMood.ENERGETIC)
        assertEquals(DayMood.ENERGETIC, viewModel.uiState.value.selectedMood)
        assertNull(viewModel.uiState.value.plan)
    }

    @Test
    fun `reset restores default state`() {
        viewModel.selectMood(DayMood.ENERGETIC)
        viewModel.reset()
        assertEquals(DayMood.NORMAL, viewModel.uiState.value.selectedMood)
    }

    @Test
    fun `clearError clears error`() {
        viewModel.clearError()
        assertNull(viewModel.uiState.value.error)
    }
}
