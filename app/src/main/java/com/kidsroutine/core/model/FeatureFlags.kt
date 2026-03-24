package com.kidsroutine.core.model

/**
 * All feature flags in one place.
 * Values come from Firestore `feature_flags` collection at startup.
 * Default values = safe production fallback (everything that exists today stays on).
 */
data class FeatureFlags(
    // Existing features — always true
    val dailyTasksEnabled: Boolean = true,
    val challengesEnabled: Boolean = true,
    val communityEnabled: Boolean = true,
    val aiGenerationEnabled: Boolean = true,

    // Batch 1
    val worldMapEnabled: Boolean = true,

    // Batch 2
    val lootBoxEnabled: Boolean = true,
    val momentsEnabled: Boolean = true,

    // Future — off by default until built
    val seasonalThemesEnabled: Boolean = false,
    val avatarShopEnabled: Boolean = false,
    val contentPacksEnabled: Boolean = false,
    val weeklyPlannerEnabled: Boolean = false
)