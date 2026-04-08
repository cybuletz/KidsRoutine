package com.kidsroutine.core.model

/**
 * Limited-Time Events & Seasonal Challenges.
 * 3-7 day events tied to holidays/seasons with exclusive rewards.
 * FOMO + seasonal monetization driver.
 */

enum class EventType(val displayName: String, val emoji: String) {
    SEASONAL("Seasonal", "🌍"),
    HOLIDAY("Holiday", "🎉"),
    COMMUNITY("Community", "👥"),
    SPECIAL("Special", "⭐")
}

data class TimedEvent(
    val eventId: String = "",
    val title: String = "",
    val description: String = "",
    val type: EventType = EventType.SEASONAL,
    val season: Season = Season.NONE,
    val emoji: String = "🎯",

    // Timing
    val startDate: String = "",       // "2026-10-25"
    val endDate: String = "",         // "2026-10-31"
    val startTimestamp: Long = 0L,
    val endTimestamp: Long = 0L,

    // Goals
    val targetTaskCount: Int = 20,
    val targetXp: Int = 0,            // optional XP goal
    val requiredCategories: List<TaskCategory> = emptyList(),

    // Rewards
    val rewardAvatarItemIds: List<String> = emptyList(),   // exclusive items
    val rewardXp: Int = 200,
    val rewardBadgeId: String = "",
    val eventTokenReward: Int = 0,

    // Event currency
    val eventTokenName: String = "🎃 Tokens",
    val tokenShopItems: List<EventShopItem> = emptyList(),

    // Participation
    val isActive: Boolean = false,
    val isGlobal: Boolean = true,     // all users vs family-only

    // Leaderboard
    val hasLeaderboard: Boolean = true,
    val leaderboardEntries: List<LeaderboardEntry> = emptyList()
)

data class EventShopItem(
    val itemId: String = "",
    val name: String = "",
    val emoji: String = "",
    val tokenCost: Int = 0,
    val type: String = "avatar_item"  // avatar_item, xp_boost, pet_accessory
)

data class EventProgress(
    val eventId: String = "",
    val userId: String = "",
    val tasksCompleted: Int = 0,
    val xpEarned: Int = 0,
    val tokensEarned: Int = 0,
    val tokensSpent: Int = 0,
    val rewardsClaimed: List<String> = emptyList(),
    val isComplete: Boolean = false,
    val lastActivityAt: Long = 0L
) {
    val tokensAvailable: Int get() = tokensEarned - tokensSpent
}

/** Pre-defined seasonal events */
object SeasonalEvents {
    val HALLOWEEN_HUNT = TimedEvent(
        eventId = "halloween_hunt_2026",
        title = "Roo's Halloween Hunt",
        description = "Complete 20 spooky-themed tasks to earn the exclusive zombie avatar set!",
        type = EventType.HOLIDAY,
        season = Season.HALLOWEEN,
        emoji = "🎃",
        startDate = "2026-10-25",
        endDate = "2026-10-31",
        targetTaskCount = 20,
        rewardXp = 500,
        eventTokenName = "🎃 Pumpkin Coins"
    )

    val WINTER_WONDERLAND = TimedEvent(
        eventId = "winter_wonderland_2026",
        title = "Winter Wonderland Week",
        description = "Family co-op: build a virtual snowman! Each task adds a piece.",
        type = EventType.HOLIDAY,
        season = Season.CHRISTMAS,
        emoji = "⛄",
        startDate = "2026-12-20",
        endDate = "2026-12-26",
        targetTaskCount = 30,
        rewardXp = 750,
        eventTokenName = "❄️ Snowflakes"
    )

    val SUMMER_SPRINT = TimedEvent(
        eventId = "summer_sprint_2026",
        title = "Summer Sprint",
        description = "Speed-run challenge! Most tasks completed wins!",
        type = EventType.SEASONAL,
        season = Season.SUMMER,
        emoji = "🏃",
        startDate = "2026-06-01",
        endDate = "2026-06-14",
        targetTaskCount = 50,
        rewardXp = 1000,
        eventTokenName = "☀️ Sun Tokens"
    )
}
