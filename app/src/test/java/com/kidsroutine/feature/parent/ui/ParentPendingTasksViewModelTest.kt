package com.kidsroutine.feature.parent.ui

import com.kidsroutine.core.model.TaskModel
import com.kidsroutine.feature.family.data.FamilyRepository
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ParentPendingTasksViewModelTest {
    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var familyRepository: FamilyRepository
    private lateinit var viewModel: ParentPendingTasksViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        familyRepository = mockk(relaxed = true)
        viewModel = ParentPendingTasksViewModel(familyRepository)
    }

    @After
    fun tearDown() { Dispatchers.resetMain() }

    @Test
    fun `initial state is default`() {
        assertFalse(viewModel.uiState.value.isLoading)
        assertTrue(viewModel.uiState.value.pendingTasks.isEmpty())
    }

    @Test
    fun `loadPendingTasks empty familyId sets error`() {
        viewModel.loadPendingTasks("")
        assertEquals("Invalid family ID", viewModel.uiState.value.error)
    }

    @Test
    fun `loadPendingTasks success loads tasks`() = runTest {
        coEvery { familyRepository.getPendingChildTasks("fam1") } returns listOf(TaskModel(id = "t1"))
        viewModel.loadPendingTasks("fam1")
        advanceUntilIdle()
        assertEquals(1, viewModel.uiState.value.pendingTasks.size)
    }

    @Test
    fun `loadPendingTasks error sets error`() = runTest {
        coEvery { familyRepository.getPendingChildTasks("fam1") } throws RuntimeException("Err")
        viewModel.loadPendingTasks("fam1")
        advanceUntilIdle()
        assertNotNull(viewModel.uiState.value.error)
    }

    @Test
    fun `approveTask removes task from list`() = runTest {
        coEvery { familyRepository.getPendingChildTasks("fam1") } returns listOf(TaskModel(id = "t1"), TaskModel(id = "t2"))
        viewModel.loadPendingTasks("fam1")
        advanceUntilIdle()
        viewModel.approveTask("fam1", "t1")
        advanceUntilIdle()
        assertEquals(1, viewModel.uiState.value.pendingTasks.size)
    }

    @Test
    fun `clearMessages clears state`() {
        viewModel.clearMessages()
        assertNull(viewModel.uiState.value.error)
        assertNull(viewModel.uiState.value.successMessage)
    }
}
