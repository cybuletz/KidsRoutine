package com.kidsroutine.feature.world.ui

import com.kidsroutine.core.model.UserModel
import com.kidsroutine.core.model.WorldModel
import com.kidsroutine.core.model.WorldNode
import com.kidsroutine.feature.daily.data.UserRepository
import com.kidsroutine.feature.world.data.WorldRepository
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.emptyFlow
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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class WorldViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var worldRepository: WorldRepository
    private lateinit var userRepository: UserRepository
    private lateinit var viewModel: WorldViewModel

    private val testUser = UserModel(userId = "u1", xp = 100)
    private val testWorld = WorldModel(worldId = "w1", nodes = listOf(WorldNode(nodeId = "n1")))
    private val testNode = WorldNode(nodeId = "n1")

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        worldRepository = mockk(relaxed = true)
        userRepository = mockk(relaxed = true)
        viewModel = WorldViewModel(worldRepository, userRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state has isLoading true`() {
        val state = viewModel.uiState.value
        assertTrue(state.isLoading)
    }

    @Test
    fun `loadWorld sets world and user`() = runTest {
        coEvery { worldRepository.getWorld(100) } returns testWorld
        every { userRepository.observeUser("u1") } returns flowOf(testUser)

        viewModel.loadWorld("u1", testUser)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNotNull(state.world)
        assertEquals("w1", state.world?.worldId)
        assertEquals("u1", state.currentUser.userId)
    }

    @Test
    fun `onNodeTapped sets selectedNode and showNodeDetail`() {
        viewModel.onNodeTapped(testNode)

        val state = viewModel.uiState.value
        assertEquals(testNode, state.selectedNode)
        assertTrue(state.showNodeDetail)
    }

    @Test
    fun `dismissNodeDetail clears showNodeDetail`() {
        viewModel.onNodeTapped(testNode)
        assertTrue(viewModel.uiState.value.showNodeDetail)

        viewModel.dismissNodeDetail()
        assertFalse(viewModel.uiState.value.showNodeDetail)
    }

    @Test
    fun `loadWorld with error sets isLoading false`() = runTest {
        coEvery { worldRepository.getWorld(any()) } throws RuntimeException("World error")
        every { userRepository.observeUser("u1") } returns emptyFlow()

        viewModel.loadWorld("u1", testUser)
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `loadWorld applies fallbackUser`() = runTest {
        coEvery { worldRepository.getWorld(100) } returns testWorld
        every { userRepository.observeUser("u1") } returns emptyFlow()

        viewModel.loadWorld("u1", testUser)
        advanceUntilIdle()

        assertEquals(testUser, viewModel.uiState.value.currentUser)
    }
}
