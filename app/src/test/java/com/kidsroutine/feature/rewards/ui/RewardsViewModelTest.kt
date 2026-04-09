package com.kidsroutine.feature.rewards.ui

import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.kidsroutine.core.model.PrivilegeRequest
import com.kidsroutine.core.model.PrivilegeRequestStatus
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
class RewardsViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var firestore: FirebaseFirestore
    private lateinit var viewModel: RewardsViewModel

    private lateinit var mockCollectionRef: CollectionReference
    private lateinit var mockDocRef: DocumentReference

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        mockkStatic("kotlinx.coroutines.tasks.TasksKt")
        firestore = mockk(relaxed = true)

        mockCollectionRef = mockk<CollectionReference>(relaxed = true)
        mockDocRef = mockk<DocumentReference>(relaxed = true)
        val mockTask = mockk<Task<Void>>(relaxed = true)
        every { firestore.collection(any()) } returns mockCollectionRef
        every { mockCollectionRef.document(any()) } returns mockDocRef
        every { mockDocRef.collection(any()) } returns mockCollectionRef
        every { mockDocRef.update(any<String>(), any()) } returns mockTask
        every { mockDocRef.set(any()) } returns mockTask
        every { mockDocRef.delete() } returns mockTask

        // Query chain mocks
        val mockQuery = mockk<Query>(relaxed = true)
        every { mockCollectionRef.whereEqualTo(any<String>(), any()) } returns mockQuery
        every { mockQuery.orderBy(any<String>(), any()) } returns mockQuery
        every { mockQuery.limit(any()) } returns mockQuery

        val mockQuerySnapshot = mockk<QuerySnapshot>(relaxed = true)
        val mockGetTask = mockk<Task<QuerySnapshot>>(relaxed = true)
        every { mockQuery.get() } returns mockGetTask
        coEvery { mockGetTask.await() } returns mockQuerySnapshot
        every { mockQuerySnapshot.documents } returns emptyList()

        viewModel = RewardsViewModel(firestore)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkStatic("kotlinx.coroutines.tasks.TasksKt")
    }

    @Test
    fun `initial state is default`() {
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertTrue(state.myRequests.isEmpty())
        assertEquals(0, state.pendingCount)
        assertNull(state.successMessage)
        assertNull(state.errorMessage)
    }

    @Test
    fun `clearMessages clears success and error`() = runTest {
        // Trigger an error so messages are set
        val privilege = Privilege(
            id = "p1",
            emoji = "📱",
            title = "Screen Time",
            description = "30 min extra",
            xpCost = 50,
            category = PrivilegeCategory.SCREEN
        )

        viewModel.requestPrivilege("fam1", "u1", "Alice", privilege)
        advanceUntilIdle()

        viewModel.clearMessages()

        assertNull(viewModel.uiState.value.successMessage)
        assertNull(viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `requestPrivilege sets successMessage`() = runTest {
        val privilege = Privilege(
            id = "p1",
            emoji = "📱",
            title = "Screen Time",
            description = "30 min extra",
            xpCost = 50,
            category = PrivilegeCategory.SCREEN
        )

        viewModel.requestPrivilege("fam1", "u1", "Alice", privilege)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertNotNull(state.successMessage)
        assertTrue(state.successMessage!!.contains("Request sent"))
        assertTrue(state.myRequests.isNotEmpty())
    }

    @Test
    fun `requestPrivilege error sets errorMessage`() = runTest {
        every { mockDocRef.set(any()) } throws RuntimeException("Firestore error")

        val privilege = Privilege(
            id = "p1",
            emoji = "📱",
            title = "Screen Time",
            description = "30 min extra",
            xpCost = 50,
            category = PrivilegeCategory.SCREEN
        )

        viewModel.requestPrivilege("fam1", "u1", "Alice", privilege)
        advanceUntilIdle()

        assertNotNull(viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `cancelRequest removes from local list`() = runTest {
        val privilege = Privilege(
            id = "p1",
            emoji = "📱",
            title = "Screen Time",
            description = "30 min extra",
            xpCost = 50,
            category = PrivilegeCategory.SCREEN
        )

        viewModel.requestPrivilege("fam1", "u1", "Alice", privilege)
        advanceUntilIdle()

        val requestId = viewModel.uiState.value.myRequests.firstOrNull()?.requestId
        assertNotNull(requestId)

        viewModel.cancelRequest(requestId!!)
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.myRequests.isEmpty())
    }

    @Test
    fun `loadMyRequests returns empty list`() = runTest {
        viewModel.loadMyRequests("u1")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertTrue(state.myRequests.isEmpty())
        assertEquals(0, state.pendingCount)
    }
}
