package com.kidsroutine.feature.challenges.ui

import androidx.lifecycle.SavedStateHandle
import com.kidsroutine.core.engine.challenge_engine.ChallengeEngine
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
class ChallengeDetailViewModelTest {
    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var challengeRepository: ChallengeRepository
    private lateinit var challengeEngine: ChallengeEngine
    private lateinit var viewModel: ChallengeDetailViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        challengeRepository = mockk(relaxed = true)
        challengeEngine = mockk(relaxed = true)
        viewModel = ChallengeDetailViewModel(challengeRepository, challengeEngine, SavedStateHandle())
    }

    @After
    fun tearDown() { Dispatchers.resetMain() }

    @Test
    fun `initial state is default`() {
        assertFalse(viewModel.uiState.value.isLoading)
        assertNull(viewModel.uiState.value.challenge)
    }

    @Test
    fun `loadChallengeDetail success sets challenge and progress`() = runTest {
        val challenge = ChallengeModel(challengeId = "ch1", title = "Read")
        val progress = ChallengeProgress(challengeId = "ch1", userId = "u1", familyId = "fam1")
        coEvery { challengeRepository.getChallenge("ch1") } returns challenge
        coEvery { challengeRepository.getChallengeProgress("u1", "fam1", "ch1") } returns progress
        viewModel.loadChallengeDetail("u1", "fam1", "ch1")
        advanceUntilIdle()
        assertNotNull(viewModel.uiState.value.challenge)
        assertNotNull(viewModel.uiState.value.progress)
    }

    @Test
    fun `loadChallengeDetail null challenge sets error`() = runTest {
        coEvery { challengeRepository.getChallenge("ch1") } returns null
        viewModel.loadChallengeDetail("u1", "fam1", "ch1")
        advanceUntilIdle()
        assertNotNull(viewModel.uiState.value.error)
    }

    @Test
    fun `loadChallengeDetail error sets error message`() = runTest {
        coEvery { challengeRepository.getChallenge("ch1") } throws RuntimeException("Fail")
        viewModel.loadChallengeDetail("u1", "fam1", "ch1")
        advanceUntilIdle()
        assertNotNull(viewModel.uiState.value.error)
    }
}
