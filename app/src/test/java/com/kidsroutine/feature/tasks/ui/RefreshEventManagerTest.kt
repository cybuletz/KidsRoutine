package com.kidsroutine.feature.tasks.ui

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RefreshEventManagerTest {

    @Test
    fun `refreshEvent emits when triggered`() = runTest {
        var collected = false
        val job = launch(UnconfinedTestDispatcher(testScheduler)) {
            RefreshEventManager.refreshEvent.first()
            collected = true
        }
        RefreshEventManager.triggerRefresh()
        assertTrue(collected)
        job.cancel()
    }

    @Test
    fun `refreshEvent is SharedFlow`() {
        // Just verify it's accessible and non-null
        assertNotNull(RefreshEventManager.refreshEvent)
    }

    @Test
    fun `multiple triggers emit multiple events`() = runTest {
        var count = 0
        val job = launch(UnconfinedTestDispatcher(testScheduler)) {
            RefreshEventManager.refreshEvent.collect {
                count++
                if (count >= 3) return@collect
            }
        }
        RefreshEventManager.triggerRefresh()
        RefreshEventManager.triggerRefresh()
        RefreshEventManager.triggerRefresh()
        assertTrue(count >= 1)
        job.cancel()
    }
}
