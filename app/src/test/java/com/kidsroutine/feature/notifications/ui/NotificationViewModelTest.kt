package com.kidsroutine.feature.notifications.ui

import com.kidsroutine.core.model.AppNotification
import com.kidsroutine.feature.notifications.data.NotificationRepository
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
class NotificationViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var repository: NotificationRepository
    private lateinit var viewModel: NotificationViewModel

    private val unreadNotification = AppNotification(id = "n1", isRead = false)
    private val readNotification = AppNotification(id = "n2", isRead = true)

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = mockk(relaxed = true)
        viewModel = NotificationViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state has defaults`() {
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertTrue(state.notifications.isEmpty())
        assertEquals(0, state.unreadCount)
        assertNull(state.error)
    }

    @Test
    fun `loadNotifications emits from flow`() = runTest {
        every { repository.observeUserNotifications("u1") } returns
            flowOf(listOf(unreadNotification, readNotification))

        viewModel.loadNotifications("u1")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(2, state.notifications.size)
    }

    @Test
    fun `unreadCount computed correctly`() = runTest {
        every { repository.observeUserNotifications("u1") } returns
            flowOf(listOf(unreadNotification, readNotification))

        viewModel.loadNotifications("u1")
        advanceUntilIdle()

        assertEquals(1, viewModel.uiState.value.unreadCount)
    }

    @Test
    fun `loadNotifications error sets error`() = runTest {
        every { repository.observeUserNotifications("u1") } throws RuntimeException("Failed")

        viewModel.loadNotifications("u1")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNotNull(state.error)
    }

    @Test
    fun `markAsRead calls repository`() = runTest {
        viewModel.markAsRead("n1")
        advanceUntilIdle()

        coVerify { repository.markAsRead("n1") }
    }

    @Test
    fun `deleteNotification calls repository`() = runTest {
        viewModel.deleteNotification("n1")
        advanceUntilIdle()

        coVerify { repository.deleteNotification("n1") }
    }

    @Test
    fun `loadNotifications with all-read list gives unreadCount 0`() = runTest {
        val allRead = listOf(
            AppNotification(id = "n3", isRead = true),
            AppNotification(id = "n4", isRead = true)
        )
        every { repository.observeUserNotifications("u1") } returns flowOf(allRead)

        viewModel.loadNotifications("u1")
        advanceUntilIdle()

        assertEquals(0, viewModel.uiState.value.unreadCount)
        assertEquals(2, viewModel.uiState.value.notifications.size)
    }
}
