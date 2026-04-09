package com.kidsroutine.feature.challenges.ui

import com.kidsroutine.core.model.ChallengeModel
import com.kidsroutine.feature.challenges.data.ChallengeRepository
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class StartChallengesViewModelTest {
    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var challengeRepository: ChallengeRepository
    private lateinit var viewModel: StartChallengesViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        challengeRepository = mockk(relaxed = true)
        viewModel = StartChallengesViewModel(challengeRepository)
    }

    @After
    fun tearDown() { Dispatchers.resetMain() }

    @Test
    fun `initial state is default`() {
        assertFalse(viewModel.uiState.value.isLoading)
        assertTrue(viewModel.uiState.value.availableChallenges.isEmpty())
    }

    @Test
    fun `loadAvailableChallenges success loads challenges`() = runTest {
        coEvery { challengeRepository.getSystemChallenges() } returns listOf(ChallengeModel(challengeId = "ch1"))
        coEvery { challengeRepository.getFamilyChallenges("fam1") } returns emptyList()
        viewModel.loadAvailableChallenges("fam1")
        advanceUntilIdle()
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `loadAvailableChallenges error sets error`() = runTest {
        coEvery { challengeRepository.getSystemChallenges() } throws RuntimeException("Error")
        viewModel.loadAvailableChallenges("fam1")
        advanceUntilIdle()
        assertNotNull(viewModel.uiState.value.error)
    }

    @Test
    fun `filterByCategory ALL shows all`() {
        viewModel.filterByCategory("ALL")
        assertEquals("ALL", viewModel.uiState.value.selectedCategory)
    }

    @Test
    fun `startChallenge success sets success message`() = runTest {
        viewModel.startChallenge("u1", "fam1", "ch1")
        advanceUntilIdle()
        assertNotNull(viewModel.uiState.value.successMessage)
        coVerify { challengeRepository.startChallenge("u1", "fam1", "ch1") }
    }

    @Test
    fun `clearMessages clears messages`() {
        viewModel.clearMessages()
        assertNull(viewModel.uiState.value.successMessage)
        assertNull(viewModel.uiState.value.error)
    }
}
