package com.kidsroutine.feature.billing

import android.app.Activity
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kidsroutine.core.model.PlanType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class BillingUiState(
    val isLoading: Boolean = false,
    val products: List<ProductInfo> = emptyList(),
    val purchaseState: PurchaseState = PurchaseState.Idle,
    val currentPlan: PlanType = PlanType.FREE,
    val error: String? = null
)

@HiltViewModel
class BillingViewModel @Inject constructor(
    private val billingRepository: BillingRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(BillingUiState())
    val uiState: StateFlow<BillingUiState> = _uiState.asStateFlow()

    init {
        // Mirror purchase state from BillingManager into UI state
        viewModelScope.launch {
            billingRepository.billingManager.purchaseState.collect { state ->
                _uiState.update { it.copy(purchaseState = state) }
            }
        }
    }

    /** Call once when UpgradeScreen opens. */
    fun init(userId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            billingRepository.billingManager.connect()
            val products = billingRepository.loadProducts()
            _uiState.update { it.copy(isLoading = false, products = products) }
            Log.d("BillingVM", "Loaded ${products.size} products")
        }
    }

    /** Start the Play Store purchase sheet. */
    fun purchase(activity: Activity, planType: PlanType) {
        val product = _uiState.value.products.firstOrNull { info ->
            info.productId == when (planType) {
                PlanType.PRO     -> BillingProducts.PRO_MONTHLY
                PlanType.PREMIUM -> BillingProducts.PREMIUM_MONTHLY
                PlanType.FREE    -> return  // nothing to buy
            }
        } ?: run {
            // No real product loaded (e.g. debug build / not in Play Store yet)
            // Fall through to simulation so the flow still works end-to-end
            _uiState.update { it.copy(error = "Product not available. Check Play Console SKU registration.") }
            return
        }
        billingRepository.purchase(activity, product)
    }

    /**
     * Called after Play Store confirms purchase (PurchaseState.Success).
     * Saves entitlements to Firestore + updates local UI.
     */
    fun onPurchaseSuccess(userId: String, planType: PlanType) {
        viewModelScope.launch {
            try {
                billingRepository.activatePlan(userId, planType)
                _uiState.update { it.copy(currentPlan = planType) }
                Log.d("BillingVM", "Plan activated: $planType for $userId")
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Plan saved locally but sync failed: ${e.message}") }
                Log.e("BillingVM", "activatePlan error", e)
            }
        }
    }

    /** Restore an existing subscription (e.g. user reinstalled app). */
    fun restorePurchases(userId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val result = billingRepository.restore()
            if (result is PurchaseState.Success) {
                billingRepository.activatePlan(userId, result.planType)
                _uiState.update { it.copy(isLoading = false, currentPlan = result.planType) }
                Log.d("BillingVM", "Restored: ${result.planType}")
            } else {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun clearError() = _uiState.update { it.copy(error = null) }
    fun resetPurchaseState() = billingRepository.billingManager.resetState()
}