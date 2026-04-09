package com.kidsroutine.feature.lootbox.ui

import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.kidsroutine.core.model.LootBox
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LootBoxViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var firestore: FirebaseFirestore
    private lateinit var viewModel: LootBoxViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        mockkStatic("kotlinx.coroutines.tasks.TasksKt")
        firestore = mockk(relaxed = true)

        val mockCollectionRef = mockk<CollectionReference>(relaxed = true)
        val mockDocRef = mockk<DocumentReference>(relaxed = true)
        val mockTask = mockk<Task<Void>>(relaxed = true)
        every { firestore.collection(any()) } returns mockCollectionRef
        every { mockCollectionRef.document(any()) } returns mockDocRef
        every { mockDocRef.collection(any()) } returns mockCollectionRef
        every { mockDocRef.update(any<String>(), any()) } returns mockTask
        every { mockDocRef.set(any()) } returns mockTask
        coEvery { mockTask.await() } returns null

        val mockAddTask = mockk<Task<DocumentReference>>(relaxed = true)
        every { mockCollectionRef.add(any()) } returns mockAddTask
        coEvery { mockAddTask.await() } returns mockDocRef

        viewModel = LootBoxViewModel(firestore)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkStatic("kotlinx.coroutines.tasks.TasksKt")
    }

    @Test
    fun `initial state is IDLE`() {
        val state = viewModel.uiState.value
        assertEquals(LootBoxPhase.IDLE, state.phase)
        assertNull(state.lootBox)
        assertNull(state.reward)
    }

    @Test
    fun `presentBox sets WAITING and rolls reward`() {
        val box = LootBox(boxId = "lb1", earnedFor = "streak")

        viewModel.presentBox(box, "u1")

        val state = viewModel.uiState.value
        assertEquals(LootBoxPhase.WAITING, state.phase)
        assertNotNull(state.lootBox)
        assertNotNull(state.reward)
    }

    @Test
    fun `presentBox stores userId`() {
        val box = LootBox(boxId = "lb1")

        viewModel.presentBox(box, "u1")

        assertEquals("u1", viewModel.uiState.value.userId)
    }

    @Test
    fun `onBoxTapped in non-WAITING phase does nothing`() {
        assertEquals(LootBoxPhase.IDLE, viewModel.uiState.value.phase)

        viewModel.onBoxTapped()

        assertEquals(LootBoxPhase.IDLE, viewModel.uiState.value.phase)
    }

    @Test
    fun `reset returns to IDLE`() {
        val box = LootBox(boxId = "lb1")
        viewModel.presentBox(box, "u1")
        assertEquals(LootBoxPhase.WAITING, viewModel.uiState.value.phase)

        viewModel.reset()

        assertEquals(LootBoxPhase.IDLE, viewModel.uiState.value.phase)
        assertNull(viewModel.uiState.value.lootBox)
    }

    @Test
    fun `rewardPool is not empty - presentBox always has reward`() {
        val box = LootBox(boxId = "lb1")

        // Call multiple times to exercise the random reward pool
        repeat(5) {
            viewModel.presentBox(box, "u1")
            assertNotNull(viewModel.uiState.value.reward)
        }
    }

    @Test
    fun `dismiss transitions to DONE`() = runTest {
        val box = LootBox(boxId = "lb1")
        viewModel.presentBox(box, "u1")

        viewModel.dismiss()
        advanceUntilIdle()

        assertEquals(LootBoxPhase.DONE, viewModel.uiState.value.phase)
    }
}
