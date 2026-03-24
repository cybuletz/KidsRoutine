package com.kidsroutine.core.model

enum class LootBoxRarity { COMMON, RARE, EPIC, LEGENDARY }

enum class LootBoxRewardType { XP_BOOST, BADGE, AVATAR_ITEM, STREAK_SHIELD, MYSTERY }

data class LootBoxReward(
    val rewardId: String = java.util.UUID.randomUUID().toString(),
    val type: LootBoxRewardType = LootBoxRewardType.XP_BOOST,
    val rarity: LootBoxRarity = LootBoxRarity.COMMON,
    val title: String = "",
    val description: String = "",
    val emoji: String = "🎁",
    val xpValue: Int = 0             // for XP_BOOST type
)

data class LootBox(
    val boxId: String = java.util.UUID.randomUUID().toString(),
    val earnedFor: String = "",      // e.g. "5-task streak", "level up"
    val reward: LootBoxReward? = null,
    val isOpened: Boolean = false,
    val earnedAt: Long = System.currentTimeMillis()
)