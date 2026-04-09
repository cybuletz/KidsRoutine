package com.kidsroutine.feature.achievements.ui

import com.kidsroutine.core.model.Badge
import com.kidsroutine.core.model.UserAchievements
import com.kidsroutine.feature.achievements.data.AchievementRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AchievementsViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var repository: AchievementRepository
    private lateinit var viewModel: AchievementsViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = mockk(relaxed = true)
        viewModel = AchievementsViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state has defaults`() {
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(UserAchievements(), state.achievements)
        assertTrue(state.unlockedBadges.isEmpty())
        assertNull(state.error)
    }

    @Test
    fun `loadAchievements emits from flow`() = runTest {
        val achievements = UserAchievements(badges = listOf(Badge(id = "b1")))
        every { repository.observeUserAchievements("u1") } returns flowOf(achievements)

        viewModel.loadAchievements("u1")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(1, state.achievements.badges.size)
        assertEquals("b1", state.achievements.badges[0].id)
    }

    @Test
    fun `loadAchievements error sets error message`() = runTest {
        every { repository.observeUserAchievements("u1") } throws RuntimeException("Fetch failed")

        viewModel.loadAchievements("u1")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNotNull(state.error)
    }

    @Test
    fun `checkNewAchievements with badges updates unlockedBadges`() = runTest {
        val newBadges = listOf(Badge(id = "b2", title = "Star"))
        coEvery { repository.checkAndUnlockAchievements("u1") } returns newBadges

        viewModel.checkNewAchievements("u1")
        advanceUntilIdle()

        coVerify { repository.checkAndUnlockAchievements("u1") }
        assertEquals(1, viewModel.uiState.value.unlockedBadges.size)
        assertEquals("b2", viewModel.uiState.value.unlockedBadges[0].id)
    }

    @Test
    fun `checkNewAchievements with empty list does not update unlockedBadges`() = runTest {
        coEvery { repository.checkAndUnlockAchievements("u1") } returns emptyList()

        viewModel.checkNewAchievements("u1")
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.unlockedBadges.isEmpty())
    }

    @Test
    fun `checkNewAchievements error does not crash`() = runTest {
        coEvery { repository.checkAndUnlockAchievements("u1") } throws RuntimeException("Error")

        viewModel.checkNewAchievements("u1")
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.unlockedBadges.isEmpty())
    }
}
