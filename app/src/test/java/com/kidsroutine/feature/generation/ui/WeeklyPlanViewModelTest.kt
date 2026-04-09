package com.kidsroutine.feature.generation.ui

import com.kidsroutine.core.model.EntitlementsRepository
import com.kidsroutine.feature.generation.data.GenerationRepository
import com.kidsroutine.feature.generation.data.WeekTheme
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class WeeklyPlanViewModelTest {
    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var repository: GenerationRepository
    private lateinit var entitlementsRepository: EntitlementsRepository
    private lateinit var viewModel: WeeklyPlanViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = mockk(relaxed = true)
        entitlementsRepository = mockk(relaxed = true)
        viewModel = WeeklyPlanViewModel(repository, entitlementsRepository)
    }

    @After
    fun tearDown() { Dispatchers.resetMain() }

    @Test
    fun `initial state is default`() {
        assertFalse(viewModel.uiState.value.isLoading)
        assertEquals(WeekTheme.ADVENTURE, viewModel.uiState.value.selectedTheme)
    }

    @Test
    fun `selectTheme updates theme`() {
        viewModel.selectTheme(WeekTheme.SCIENCE)
        assertEquals(WeekTheme.SCIENCE, viewModel.uiState.value.selectedTheme)
    }

    @Test
    fun `toggleGoal adds goal`() {
        viewModel.toggleGoal("FOCUS")
        assertTrue(viewModel.uiState.value.selectedGoals.contains("FOCUS"))
    }

    @Test
    fun `toggleGoal removes existing goal`() {
        viewModel.toggleGoal("FOCUS")
        viewModel.toggleGoal("FOCUS")
        assertFalse(viewModel.uiState.value.selectedGoals.contains("FOCUS"))
    }
}
