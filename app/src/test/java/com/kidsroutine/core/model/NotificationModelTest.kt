package com.kidsroutine.core.model

import org.junit.Assert.*
import org.junit.Test

class NotificationModelTest {

    // ── NotificationType enum ───────────────────────────────────────

    @Test
    fun `NotificationType has all expected entries`() {
        // 6 existing + 17 new = 23
        assertTrue(NotificationType.entries.size >= 23)
    }

    @Test
    fun `includes original notification types`() {
        val names = NotificationType.entries.map { it.name }
        assertTrue(names.contains("TASK_REMINDER"))
        assertTrue(names.contains("ACHIEVEMENT_UNLOCKED"))
        assertTrue(names.contains("PARENT_APPROVAL_NEEDED"))
        assertTrue(names.contains("CHALLENGE_STARTED"))
        assertTrue(names.contains("LEADERBOARD_CHANGED"))
        assertTrue(names.contains("FAMILY_MESSAGE"))
    }

    @Test
    fun `includes Roo-powered notification types`() {
        val names = NotificationType.entries.map { it.name }
        assertTrue(names.contains("STREAK_AT_RISK"))
        assertTrue(names.contains("COMEBACK_NUDGE"))
        assertTrue(names.contains("PET_HUNGRY"))
        assertTrue(names.contains("PET_EVOLVED"))
        assertTrue(names.contains("LEAGUE_PROMOTION"))
        assertTrue(names.contains("LEAGUE_DEMOTION"))
        assertTrue(names.contains("BOSS_APPEARED"))
        assertTrue(names.contains("BOSS_DEFEATED"))
    }

    @Test
    fun `includes special event types`() {
        val names = NotificationType.entries.map { it.name }
        assertTrue(names.contains("EVENT_STARTED"))
        assertTrue(names.contains("EVENT_ENDING_SOON"))
        assertTrue(names.contains("DAILY_SPIN_AVAILABLE"))
        assertTrue(names.contains("COMEBACK_CHALLENGE"))
        assertTrue(names.contains("RANDOM_ENCOURAGEMENT"))
        assertTrue(names.contains("MILESTONE_REACHED"))
        assertTrue(names.contains("BIRTHDAY_CELEBRATION"))
        assertTrue(names.contains("AGE_UP_CEREMONY"))
    }

    // ── AppNotification defaults ────────────────────────────────────

    @Test
    fun `default type is TASK_REMINDER`() {
        val notif = AppNotification()
        assertEquals(NotificationType.TASK_REMINDER, notif.type)
    }

    @Test
    fun `default isRead is false`() {
        val notif = AppNotification()
        assertFalse(notif.isRead)
    }

    @Test
    fun `default priority is MEDIUM`() {
        val notif = AppNotification()
        assertEquals("MEDIUM", notif.priority)
    }

    @Test
    fun `default ageGroup is null`() {
        val notif = AppNotification()
        assertNull(notif.ageGroup)
    }

    @Test
    fun `default rooExpression is null`() {
        val notif = AppNotification()
        assertNull(notif.rooExpression)
    }

    @Test
    fun `notification stores all fields`() {
        val notif = AppNotification(
            id = "n1",
            userId = "u1",
            type = NotificationType.PET_HUNGRY,
            title = "Your pet is hungry!",
            body = "Complete a task to feed them.",
            ageGroup = AgeGroup.SPROUT,
            rooExpression = "🦘😢",
            priority = "HIGH"
        )
        assertEquals("n1", notif.id)
        assertEquals(NotificationType.PET_HUNGRY, notif.type)
        assertEquals(AgeGroup.SPROUT, notif.ageGroup)
        assertEquals("HIGH", notif.priority)
    }
}
