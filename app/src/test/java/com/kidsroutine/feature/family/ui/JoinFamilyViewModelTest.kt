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
class JoinFamilyViewModelTest {
    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var familyRepository: FamilyRepository
    private lateinit var viewModel: JoinFamilyViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        familyRepository = mockk(relaxed = true)
        viewModel = JoinFamilyViewModel(familyRepository)
    }

    @After
    fun tearDown() { Dispatchers.resetMain() }

    @Test
    fun `initial state is default`() {
        assertFalse(viewModel.uiState.value.isLoading)
        assertFalse(viewModel.uiState.value.success)
    }

    @Test
    fun `joinFamily success sets success true`() = runTest {
        coEvery { familyRepository.getFamiliesByInviteCode("ABC") } returns listOf(FamilyModel(familyId = "fam1"))
        viewModel.joinFamily("u1", "ABC")
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.success)
    }

    @Test
    fun `joinFamily invalid code sets error`() = runTest {
        coEvery { familyRepository.getFamiliesByInviteCode("BAD") } returns emptyList()
        viewModel.joinFamily("u1", "BAD")
        advanceUntilIdle()
        assertEquals("Invalid invite code", viewModel.uiState.value.error)
    }

    @Test
    fun `joinFamily error sets error`() = runTest {
        coEvery { familyRepository.getFamiliesByInviteCode("ABC") } throws RuntimeException("Net")
        viewModel.joinFamily("u1", "ABC")
        advanceUntilIdle()
        assertNotNull(viewModel.uiState.value.error)
    }
}
