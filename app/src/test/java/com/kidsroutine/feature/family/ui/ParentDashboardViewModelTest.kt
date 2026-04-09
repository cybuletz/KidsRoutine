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
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ParentDashboardViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var repository: FamilyRepository
    private lateinit var viewModel: ParentDashboardViewModel

    private val testFamily = FamilyModel(familyId = "fam1", familyName = "Test Family")

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = mockk(relaxed = true)
        viewModel = ParentDashboardViewModel(repository)
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
        assertEquals("", state.inviteCode)
        assertNull(state.error)
    }

    @Test
    fun `loadFamily success sets family and invite code`() = runTest {
        coEvery { repository.getFamily("fam1") } returns testFamily
        coEvery { repository.getInviteCode("fam1") } returns "ABC123"

        viewModel.loadFamily("fam1")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNotNull(state.family)
        assertEquals("Test Family", state.family?.familyName)
        assertEquals("ABC123", state.inviteCode)
        assertNull(state.error)
    }

    @Test
    fun `loadFamily null family shows error`() = runTest {
        coEvery { repository.getFamily("fam1") } returns null
        coEvery { repository.getInviteCode("fam1") } returns ""

        viewModel.loadFamily("fam1")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNull(state.family)
        assertEquals("Family not found", state.error)
    }

    @Test
    fun `loadFamily error sets error message`() = runTest {
        coEvery { repository.getFamily("fam1") } throws RuntimeException("Network error")

        viewModel.loadFamily("fam1")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNotNull(state.error)
    }

    @Test
    fun `loadFamily with empty familyId shows error without calling repo`() = runTest {
        viewModel.loadFamily("")
        advanceUntilIdle()

        assertEquals("Invalid family ID", viewModel.uiState.value.error)
        coVerify(exactly = 0) { repository.getFamily(any()) }
    }

    @Test
    fun `isLoading transitions correctly`() = runTest {
        coEvery { repository.getFamily("fam1") } returns testFamily
        coEvery { repository.getInviteCode("fam1") } returns "XYZ"

        viewModel.loadFamily("fam1")
        advanceUntilIdle()

        // After completion, isLoading should be false
        assertFalse(viewModel.uiState.value.isLoading)
    }
}
