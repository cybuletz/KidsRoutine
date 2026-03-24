package com.kidsroutine.feature.billing

import android.app.Activity
import com.kidsroutine.core.model.EntitlementsRepository
import com.kidsroutine.core.model.PlanType
import com.kidsroutine.core.model.UserEntitlements
import com.kidsroutine.core.model.defaultEntitlements
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BillingRepository @Inject constructor(
    val billingManager: BillingManager,                      // `val` — exposed for VM
    private val entitlementsRepository: EntitlementsRepository
) {
    /** Load product details from Play Store. */
    suspend fun loadProducts(): List<ProductInfo> =
        billingManager.queryProducts()

    /** Launch the Play Store purchase sheet. */
    fun purchase(activity: Activity, productInfo: ProductInfo) =
        billingManager.launchPurchase(activity, productInfo)

    /** Restore existing subscriptions for this device. */
    suspend fun restore(): PurchaseState =
        billingManager.restorePurchases()

    /**
     * Called after a successful purchase.
     * Persists the new plan level to Firestore so the app reads it
     * immediately on next launch without waiting for a backend webhook.
     */
    suspend fun activatePlan(userId: String, planType: PlanType) {
        val entitlements = planType.defaultEntitlements(userId)
        entitlementsRepository.saveEntitlements(entitlements)
        entitlementsRepository.clearCache(userId)
    }
}