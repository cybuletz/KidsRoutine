package com.kidsroutine.feature.family.ui

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
class ChildTaskProposalViewModelTest {
    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var familyRepository: FamilyRepository
    private lateinit var viewModel: ChildTaskProposalViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        familyRepository = mockk(relaxed = true)
        viewModel = ChildTaskProposalViewModel(familyRepository)
    }

    @After
    fun tearDown() { Dispatchers.resetMain() }

    @Test
    fun `initial state is default`() {
        assertFalse(viewModel.uiState.value.isSubmitting)
        assertNull(viewModel.uiState.value.error)
    }

    @Test
    fun `proposeTask empty familyId sets error`() {
        viewModel.proposeTask("", "c1", "Task", "desc", 10)
        assertEquals("Invalid family or child ID", viewModel.uiState.value.error)
    }

    @Test
    fun `proposeTask empty title sets error`() {
        viewModel.proposeTask("fam1", "c1", "", "desc", 10)
        assertEquals("Task title is required", viewModel.uiState.value.error)
    }

    @Test
    fun `proposeTask success sets success message`() = runTest {
        viewModel.proposeTask("fam1", "c1", "Clean room", "desc", 20)
        advanceUntilIdle()
        assertNotNull(viewModel.uiState.value.successMessage)
    }

    @Test
    fun `clearMessages clears state`() {
        viewModel.clearMessages()
        assertNull(viewModel.uiState.value.error)
        assertNull(viewModel.uiState.value.successMessage)
    }
}
