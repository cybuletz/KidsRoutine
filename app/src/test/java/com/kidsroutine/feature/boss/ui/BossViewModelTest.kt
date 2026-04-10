package com.kidsroutine.feature.boss.ui

import com.kidsroutine.core.engine.boss_engine.BossEngine
import com.kidsroutine.core.model.BossModel
import com.kidsroutine.core.model.BossType
import com.kidsroutine.core.model.DifficultyLevel
import com.kidsroutine.core.model.Season
import com.kidsroutine.core.model.UserModel
import com.kidsroutine.feature.boss.data.BossRepository
import com.kidsroutine.feature.daily.data.UserRepository
import io.mockk.*
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class BossViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var bossRepository: BossRepository
    private lateinit var bossEngine: BossEngine
    private lateinit var userRepository: UserRepository
    private lateinit var viewModel: BossViewModel

    private val testUser = UserModel(userId = "u1", xp = 100)
    private val testBoss = BossModel(
        bossId = "b1",
        familyId = "fam1",
        type = BossType.HOMEWORK_HYDRA,
        currentHp = 100,
        maxHp = 200,
        totalDamage = 100,
        deadline = System.currentTimeMillis() + 100_000,
        isExpired = false
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        bossRepository = mockk(relaxed = true)
        bossEngine = mockk(relaxed = true)
        userRepository = mockk(relaxed = true)
        every { userRepository.observeUser(any()) } returns flowOf(testUser)
        every { bossEngine.checkExpiry(any(), any()) } returns testBoss
        every { bossEngine.getMvp(any()) } returns "u1"
        viewModel = BossViewModel(bossRepository, bossEngine, userRepository)
    }

    @After
    fun tearDown() {
        // Cancel any pending countdown coroutines that would otherwise leak
        // into subsequent tests and hang advanceUntilIdle() calls.
        viewModel.viewModelScope.coroutineContext.cancelChildren()
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is default`() {
        val state = viewModel.uiState.value
        assertNull(state.boss)
        assertFalse(state.isLoading)
        assertNull(state.error)
        assertEquals(0, state.damageDealt)
    }

    @Test
    fun `loadBoss success sets boss and mvp`() = runTest {
        coEvery { bossRepository.getActiveBoss("fam1") } returns testBoss

        viewModel.loadBoss("fam1", "u1")
        // Don't call advanceUntilIdle() — the countdown loop would never finish
        // because System.currentTimeMillis() doesn't advance with virtual time.
        // UnconfinedTestDispatcher eagerly dispatches, so state is already set.

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNotNull(state.boss)
        assertEquals("u1", state.mvpUserId)
        assertEquals(testBoss.totalDamage, state.damageDealt)
    }

    @Test
    fun `loadBoss null boss sets boss to null`() = runTest {
        coEvery { bossRepository.getActiveBoss("fam1") } returns null

        viewModel.loadBoss("fam1", "u1")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNull(state.boss)
    }

    @Test
    fun `loadBoss error sets error message`() = runTest {
        coEvery { bossRepository.getActiveBoss("fam1") } throws RuntimeException("DB error")

        viewModel.loadBoss("fam1", "u1")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals("DB error", state.error)
    }

    @Test
    fun `generateNewBoss success saves boss`() = runTest {
        val generatedBoss = BossModel(
            bossId = "b2",
            familyId = "fam1",
            type = BossType.DISTRACTION_DRAGON,
            maxHp = 300,
            currentHp = 300
        )
        coEvery {
            bossEngine.generateWeeklyBoss(
                familyId = "fam1",
                familySize = 3,
                week = any(),
                season = Season.NONE,
                difficulty = DifficultyLevel.MEDIUM
            )
        } returns generatedBoss

        // Load user XP first (no active boss → no countdown)
        viewModel.loadBoss("fam1", "u1")
        advanceUntilIdle()

        viewModel.generateNewBoss("fam1", 3)
        // Don't advanceUntilIdle — generateNewBoss starts a countdown loop.
        // UnconfinedTestDispatcher eagerly dispatches, so state is already set.

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNotNull(state.boss)
        coVerify { bossRepository.saveBoss(any()) }
    }

    @Test
    fun `generateNewBoss not enough XP shows error`() = runTest {
        val lowXpUser = UserModel(userId = "u1", xp = 5)
        every { userRepository.observeUser(any()) } returns flowOf(lowXpUser)

        viewModel.loadBoss("fam1", "u1")
        advanceUntilIdle()

        viewModel.generateNewBoss("fam1", 3)

        val state = viewModel.uiState.value
        assertNotNull(state.error)
        assertTrue(state.error!!.contains("Not enough XP"))
    }

    @Test
    fun `generateNewBoss deducts XP`() = runTest {
        val generatedBoss = BossModel(bossId = "b2", familyId = "fam1")
        coEvery {
            bossEngine.generateWeeklyBoss(any(), any(), any(), any(), any())
        } returns generatedBoss

        viewModel.loadBoss("fam1", "u1")
        advanceUntilIdle()

        viewModel.generateNewBoss("fam1", 3)
        // Don't advanceUntilIdle — countdown loop prevents idle.

        coVerify { userRepository.updateUserXp("u1", -BossViewModel.BOSS_ENTRY_COST) }
    }

    @Test
    fun `clearError clears error`() = runTest {
        coEvery { bossRepository.getActiveBoss("fam1") } throws RuntimeException("fail")

        viewModel.loadBoss("fam1")
        advanceUntilIdle()

        assertNotNull(viewModel.uiState.value.error)

        viewModel.clearError()

        assertNull(viewModel.uiState.value.error)
    }
}
