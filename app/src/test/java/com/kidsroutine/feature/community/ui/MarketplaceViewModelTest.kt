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
class MarketplaceViewModelTest {
    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var communityRepository: CommunityRepository
    private lateinit var viewModel: MarketplaceViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        communityRepository = mockk(relaxed = true)
        viewModel = MarketplaceViewModel(communityRepository)
    }

    @After
    fun tearDown() { Dispatchers.resetMain() }

    @Test
    fun `initial state is default`() {
        assertFalse(viewModel.uiState.value.isLoading)
        assertEquals(MarketplaceTab.TASKS, viewModel.uiState.value.activeTab)
    }

    @Test
    fun `selectTab updates tab`() {
        viewModel.selectTab(MarketplaceTab.CHALLENGES)
        assertEquals(MarketplaceTab.CHALLENGES, viewModel.uiState.value.activeTab)
    }

    @Test
    fun `loadMarketplace success loads data`() = runTest {
        coEvery { communityRepository.getApprovedTasks(any(), any(), any()) } returns emptyList()
        coEvery { communityRepository.getApprovedChallenges(any(), any(), any()) } returns emptyList()
        viewModel.loadMarketplace()
        advanceUntilIdle()
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `loadMarketplace error sets error`() = runTest {
        coEvery { communityRepository.getApprovedTasks(any(), any(), any()) } throws RuntimeException("Net")
        viewModel.loadMarketplace()
        advanceUntilIdle()
        assertNotNull(viewModel.uiState.value.error)
    }

    @Test
    fun `clearMessages clears messages`() {
        viewModel.clearMessages()
        assertNull(viewModel.uiState.value.error)
        assertNull(viewModel.uiState.value.successMessage)
    }
}
