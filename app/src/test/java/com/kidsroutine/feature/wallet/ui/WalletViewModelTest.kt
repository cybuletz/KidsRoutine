package com.kidsroutine.feature.wallet.ui

import com.kidsroutine.core.model.FamilyWallet
import com.kidsroutine.core.model.SavingsGoal
import com.kidsroutine.feature.wallet.data.WalletRepository
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class WalletViewModelTest {
    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var walletRepository: WalletRepository
    private lateinit var viewModel: WalletViewModel

    private val testWallet = FamilyWallet(familyId = "fam1", isEnabled = true)

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        walletRepository = mockk(relaxed = true)
        viewModel = WalletViewModel(walletRepository)
    }

    @After
    fun tearDown() { Dispatchers.resetMain() }

    @Test
    fun `initial state is default`() {
        assertNull(viewModel.uiState.value.wallet)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `loadWallet success sets wallet`() = runTest {
        coEvery { walletRepository.getWallet("fam1") } returns testWallet
        coEvery { walletRepository.getSavingsGoals("u1") } returns emptyList()
        viewModel.loadWallet("fam1", "u1")
        advanceUntilIdle()
        assertNotNull(viewModel.uiState.value.wallet)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `loadWallet error sets error`() = runTest {
        coEvery { walletRepository.getWallet("fam1") } throws RuntimeException("Error")
        viewModel.loadWallet("fam1", "u1")
        advanceUntilIdle()
        assertNotNull(viewModel.uiState.value.error)
    }

    @Test
    fun `toggleCreateGoal toggles flag`() {
        assertFalse(viewModel.uiState.value.showCreateGoal)
        viewModel.toggleCreateGoal()
        assertTrue(viewModel.uiState.value.showCreateGoal)
        viewModel.toggleCreateGoal()
        assertFalse(viewModel.uiState.value.showCreateGoal)
    }

    @Test
    fun `enableWallet creates and saves wallet`() = runTest {
        viewModel.enableWallet("fam1")
        advanceUntilIdle()
        coVerify { walletRepository.saveWallet("fam1", any()) }
    }
}
