package com.kidsroutine.feature.tasks.ui

import com.kidsroutine.core.model.TaskModel
import com.kidsroutine.feature.tasks.data.TaskRepository
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TaskManagementViewModelTest {
    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var taskRepository: TaskRepository
    private lateinit var viewModel: TaskManagementViewModel

    private val testTask = TaskModel(id = "t1", title = "Homework")

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        taskRepository = mockk(relaxed = true)
        viewModel = TaskManagementViewModel(taskRepository)
    }

    @After
    fun tearDown() { Dispatchers.resetMain() }

    @Test
    fun `initial state is default`() {
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertTrue(state.tasks.isEmpty())
        assertNull(state.error)
    }

    @Test
    fun `loadFamilyTasks with empty ID sets error`() {
        viewModel.loadFamilyTasks("")
        assertEquals("Invalid family ID", viewModel.uiState.value.error)
    }

    @Test
    fun `loadFamilyTasks success loads tasks`() = runTest {
        coEvery { taskRepository.getFamilyTasks("fam1") } returns listOf(testTask)
        viewModel.loadFamilyTasks("fam1")
        advanceUntilIdle()
        assertEquals(1, viewModel.uiState.value.tasks.size)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `loadFamilyTasks error sets error message`() = runTest {
        coEvery { taskRepository.getFamilyTasks("fam1") } throws RuntimeException("DB fail")
        viewModel.loadFamilyTasks("fam1")
        advanceUntilIdle()
        assertEquals("DB fail", viewModel.uiState.value.error)
    }

    @Test
    fun `createTask success sets success message`() = runTest {
        viewModel.createTask("fam1", testTask)
        advanceUntilIdle()
        assertNotNull(viewModel.uiState.value.successMessage)
        coVerify { taskRepository.createTask("fam1", testTask) }
    }

    @Test
    fun `deleteTask success calls repository`() = runTest {
        viewModel.deleteTask("fam1", "t1")
        advanceUntilIdle()
        coVerify { taskRepository.deleteTask("fam1", "t1") }
    }

    @Test
    fun `clearMessages clears success and error`() {
        viewModel.clearMessages()
        assertNull(viewModel.uiState.value.successMessage)
        assertNull(viewModel.uiState.value.error)
    }
}
