package com.kidsroutine.core.model

/**
 * All feature flags in one place.
 * Values come from Firestore `feature_flags/global` at startup.
 * Default values = safe production fallback.
 */
data class FeatureFlags(
    // Core — always on
    val dailyTasksEnabled: Boolean   = true,
    val challengesEnabled: Boolean   = true,
    val communityEnabled: Boolean    = true,
    val aiGenerationEnabled: Boolean = true,

    // Batch 1
    val worldMapEnabled: Boolean     = true,

    // Batch 2
    val lootBoxEnabled: Boolean      = true,
    val momentsEnabled: Boolean      = true,

    // Batch 5
    val seasonalThemesEnabled: Boolean = true,
    val avatarShopEnabled: Boolean     = true,
    val contentPacksEnabled: Boolean   = true,

    // Batch 7 / 8
    val storyArcsEnabled: Boolean      = true,

    // Future
    val weeklyPlannerEnabled: Boolean  = false
)