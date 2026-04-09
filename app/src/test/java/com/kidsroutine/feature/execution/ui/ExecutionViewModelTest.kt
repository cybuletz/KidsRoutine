package com.kidsroutine.feature.execution.ui

import androidx.lifecycle.SavedStateHandle
import com.kidsroutine.core.model.Badge
import com.kidsroutine.core.model.TaskModel
import com.kidsroutine.core.model.UserModel
import com.kidsroutine.feature.achievements.data.AchievementRepository
import com.kidsroutine.feature.execution.domain.CompleteTaskUseCase
import com.kidsroutine.feature.execution.domain.CompletionResult
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ExecutionViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var completeTaskUseCase: CompleteTaskUseCase
    private lateinit var achievementRepository: AchievementRepository
    private lateinit var savedStateHandle: SavedStateHandle
    private lateinit var viewModel: ExecutionViewModel

    private val testTask = TaskModel(id = "t1", title = "Brush teeth")
    private val testUser = UserModel(
        userId = "u1",
        familyId = "fam1",
        displayName = "Alice",
        age = 8,
        streak = 5,
        lastActiveDate = "2026-01-15"
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        completeTaskUseCase = mockk(relaxed = true)
        achievementRepository = mockk(relaxed = true)
        savedStateHandle = SavedStateHandle()
        viewModel = ExecutionViewModel(completeTaskUseCase, achievementRepository, savedStateHandle)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is default`() {
        val state = viewModel.uiState.value
        assertEquals("", state.task.id)
        assertEquals("", state.instanceId)
        assertEquals(0, state.currentBlockIndex)
        assertTrue(state.blockAnswers.isEmpty())
        assertNull(state.photoUrl)
        assertFalse(state.timerRunning)
        assertFalse(state.isCompleting)
        assertNull(state.result)
    }

    @Test
    fun `loadTask sets task and instanceId`() {
        viewModel.loadTask(testTask, "inst-1")

        val state = viewModel.uiState.value
        assertEquals("t1", state.task.id)
        assertEquals("Brush teeth", state.task.title)
        assertEquals("inst-1", state.instanceId)
    }

    @Test
    fun `BlockAnswered advances index and stores answer`() {
        viewModel.loadTask(testTask)

        viewModel.onEvent(ExecutionEvent.BlockAnswered("block1", "yes"))

        val state = viewModel.uiState.value
        assertEquals(1, state.currentBlockIndex)
        assertEquals("yes", state.blockAnswers["block1"])
    }

    @Test
    fun `PhotoCaptured sets photoUrl`() {
        viewModel.onEvent(ExecutionEvent.PhotoCaptured("https://photo.url/img.jpg"))

        assertEquals("https://photo.url/img.jpg", viewModel.uiState.value.photoUrl)
    }

    @Test
    fun `TimerStarted sets timerRunning true`() {
        viewModel.onEvent(ExecutionEvent.TimerStarted)

        assertTrue(viewModel.uiState.value.timerRunning)
    }

    @Test
    fun `TimerFinished sets timerRunning false`() {
        viewModel.onEvent(ExecutionEvent.TimerStarted)
        assertTrue(viewModel.uiState.value.timerRunning)

        viewModel.onEvent(ExecutionEvent.TimerFinished)
        assertFalse(viewModel.uiState.value.timerRunning)
    }

    @Test
    fun `SubmitTask success sets result and celebrationMessage`() = runTest {
        val successResult = CompletionResult.Success(
            xpGained = 50,
            newStreak = 3,
            needsParent = false,
            celebrationMessage = "Great job!"
        )
        coEvery {
            completeTaskUseCase.invoke(any(), any(), any(), any(), any(), any(), any(), any(), any())
        } returns successResult
        coEvery { achievementRepository.checkAndUnlockAchievements(any()) } returns listOf(
            Badge(id = "badge1", title = "First Win")
        )

        viewModel.setCurrentUser(testUser)
        viewModel.loadTask(testTask)
        viewModel.onEvent(ExecutionEvent.SubmitTask)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isCompleting)
        assertTrue(state.result is CompletionResult.Success)
        assertEquals("Great job!", state.celebrationMessage)
        assertTrue(state.showSuccessAnim)
        assertEquals(1, state.newBadgesUnlocked.size)
    }

    @Test
    fun `SubmitTask rejected sets result`() = runTest {
        val rejectedResult = CompletionResult.Rejected(reason = "Too early")
        coEvery {
            completeTaskUseCase.invoke(any(), any(), any(), any(), any(), any(), any(), any(), any())
        } returns rejectedResult

        viewModel.setCurrentUser(testUser)
        viewModel.loadTask(testTask)
        viewModel.onEvent(ExecutionEvent.SubmitTask)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isCompleting)
        assertTrue(state.result is CompletionResult.Rejected)
        assertFalse(state.showSuccessAnim)
    }

    @Test
    fun `DismissResult clears result`() = runTest {
        val successResult = CompletionResult.Success(
            xpGained = 50,
            newStreak = 3,
            needsParent = false,
            celebrationMessage = "Great!"
        )
        coEvery {
            completeTaskUseCase.invoke(any(), any(), any(), any(), any(), any(), any(), any(), any())
        } returns successResult
        coEvery { achievementRepository.checkAndUnlockAchievements(any()) } returns emptyList()

        viewModel.setCurrentUser(testUser)
        viewModel.loadTask(testTask)
        viewModel.onEvent(ExecutionEvent.SubmitTask)
        advanceUntilIdle()

        assertNotNull(viewModel.uiState.value.result)

        viewModel.onEvent(ExecutionEvent.DismissResult)

        assertNull(viewModel.uiState.value.result)
        assertFalse(viewModel.uiState.value.showSuccessAnim)
    }
}
