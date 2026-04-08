package com.kidsroutine.core.engine.boss_engine

import com.kidsroutine.core.model.BossModel
import com.kidsroutine.core.model.BossType
import com.kidsroutine.core.model.DifficultyLevel
import com.kidsroutine.core.model.LootBoxRarity
import com.kidsroutine.core.model.Season
import com.kidsroutine.core.model.TaskModel
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Boss Engine — manages weekly family boss battles.
 * Each completed task by any family member deals damage.
 */
@Singleton
class BossEngine @Inject constructor() {

    /**
     * Generate a weekly boss for a family.
     * Boss type is randomly selected, themed to current season if applicable.
     */
    fun generateWeeklyBoss(
        familyId: String,
        familySize: Int,
        week: String,
        season: Season = Season.NONE,
        difficulty: DifficultyLevel = DifficultyLevel.MEDIUM
    ): BossModel {
        val bossType = selectBossType(season)
        val hp = BossModel.hpForFamily(bossType, familySize, difficulty)

        return BossModel(
            bossId = "${familyId}_${week}",
            familyId = familyId,
            type = bossType,
            week = week,
            season = season,
            maxHp = hp,
            currentHp = hp,
            victoryXpBonus = (hp * 0.5f).toInt().coerceIn(50, 500),
            victoryLootRarity = when (difficulty) {
                DifficultyLevel.EASY -> LootBoxRarity.COMMON
                DifficultyLevel.MEDIUM -> LootBoxRarity.RARE
                DifficultyLevel.HARD -> LootBoxRarity.EPIC
            },
            defeatXpPenalty = 20,
            startedAt = System.currentTimeMillis()
        )
    }

    /**
     * Apply damage from a completed task.
     */
    fun applyDamage(
        boss: BossModel,
        task: TaskModel,
        userId: String
    ): BossModel {
        if (!boss.isAlive) return boss

        val damage = boss.damageForTask(task)
        val newHp = (boss.currentHp - damage).coerceAtLeast(0)
        val updatedDamageLog = boss.damageLog.toMutableMap().apply {
            this[userId] = (this[userId] ?: 0) + damage
        }

        return boss.copy(
            currentHp = newHp,
            totalDamage = boss.totalDamage + damage,
            damageLog = updatedDamageLog,
            isDefeated = newHp <= 0
        )
    }

    /**
     * Check if boss deadline has passed (Sunday midnight).
     */
    fun checkExpiry(boss: BossModel, currentTime: Long): BossModel {
        if (boss.isDefeated) return boss
        return if (currentTime >= boss.deadline) {
            boss.copy(isExpired = true)
        } else {
            boss
        }
    }

    /**
     * Get XP reward for each family member when boss is defeated.
     */
    fun victoryRewardPerMember(boss: BossModel, familySize: Int): Int {
        return (boss.victoryXpBonus / familySize.coerceAtLeast(1)).coerceAtLeast(25)
    }

    /**
     * Get XP penalty for each family member when boss wins.
     */
    fun defeatPenalty(boss: BossModel): Int = boss.defeatXpPenalty

    /**
     * Get MVP (most damage dealt) userId.
     */
    fun getMvp(boss: BossModel): String? {
        return boss.damageLog.maxByOrNull { it.value }?.key
    }

    /**
     * Select boss type, preferring seasonal bosses when applicable.
     */
    private fun selectBossType(season: Season): BossType {
        val seasonalPreference = when (season) {
            Season.HALLOWEEN -> BossType.SCREEN_SPECTER
            Season.WINTER, Season.CHRISTMAS -> BossType.BEDTIME_BASILISK
            Season.SUMMER -> BossType.DISTRACTION_DRAGON
            else -> null
        }
        return seasonalPreference ?: BossType.entries.random()
    }
}
