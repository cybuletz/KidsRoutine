package com.kidsroutine.core.model

import org.junit.Assert.*
import org.junit.Test

class UserModelTest {

    // ── UserModel defaults ──────────────────────────────────────────

    @Test
    fun `default role is CHILD`() {
        val user = UserModel()
        assertEquals(Role.CHILD, user.role)
    }

    @Test
    fun `default xp is 0`() {
        val user = UserModel()
        assertEquals(0, user.xp)
    }

    @Test
    fun `default level is 1`() {
        val user = UserModel()
        assertEquals(1, user.level)
    }

    @Test
    fun `default streak is 0`() {
        val user = UserModel()
        assertEquals(0, user.streak)
    }

    @Test
    fun `default streakShieldActive is false`() {
        val user = UserModel()
        assertFalse(user.streakShieldActive)
    }

    @Test
    fun `default isAdmin is false`() {
        val user = UserModel()
        assertFalse(user.isAdmin)
    }

    @Test
    fun `default isOnline is false`() {
        val user = UserModel()
        assertFalse(user.isOnline)
    }

    @Test
    fun `default age is 8`() {
        val user = UserModel()
        assertEquals(8, user.age)
    }

    @Test
    fun `default ageGroup is from age 8 (EXPLORER)`() {
        val user = UserModel()
        assertEquals(AgeGroup.fromAge(8), user.ageGroup)
    }

    @Test
    fun `default league is BRONZE`() {
        val user = UserModel()
        assertEquals(League.BRONZE, user.league)
    }

    @Test
    fun `default weeklyXp is 0`() {
        val user = UserModel()
        assertEquals(0, user.weeklyXp)
    }

    @Test
    fun `default darkModeEnabled is false`() {
        val user = UserModel()
        assertFalse(user.darkModeEnabled)
    }

    @Test
    fun `default comebackStreakSaved is 0`() {
        val user = UserModel()
        assertEquals(0, user.comebackStreakSaved)
    }

    @Test
    fun `default activeChallengeIds is empty`() {
        val user = UserModel()
        assertTrue(user.activeChallengeIds.isEmpty())
    }

    @Test
    fun `default badges is empty`() {
        val user = UserModel()
        assertTrue(user.badges.isEmpty())
    }

    // ── UserModel with data ─────────────────────────────────────────

    @Test
    fun `user stores all fields`() {
        val user = UserModel(
            userId = "u1",
            role = Role.PARENT,
            familyId = "f1",
            displayName = "John",
            xp = 500,
            level = 10,
            streak = 7,
            age = 35,
            league = League.GOLD,
            weeklyXp = 200
        )
        assertEquals("u1", user.userId)
        assertEquals(Role.PARENT, user.role)
        assertEquals("f1", user.familyId)
        assertEquals("John", user.displayName)
        assertEquals(500, user.xp)
        assertEquals(10, user.level)
        assertEquals(7, user.streak)
        assertEquals(35, user.age)
        assertEquals(League.GOLD, user.league)
        assertEquals(200, user.weeklyXp)
    }

    // ── UserPreferences defaults ────────────────────────────────────

    @Test
    fun `default preferences allowedTaskTypes has all types`() {
        val prefs = UserPreferences()
        assertEquals(TaskType.entries.size, prefs.allowedTaskTypes.size)
    }

    @Test
    fun `default preferences maxDifficulty is HARD`() {
        val prefs = UserPreferences()
        assertEquals(DifficultyLevel.HARD, prefs.maxDifficulty)
    }

    @Test
    fun `default preferences screenTimeLimitMin is 60`() {
        val prefs = UserPreferences()
        assertEquals(60, prefs.screenTimeLimitMin)
    }

    // ── AuthState sealed class ──────────────────────────────────────

    @Test
    fun `AuthState Unauthenticated is singleton`() {
        val a = AuthState.Unauthenticated
        val b = AuthState.Unauthenticated
        assertSame(a, b)
    }

    @Test
    fun `AuthState Loading is singleton`() {
        val a = AuthState.Loading
        val b = AuthState.Loading
        assertSame(a, b)
    }

    @Test
    fun `AuthState Authenticated stores user`() {
        val user = UserModel(userId = "u1", displayName = "Alice")
        val state = AuthState.Authenticated(user)
        assertEquals("u1", state.user.userId)
    }

    @Test
    fun `AuthState Error stores message`() {
        val state = AuthState.Error("Network error")
        assertEquals("Network error", state.message)
    }
}
