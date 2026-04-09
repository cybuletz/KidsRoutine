package com.kidsroutine.feature.daily.domain

import com.kidsroutine.core.model.DailyStateModel
import com.kidsroutine.feature.daily.data.DailyRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class GetDailyStateUseCaseTest {

    private lateinit var repository: DailyRepository
    private lateinit var useCase: GetDailyStateUseCase

    @Before
    fun setUp() {
        repository = mockk()
        useCase = GetDailyStateUseCase(repository)
    }

    @Test
    fun `invoke delegates to repository observeDailyState`() = runTest {
        val expected = DailyStateModel(userId = "u1")
        every {
            repository.observeDailyState("f1", "u1", "2026-01-01")
        } returns flowOf(expected)

        val result = useCase("f1", "u1", "2026-01-01").first()

        assertEquals("u1", result.userId)
        verify(exactly = 1) { repository.observeDailyState("f1", "u1", "2026-01-01") }
    }

    @Test
    fun `returns flow from repository`() = runTest {
        val state1 = DailyStateModel(userId = "u1")
        val state2 = DailyStateModel(userId = "u1", completedCount = 5)
        every {
            repository.observeDailyState("f1", "u1", "2026-01-01")
        } returns flowOf(state1, state2)

        val flow = useCase("f1", "u1", "2026-01-01")
        assertNotNull(flow)
    }

    @Test
    fun `passes familyId correctly`() = runTest {
        every {
            repository.observeDailyState(any(), any(), any())
        } returns flowOf(DailyStateModel())

        useCase("family_abc", "user_xyz", "2026-04-09")

        verify { repository.observeDailyState("family_abc", "user_xyz", "2026-04-09") }
    }
}
