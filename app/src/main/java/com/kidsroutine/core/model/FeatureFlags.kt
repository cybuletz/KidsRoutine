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
    val weeklyPlannerEnabled: Boolean  = false,

    // ── NEW: Competitive feature flags ──────────────────────────────
    // P0 Features
    val ageAdaptiveGamesEnabled: Boolean  = true,   // Age-group-scaled game difficulty
    val companionPetEnabled: Boolean      = true,   // Pet companion system
    val mascotRooEnabled: Boolean         = true,   // Roo mascot throughout app

    // P1 Features
    val leagueSystemEnabled: Boolean      = true,   // 10-tier league competition
    val smartNotificationsEnabled: Boolean = true,   // Personality-driven notifications
    val darkModeEnabled: Boolean          = true,    // Dark theme + teen UI
    val hapticFeedbackEnabled: Boolean    = true,    // Vibration feedback on events

    // P2 Features
    val bossBattlesEnabled: Boolean       = true,    // Weekly family boss battles
    val dailySpinWheelEnabled: Boolean    = true,    // Daily reward spin
    val timedEventsEnabled: Boolean       = true,    // Seasonal limited-time events
    val comebackSystemEnabled: Boolean    = true,    // Roo-covery for lapsed users

    // P3 Features
    val skillTreesEnabled: Boolean        = false,   // Skill tree progression
    val familyRitualsEnabled: Boolean     = false,   // Gratitude circle, family meetings
    val financialLiteracyEnabled: Boolean = false,   // Family wallet, savings goals
    val friendSystemEnabled: Boolean      = false,   // Beyond-family friend connections

    // P4 Features
    val i18nEnabled: Boolean              = false    // Multi-language support
)