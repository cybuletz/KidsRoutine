package com.kidsroutine.feature.community.ui

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
class ModerationViewModelTest {
    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var communityRepository: CommunityRepository
    private lateinit var viewModel: ModerationViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        communityRepository = mockk(relaxed = true)
        viewModel = ModerationViewModel(communityRepository)
    }

    @After
    fun tearDown() { Dispatchers.resetMain() }

    @Test
    fun `initial state is default`() {
        assertFalse(viewModel.uiState.value.isLoading)
        assertEquals(ModerationTab.PENDING_TASKS, viewModel.uiState.value.activeTab)
    }

    @Test
    fun `selectTab updates tab`() {
        viewModel.selectTab(ModerationTab.REPORTS)
        assertEquals(ModerationTab.REPORTS, viewModel.uiState.value.activeTab)
    }

    @Test
    fun `loadModeration success`() = runTest {
        coEvery { communityRepository.getPendingTasks(any()) } returns emptyList()
        coEvery { communityRepository.getPendingChallenges(any()) } returns emptyList()
        coEvery { communityRepository.getPendingReports(any()) } returns emptyList()
        viewModel.loadModeration()
        advanceUntilIdle()
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `loadModeration error sets error`() = runTest {
        coEvery { communityRepository.getPendingTasks(any()) } throws RuntimeException("Err")
        viewModel.loadModeration()
        advanceUntilIdle()
        assertNotNull(viewModel.uiState.value.error)
    }
}
