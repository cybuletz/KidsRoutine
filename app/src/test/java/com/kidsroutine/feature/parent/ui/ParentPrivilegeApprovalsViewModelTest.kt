package com.kidsroutine.feature.parent.ui

import com.google.firebase.firestore.FirebaseFirestore
import com.kidsroutine.core.model.PrivilegeRequest
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ParentPrivilegeApprovalsViewModelTest {
    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var firestore: FirebaseFirestore
    private lateinit var viewModel: ParentPrivilegeApprovalsViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        firestore = mockk(relaxed = true)
        viewModel = ParentPrivilegeApprovalsViewModel(firestore)
    }

    @After
    fun tearDown() { Dispatchers.resetMain() }

    @Test
    fun `initial state is default`() {
        assertFalse(viewModel.uiState.value.isLoading)
        assertTrue(viewModel.uiState.value.requests.isEmpty())
        assertNull(viewModel.uiState.value.error)
    }

    @Test
    fun `uiState exposes stateflow`() {
        assertNotNull(viewModel.uiState)
    }
}
