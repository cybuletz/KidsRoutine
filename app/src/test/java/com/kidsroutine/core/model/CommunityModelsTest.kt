package com.kidsroutine.core.model

import org.junit.Assert.*
import org.junit.Test

class CommunityModelsTest {

    // ── ContentStatus enum ──────────────────────────────────────────

    @Test
    fun `ContentStatus has 4 entries`() {
        assertEquals(4, ContentStatus.entries.size)
    }

    @Test
    fun `ContentStatus includes PENDING APPROVED REJECTED FLAGGED`() {
        val names = ContentStatus.entries.map { it.name }
        assertTrue(names.contains("PENDING"))
        assertTrue(names.contains("APPROVED"))
        assertTrue(names.contains("REJECTED"))
        assertTrue(names.contains("FLAGGED"))
    }

    // ── ReportReason enum ───────────────────────────────────────────

    @Test
    fun `ReportReason has 5 entries`() {
        assertEquals(5, ReportReason.entries.size)
    }

    // ── SharedTask defaults ─────────────────────────────────────────

    @Test
    fun `SharedTask default status is PENDING`() {
        val task = SharedTask()
        assertEquals(ContentStatus.PENDING, task.status)
    }

    @Test
    fun `SharedTask default averageRating is 0`() {
        val task = SharedTask()
        assertEquals(0f, task.averageRating, 0.01f)
    }

    @Test
    fun `SharedTask default usageCount is 0`() {
        val task = SharedTask()
        assertEquals(0, task.usageCount)
    }

    @Test
    fun `SharedTask default ratingBreakdown is empty`() {
        val task = SharedTask()
        assertTrue(task.ratingBreakdown.isEmpty())
    }

    // ── SharedChallenge defaults ────────────────────────────────────

    @Test
    fun `SharedChallenge default status is PENDING`() {
        val challenge = SharedChallenge()
        assertEquals(ContentStatus.PENDING, challenge.status)
    }

    @Test
    fun `SharedChallenge default duration is 7`() {
        val challenge = SharedChallenge()
        assertEquals(7, challenge.duration)
    }

    @Test
    fun `SharedChallenge default dailyXpReward is 10`() {
        val challenge = SharedChallenge()
        assertEquals(10, challenge.dailyXpReward)
    }

    // ── UserRating ──────────────────────────────────────────────────

    @Test
    fun `UserRating default rating is 5`() {
        val rating = UserRating()
        assertEquals(5, rating.rating)
    }

    @Test
    fun `UserRating stores all fields`() {
        val rating = UserRating(
            ratingId = "r1",
            userId = "u1",
            contentId = "c1",
            contentType = "task",
            rating = 4,
            review = "Great task!"
        )
        assertEquals("r1", rating.ratingId)
        assertEquals("task", rating.contentType)
        assertEquals(4, rating.rating)
        assertEquals("Great task!", rating.review)
    }

    // ── ContentReport ───────────────────────────────────────────────

    @Test
    fun `ContentReport default reason is OTHER`() {
        val report = ContentReport()
        assertEquals(ReportReason.OTHER, report.reason)
    }

    @Test
    fun `ContentReport default status is PENDING`() {
        val report = ContentReport()
        assertEquals("PENDING", report.status)
    }

    // ── ChildLeaderboardEntry defaults ──────────────────────────────

    @Test
    fun `ChildLeaderboardEntry default level is 1`() {
        val entry = ChildLeaderboardEntry()
        assertEquals(1, entry.level)
    }

    @Test
    fun `ChildLeaderboardEntry default xp is 0`() {
        val entry = ChildLeaderboardEntry()
        assertEquals(0, entry.xp)
    }

    // ── FamilyLeaderboardEntry defaults ─────────────────────────────

    @Test
    fun `FamilyLeaderboardEntry default totalXp is 0`() {
        val entry = FamilyLeaderboardEntry()
        assertEquals(0, entry.totalXp)
    }

    @Test
    fun `FamilyLeaderboardEntry default memberCount is 0`() {
        val entry = FamilyLeaderboardEntry()
        assertEquals(0, entry.memberCount)
    }

    // ── ChallengeLeaderboardEntry defaults ──────────────────────────

    @Test
    fun `ChallengeLeaderboardEntry default completedByCount is 0`() {
        val entry = ChallengeLeaderboardEntry()
        assertEquals(0, entry.completedByCount)
    }
}
