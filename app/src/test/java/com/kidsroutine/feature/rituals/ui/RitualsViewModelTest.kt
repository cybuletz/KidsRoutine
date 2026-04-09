package com.kidsroutine.feature.rituals.ui

import com.kidsroutine.core.model.FamilyRitual
import com.kidsroutine.core.model.RitualType
import com.kidsroutine.core.model.RitualFrequency
import com.kidsroutine.feature.rituals.data.RitualsRepository
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RitualsViewModelTest {
    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var ritualsRepository: RitualsRepository
    private lateinit var viewModel: RitualsViewModel

    private val testRitual = FamilyRitual(ritualId = "r1", familyId = "fam1", title = "Bedtime story", type = RitualType.GRATITUDE, frequency = RitualFrequency.DAILY)

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        ritualsRepository = mockk(relaxed = true)
        viewModel = RitualsViewModel(ritualsRepository)
    }

    @After
    fun tearDown() { Dispatchers.resetMain() }

    @Test
    fun `initial state is default`() {
        assertFalse(viewModel.uiState.value.isLoading)
        assertTrue(viewModel.uiState.value.rituals.isEmpty())
    }

    @Test
    fun `loadRituals success sets rituals`() = runTest {
        coEvery { ritualsRepository.getRituals("fam1") } returns listOf(testRitual)
        viewModel.loadRituals("fam1")
        advanceUntilIdle()
        assertEquals(1, viewModel.uiState.value.rituals.size)
    }

    @Test
    fun `loadRituals error sets error`() = runTest {
        coEvery { ritualsRepository.getRituals("fam1") } throws RuntimeException("Fail")
        viewModel.loadRituals("fam1")
        advanceUntilIdle()
        assertNotNull(viewModel.uiState.value.error)
    }

    @Test
    fun `deleteRitual calls repository`() = runTest {
        viewModel.deleteRitual("r1", "fam1")
        advanceUntilIdle()
        coVerify { ritualsRepository.deleteRitual("r1") }
    }

    @Test
    fun `completeRitual calls repository`() = runTest {
        viewModel.completeRitual("r1", "fam1")
        advanceUntilIdle()
        coVerify { ritualsRepository.completeRitual("r1") }
    }

    @Test
    fun `selectRitual sets selected ritual`() {
        viewModel.selectRitual(testRitual)
        assertEquals(testRitual, viewModel.uiState.value.selectedRitual)
    }

    @Test
    fun `toggleCreateForm toggles flag`() {
        viewModel.toggleCreateForm()
        assertTrue(viewModel.uiState.value.showCreateForm)
    }
}
