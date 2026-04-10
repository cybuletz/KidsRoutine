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
        // checkEvolution: by default, return the pet unchanged (no evolution)
        every { petEngine.checkEvolution(any(), any()) } answers { firstArg() }
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

    // ── trainPet ──────────────────────────────────────────────────────

    @Test
    fun `trainPet success updates pet`() = runTest {
        val trainedPet = testPet.copy(happiness = 88, energy = 85)
        coEvery { petRepository.getPet("u1") } returns testPet
        coEvery { petEngine.trainPet(testPet) } returns trainedPet

        viewModel.loadPet("u1")
        advanceUntilIdle()

        viewModel.trainPet("u1")
        advanceUntilIdle()

        assertEquals(trainedPet, viewModel.uiState.value.pet)
        coVerify { userRepository.updateUserXp("u1", -PetViewModel.TRAIN_COST) }
        coVerify { petRepository.savePet(trainedPet) }
    }

    @Test
    fun `trainPet not enough XP shows error`() = runTest {
        val lowXpUser = UserModel(userId = "u1", xp = 5)
        every { userRepository.observeUser(any()) } returns flowOf(lowXpUser)

        coEvery { petRepository.getPet("u1") } returns testPet

        viewModel.loadPet("u1")
        advanceUntilIdle()

        viewModel.trainPet("u1")

        assertNotNull(viewModel.uiState.value.error)
        assertTrue(viewModel.uiState.value.error!!.contains("Not enough XP"))
    }

    // ── groomPet ──────────────────────────────────────────────────────

    @Test
    fun `groomPet updates pet`() = runTest {
        val groomedPet = testPet.copy(happiness = 85, energy = 83)
        coEvery { petRepository.getPet("u1") } returns testPet
        coEvery { petEngine.groomPet(testPet) } returns groomedPet

        viewModel.loadPet("u1")
        advanceUntilIdle()

        viewModel.groomPet()
        advanceUntilIdle()

        assertEquals(groomedPet, viewModel.uiState.value.pet)
        coVerify { petRepository.savePet(groomedPet) }
    }

    // ── adventureWithPet ──────────────────────────────────────────────

    @Test
    fun `adventureWithPet success updates pet`() = runTest {
        val adventurePet = testPet.copy(happiness = 95, energy = 90)
        coEvery { petRepository.getPet("u1") } returns testPet
        coEvery { petEngine.adventureWithPet(testPet) } returns adventurePet

        viewModel.loadPet("u1")
        advanceUntilIdle()

        viewModel.adventureWithPet("u1")
        advanceUntilIdle()

        assertEquals(adventurePet, viewModel.uiState.value.pet)
        coVerify { userRepository.updateUserXp("u1", -PetViewModel.ADVENTURE_COST) }
        coVerify { petRepository.savePet(adventurePet) }
    }

    @Test
    fun `adventureWithPet not enough XP shows error`() = runTest {
        val lowXpUser = UserModel(userId = "u1", xp = 10)
        every { userRepository.observeUser(any()) } returns flowOf(lowXpUser)

        coEvery { petRepository.getPet("u1") } returns testPet

        viewModel.loadPet("u1")
        advanceUntilIdle()

        viewModel.adventureWithPet("u1")

        assertNotNull(viewModel.uiState.value.error)
        assertTrue(viewModel.uiState.value.error!!.contains("Not enough XP"))
    }

    // ── napPet ──────────────────────────────────────────────────────

    @Test
    fun `napPet updates pet`() = runTest {
        val nappedPet = testPet.copy(energy = 92)
        coEvery { petRepository.getPet("u1") } returns testPet
        coEvery { petEngine.napPet(testPet) } returns nappedPet

        viewModel.loadPet("u1")
        advanceUntilIdle()

        viewModel.napPet()
        advanceUntilIdle()

        assertEquals(nappedPet, viewModel.uiState.value.pet)
        coVerify { petRepository.savePet(nappedPet) }
    }

    @Test
    fun `napPet with no pet does nothing`() {
        viewModel.napPet()
        assertNull(viewModel.uiState.value.pet)
    }

    // ── treatPet ──────────────────────────────────────────────────────

    @Test
    fun `treatPet success updates pet`() = runTest {
        val treatedPet = testPet.copy(happiness = 86, energy = 84)
        coEvery { petRepository.getPet("u1") } returns testPet
        coEvery { petEngine.treatPet(testPet) } returns treatedPet

        viewModel.loadPet("u1")
        advanceUntilIdle()

        viewModel.treatPet("u1")
        advanceUntilIdle()

        assertEquals(treatedPet, viewModel.uiState.value.pet)
        coVerify { userRepository.updateUserXp("u1", -PetViewModel.TREAT_COST) }
        coVerify { petRepository.savePet(treatedPet) }
    }

    @Test
    fun `treatPet not enough XP shows error`() = runTest {
        val lowXpUser = UserModel(userId = "u1", xp = 1)
        every { userRepository.observeUser(any()) } returns flowOf(lowXpUser)

        coEvery { petRepository.getPet("u1") } returns testPet

        viewModel.loadPet("u1")
        advanceUntilIdle()

        viewModel.treatPet("u1")

        assertNotNull(viewModel.uiState.value.error)
        assertTrue(viewModel.uiState.value.error!!.contains("Not enough XP"))
    }

    // ── treasureHuntWithPet ──────────────────────────────────────────

    @Test
    fun `treasureHuntWithPet success updates pet`() = runTest {
        val huntPet = testPet.copy(happiness = 92, energy = 85)
        coEvery { petRepository.getPet("u1") } returns testPet
        coEvery { petEngine.treasureHuntWithPet(testPet) } returns huntPet

        viewModel.loadPet("u1")
        advanceUntilIdle()

        viewModel.treasureHuntWithPet("u1")
        advanceUntilIdle()

        assertEquals(huntPet, viewModel.uiState.value.pet)
        coVerify { userRepository.updateUserXp("u1", -PetViewModel.TREASURE_HUNT_COST) }
        coVerify { petRepository.savePet(huntPet) }
    }

    @Test
    fun `treasureHuntWithPet not enough XP shows error`() = runTest {
        val lowXpUser = UserModel(userId = "u1", xp = 7)
        every { userRepository.observeUser(any()) } returns flowOf(lowXpUser)

        coEvery { petRepository.getPet("u1") } returns testPet

        viewModel.loadPet("u1")
        advanceUntilIdle()

        viewModel.treasureHuntWithPet("u1")

        assertNotNull(viewModel.uiState.value.error)
        assertTrue(viewModel.uiState.value.error!!.contains("Not enough XP"))
    }

    // ── purchaseAccessory ──────────────────────────────────────────────

    @Test
    fun `purchaseAccessory not enough XP shows error`() = runTest {
        val lowXpUser = UserModel(userId = "u1", xp = 1)
        every { userRepository.observeUser(any()) } returns flowOf(lowXpUser)

        coEvery { petRepository.getPet("u1") } returns testPet

        viewModel.loadPet("u1")
        advanceUntilIdle()

        viewModel.purchaseAccessory(PetUiState.SHOP_ITEMS.first())

        assertNotNull(viewModel.uiState.value.error)
        assertTrue(viewModel.uiState.value.error!!.contains("Not enough XP"))
    }

    // ── clearError ──────────────────────────────────────────────────────

    @Test
    fun `clearError clears error`() = runTest {
        val lowXpUser = UserModel(userId = "u1", xp = 1)
        every { userRepository.observeUser(any()) } returns flowOf(lowXpUser)

        coEvery { petRepository.getPet("u1") } returns testPet

        viewModel.loadPet("u1")
        advanceUntilIdle()

        viewModel.treatPet("u1")
        assertNotNull(viewModel.uiState.value.error)

        viewModel.clearError()
        assertNull(viewModel.uiState.value.error)
    }
}
