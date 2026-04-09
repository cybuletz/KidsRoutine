package com.kidsroutine.feature.community.ui

import com.kidsroutine.core.model.DifficultyLevel
import com.kidsroutine.core.model.TaskCategory
import com.kidsroutine.feature.community.data.CommunityRepository
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PublishViewModelTest {
    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var communityRepository: CommunityRepository
    private lateinit var viewModel: PublishViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        communityRepository = mockk(relaxed = true)
        viewModel = PublishViewModel(communityRepository)
    }

    @After
    fun tearDown() { Dispatchers.resetMain() }

    @Test
    fun `initial state is default`() {
        assertEquals(PublishTab.TASK, viewModel.uiState.value.activeTab)
        assertEquals("", viewModel.uiState.value.taskTitle)
    }

    @Test
    fun `selectTab updates tab`() {
        viewModel.selectTab(PublishTab.CHALLENGE)
        assertEquals(PublishTab.CHALLENGE, viewModel.uiState.value.activeTab)
    }

    @Test
    fun `updateTaskTitle updates title`() {
        viewModel.updateTaskTitle("Homework")
        assertEquals("Homework", viewModel.uiState.value.taskTitle)
    }

    @Test
    fun `updateTaskDescription updates description`() {
        viewModel.updateTaskDescription("Do math")
        assertEquals("Do math", viewModel.uiState.value.taskDescription)
    }

    @Test
    fun `updateTaskCategory updates category`() {
        viewModel.updateTaskCategory(TaskCategory.HEALTH)
        assertEquals(TaskCategory.HEALTH, viewModel.uiState.value.taskCategory)
    }

    @Test
    fun `updateTaskDifficulty updates difficulty`() {
        viewModel.updateTaskDifficulty(DifficultyLevel.HARD)
        assertEquals(DifficultyLevel.HARD, viewModel.uiState.value.taskDifficulty)
    }
}
