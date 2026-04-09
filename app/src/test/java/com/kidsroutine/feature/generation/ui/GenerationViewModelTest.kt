package com.kidsroutine.feature.generation.ui

import com.kidsroutine.core.model.EntitlementsRepository
import com.kidsroutine.feature.daily.data.StoryArcRepository
import com.kidsroutine.feature.daily.data.TaskSaveRepository
import com.kidsroutine.feature.generation.data.GenerationRepository
import com.google.firebase.firestore.FirebaseFirestore
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class GenerationViewModelTest {
    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var repository: GenerationRepository
    private lateinit var taskSaveRepository: TaskSaveRepository
    private lateinit var firestore: FirebaseFirestore
    private lateinit var entitlementsRepository: EntitlementsRepository
    private lateinit var storyArcRepository: StoryArcRepository
    private lateinit var viewModel: GenerationViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = mockk(relaxed = true)
        taskSaveRepository = mockk(relaxed = true)
        firestore = mockk(relaxed = true)
        entitlementsRepository = mockk(relaxed = true)
        storyArcRepository = mockk(relaxed = true)
        viewModel = GenerationViewModel(repository, taskSaveRepository, firestore, entitlementsRepository, storyArcRepository)
    }

    @After
    fun tearDown() { Dispatchers.resetMain() }

    @Test
    fun `initial state has default values`() {
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertTrue(state.generatedTasks.isEmpty())
        assertNull(state.error)
    }

    @Test
    fun `setAge updates selectedAge`() {
        viewModel.setAge(8)
        assertEquals(8, viewModel.uiState.value.selectedAge)
    }

    @Test
    fun `toggleDifficulty updates difficulty`() {
        viewModel.toggleDifficulty("HARD")
        assertEquals("HARD", viewModel.uiState.value.selectedDifficulty)
    }

    @Test
    fun `togglePreference adds preference`() {
        viewModel.togglePreference("OUTDOORS")
        assertTrue(viewModel.uiState.value.selectedPreferences.contains("OUTDOORS"))
    }

    @Test
    fun `togglePreference removes existing preference`() {
        viewModel.togglePreference("OUTDOORS")
        viewModel.togglePreference("OUTDOORS")
        assertFalse(viewModel.uiState.value.selectedPreferences.contains("OUTDOORS"))
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
