package com.kidsroutine.feature.billing

import android.app.Activity
import com.kidsroutine.core.model.EntitlementsRepository
import com.kidsroutine.core.model.FamilySubscriptionInfo
import com.kidsroutine.core.model.PlanType
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
     * Called after a successful purchase or restore.
     *
     * Writes entitlements to **both**:
     *  1. `user_entitlements/{userId}` — for the purchasing parent
     *  2. `families/{familyId}/subscription/current` — so all family members inherit
     *
     * If [familyId] is blank, only the per-user doc is written (safe fallback).
     */
    suspend fun activatePlan(userId: String, familyId: String, planType: PlanType) {
        // 1. Per-user entitlements (purchasing parent)
        val entitlements = planType.defaultEntitlements(userId)
        entitlementsRepository.saveEntitlements(entitlements)

        // 2. Family-level subscription (all members inherit)
        if (familyId.isNotBlank()) {
            entitlementsRepository.saveFamilySubscription(
                familyId        = familyId,
                billingParentId = userId,
                planType        = planType
            )
        }

        entitlementsRepository.clearCache(userId)
    }

    /**
     * Check if this family already has an active subscription.
     * Returns info about the billing parent + plan, or null if none.
     */
    suspend fun getFamilySubscription(familyId: String): FamilySubscriptionInfo? =
        entitlementsRepository.getFamilySubscriptionInfo(familyId)
}