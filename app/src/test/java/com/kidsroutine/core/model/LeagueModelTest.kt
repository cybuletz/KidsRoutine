package com.kidsroutine.core.model

import org.junit.Assert.*
import org.junit.Test

class LeagueModelTest {

    // ── League enum basic properties ────────────────────────────────

    @Test
    fun `Bronze is tier 1`() {
        assertEquals(1, League.BRONZE.tier)
    }

    @Test
    fun `Champion is tier 10`() {
        assertEquals(10, League.CHAMPION.tier)
    }

    @Test
    fun `there are exactly 10 leagues`() {
        assertEquals(10, League.entries.size)
    }

    @Test
    fun `leagues are ordered by tier`() {
        val tiers = League.entries.map { it.tier }
        assertEquals(tiers.sorted(), tiers)
    }

    // ── nextLeague / previousLeague ─────────────────────────────────

    @Test
    fun `Bronze nextLeague is Silver`() {
        assertEquals(League.SILVER, League.BRONZE.nextLeague)
    }

    @Test
    fun `Champion nextLeague is null`() {
        assertNull(League.CHAMPION.nextLeague)
    }

    @Test
    fun `Silver previousLeague is Bronze`() {
        assertEquals(League.BRONZE, League.SILVER.previousLeague)
    }

    @Test
    fun `Bronze previousLeague is null`() {
        assertNull(League.BRONZE.previousLeague)
    }

    @Test
    fun `nextLeague chain reaches Champion`() {
        var league: League? = League.BRONZE
        var count = 0
        while (league != null) {
            count++
            league = league.nextLeague
        }
        assertEquals(10, count)
    }

    // ── fromTier ────────────────────────────────────────────────────

    @Test
    fun `fromTier returns correct league`() {
        assertEquals(League.BRONZE, League.fromTier(1))
        assertEquals(League.GOLD, League.fromTier(3))
        assertEquals(League.CHAMPION, League.fromTier(10))
    }

    @Test
    fun `fromTier returns Bronze for unknown tier`() {
        assertEquals(League.BRONZE, League.fromTier(99))
        assertEquals(League.BRONZE, League.fromTier(0))
    }

    // ── promotion / demotion slots ──────────────────────────────────

    @Test
    fun `Bronze has 0 demotion slots`() {
        assertEquals(0, League.BRONZE.demotionSlots)
    }

    @Test
    fun `Champion has 0 promotion slots`() {
        assertEquals(0, League.CHAMPION.promotionSlots)
    }

    @Test
    fun `mid-tier leagues have promotion and demotion slots`() {
        for (league in listOf(League.SILVER, League.GOLD, League.PLATINUM, League.DIAMOND)) {
            assertTrue("${league.displayName} should have promotion slots", league.promotionSlots > 0)
            assertTrue("${league.displayName} should have demotion slots", league.demotionSlots > 0)
        }
    }

    // ── minXpForPromotion increases with tier ───────────────────────

    @Test
    fun `minXpForPromotion increases with tier`() {
        val promotable = League.entries.filter { it.promotionSlots > 0 }
        for (i in 0 until promotable.size - 1) {
            assertTrue(
                "${promotable[i].displayName} minXp should be <= ${promotable[i + 1].displayName} minXp",
                promotable[i].minXpForPromotion <= promotable[i + 1].minXpForPromotion
            )
        }
    }

    // ── display properties ──────────────────────────────────────────

    @Test
    fun `all leagues have non-empty display names`() {
        League.entries.forEach { assertTrue(it.displayName.isNotEmpty()) }
    }

    @Test
    fun `all leagues have non-empty emojis`() {
        League.entries.forEach { assertTrue(it.emoji.isNotEmpty()) }
    }

    // ── EventProgress tokensAvailable ───────────────────────────────

    @Test
    fun `EventProgress tokensAvailable is earned minus spent`() {
        val progress = EventProgress(tokensEarned = 15, tokensSpent = 7)
        assertEquals(8, progress.tokensAvailable)
    }

    @Test
    fun `EventProgress tokensAvailable is 0 when all spent`() {
        val progress = EventProgress(tokensEarned = 10, tokensSpent = 10)
        assertEquals(0, progress.tokensAvailable)
    }
}
