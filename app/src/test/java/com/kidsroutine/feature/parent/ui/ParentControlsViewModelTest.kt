package com.kidsroutine.feature.parent.ui

import com.kidsroutine.core.model.EntitlementsRepository
import com.kidsroutine.core.model.ParentControlSettings
import com.kidsroutine.core.model.UserEntitlements
import com.kidsroutine.core.model.XpLoan
import com.kidsroutine.feature.daily.data.UserRepository
import com.kidsroutine.feature.parent.data.ParentControlRepository
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ParentControlsViewModelTest {
    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var parentControlRepository: ParentControlRepository
    private lateinit var userRepository: UserRepository
    private lateinit var entitlementsRepository: EntitlementsRepository
    private lateinit var viewModel: ParentControlsViewModel

    private val testSettings = ParentControlSettings(childId = "c1", familyId = "fam1")

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        parentControlRepository = mockk(relaxed = true)
        userRepository = mockk(relaxed = true)
        entitlementsRepository = mockk(relaxed = true)
        viewModel = ParentControlsViewModel(parentControlRepository, userRepository, entitlementsRepository)
    }

    @After
    fun tearDown() { Dispatchers.resetMain() }

    @Test
    fun `initial state is default`() {
        assertFalse(viewModel.uiState.value.isLoading)
        assertNull(viewModel.uiState.value.error)
    }

    @Test
    fun `loadForChild success sets settings`() = runTest {
        coEvery { parentControlRepository.getControlSettings("fam1", "c1") } returns testSettings
        coEvery { parentControlRepository.getActiveLoans("fam1", "c1") } returns emptyList()
        coEvery { entitlementsRepository.getEntitlements("p1", "fam1") } returns UserEntitlements()
        viewModel.loadForChild("fam1", "c1", "p1")
        advanceUntilIdle()
        assertFalse(viewModel.uiState.value.isLoading)
        assertNotNull(viewModel.uiState.value.controlSettings)
    }

    @Test
    fun `loadForChild error sets error`() = runTest {
        coEvery { parentControlRepository.getControlSettings("fam1", "c1") } throws RuntimeException("Fail")
        viewModel.loadForChild("fam1", "c1", "p1")
        advanceUntilIdle()
        assertNotNull(viewModel.uiState.value.error)
    }

    @Test
    fun `showLoanDialog sets flag`() {
        viewModel.showLoanDialog()
        assertTrue(viewModel.uiState.value.showLoanDialog)
    }

    @Test
    fun `dismissLoanDialog clears flag`() {
        viewModel.showLoanDialog()
        viewModel.dismissLoanDialog()
        assertFalse(viewModel.uiState.value.showLoanDialog)
    }

    @Test
    fun `clearMessage clears error and success`() {
        viewModel.clearMessage()
        assertNull(viewModel.uiState.value.error)
        assertNull(viewModel.uiState.value.successMessage)
    }
}
