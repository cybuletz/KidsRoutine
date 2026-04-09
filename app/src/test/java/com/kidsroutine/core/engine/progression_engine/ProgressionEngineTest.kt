package com.kidsroutine.core.engine.progression_engine

import io.mockk.mockk
import org.junit.Assert.*
import org.junit.Test

class ProgressionEngineTest {

    // ── exposes calculators ─────────────────────────────────────────

    @Test
    fun `xpCalculator is accessible`() {
        val xpCalc = mockk<XpCalculator>()
        val streakCalc = mockk<StreakCalculator>()
        val engine = ProgressionEngine(xpCalc, streakCalc)
        assertSame(xpCalc, engine.xpCalculator)
    }

    @Test
    fun `streakCalculator is accessible`() {
        val xpCalc = mockk<XpCalculator>()
        val streakCalc = mockk<StreakCalculator>()
        val engine = ProgressionEngine(xpCalc, streakCalc)
        assertSame(streakCalc, engine.streakCalculator)
    }

    @Test
    fun `holds both calculators from construction`() {
        val xpCalc = XpCalculator()
        val streakCalc = StreakCalculator()
        val engine = ProgressionEngine(xpCalc, streakCalc)
        assertNotNull(engine.xpCalculator)
        assertNotNull(engine.streakCalculator)
    }
}
