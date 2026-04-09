package com.kidsroutine.feature.family.ui

import com.kidsroutine.core.model.FamilyModel
import com.kidsroutine.feature.family.data.FamilyRepository
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
class FamilySetupViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var repository: FamilyRepository
    private lateinit var viewModel: FamilySetupViewModel

    private val testFamily = FamilyModel(familyId = "fam1", familyName = "Test Family")

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = mockk(relaxed = true)
        viewModel = FamilySetupViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state has defaults`() {
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNull(state.family)
        assertEquals("", state.familyName)
        assertNull(state.error)
        assertFalse(state.success)
    }

    @Test
    fun `updateFamilyName sets name and clears error`() {
        viewModel.updateFamilyName("My Family")

        val state = viewModel.uiState.value
        assertEquals("My Family", state.familyName)
        assertNull(state.error)
    }

    @Test
    fun `createFamily success sets family and success`() = runTest {
        coEvery { repository.createFamily("u1", "Test Family") } returns testFamily

        viewModel.createFamily("u1", "Test Family")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNotNull(state.family)
        assertEquals("fam1", state.family?.familyId)
        assertTrue(state.success)
        assertNull(state.error)
    }

    @Test
    fun `createFamily error sets error message`() = runTest {
        coEvery { repository.createFamily("u1", "Test") } throws RuntimeException("Create failed")

        viewModel.createFamily("u1", "Test")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNotNull(state.error)
        assertFalse(state.success)
    }

    @Test
    fun `createFamily with empty name shows error without calling repo`() = runTest {
        viewModel.createFamily("u1", "")
        advanceUntilIdle()

        assertEquals("Family name is required", viewModel.uiState.value.error)
        coVerify(exactly = 0) { repository.createFamily(any(), any()) }
    }

    @Test
    fun `createFamily sets isLoading during call`() = runTest {
        coEvery { repository.createFamily("u1", "Fam") } returns testFamily

        viewModel.createFamily("u1", "Fam")
        advanceUntilIdle()

        // After completion isLoading should be false
        assertFalse(viewModel.uiState.value.isLoading)
    }
}
