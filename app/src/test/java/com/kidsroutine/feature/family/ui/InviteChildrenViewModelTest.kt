package com.kidsroutine.feature.family.ui

import com.kidsroutine.core.model.FamilyModel
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
class InviteChildrenViewModelTest {
    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var familyRepository: FamilyRepository
    private lateinit var viewModel: InviteChildrenViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        familyRepository = mockk(relaxed = true)
        viewModel = InviteChildrenViewModel(familyRepository)
    }

    @After
    fun tearDown() { Dispatchers.resetMain() }

    @Test
    fun `initial state is default`() {
        assertFalse(viewModel.uiState.value.isLoading)
        assertNull(viewModel.uiState.value.family)
    }

    @Test
    fun `loadFamily empty ID sets error`() {
        viewModel.loadFamily("")
        assertEquals("Invalid family ID", viewModel.uiState.value.error)
    }

    @Test
    fun `loadFamily success sets family and invite code`() = runTest {
        coEvery { familyRepository.getFamily("fam1") } returns FamilyModel(familyId = "fam1", familyName = "Test")
        coEvery { familyRepository.getInviteCode("fam1") } returns "ABC123"
        viewModel.loadFamily("fam1")
        advanceUntilIdle()
        assertNotNull(viewModel.uiState.value.family)
        assertEquals("ABC123", viewModel.uiState.value.inviteCode)
    }

    @Test
    fun `loadFamily null family sets error`() = runTest {
        coEvery { familyRepository.getFamily("fam1") } returns null
        viewModel.loadFamily("fam1")
        advanceUntilIdle()
        assertEquals("Family not found", viewModel.uiState.value.error)
    }

    @Test
    fun `loadFamily exception sets error`() = runTest {
        coEvery { familyRepository.getFamily("fam1") } throws RuntimeException("Net")
        viewModel.loadFamily("fam1")
        advanceUntilIdle()
        assertNotNull(viewModel.uiState.value.error)
    }
}
