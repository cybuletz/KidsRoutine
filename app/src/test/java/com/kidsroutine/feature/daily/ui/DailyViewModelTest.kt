package com.kidsroutine.feature.daily.ui

import com.kidsroutine.feature.challenges.data.ChallengeRepository
import com.kidsroutine.feature.daily.data.DailyRepository
import com.kidsroutine.feature.daily.data.StoryArcRepository
import com.kidsroutine.feature.daily.data.TaskSaveRepository
import com.kidsroutine.feature.daily.data.UserRepository
import com.kidsroutine.feature.daily.domain.GenerateDailyTasksUseCase
import com.kidsroutine.feature.daily.domain.GetDailyStateUseCase
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
class DailyViewModelTest {
    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var getDailyState: GetDailyStateUseCase
    private lateinit var generateDailyTasks: GenerateDailyTasksUseCase
    private lateinit var userRepository: UserRepository
    private lateinit var storyArcRepository: StoryArcRepository
    private lateinit var taskSaveRepository: TaskSaveRepository
    private lateinit var dailyRepository: DailyRepository
    private lateinit var challengeRepository: ChallengeRepository
    private lateinit var firestore: FirebaseFirestore
    private lateinit var viewModel: DailyViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        getDailyState = mockk(relaxed = true)
        generateDailyTasks = mockk(relaxed = true)
        userRepository = mockk(relaxed = true)
        storyArcRepository = mockk(relaxed = true)
        taskSaveRepository = mockk(relaxed = true)
        dailyRepository = mockk(relaxed = true)
        challengeRepository = mockk(relaxed = true)
        firestore = mockk(relaxed = true)
        viewModel = DailyViewModel(getDailyState, generateDailyTasks, userRepository, storyArcRepository, taskSaveRepository, dailyRepository, challengeRepository, firestore)
    }

    @After
    fun tearDown() { Dispatchers.resetMain() }

    @Test
    fun `initial state is loading`() {
        assertTrue(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `forceRefresh calls dailyRepository`() = runTest {
        viewModel.forceRefresh()
        advanceUntilIdle()
        coVerify { dailyRepository.refreshTasksForDate(any(), any(), any()) }
    }

    @Test
    fun `addSuggestedTask calls taskSaveRepository`() = runTest {
        val task = com.kidsroutine.core.model.TaskModel(id = "t1", title = "Test")
        viewModel.addSuggestedTask(task)
        advanceUntilIdle()
        coVerify { taskSaveRepository.assignTaskToChild(any()) }
    }

    @Test
    fun `uiState exposes stateflow`() {
        assertNotNull(viewModel.uiState)
    }
}
