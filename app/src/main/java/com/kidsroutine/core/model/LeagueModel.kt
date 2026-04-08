package com.kidsroutine.core.model

/**
 * Duolingo-style League System with 10 tiers.
 * Weekly cycles: top performers promote, bottom demote.
 * Drives loss aversion and competitive engagement.
 */

enum class League(
    val displayName: String,
    val emoji: String,
    val tier: Int,
    val color: Long,
    val promotionSlots: Int,   // top N promote
    val demotionSlots: Int,    // bottom N demote
    val minXpForPromotion: Int // minimum weekly XP to be eligible for promotion
) {
    BRONZE(
        displayName = "Bronze",
        emoji = "🥉",
        tier = 1,
        color = 0xFFCD7F32,
        promotionSlots = 10,
        demotionSlots = 0,  // can't demote from bottom
        minXpForPromotion = 50
    ),
    SILVER(
        displayName = "Silver",
        emoji = "🥈",
        tier = 2,
        color = 0xFFC0C0C0,
        promotionSlots = 10,
        demotionSlots = 5,
        minXpForPromotion = 100
    ),
    GOLD(
        displayName = "Gold",
        emoji = "🥇",
        tier = 3,
        color = 0xFFFFD700,
        promotionSlots = 10,
        demotionSlots = 5,
        minXpForPromotion = 150
    ),
    PLATINUM(
        displayName = "Platinum",
        emoji = "💎",
        tier = 4,
        color = 0xFFE5E4E2,
        promotionSlots = 10,
        demotionSlots = 5,
        minXpForPromotion = 200
    ),
    DIAMOND(
        displayName = "Diamond",
        emoji = "💠",
        tier = 5,
        color = 0xFFB9F2FF,
        promotionSlots = 10,
        demotionSlots = 5,
        minXpForPromotion = 300
    ),
    RUBY(
        displayName = "Ruby",
        emoji = "❤️‍🔥",
        tier = 6,
        color = 0xFFE0115F,
        promotionSlots = 10,
        demotionSlots = 5,
        minXpForPromotion = 400
    ),
    EMERALD(
        displayName = "Emerald",
        emoji = "💚",
        tier = 7,
        color = 0xFF50C878,
        promotionSlots = 10,
        demotionSlots = 5,
        minXpForPromotion = 500
    ),
    AMETHYST(
        displayName = "Amethyst",
        emoji = "💜",
        tier = 8,
        color = 0xFF9966CC,
        promotionSlots = 10,
        demotionSlots = 5,
        minXpForPromotion = 650
    ),
    OBSIDIAN(
        displayName = "Obsidian",
        emoji = "🖤",
        tier = 9,
        color = 0xFF3D3635,
        promotionSlots = 10,
        demotionSlots = 5,
        minXpForPromotion = 800
    ),
    CHAMPION(
        displayName = "Champion",
        emoji = "🏆",
        tier = 10,
        color = 0xFFFF4500,
        promotionSlots = 0,  // top league — no promotion
        demotionSlots = 5,
        minXpForPromotion = 0
    );

    val nextLeague: League?
        get() = entries.getOrNull(ordinal + 1)

    val previousLeague: League?
        get() = entries.getOrNull(ordinal - 1)

    companion object {
        fun fromTier(tier: Int): League =
            entries.firstOrNull { it.tier == tier } ?: BRONZE
    }
}

data class LeagueEntry(
    val userId: String = "",
    val displayName: String = "",
    val avatarUrl: String = "",
    val weeklyXp: Int = 0,
    val rank: Int = 0,
    val league: League = League.BRONZE,
    val ageGroup: AgeGroup = AgeGroup.EXPLORER,
    val isPromoting: Boolean = false,
    val isDemoting: Boolean = false,
    val isCurrentUser: Boolean = false
)

data class LeagueStanding(
    val league: League = League.BRONZE,
    val week: String = "",          // "2026-W15" format
    val ageGroup: AgeGroup = AgeGroup.EXPLORER,
    val entries: List<LeagueEntry> = emptyList(),
    val userRank: Int = 0,
    val promotionThresholdXp: Int = 0,
    val demotionThresholdXp: Int = 0,
    val daysRemaining: Int = 7
)
