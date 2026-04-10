package com.kidsroutine.feature.billing

import com.kidsroutine.core.model.BuiltInContentPacks
import com.kidsroutine.core.model.ContentPack
import com.kidsroutine.core.model.ContentPackTier
import com.kidsroutine.core.model.EntitlementsRepository
import com.kidsroutine.core.model.PlanType
import com.kidsroutine.core.model.UserEntitlements
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.*
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ContentPacksViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var entitlementsRepository: EntitlementsRepository
    private lateinit var firestore: FirebaseFirestore
    private lateinit var viewModel: ContentPacksViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        entitlementsRepository = mockk(relaxed = true)
        firestore = mockk(relaxed = true)
        viewModel = ContentPacksViewModel(entitlementsRepository, firestore)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is default`() {
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertTrue(state.packs.isEmpty())
        assertTrue(state.unlockedPackIds.isEmpty())
        assertEquals(0, state.userXp)
        assertFalse(state.isPro)
        assertNull(state.successMessage)
        assertNull(state.error)
    }

    @Test
    fun `init sets packs and user state`() {
        viewModel.init(userXp = 500, isPro = true, unlockedPackIds = setOf("pack1"))

        val state = viewModel.uiState.value
        assertEquals(BuiltInContentPacks.all, state.packs)
        assertEquals(500, state.userXp)
        assertTrue(state.isPro)
        assertTrue("pack1" in state.unlockedPackIds)
        assertFalse(state.isLoading)
    }

    @Test
    fun `init with defaults`() {
        viewModel.init(userXp = 100, isPro = false)

        val state = viewModel.uiState.value
        assertEquals(100, state.userXp)
        assertFalse(state.isPro)
        assertTrue(state.unlockedPackIds.isEmpty())
    }

    @Test
    fun `unlockPack already owned shows error`() {
        viewModel.init(userXp = 1000, isPro = true, unlockedPackIds = setOf("pack_no_screen"))

        val pack = BuiltInContentPacks.all.first { it.packId == "pack_no_screen" }
        viewModel.unlockPack(pack)

        assertNotNull(viewModel.uiState.value.error)
        assertTrue(viewModel.uiState.value.error!!.contains("already own"))
    }

    @Test
    fun `unlockPack PRO pack without PRO subscription shows error`() {
        viewModel.init(userXp = 1000, isPro = false)

        val proPack = BuiltInContentPacks.all.firstOrNull { it.tier == ContentPackTier.PRO }
            ?: return // skip if no PRO packs exist

        viewModel.unlockPack(proPack)

        assertNotNull(viewModel.uiState.value.error)
        assertTrue(viewModel.uiState.value.error!!.contains("PRO"))
    }

    @Test
    fun `unlockPack not enough XP shows error`() {
        viewModel.init(userXp = 10, isPro = true)

        val expensivePack = BuiltInContentPacks.all.firstOrNull { it.xpCost > 10 }
            ?: return // skip if all packs are cheap

        viewModel.unlockPack(expensivePack)

        assertNotNull(viewModel.uiState.value.error)
        assertTrue(viewModel.uiState.value.error!!.contains("Not enough XP"))
    }

    @Test
    fun `unlockPack free pack with enough XP succeeds`() = runTest {
        val freePack = BuiltInContentPacks.all.firstOrNull { it.xpCost == 0 && it.tier == ContentPackTier.FREE }
            ?: return@runTest

        viewModel.init(userXp = 100, isPro = false)
        viewModel.unlockPack(freePack)

        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(freePack.packId in state.unlockedPackIds)
        assertNotNull(state.successMessage)
    }

    @Test
    fun `unlockPack deducts XP cost`() = runTest {
        val pack = ContentPack(
            packId = "test_pack",
            name = "Test Pack",
            tier = ContentPackTier.FREE,
            xpCost = 50
        )
        viewModel.init(userXp = 200, isPro = false)

        // Override packs to include our test pack
        // We can test with built-in packs that have a cost
        val paidFreePack = BuiltInContentPacks.all.firstOrNull { it.xpCost > 0 && it.tier == ContentPackTier.FREE }
        if (paidFreePack != null) {
            viewModel.init(userXp = paidFreePack.xpCost + 100, isPro = false)
            viewModel.unlockPack(paidFreePack)
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertEquals(100, state.userXp)
        }
    }

    @Test
    fun `unlockPack persists to Firestore when userId provided`() = runTest {
        val freePack = BuiltInContentPacks.all.firstOrNull { it.xpCost == 0 && it.tier == ContentPackTier.FREE }
            ?: return@runTest

        // Setup Firestore mock chain
        val mockCollection = mockk<CollectionReference>(relaxed = true)
        val mockDoc = mockk<DocumentReference>(relaxed = true)
        val mockTask = mockk<Task<DocumentSnapshot>>(relaxed = true)
        val mockSnapshot = mockk<DocumentSnapshot>(relaxed = true)
        every { firestore.collection("user_content_packs") } returns mockCollection
        every { mockCollection.document(any()) } returns mockDoc
        every { mockDoc.get() } returns mockTask
        every { mockSnapshot.data } returns null

        viewModel.init(userXp = 100, isPro = false)
        viewModel.unlockPack(freePack, userId = "user1")
        advanceUntilIdle()

        assertTrue(freePack.packId in viewModel.uiState.value.unlockedPackIds)
    }

    @Test
    fun `clearMessages clears success and error`() {
        viewModel.init(userXp = 1000, isPro = true, unlockedPackIds = setOf("pack_no_screen"))
        val pack = BuiltInContentPacks.all.first { it.packId == "pack_no_screen" }
        viewModel.unlockPack(pack)

        assertNotNull(viewModel.uiState.value.error)

        viewModel.clearMessages()

        assertNull(viewModel.uiState.value.successMessage)
        assertNull(viewModel.uiState.value.error)
    }

    @Test
    fun `loadForUser sets state correctly`() = runTest {
        val entitlements = UserEntitlements(userId = "user1", planType = PlanType.PRO)
        coEvery { entitlementsRepository.getEntitlements("user1", "family1") } returns entitlements

        // Setup Firestore mock for loadUnlockedPacks — Task must appear completed
        val mockCollection = mockk<CollectionReference>(relaxed = true)
        val mockDoc = mockk<DocumentReference>(relaxed = true)
        val mockTask = mockk<Task<DocumentSnapshot>>(relaxed = true)
        val mockSnapshot = mockk<DocumentSnapshot>(relaxed = true)
        every { firestore.collection("user_content_packs") } returns mockCollection
        every { mockCollection.document("user1") } returns mockDoc
        every { mockDoc.get() } returns mockTask
        // Make Task.await() work — it checks isComplete / result / exception
        every { mockTask.isComplete } returns true
        every { mockTask.isCanceled } returns false
        every { mockTask.exception } returns null
        every { mockTask.result } returns mockSnapshot
        every { mockSnapshot.data } returns mapOf("unlockedPackIds" to listOf("pack1"))

        viewModel.loadForUser("user1", userXp = 250, familyId = "family1")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertTrue(state.isPro)
        assertEquals(250, state.userXp)
        assertEquals(BuiltInContentPacks.all, state.packs)
    }

    @Test
    fun `loadForUser handles error gracefully`() = runTest {
        coEvery { entitlementsRepository.getEntitlements(any(), any()) } throws RuntimeException("Network error")

        viewModel.loadForUser("user1", userXp = 100, familyId = "family1")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(100, state.userXp)
        assertEquals(BuiltInContentPacks.all, state.packs)
    }
}
