package com.kidsroutine.feature.skilltree.ui

import com.kidsroutine.core.model.SkillBranch
import com.kidsroutine.core.model.SkillNode
import com.kidsroutine.core.model.SkillTree
import com.kidsroutine.feature.skilltree.data.SkillTreeRepository
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
class SkillTreeViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var repository: SkillTreeRepository
    private lateinit var viewModel: SkillTreeViewModel

    private val testNode = SkillNode(nodeId = "n1")
    private val testTree = SkillTree(
        userId = "u1",
        branches = mapOf(SkillBranch.RESPONSIBILITY to listOf(testNode))
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = mockk(relaxed = true)
        viewModel = SkillTreeViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state has defaults`() {
        val state = viewModel.uiState.value
        assertNull(state.tree)
        assertFalse(state.isLoading)
        assertEquals(SkillBranch.RESPONSIBILITY, state.selectedBranch)
        assertNull(state.selectedNode)
        assertNull(state.error)
    }

    @Test
    fun `loadSkillTree success sets tree`() = runTest {
        coEvery { repository.getSkillTree("u1") } returns testTree

        viewModel.loadSkillTree("u1")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertNotNull(state.tree)
        assertEquals("u1", state.tree?.userId)
        assertFalse(state.isLoading)
        assertNull(state.error)
    }

    @Test
    fun `loadSkillTree error sets error message`() = runTest {
        coEvery { repository.getSkillTree("u1") } throws RuntimeException("DB error")

        viewModel.loadSkillTree("u1")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals("DB error", state.error)
    }

    @Test
    fun `selectBranch updates selectedBranch and clears selectedNode`() {
        viewModel.selectNode(testNode)
        assertNotNull(viewModel.uiState.value.selectedNode)

        viewModel.selectBranch(SkillBranch.CREATIVITY)

        val state = viewModel.uiState.value
        assertEquals(SkillBranch.CREATIVITY, state.selectedBranch)
        assertNull(state.selectedNode)
    }

    @Test
    fun `selectNode sets selectedNode`() {
        viewModel.selectNode(testNode)
        assertEquals(testNode, viewModel.uiState.value.selectedNode)
    }

    @Test
    fun `dismissNodeDetail clears selectedNode`() {
        viewModel.selectNode(testNode)
        assertNotNull(viewModel.uiState.value.selectedNode)

        viewModel.dismissNodeDetail()
        assertNull(viewModel.uiState.value.selectedNode)
    }

    @Test
    fun `unlockNode success updates tree and clears selectedNode`() = runTest {
        val updatedTree = testTree.copy(totalNodesUnlocked = 1)
        coEvery { repository.unlockNode("u1", "n1") } returns updatedTree

        viewModel.selectNode(testNode)
        viewModel.unlockNode("u1", "n1")
        advanceUntilIdle()

        coVerify { repository.unlockNode("u1", "n1") }
        val state = viewModel.uiState.value
        assertEquals(1, state.tree?.totalNodesUnlocked)
        assertNull(state.selectedNode)
    }
}
