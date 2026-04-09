package com.kidsroutine.feature.moments.ui

import com.kidsroutine.core.model.MomentModel
import com.kidsroutine.feature.moments.data.MomentsRepository
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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MomentsViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var repository: MomentsRepository
    private lateinit var viewModel: MomentsViewModel

    private val testMoment = MomentModel(momentId = "m1", reactions = emptyMap())

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = mockk(relaxed = true)
        viewModel = MomentsViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state has defaults`() {
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertTrue(state.moments.isEmpty())
        assertNull(state.error)
    }

    @Test
    fun `loadMoments success sets moments`() = runTest {
        coEvery { repository.getMoments("fam1") } returns listOf(testMoment)

        viewModel.loadMoments("fam1")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(1, state.moments.size)
        assertEquals("m1", state.moments[0].momentId)
    }

    @Test
    fun `loadMoments error sets error message`() = runTest {
        coEvery { repository.getMoments("fam1") } throws RuntimeException("Network error")

        viewModel.loadMoments("fam1")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNotNull(state.error)
    }

    @Test
    fun `loadMoments empty list sets empty moments`() = runTest {
        coEvery { repository.getMoments("fam1") } returns emptyList()

        viewModel.loadMoments("fam1")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertTrue(state.moments.isEmpty())
        assertNull(state.error)
    }

    @Test
    fun `addReaction optimistically updates local state`() = runTest {
        coEvery { repository.getMoments("fam1") } returns listOf(testMoment)
        viewModel.loadMoments("fam1")
        advanceUntilIdle()

        viewModel.addReaction("m1", "user1", "👍")
        advanceUntilIdle()

        val updatedMoment = viewModel.uiState.value.moments.first { it.momentId == "m1" }
        assertEquals("👍", updatedMoment.reactions["user1"])
    }

    @Test
    fun `addReaction calls repository`() = runTest {
        coEvery { repository.getMoments("fam1") } returns listOf(testMoment)
        viewModel.loadMoments("fam1")
        advanceUntilIdle()

        viewModel.addReaction("m1", "user1", "❤️")
        advanceUntilIdle()

        coVerify { repository.addReaction("m1", "user1", "❤️") }
    }
}
