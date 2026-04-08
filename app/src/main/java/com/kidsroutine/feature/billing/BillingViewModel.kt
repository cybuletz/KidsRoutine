package com.kidsroutine.feature.billing

import android.app.Activity
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kidsroutine.core.model.FamilySubscriptionInfo
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
    /** Non-null when the family already has an active subscription. */
    val existingFamilySubscription: FamilySubscriptionInfo? = null,
    /** Display name of the parent who owns the subscription. */
    val billingParentName: String = "",
    val error: String? = null
)

@HiltViewModel
class BillingViewModel @Inject constructor(
    private val billingRepository: BillingRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(BillingUiState())
    val uiState: StateFlow<BillingUiState> = _uiState.asStateFlow()

    /** Cached familyId for the active session (set in [init]). */
    private var familyId: String = ""

    init {
        // Mirror purchase state from BillingManager into UI state
        viewModelScope.launch {
            billingRepository.billingManager.purchaseState.collect { state ->
                _uiState.update { it.copy(purchaseState = state) }
            }
        }
    }

    /** Call once when UpgradeScreen opens. */
    fun init(userId: String, familyId: String) {
        this.familyId = familyId
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            // 1. Check if the family already has a subscription
            if (familyId.isNotBlank()) {
                val existing = billingRepository.getFamilySubscription(familyId)
                if (existing != null && existing.planType != PlanType.FREE) {
                    // Look up billing parent's display name
                    val parentName = loadParentName(existing.billingParentId)
                    _uiState.update {
                        it.copy(
                            existingFamilySubscription = existing,
                            currentPlan                = existing.planType,
                            billingParentName          = parentName
                        )
                    }
                    Log.d("BillingVM", "Family already subscribed: ${existing.planType} by ${existing.billingParentId}")
                }
            }

            // 2. Connect to Play Billing and load products
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
            _uiState.update { it.copy(error = "Product not available. Check Play Console SKU registration.") }
            return
        }
        billingRepository.purchase(activity, product)
    }

    /**
     * Called after Play Store confirms purchase (PurchaseState.Success).
     * Saves entitlements to Firestore (both user-level AND family-level)
     * + updates local UI.
     */
    fun onPurchaseSuccess(userId: String, planType: PlanType) {
        viewModelScope.launch {
            try {
                billingRepository.activatePlan(userId, familyId, planType)
                _uiState.update {
                    it.copy(
                        currentPlan = planType,
                        existingFamilySubscription = FamilySubscriptionInfo(
                            planType        = planType,
                            billingParentId = userId,
                            updatedAt       = System.currentTimeMillis()
                        )
                    )
                }
                Log.d("BillingVM", "Plan activated: $planType for $userId (family=$familyId)")
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
                billingRepository.activatePlan(userId, familyId, result.planType)
                _uiState.update { it.copy(isLoading = false, currentPlan = result.planType) }
                Log.d("BillingVM", "Restored: ${result.planType}")
            } else {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun clearError() = _uiState.update { it.copy(error = null) }
    fun resetPurchaseState() = billingRepository.billingManager.resetState()

    // ── Helpers ────────────────────────────────────────────────────────────

    private suspend fun loadParentName(parentId: String): String {
        return try {
            val doc = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                .collection("users")
                .document(parentId)
                .get()
                .let { kotlinx.coroutines.tasks.await(it) }
            doc.data?.get("displayName") as? String ?: "a parent"
        } catch (_: Exception) {
            "a parent"
        }
    }

    private suspend fun <T> await(task: com.google.android.gms.tasks.Task<T>): T {
        return kotlinx.coroutines.tasks.await(task)
    }
}