package com.kidsroutine.feature.family.ui

import com.kidsroutine.core.model.FamilyMessage
import com.kidsroutine.feature.family.data.FamilyMessageRepository
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
class FamilyMessagingViewModelTest {
    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var messageRepository: FamilyMessageRepository
    private lateinit var viewModel: FamilyMessagingViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        messageRepository = mockk(relaxed = true)
        viewModel = FamilyMessagingViewModel(messageRepository)
    }

    @After
    fun tearDown() { Dispatchers.resetMain() }

    @Test
    fun `initial state is default`() {
        assertFalse(viewModel.uiState.value.isLoading)
        assertTrue(viewModel.uiState.value.messages.isEmpty())
    }

    @Test
    fun `loadMessages success from flow`() = runTest {
        val msg = FamilyMessage(id = "m1", familyId = "fam1", content = "Hello")
        coEvery { messageRepository.observeFamilyMessages("fam1") } returns flowOf(listOf(msg))
        viewModel.loadMessages("fam1")
        advanceUntilIdle()
        assertEquals(1, viewModel.uiState.value.messages.size)
    }

    @Test
    fun `loadMessages error sets error`() = runTest {
        coEvery { messageRepository.observeFamilyMessages("fam1") } throws RuntimeException("Fail")
        viewModel.loadMessages("fam1")
        advanceUntilIdle()
        assertNotNull(viewModel.uiState.value.error)
    }
}
