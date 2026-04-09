package com.kidsroutine.feature.stats.ui

import com.kidsroutine.core.model.WorldModel
import com.kidsroutine.core.model.WorldNode
import com.kidsroutine.core.model.WorldNodeStatus
import com.kidsroutine.feature.stats.data.FamilyStatsModel
import com.kidsroutine.feature.stats.data.StatsRepository
import com.kidsroutine.feature.stats.data.UserStatsModel
import com.kidsroutine.feature.world.data.WorldRepository
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class StatsViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var statsRepository: StatsRepository
    private lateinit var worldRepository: WorldRepository
    private lateinit var viewModel: StatsViewModel

    private val testUserStats = UserStatsModel(
        userId = "u1",
        totalXp = 500,
        level = 5,
        currentStreak = 7,
        tasksCompleted = 42
    )
    private val testFamilyStats = FamilyStatsModel(
        familyId = "fam1",
        familyName = "The Smiths",
        memberCount = 4,
        familyXp = 1500
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        statsRepository = mockk(relaxed = true)
        worldRepository = mockk(relaxed = true)
        viewModel = StatsViewModel(statsRepository, worldRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is default`() {
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNull(state.userStats)
        assertNull(state.familyStats)
        assertTrue(state.weeklyProgress.isEmpty())
        assertTrue(state.monthlyProgress.isEmpty())
        assertNull(state.error)
    }

    @Test
    fun `loadUserStats success sets stats and weekly progress`() = runTest {
        val weekly = listOf(10, 20, 30, 15, 25, 10, 40)
        coEvery { statsRepository.getUserStats("u1") } returns testUserStats
        coEvery { statsRepository.getWeeklyProgress("u1") } returns weekly

        viewModel.loadUserStats("u1")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(testUserStats, state.userStats)
        assertEquals(weekly, state.weeklyProgress)
    }

    @Test
    fun `loadUserStats error sets error`() = runTest {
        coEvery { statsRepository.getUserStats(any()) } throws RuntimeException("Failed")

        viewModel.loadUserStats("u1")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals("Failed", state.error)
    }

    @Test
    fun `loadFamilyStats success sets family stats`() = runTest {
        coEvery { statsRepository.getFamilyStats("fam1") } returns testFamilyStats

        viewModel.loadFamilyStats("fam1")
        advanceUntilIdle()

        assertEquals(testFamilyStats, viewModel.uiState.value.familyStats)
    }

    @Test
    fun `loadMonthlyProgress success sets monthly data`() = runTest {
        val monthly = listOf(100, 120, 90, 150)
        coEvery { statsRepository.getMonthlyProgress("u1") } returns monthly

        viewModel.loadMonthlyProgress("u1")
        advanceUntilIdle()

        assertEquals(monthly, viewModel.uiState.value.monthlyProgress)
    }

    @Test
    fun `loadWorldProgress sets current and next nodes`() = runTest {
        val nodes = listOf(
            WorldNode(nodeId = "n1", title = "Start", requiredXp = 0, status = WorldNodeStatus.COMPLETED),
            WorldNode(nodeId = "n2", title = "Forest", requiredXp = 100, status = WorldNodeStatus.UNLOCKED),
            WorldNode(nodeId = "n3", title = "Mountain", requiredXp = 300, status = WorldNodeStatus.LOCKED),
            WorldNode(nodeId = "n4", title = "Sky", requiredXp = 500, status = WorldNodeStatus.LOCKED)
        )
        val world = WorldModel(nodes = nodes)
        coEvery { worldRepository.getWorld(200) } returns world

        viewModel.loadWorldProgress(200)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("n2", state.currentWorldNode?.nodeId)
        assertEquals("n3", state.nextWorldNode?.nodeId)
    }

    @Test
    fun `loadWorldProgress finds current as highest non-locked`() = runTest {
        val nodes = listOf(
            WorldNode(nodeId = "n1", requiredXp = 0, status = WorldNodeStatus.COMPLETED),
            WorldNode(nodeId = "n2", requiredXp = 50, status = WorldNodeStatus.COMPLETED),
            WorldNode(nodeId = "n3", requiredXp = 200, status = WorldNodeStatus.LOCKED)
        )
        val world = WorldModel(nodes = nodes)
        coEvery { worldRepository.getWorld(150) } returns world

        viewModel.loadWorldProgress(150)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("n2", state.currentWorldNode?.nodeId)
        assertEquals("n3", state.nextWorldNode?.nodeId)
    }
}
