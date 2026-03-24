package com.kidsroutine.feature.billing

import android.app.Activity
import android.content.Context
import android.util.Log
import com.android.billingclient.api.*
import com.android.billingclient.api.BillingClient
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

// ─────────────────────────────────────────────────────────────────────────────
// Product IDs — must match exactly what you register in Google Play Console
// ─────────────────────────────────────────────────────────────────────────────
object BillingProducts {
    const val PRO_MONTHLY     = "kids_routine_pro_monthly"
    const val PREMIUM_MONTHLY = "kids_routine_premium_monthly"

    val ALL = listOf(PRO_MONTHLY, PREMIUM_MONTHLY)
}

// ─────────────────────────────────────────────────────────────────────────────
// State models
// ─────────────────────────────────────────────────────────────────────────────
data class ProductInfo(
    val productId: String,
    val title: String,
    val description: String,
    val formattedPrice: String,
    val priceAmountMicros: Long,
    val productDetails: ProductDetails
)

sealed class PurchaseState {
    object Idle : PurchaseState()
    object Pending : PurchaseState()
    data class Success(val productId: String, val planType: com.kidsroutine.core.model.PlanType) : PurchaseState()
    data class Error(val message: String) : PurchaseState()
    object Cancelled : PurchaseState()
}

// ─────────────────────────────────────────────────────────────────────────────
// BILLING MANAGER
// ─────────────────────────────────────────────────────────────────────────────
@Singleton
class BillingManager @Inject constructor(
    @ApplicationContext private val context: Context
) : PurchasesUpdatedListener {

    companion object {
        private const val TAG = "BillingManager"
    }

    // ── State ──────────────────────────────────────────────────────────────
    private val _purchaseState = MutableStateFlow<PurchaseState>(PurchaseState.Idle)
    val purchaseState: StateFlow<PurchaseState> = _purchaseState.asStateFlow()

    private val _products = MutableStateFlow<List<ProductInfo>>(emptyList())
    val products: StateFlow<List<ProductInfo>> = _products.asStateFlow()

    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    // ── BillingClient ──────────────────────────────────────────────────────
    private val billingClient: BillingClient = BillingClient.newBuilder(context)
        .setListener(this)
        .enablePendingPurchases(
            PendingPurchasesParams.newBuilder()
                .enableOneTimeProducts()
                .build()
        )
        .build()

    // ── Connection ─────────────────────────────────────────────────────────
    fun connect() {
        if (billingClient.isReady) {
            _isConnected.value = true
            return
        }
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(result: BillingResult) {
                if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                    _isConnected.value = true
                    Log.d(TAG, "Billing connected ✓")
                } else {
                    Log.e(TAG, "Billing setup failed: ${result.debugMessage}")
                    _isConnected.value = false
                }
            }
            override fun onBillingServiceDisconnected() {
                _isConnected.value = false
                Log.w(TAG, "Billing disconnected — will retry on next call")
            }
        })
    }

    // ── Query available products ───────────────────────────────────────────
    suspend fun queryProducts(): List<ProductInfo> {
        if (!billingClient.isReady) connect()

        val productList = BillingProducts.ALL.map { id ->
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(id)
                .setProductType(BillingClient.ProductType.SUBS)
                .build()
        }

        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(productList)
            .build()

        return suspendCancellableCoroutine { cont ->
            billingClient.queryProductDetailsAsync(params) { billingResult, queryResult ->
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    val infos = queryResult.productDetailsList.mapNotNull { pd ->
                        val offer = pd.subscriptionOfferDetails?.firstOrNull() ?: return@mapNotNull null
                        val pricing = offer.pricingPhases.pricingPhaseList.firstOrNull()
                            ?: return@mapNotNull null
                        ProductInfo(
                            productId         = pd.productId,
                            title             = pd.title,
                            description       = pd.description,
                            formattedPrice    = pricing.formattedPrice,
                            priceAmountMicros = pricing.priceAmountMicros,
                            productDetails    = pd
                        )
                    }
                    _products.value = infos
                    cont.resume(infos)
                } else {
                    cont.resume(emptyList())
                }
            }
        }
    }

    // ── Launch purchase flow ───────────────────────────────────────────────
    fun launchPurchase(activity: Activity, productInfo: ProductInfo) {
        val offer = productInfo.productDetails
            .subscriptionOfferDetails
            ?.firstOrNull()
            ?: run {
                _purchaseState.value = PurchaseState.Error("No offer available")
                return
            }

        val flowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(
                listOf(
                    BillingFlowParams.ProductDetailsParams.newBuilder()
                        .setProductDetails(productInfo.productDetails)
                        .setOfferToken(offer.offerToken)
                        .build()
                )
            )
            .build()

        _purchaseState.value = PurchaseState.Pending
        val result = billingClient.launchBillingFlow(activity, flowParams)

        if (result.responseCode != BillingClient.BillingResponseCode.OK) {
            _purchaseState.value = PurchaseState.Error(result.debugMessage)
            Log.e(TAG, "Launch billing flow failed: ${result.debugMessage}")
        }
    }

    // ── Purchase result callback ───────────────────────────────────────────
    override fun onPurchasesUpdated(result: BillingResult, purchases: List<Purchase>?) {
        when (result.responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                purchases?.forEach { purchase ->
                    if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                        handlePurchase(purchase)
                    }
                }
            }
            BillingClient.BillingResponseCode.USER_CANCELED -> {
                _purchaseState.value = PurchaseState.Cancelled
                Log.d(TAG, "User cancelled purchase")
            }
            else -> {
                _purchaseState.value = PurchaseState.Error(result.debugMessage)
                Log.e(TAG, "Purchase error: ${result.debugMessage}")
            }
        }
    }

    private fun handlePurchase(purchase: Purchase) {
        val productId = purchase.products.firstOrNull() ?: return
        val planType = when (productId) {
            BillingProducts.PRO_MONTHLY     -> com.kidsroutine.core.model.PlanType.PRO
            BillingProducts.PREMIUM_MONTHLY -> com.kidsroutine.core.model.PlanType.PREMIUM
            else -> com.kidsroutine.core.model.PlanType.FREE
        }

        // Acknowledge the purchase (mandatory for Google Play)
        if (!purchase.isAcknowledged) {
            val ackParams = AcknowledgePurchaseParams.newBuilder()
                .setPurchaseToken(purchase.purchaseToken)
                .build()
            billingClient.acknowledgePurchase(ackParams) { ackResult ->
                if (ackResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    _purchaseState.value = PurchaseState.Success(productId, planType)
                    Log.d(TAG, "Purchase acknowledged: $productId → $planType")
                } else {
                    _purchaseState.value = PurchaseState.Error("Acknowledgment failed: ${ackResult.debugMessage}")
                }
            }
        } else {
            _purchaseState.value = PurchaseState.Success(productId, planType)
        }
    }

    // ── Restore purchases ──────────────────────────────────────────────────
    suspend fun restorePurchases(): PurchaseState {
        if (!billingClient.isReady) connect()

        return suspendCancellableCoroutine { cont ->
            billingClient.queryPurchasesAsync(
                QueryPurchasesParams.newBuilder()
                    .setProductType(BillingClient.ProductType.SUBS)
                    .build()
            ) { result, purchases ->
                if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                    val activePurchase = purchases.firstOrNull {
                        it.purchaseState == Purchase.PurchaseState.PURCHASED
                    }
                    if (activePurchase != null) {
                        val productId = activePurchase.products.firstOrNull() ?: ""
                        val planType = when (productId) {
                            BillingProducts.PRO_MONTHLY     -> com.kidsroutine.core.model.PlanType.PRO
                            BillingProducts.PREMIUM_MONTHLY -> com.kidsroutine.core.model.PlanType.PREMIUM
                            else -> com.kidsroutine.core.model.PlanType.FREE
                        }
                        val state = PurchaseState.Success(productId, planType)
                        _purchaseState.value = state
                        Log.d(TAG, "Restored purchase: $productId → $planType")
                        cont.resume(state)
                    } else {
                        cont.resume(PurchaseState.Idle)
                    }
                } else {
                    cont.resume(PurchaseState.Error(result.debugMessage))
                }
            }
        }
    }

    fun resetState() {
        _purchaseState.value = PurchaseState.Idle
    }
}