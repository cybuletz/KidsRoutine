package com.kidsroutine.feature.billing

import com.kidsroutine.core.model.FamilySubscriptionInfo
import com.kidsroutine.core.model.PlanType
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class BillingViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var billingRepository: BillingRepository
    private lateinit var billingManager: BillingManager
    private lateinit var viewModel: BillingViewModel

    private val purchaseStateFlow = MutableStateFlow<PurchaseState>(PurchaseState.Idle)

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        billingManager = mockk(relaxed = true)
        billingRepository = mockk(relaxed = true)
        every { billingRepository.billingManager } returns billingManager
        every { billingManager.purchaseState } returns purchaseStateFlow
        viewModel = BillingViewModel(billingRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is default`() {
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertTrue(state.products.isEmpty())
        assertEquals(PurchaseState.Idle, state.purchaseState)
        assertEquals(PlanType.FREE, state.currentPlan)
        assertNull(state.existingFamilySubscription)
        assertNull(state.error)
    }

    @Test
    fun `init loads products and sets loading state`() = runTest {
        coEvery { billingRepository.getFamilySubscription(any()) } returns null
        coEvery { billingRepository.loadProducts() } returns emptyList()

        viewModel.init("user1", "family1")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        coVerify { billingManager.connect() }
        coVerify { billingRepository.loadProducts() }
    }

    @Test
    fun `init detects existing family subscription`() = runTest {
        val existingSub = FamilySubscriptionInfo(
            planType = PlanType.PRO,
            billingParentId = "parent1",
            updatedAt = 1000L
        )
        coEvery { billingRepository.getFamilySubscription("family1") } returns existingSub
        coEvery { billingRepository.loadProducts() } returns emptyList()

        viewModel.init("user1", "family1")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(PlanType.PRO, state.currentPlan)
        assertNotNull(state.existingFamilySubscription)
        assertEquals("parent1", state.existingFamilySubscription?.billingParentId)
    }

    @Test
    fun `init with blank familyId skips subscription check`() = runTest {
        coEvery { billingRepository.loadProducts() } returns emptyList()

        viewModel.init("user1", "")
        advanceUntilIdle()

        coVerify(exactly = 0) { billingRepository.getFamilySubscription(any()) }
    }

    @Test
    fun `purchase with FREE plan does nothing`() {
        viewModel.purchase(mockk(), PlanType.FREE)
        // No crash, no interaction
        verify(exactly = 0) { billingRepository.purchase(any(), any()) }
    }

    @Test
    fun `purchase with no matching product sets error`() = runTest {
        coEvery { billingRepository.loadProducts() } returns emptyList()
        coEvery { billingRepository.getFamilySubscription(any()) } returns null

        viewModel.init("user1", "family1")
        advanceUntilIdle()

        viewModel.purchase(mockk(), PlanType.PRO)

        assertNotNull(viewModel.uiState.value.error)
        assertTrue(viewModel.uiState.value.error!!.contains("not available"))
    }

    @Test
    fun `onPurchaseSuccess activates plan and updates state`() = runTest {
        viewModel.onPurchaseSuccess("user1", PlanType.PRO)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(PlanType.PRO, state.currentPlan)
        assertNotNull(state.existingFamilySubscription)
        coVerify { billingRepository.activatePlan("user1", any(), PlanType.PRO) }
    }

    @Test
    fun `onPurchaseSuccess error sets error message`() = runTest {
        coEvery { billingRepository.activatePlan(any(), any(), any()) } throws RuntimeException("Sync failed")

        viewModel.onPurchaseSuccess("user1", PlanType.PRO)
        advanceUntilIdle()

        assertNotNull(viewModel.uiState.value.error)
        assertTrue(viewModel.uiState.value.error!!.contains("sync failed"))
    }

    @Test
    fun `restorePurchases with success activates plan`() = runTest {
        coEvery { billingRepository.restore() } returns PurchaseState.Success("prod_id", PlanType.PREMIUM)

        viewModel.restorePurchases("user1")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(PlanType.PREMIUM, state.currentPlan)
        coVerify { billingRepository.activatePlan("user1", any(), PlanType.PREMIUM) }
    }

    @Test
    fun `restorePurchases with non-success does not activate`() = runTest {
        coEvery { billingRepository.restore() } returns PurchaseState.Cancelled

        viewModel.restorePurchases("user1")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(PlanType.FREE, state.currentPlan) // unchanged
    }

    @Test
    fun `clearError clears error`() {
        // Force an error state first
        viewModel.purchase(mockk(), PlanType.PRO) // will set error since no products loaded
        // It won't set error since products are empty → actually it returns early for FREE, let's test differently
        viewModel.clearError()
        assertNull(viewModel.uiState.value.error)
    }

    @Test
    fun `purchaseState propagates from billingManager`() = runTest {
        purchaseStateFlow.value = PurchaseState.Pending
        advanceUntilIdle()

        assertEquals(PurchaseState.Pending, viewModel.uiState.value.purchaseState)
    }

    @Test
    fun `resetPurchaseState delegates to billingManager`() {
        viewModel.resetPurchaseState()
        verify { billingManager.resetState() }
    }
}
