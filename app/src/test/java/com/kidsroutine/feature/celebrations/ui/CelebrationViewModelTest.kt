package com.kidsroutine.feature.celebrations.ui

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CelebrationViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var viewModel: CelebrationViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        viewModel = CelebrationViewModel()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is null`() {
        assertNull(viewModel.celebrationEvent.value)
    }

    @Test
    fun `showTaskCompletion sets type to TASK_COMPLETION`() {
        viewModel.showTaskCompletion()

        val event = viewModel.celebrationEvent.value
        assertNotNull(event)
        assertEquals(CelebrationType.TASK_COMPLETION, event!!.type)
    }

    @Test
    fun `showAchievementUnlock stores achievement name`() {
        viewModel.showAchievementUnlock("First Steps")

        val event = viewModel.celebrationEvent.value
        assertNotNull(event)
        assertEquals(CelebrationType.ACHIEVEMENT_UNLOCK, event!!.type)
        assertEquals("First Steps", event.data)
    }

    @Test
    fun `showLevelUp stores level as string`() {
        viewModel.showLevelUp(5)

        val event = viewModel.celebrationEvent.value
        assertNotNull(event)
        assertEquals(CelebrationType.LEVEL_UP, event!!.type)
        assertEquals("5", event.data)
    }

    @Test
    fun `showMilestone stores milestone data`() {
        viewModel.showMilestone("100 tasks done")

        val event = viewModel.celebrationEvent.value
        assertNotNull(event)
        assertEquals(CelebrationType.MILESTONE, event!!.type)
        assertEquals("100 tasks done", event.data)
    }

    @Test
    fun `dismissCelebration clears event to null`() {
        viewModel.showTaskCompletion()
        assertNotNull(viewModel.celebrationEvent.value)

        viewModel.dismissCelebration()
        assertNull(viewModel.celebrationEvent.value)
    }

    @Test
    fun `sequential events overwrite previous event`() {
        viewModel.showTaskCompletion()
        assertEquals(CelebrationType.TASK_COMPLETION, viewModel.celebrationEvent.value?.type)

        viewModel.showLevelUp(3)
        assertEquals(CelebrationType.LEVEL_UP, viewModel.celebrationEvent.value?.type)
        assertEquals("3", viewModel.celebrationEvent.value?.data)
    }

    @Test
    fun `event has non-empty id`() {
        viewModel.showTaskCompletion()

        val event = viewModel.celebrationEvent.value
        assertNotNull(event)
        assertTrue(event!!.id.isNotEmpty())
    }
}
