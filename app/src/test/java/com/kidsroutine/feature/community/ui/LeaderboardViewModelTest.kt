package com.kidsroutine.feature.community.ui

import com.kidsroutine.feature.community.data.CommunityRepository
import com.google.firebase.firestore.FirebaseFirestore
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LeaderboardViewModelTest {
    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var communityRepository: CommunityRepository
    private lateinit var firestore: FirebaseFirestore
    private lateinit var viewModel: LeaderboardViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        communityRepository = mockk(relaxed = true)
        firestore = mockk(relaxed = true)
        viewModel = LeaderboardViewModel(communityRepository, firestore)
    }

    @After
    fun tearDown() { Dispatchers.resetMain() }

    @Test
    fun `initial state is default`() {
        assertFalse(viewModel.uiState.value.isLoading)
        assertEquals(LeaderboardTab.CHILDREN, viewModel.uiState.value.activeTab)
    }

    @Test
    fun `selectTab updates active tab`() {
        viewModel.selectTab(LeaderboardTab.FAMILIES)
        assertEquals(LeaderboardTab.FAMILIES, viewModel.uiState.value.activeTab)
    }

    @Test
    fun `selectTab to challenges`() {
        viewModel.selectTab(LeaderboardTab.CHALLENGES)
        assertEquals(LeaderboardTab.CHALLENGES, viewModel.uiState.value.activeTab)
    }

    @Test
    fun `selectTab to my family`() {
        viewModel.selectTab(LeaderboardTab.MY_FAMILY)
        assertEquals(LeaderboardTab.MY_FAMILY, viewModel.uiState.value.activeTab)
    }
}
