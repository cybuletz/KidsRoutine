package com.kidsroutine.feature.challenges.ui

import com.kidsroutine.core.model.ChallengeModel
import com.kidsroutine.core.model.ChallengeProgress
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
class ActiveChallengesViewModelTest {
    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var challengeRepository: ChallengeRepository
    private lateinit var viewModel: ActiveChallengesViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        challengeRepository = mockk(relaxed = true)
        viewModel = ActiveChallengesViewModel(challengeRepository)
    }

    @After
    fun tearDown() { Dispatchers.resetMain() }

    @Test
    fun `initial state is default`() {
        assertFalse(viewModel.uiState.value.isLoading)
        assertTrue(viewModel.uiState.value.activeChallenges.isEmpty())
    }

    @Test
    fun `loadActiveChallenges success sets challenges`() = runTest {
        val progress = ChallengeProgress(challengeId = "ch1", userId = "u1", lastCompletedDate = "2020-01-01")
        val challenge = ChallengeModel(challengeId = "ch1", title = "Read daily")
        coEvery { challengeRepository.getActiveChallenges("u1", "fam1") } returns listOf(progress)
        coEvery { challengeRepository.getChallenge("ch1") } returns challenge
        viewModel.loadActiveChallenges("u1", "fam1")
        advanceUntilIdle()
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `loadActiveChallenges error sets error`() = runTest {
        coEvery { challengeRepository.getActiveChallenges("u1", "fam1") } throws RuntimeException("Error")
        viewModel.loadActiveChallenges("u1", "fam1")
        advanceUntilIdle()
        assertNotNull(viewModel.uiState.value.error)
    }
}
