package com.kidsroutine.core.model

import org.junit.Assert.*
import org.junit.Test

class AchievementModelTest {

    // ── AchievementType enum ────────────────────────────────────────

    @Test
    fun `AchievementType has 10 entries`() {
        assertEquals(10, AchievementType.entries.size)
    }

    @Test
    fun `includes task completion achievements`() {
        val names = AchievementType.entries.map { it.name }
        assertTrue(names.contains("TASKS_COMPLETED_10"))
        assertTrue(names.contains("TASKS_COMPLETED_50"))
    }

    @Test
    fun `includes XP achievements`() {
        val names = AchievementType.entries.map { it.name }
        assertTrue(names.contains("XP_EARNED_100"))
        assertTrue(names.contains("XP_EARNED_500"))
    }

    @Test
    fun `includes streak achievements`() {
        val names = AchievementType.entries.map { it.name }
        assertTrue(names.contains("STREAK_7_DAYS"))
        assertTrue(names.contains("STREAK_30_DAYS"))
    }

    @Test
    fun `includes challenge achievements`() {
        val names = AchievementType.entries.map { it.name }
        assertTrue(names.contains("FIRST_CHALLENGE"))
        assertTrue(names.contains("CHALLENGE_MASTER"))
    }

    @Test
    fun `includes social achievements`() {
        val names = AchievementType.entries.map { it.name }
        assertTrue(names.contains("COMMUNITY_CONTRIBUTOR"))
        assertTrue(names.contains("FAMILY_HERO"))
    }

    // ── Badge defaults ──────────────────────────────────────────────

    @Test
    fun `default badge is not unlocked`() {
        val badge = Badge()
        assertFalse(badge.isUnlocked)
    }

    @Test
    fun `default badge type is TASKS_COMPLETED_10`() {
        val badge = Badge()
        assertEquals(AchievementType.TASKS_COMPLETED_10, badge.type)
    }

    @Test
    fun `default badge unlockedAt is 0`() {
        val badge = Badge()
        assertEquals(0L, badge.unlockedAt)
    }

    @Test
    fun `badge stores all fields`() {
        val badge = Badge(
            id = "b1",
            type = AchievementType.STREAK_30_DAYS,
            title = "30 Day Streak",
            description = "Keep going for 30 days!",
            icon = "🔥",
            unlockedAt = 12345L,
            isUnlocked = true
        )
        assertEquals("b1", badge.id)
        assertEquals(AchievementType.STREAK_30_DAYS, badge.type)
        assertEquals("30 Day Streak", badge.title)
        assertTrue(badge.isUnlocked)
    }

    // ── UserAchievements defaults ───────────────────────────────────

    @Test
    fun `default achievements has empty badges`() {
        val achievements = UserAchievements()
        assertTrue(achievements.badges.isEmpty())
    }

    @Test
    fun `default totalBadgesUnlocked is 0`() {
        val achievements = UserAchievements()
        assertEquals(0, achievements.totalBadgesUnlocked)
    }

    @Test
    fun `achievements stores badges list`() {
        val badges = listOf(
            Badge(id = "b1", isUnlocked = true),
            Badge(id = "b2", isUnlocked = false)
        )
        val achievements = UserAchievements(
            userId = "u1",
            badges = badges,
            totalBadgesUnlocked = 1
        )
        assertEquals(2, achievements.badges.size)
        assertEquals(1, achievements.totalBadgesUnlocked)
    }
}
