package com.kidsroutine.feature.spinwheel.ui

import com.kidsroutine.core.engine.spin_engine.DailyRewardEngine
import com.kidsroutine.core.model.DailySpinState
import com.kidsroutine.core.model.PlanType
import com.kidsroutine.core.model.SpinWheelResult
import com.kidsroutine.core.model.UserModel
import com.kidsroutine.feature.daily.data.UserRepository
import com.kidsroutine.feature.spinwheel.data.SpinWheelRepository
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SpinWheelViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var repository: SpinWheelRepository
    private lateinit var rewardEngine: DailyRewardEngine
    private lateinit var userRepository: UserRepository
    private lateinit var viewModel: SpinWheelViewModel

    private val testUser = UserModel(userId = "u1", xp = 100)

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = mockk(relaxed = true)
        rewardEngine = mockk(relaxed = true)
        userRepository = mockk(relaxed = true)
        every { userRepository.observeUser(any()) } returns flowOf(testUser)
        viewModel = SpinWheelViewModel(repository, rewardEngine, userRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is default`() {
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(SpinPhase.IDLE, state.phase)
        assertNull(state.lastResult)
        assertNull(state.error)
    }

    @Test
    fun `loadState with existing daily state sets state`() = runTest {
        val dailyState = DailySpinState(userId = "u1", maxSpins = 3, spinsUsed = 1)
        coEvery { repository.getDailyState("u1", any()) } returns dailyState

        viewModel.loadState("u1")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(dailyState, state.dailyState)
        assertEquals(2, state.spinsRemaining)
        assertEquals(SpinPhase.IDLE, state.phase)
    }

    @Test
    fun `loadState creates new state when null`() = runTest {
        val newState = DailySpinState(userId = "u1", maxSpins = 1, spinsUsed = 0)
        coEvery { repository.getDailyState("u1", any()) } returns null
        coEvery { rewardEngine.createDailyState("u1", any(), PlanType.FREE) } returns newState

        viewModel.loadState("u1")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(newState, state.dailyState)
        coVerify { repository.saveDailyState(newState) }
    }

    @Test
    fun `loadState error sets error`() = runTest {
        coEvery { repository.getDailyState(any(), any()) } throws RuntimeException("Network error")

        viewModel.loadState("u1")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals("Network error", state.error)
    }

    @Test
    fun `spin success transitions through phases`() = runTest {
        val dailyState = DailySpinState(userId = "u1", maxSpins = 3, spinsUsed = 0)
        val updatedState = dailyState.copy(spinsUsed = 1)
        val result = SpinWheelResult()
        coEvery { repository.getDailyState("u1", any()) } returns dailyState
        coEvery { rewardEngine.spin(any()) } returns Pair(updatedState, result)

        viewModel.loadState("u1")
        advanceUntilIdle()

        viewModel.spin()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(SpinPhase.DONE, state.phase)
        assertEquals(updatedState, state.dailyState)
        assertEquals(2, state.spinsRemaining)
    }

    @Test
    fun `spin not enough XP shows error`() = runTest {
        val lowXpUser = UserModel(userId = "u1", xp = 1)
        every { userRepository.observeUser(any()) } returns flowOf(lowXpUser)

        val dailyState = DailySpinState(userId = "u1", maxSpins = 3, spinsUsed = 0)
        coEvery { repository.getDailyState("u1", any()) } returns dailyState

        viewModel.loadState("u1")
        advanceUntilIdle()

        viewModel.spin()

        val state = viewModel.uiState.value
        assertNotNull(state.error)
        assertTrue(state.error!!.contains("Not enough XP"))
    }

    @Test
    fun `spin when already spinning does nothing`() = runTest {
        val dailyState = DailySpinState(userId = "u1", maxSpins = 3, spinsUsed = 0)
        val updatedState = dailyState.copy(spinsUsed = 1)
        val result = SpinWheelResult()
        coEvery { repository.getDailyState("u1", any()) } returns dailyState
        coEvery { rewardEngine.spin(any()) } returns Pair(updatedState, result)

        viewModel.loadState("u1")
        advanceUntilIdle()

        // First spin starts SPINNING phase
        viewModel.spin()
        // Immediately try second spin while SPINNING
        viewModel.spin()
        advanceUntilIdle()

        // Should only have been called once for the XP deduction
        coVerify(exactly = 1) { userRepository.updateUserXp(any(), any()) }
    }

    @Test
    fun `spin when canSpin is false does nothing`() = runTest {
        val dailyState = DailySpinState(userId = "u1", maxSpins = 3, spinsUsed = 3)
        coEvery { repository.getDailyState("u1", any()) } returns dailyState

        viewModel.loadState("u1")
        advanceUntilIdle()

        viewModel.spin()

        coVerify(exactly = 0) { rewardEngine.spin(any()) }
    }

    @Test
    fun `resetForNextSpin to IDLE when canSpin`() = runTest {
        val dailyState = DailySpinState(userId = "u1", maxSpins = 3, spinsUsed = 1)
        val updatedState = dailyState.copy(spinsUsed = 1)
        val result = SpinWheelResult()
        coEvery { repository.getDailyState("u1", any()) } returns dailyState
        coEvery { rewardEngine.spin(any()) } returns Pair(updatedState, result)

        viewModel.loadState("u1")
        advanceUntilIdle()

        viewModel.spin()
        advanceUntilIdle()

        assertEquals(SpinPhase.DONE, viewModel.uiState.value.phase)

        viewModel.resetForNextSpin()

        assertEquals(SpinPhase.IDLE, viewModel.uiState.value.phase)
        assertNull(viewModel.uiState.value.lastResult)
    }

    @Test
    fun `resetForNextSpin stays DONE when no spins left`() = runTest {
        val dailyState = DailySpinState(userId = "u1", maxSpins = 1, spinsUsed = 0)
        val updatedState = dailyState.copy(spinsUsed = 1)
        val result = SpinWheelResult()
        coEvery { repository.getDailyState("u1", any()) } returns dailyState
        coEvery { rewardEngine.spin(any()) } returns Pair(updatedState, result)

        viewModel.loadState("u1")
        advanceUntilIdle()

        viewModel.spin()
        advanceUntilIdle()

        assertEquals(SpinPhase.DONE, viewModel.uiState.value.phase)

        viewModel.resetForNextSpin()

        assertEquals(SpinPhase.DONE, viewModel.uiState.value.phase)
    }
}
