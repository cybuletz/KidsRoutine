package com.kidsroutine.feature.pet.ui

import com.kidsroutine.core.engine.pet_engine.PetEngine
import com.kidsroutine.core.model.PetModel
import com.kidsroutine.core.model.PetSpecies
import com.kidsroutine.core.model.UserModel
import com.kidsroutine.feature.daily.data.UserRepository
import com.kidsroutine.feature.pet.data.PetRepository
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
class PetViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var petRepository: PetRepository
    private lateinit var petEngine: PetEngine
    private lateinit var userRepository: UserRepository
    private lateinit var viewModel: PetViewModel

    private val testUser = UserModel(userId = "u1", xp = 100)
    private val testPet = PetModel(petId = "p1", userId = "u1", name = "Buddy", species = PetSpecies.DRAGON)

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        petRepository = mockk(relaxed = true)
        petEngine = mockk(relaxed = true)
        userRepository = mockk(relaxed = true)
        every { userRepository.observeUser(any()) } returns flowOf(testUser)
        viewModel = PetViewModel(petRepository, petEngine, userRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is default`() {
        val state = viewModel.uiState.value
        assertNull(state.pet)
        assertFalse(state.isLoading)
        assertFalse(state.adoptionMode)
        assertNull(state.selectedSpecies)
        assertNull(state.error)
        assertFalse(state.showShop)
    }

    @Test
    fun `loadPet success sets pet`() = runTest {
        coEvery { petRepository.getPet("u1") } returns testPet

        viewModel.loadPet("u1")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(testPet, state.pet)
        assertFalse(state.adoptionMode)
    }

    @Test
    fun `loadPet null enters adoption mode`() = runTest {
        coEvery { petRepository.getPet("u1") } returns null

        viewModel.loadPet("u1")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNull(state.pet)
        assertTrue(state.adoptionMode)
    }

    @Test
    fun `loadPet error sets error`() = runTest {
        coEvery { petRepository.getPet("u1") } throws RuntimeException("DB error")

        viewModel.loadPet("u1")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals("DB error", state.error)
    }

    @Test
    fun `feedPet success updates pet`() = runTest {
        val fedPet = testPet.copy(happiness = 95, totalFed = 1)
        coEvery { petRepository.getPet("u1") } returns testPet
        coEvery { petEngine.feedPet(testPet, xpEarned = 50) } returns fedPet

        viewModel.loadPet("u1")
        advanceUntilIdle()

        viewModel.feedPet("u1")
        advanceUntilIdle()

        assertEquals(fedPet, viewModel.uiState.value.pet)
        coVerify { userRepository.updateUserXp("u1", -PetUiState.FEED_COST) }
        coVerify { petRepository.savePet(fedPet) }
    }

    @Test
    fun `feedPet not enough XP shows error`() = runTest {
        val lowXpUser = UserModel(userId = "u1", xp = 2)
        every { userRepository.observeUser(any()) } returns flowOf(lowXpUser)

        coEvery { petRepository.getPet("u1") } returns testPet

        viewModel.loadPet("u1")
        advanceUntilIdle()

        viewModel.feedPet("u1")

        val state = viewModel.uiState.value
        assertNotNull(state.error)
        assertTrue(state.error!!.contains("Not enough XP"))
    }

    @Test
    fun `interactWithPet updates pet`() = runTest {
        val updatedPet = testPet.copy(happiness = 90)
        coEvery { petRepository.getPet("u1") } returns testPet
        coEvery { petEngine.interactWithPet(testPet) } returns updatedPet

        viewModel.loadPet("u1")
        advanceUntilIdle()

        viewModel.interactWithPet()
        advanceUntilIdle()

        assertEquals(updatedPet, viewModel.uiState.value.pet)
        coVerify { petRepository.savePet(updatedPet) }
    }

    @Test
    fun `selectSpecies sets selected species`() {
        viewModel.selectSpecies(PetSpecies.WOLF)
        assertEquals(PetSpecies.WOLF, viewModel.uiState.value.selectedSpecies)
    }

    @Test
    fun `adoptPet blank name shows error`() {
        viewModel.adoptPet("u1", PetSpecies.DRAGON, "  ")

        val state = viewModel.uiState.value
        assertNotNull(state.error)
        assertTrue(state.error!!.contains("name"))
    }

    @Test
    fun `toggleShop toggles showShop`() {
        assertFalse(viewModel.uiState.value.showShop)

        viewModel.toggleShop()
        assertTrue(viewModel.uiState.value.showShop)

        viewModel.toggleShop()
        assertFalse(viewModel.uiState.value.showShop)
    }
}
