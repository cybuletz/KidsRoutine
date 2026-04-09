package com.kidsroutine.core.model

import org.junit.Assert.*
import org.junit.Test

class FamilyWalletTest {

    // ── FamilyWallet.xpToMoney ──────────────────────────────────────

    @Test
    fun `xpToMoney with default rate`() {
        val wallet = FamilyWallet()
        // default rate is 0.01, so 100 XP = $1.00
        assertEquals(1.0f, wallet.xpToMoney(100), 0.001f)
    }

    @Test
    fun `xpToMoney with custom rate`() {
        val wallet = FamilyWallet(xpToMoneyRate = 0.05f)
        assertEquals(5.0f, wallet.xpToMoney(100), 0.001f)
    }

    @Test
    fun `xpToMoney with zero xp`() {
        val wallet = FamilyWallet()
        assertEquals(0f, wallet.xpToMoney(0), 0.001f)
    }

    // ── FamilyWallet.formatMoney ────────────────────────────────────

    @Test
    fun `formatMoney with default currency`() {
        val wallet = FamilyWallet()
        assertEquals("$1.00", wallet.formatMoney(100))
    }

    @Test
    fun `formatMoney with euro symbol`() {
        val wallet = FamilyWallet(currencySymbol = "€")
        assertEquals("€1.00", wallet.formatMoney(100))
    }

    @Test
    fun `formatMoney with small amount`() {
        val wallet = FamilyWallet()
        assertEquals("$0.10", wallet.formatMoney(10))
    }

    @Test
    fun `formatMoney with zero`() {
        val wallet = FamilyWallet()
        assertEquals("$0.00", wallet.formatMoney(0))
    }

    // ── FamilyWallet defaults ───────────────────────────────────────

    @Test
    fun `default isEnabled is false`() {
        val wallet = FamilyWallet()
        assertFalse(wallet.isEnabled)
    }

    // ── SavingsGoal.progressPercent ─────────────────────────────────

    @Test
    fun `progressPercent at zero`() {
        val goal = SavingsGoal(targetXp = 100, currentXp = 0)
        assertEquals(0f, goal.progressPercent, 0.01f)
    }

    @Test
    fun `progressPercent at 50 percent`() {
        val goal = SavingsGoal(targetXp = 100, currentXp = 50)
        assertEquals(0.5f, goal.progressPercent, 0.01f)
    }

    @Test
    fun `progressPercent capped at 1_0`() {
        val goal = SavingsGoal(targetXp = 100, currentXp = 200)
        assertEquals(1.0f, goal.progressPercent, 0.01f)
    }

    @Test
    fun `progressPercent with zero target`() {
        val goal = SavingsGoal(targetXp = 0, currentXp = 50)
        assertEquals(0f, goal.progressPercent, 0.01f)
    }

    // ── SavingsGoal.xpRemaining ─────────────────────────────────────

    @Test
    fun `xpRemaining with progress`() {
        val goal = SavingsGoal(targetXp = 100, currentXp = 30)
        assertEquals(70, goal.xpRemaining)
    }

    @Test
    fun `xpRemaining cannot go below zero`() {
        val goal = SavingsGoal(targetXp = 50, currentXp = 75)
        assertEquals(0, goal.xpRemaining)
    }

    @Test
    fun `xpRemaining when fully funded`() {
        val goal = SavingsGoal(targetXp = 100, currentXp = 100)
        assertEquals(0, goal.xpRemaining)
    }

    // ── FinancialLiteracyTasks ──────────────────────────────────────

    @Test
    fun `financial literacy templates are non-empty`() {
        assertTrue(FinancialLiteracyTasks.templates.isNotEmpty())
    }

    @Test
    fun `all financial templates have positive XP rewards`() {
        FinancialLiteracyTasks.templates.forEach {
            assertTrue("${it.title} should have positive XP", it.reward.xp > 0)
        }
    }

    @Test
    fun `all financial templates are LEARNING category`() {
        FinancialLiteracyTasks.templates.forEach {
            assertEquals(TaskCategory.LEARNING, it.category)
        }
    }
}
