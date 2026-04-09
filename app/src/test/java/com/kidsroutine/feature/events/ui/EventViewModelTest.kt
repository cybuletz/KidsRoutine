package com.kidsroutine.feature.events.ui

import com.kidsroutine.core.engine.event_engine.EventEngine
import com.kidsroutine.core.model.EventProgress
import com.kidsroutine.core.model.EventShopItem
import com.kidsroutine.core.model.TimedEvent
import com.kidsroutine.feature.events.data.EventRepository
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class EventViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var eventRepository: EventRepository
    private lateinit var eventEngine: EventEngine
    private lateinit var viewModel: EventViewModel

    private val testEvent = TimedEvent(eventId = "e1", title = "Halloween Bash")
    private val testProgress = EventProgress(
        eventId = "e1",
        userId = "u1",
        tokensEarned = 50,
        tokensSpent = 10
    )
    private val testShopItem = EventShopItem(
        itemId = "item1",
        name = "Badge",
        emoji = "🏅",
        tokenCost = 10
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        eventRepository = mockk(relaxed = true)
        eventEngine = mockk(relaxed = true)
        every { eventEngine.timeRemainingSeconds(any(), any()) } returns 3600L
        viewModel = EventViewModel(eventRepository, eventEngine)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is default`() {
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertTrue(state.events.isEmpty())
        assertNull(state.selectedEvent)
        assertNull(state.progress)
        assertFalse(state.showShop)
        assertNull(state.error)
    }

    @Test
    fun `loadEvents success sets events and progress`() = runTest {
        coEvery { eventRepository.getActiveEvents() } returns listOf(testEvent)
        coEvery { eventRepository.getEventProgress("e1", "u1") } returns testProgress
        coEvery { eventRepository.getShopItems("e1") } returns listOf(testShopItem)

        viewModel.loadEvents("u1")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(1, state.events.size)
        assertEquals(testEvent, state.selectedEvent)
        assertEquals(testProgress, state.progress)
        assertEquals(1, state.shopItems.size)
    }

    @Test
    fun `loadEvents error sets error`() = runTest {
        coEvery { eventRepository.getActiveEvents() } throws RuntimeException("Network error")

        viewModel.loadEvents("u1")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals("Network error", state.error)
    }

    @Test
    fun `loadEvents empty list`() = runTest {
        coEvery { eventRepository.getActiveEvents() } returns emptyList()

        viewModel.loadEvents("u1")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertTrue(state.events.isEmpty())
        assertNull(state.selectedEvent)
    }

    @Test
    fun `purchaseShopItem success updates progress`() = runTest {
        val updatedProgress = testProgress.copy(tokensSpent = 20)
        every { eventEngine.spendTokens(testProgress, 10) } returns updatedProgress

        coEvery { eventRepository.getActiveEvents() } returns listOf(testEvent)
        coEvery { eventRepository.getEventProgress("e1", "u1") } returns testProgress
        coEvery { eventRepository.getShopItems("e1") } returns listOf(testShopItem)

        viewModel.loadEvents("u1")
        advanceUntilIdle()

        viewModel.purchaseShopItem(testShopItem)

        val state = viewModel.uiState.value
        assertNotNull(state.purchaseMessage)
        assertTrue(state.purchaseMessage!!.contains("Purchased"))
        assertTrue(state.progress!!.rewardsClaimed.contains("item1"))
    }

    @Test
    fun `purchaseShopItem not enough tokens shows message`() = runTest {
        every { eventEngine.spendTokens(any(), any()) } returns null

        coEvery { eventRepository.getActiveEvents() } returns listOf(testEvent)
        coEvery { eventRepository.getEventProgress("e1", "u1") } returns testProgress
        coEvery { eventRepository.getShopItems("e1") } returns listOf(testShopItem)

        viewModel.loadEvents("u1")
        advanceUntilIdle()

        viewModel.purchaseShopItem(testShopItem)

        val state = viewModel.uiState.value
        assertEquals("Not enough tokens!", state.purchaseMessage)
    }

    @Test
    fun `toggleShop toggles showShop`() {
        assertFalse(viewModel.uiState.value.showShop)

        viewModel.toggleShop()
        assertTrue(viewModel.uiState.value.showShop)

        viewModel.toggleShop()
        assertFalse(viewModel.uiState.value.showShop)
    }

    @Test
    fun `dismissPurchaseMessage clears message`() = runTest {
        every { eventEngine.spendTokens(any(), any()) } returns null

        coEvery { eventRepository.getActiveEvents() } returns listOf(testEvent)
        coEvery { eventRepository.getEventProgress("e1", "u1") } returns testProgress
        coEvery { eventRepository.getShopItems("e1") } returns listOf(testShopItem)

        viewModel.loadEvents("u1")
        advanceUntilIdle()

        viewModel.purchaseShopItem(testShopItem)
        assertNotNull(viewModel.uiState.value.purchaseMessage)

        viewModel.dismissPurchaseMessage()
        assertNull(viewModel.uiState.value.purchaseMessage)
    }
}
