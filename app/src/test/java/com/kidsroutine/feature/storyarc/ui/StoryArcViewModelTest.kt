package com.kidsroutine.feature.storyarc.ui

import com.kidsroutine.core.model.StoryArc
import com.kidsroutine.core.model.StoryChapter
import com.kidsroutine.feature.daily.data.StoryArcRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class StoryArcViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var repository: StoryArcRepository
    private lateinit var viewModel: StoryArcViewModel

    private val testArc = StoryArc(
        arcId = "arc1",
        familyId = "fam1",
        currentDay = 2,
        chapters = listOf(StoryChapter(day = 1), StoryChapter(day = 2))
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = mockk(relaxed = true)
        viewModel = StoryArcViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state has isLoading false`() {
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNull(state.arc)
        assertEquals(0, state.currentChapterIndex)
    }

    @Test
    fun `loadArc success sets arc and chapter index`() = runTest {
        coEvery { repository.getActiveArc("fam1") } returns testArc

        viewModel.loadArc("fam1")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertNotNull(state.arc)
        assertEquals("arc1", state.arc?.arcId)
        assertEquals(1, state.currentChapterIndex) // currentDay=2, index=1
        assertFalse(state.isLoading)
        assertNull(state.error)
    }

    @Test
    fun `loadArc with null arc sets index to 0`() = runTest {
        coEvery { repository.getActiveArc("fam1") } returns null

        viewModel.loadArc("fam1")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertNull(state.arc)
        assertEquals(0, state.currentChapterIndex) // (null?.currentDay ?: 1) - 1 = 0
        assertFalse(state.isLoading)
    }

    @Test
    fun `loadArc error sets error message`() = runTest {
        coEvery { repository.getActiveArc("fam1") } throws RuntimeException("Network error")

        viewModel.loadArc("fam1")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals("Network error", state.error)
    }

    @Test
    fun `advanceDay calls repository and reloads arc`() = runTest {
        coEvery { repository.getActiveArc("fam1") } returns testArc
        viewModel.loadArc("fam1")
        advanceUntilIdle()

        val updatedArc = testArc.copy(currentDay = 3)
        coEvery { repository.getActiveArc("fam1") } returns updatedArc

        viewModel.advanceDay()
        advanceUntilIdle()

        coVerify { repository.advanceDay("arc1") }
        assertEquals(2, viewModel.uiState.value.currentChapterIndex) // day 3 -> index 2
    }

    @Test
    fun `advanceDay when no arc does nothing`() = runTest {
        viewModel.advanceDay()
        advanceUntilIdle()

        coVerify(exactly = 0) { repository.advanceDay(any()) }
    }

    @Test
    fun `selectChapter updates currentChapterIndex`() {
        viewModel.selectChapter(3)
        assertEquals(3, viewModel.uiState.value.currentChapterIndex)
    }

    @Test
    fun `currentChapterIndex based on currentDay`() = runTest {
        val arc = testArc.copy(currentDay = 5)
        coEvery { repository.getActiveArc("fam1") } returns arc

        viewModel.loadArc("fam1")
        advanceUntilIdle()

        assertEquals(4, viewModel.uiState.value.currentChapterIndex) // day 5 -> index 4
    }
}
