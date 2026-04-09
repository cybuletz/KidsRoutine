package com.kidsroutine.core.engine.notification_engine

import com.kidsroutine.core.model.AgeGroup
import com.kidsroutine.core.model.League
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class NotificationCopyEngineTest {

    private lateinit var engine: NotificationCopyEngine

    @Before
    fun setUp() {
        engine = NotificationCopyEngine()
    }

    // ── streakAtRisk ────────────────────────────────────────────────

    @Test
    fun `streakAtRisk for SPROUT mentions Roo`() {
        val copy = engine.streakAtRisk(5, AgeGroup.SPROUT)
        assertTrue(copy.title.contains("Roo"))
        assertEquals(NotificationPriority.HIGH, copy.priority)
    }

    @Test
    fun `streakAtRisk for EXPLORER has alert title`() {
        val copy = engine.streakAtRisk(10, AgeGroup.EXPLORER)
        assertTrue(copy.title.contains("Streak"))
    }

    @Test
    fun `streakAtRisk for TRAILBLAZER is casual`() {
        val copy = engine.streakAtRisk(7, AgeGroup.TRAILBLAZER)
        assertTrue(copy.title.contains("lose"))
    }

    @Test
    fun `streakAtRisk for LEGEND is concise`() {
        val copy = engine.streakAtRisk(20, AgeGroup.LEGEND)
        assertTrue(copy.title.contains("risk"))
    }

    @Test
    fun `streakAtRisk body includes streak count`() {
        for (ageGroup in AgeGroup.entries) {
            val copy = engine.streakAtRisk(15, ageGroup)
            assertTrue("Body should contain streak count for $ageGroup", copy.body.contains("15"))
        }
    }

    // ── comebackNudge ───────────────────────────────────────────────

    @Test
    fun `comebackNudge returns medium priority`() {
        val copy = engine.comebackNudge(5, AgeGroup.EXPLORER)
        assertEquals(NotificationPriority.MEDIUM, copy.priority)
    }

    @Test
    fun `comebackNudge has non-empty body for all age groups`() {
        for (ageGroup in AgeGroup.entries) {
            val copy = engine.comebackNudge(3, ageGroup)
            assertTrue(copy.body.isNotEmpty())
            assertTrue(copy.title.isNotEmpty())
        }
    }

    // ── petHungry ───────────────────────────────────────────────────

    @Test
    fun `petHungry includes pet name in title`() {
        for (ageGroup in AgeGroup.entries) {
            val copy = engine.petHungry("Rex", ageGroup)
            assertTrue("Title should contain pet name for $ageGroup", copy.title.contains("Rex"))
        }
    }

    @Test
    fun `petHungry includes pet name in body`() {
        for (ageGroup in AgeGroup.entries) {
            val copy = engine.petHungry("Buddy", ageGroup)
            assertTrue("Body should contain pet name for $ageGroup", copy.body.contains("Buddy"))
        }
    }

    // ── leaguePromotion ─────────────────────────────────────────────

    @Test
    fun `leaguePromotion is high priority`() {
        val copy = engine.leaguePromotion(League.GOLD, 50, AgeGroup.EXPLORER)
        assertEquals(NotificationPriority.HIGH, copy.priority)
    }

    @Test
    fun `leaguePromotion body includes xp needed`() {
        for (ageGroup in AgeGroup.entries) {
            val copy = engine.leaguePromotion(League.SILVER, 42, ageGroup)
            assertTrue("Body should include XP needed for $ageGroup", copy.body.contains("42"))
        }
    }

    // ── leagueDemotion ──────────────────────────────────────────────

    @Test
    fun `leagueDemotion is high priority`() {
        val copy = engine.leagueDemotion(League.SILVER, AgeGroup.LEGEND)
        assertEquals(NotificationPriority.HIGH, copy.priority)
    }

    @Test
    fun `leagueDemotion body includes league name`() {
        for (ageGroup in AgeGroup.entries) {
            val copy = engine.leagueDemotion(League.GOLD, ageGroup)
            assertTrue("Body should include league name for $ageGroup", copy.body.contains("Gold"))
        }
    }

    // ── bossAppeared ────────────────────────────────────────────────

    @Test
    fun `bossAppeared includes boss name for all age groups`() {
        for (ageGroup in AgeGroup.entries) {
            val copy = engine.bossAppeared("Hydra", ageGroup)
            assertTrue("Body should include boss name for $ageGroup", copy.body.contains("Hydra"))
        }
    }

    @Test
    fun `bossAppeared is high priority`() {
        val copy = engine.bossAppeared("Dragon", AgeGroup.SPROUT)
        assertEquals(NotificationPriority.HIGH, copy.priority)
    }

    // ── friendActivity ──────────────────────────────────────────────

    @Test
    fun `friendActivity includes friend name and XP`() {
        for (ageGroup in AgeGroup.entries) {
            val copy = engine.friendActivity("Alice", 150, ageGroup)
            assertTrue(copy.body.contains("Alice"))
            assertTrue(copy.body.contains("150"))
        }
    }

    @Test
    fun `friendActivity is low priority`() {
        val copy = engine.friendActivity("Bob", 50, AgeGroup.EXPLORER)
        assertEquals(NotificationPriority.LOW, copy.priority)
    }

    // ── randomEncouragement ─────────────────────────────────────────

    @Test
    fun `randomEncouragement returns non-empty for all age groups`() {
        for (ageGroup in AgeGroup.entries) {
            val copy = engine.randomEncouragement(ageGroup)
            assertTrue(copy.title.isNotEmpty())
            assertTrue(copy.body.isNotEmpty())
            assertEquals(NotificationPriority.LOW, copy.priority)
        }
    }

    // ── eventStarted ────────────────────────────────────────────────

    @Test
    fun `eventStarted includes event title in body`() {
        for (ageGroup in AgeGroup.entries) {
            val copy = engine.eventStarted("Summer Sprint", "🏃", ageGroup)
            assertTrue(copy.body.contains("Summer Sprint"))
        }
    }

    @Test
    fun `eventStarted is high priority`() {
        val copy = engine.eventStarted("Test", "🎯", AgeGroup.SPROUT)
        assertEquals(NotificationPriority.HIGH, copy.priority)
    }

    // ── dailySpinAvailable ──────────────────────────────────────────

    @Test
    fun `dailySpinAvailable returns non-empty for all age groups`() {
        for (ageGroup in AgeGroup.entries) {
            val copy = engine.dailySpinAvailable(ageGroup)
            assertTrue(copy.title.isNotEmpty())
            assertTrue(copy.body.isNotEmpty())
            assertEquals(NotificationPriority.LOW, copy.priority)
        }
    }

    @Test
    fun `dailySpinAvailable mentions Roo in body`() {
        for (ageGroup in AgeGroup.entries) {
            val copy = engine.dailySpinAvailable(ageGroup)
            assertTrue("Body should mention Roo for $ageGroup", copy.body.contains("Roo") || copy.body.contains("🦘"))
        }
    }
}
