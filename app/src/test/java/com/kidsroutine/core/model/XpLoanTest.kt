package com.kidsroutine.core.model

import org.junit.Assert.*
import org.junit.Test

class XpLoanTest {

    // ── remaining ───────────────────────────────────────────────────

    @Test
    fun `remaining is amount minus repaid`() {
        val loan = XpLoan(amount = 100, amountRepaid = 30)
        assertEquals(70, loan.remaining)
    }

    @Test
    fun `remaining cannot go below zero`() {
        val loan = XpLoan(amount = 50, amountRepaid = 60)
        assertEquals(0, loan.remaining)
    }

    @Test
    fun `remaining is zero when fully repaid`() {
        val loan = XpLoan(amount = 100, amountRepaid = 100)
        assertEquals(0, loan.remaining)
    }

    // ── isFullyRepaid ───────────────────────────────────────────────

    @Test
    fun `isFullyRepaid true when repaid equals amount`() {
        val loan = XpLoan(amount = 100, amountRepaid = 100)
        assertTrue(loan.isFullyRepaid)
    }

    @Test
    fun `isFullyRepaid true when repaid exceeds amount`() {
        val loan = XpLoan(amount = 50, amountRepaid = 75)
        assertTrue(loan.isFullyRepaid)
    }

    @Test
    fun `isFullyRepaid false when not fully repaid`() {
        val loan = XpLoan(amount = 100, amountRepaid = 99)
        assertFalse(loan.isFullyRepaid)
    }

    // ── progressPercent ─────────────────────────────────────────────

    @Test
    fun `progressPercent at zero`() {
        val loan = XpLoan(amount = 100, amountRepaid = 0)
        assertEquals(0f, loan.progressPercent, 0.01f)
    }

    @Test
    fun `progressPercent at 50 percent`() {
        val loan = XpLoan(amount = 100, amountRepaid = 50)
        assertEquals(0.5f, loan.progressPercent, 0.01f)
    }

    @Test
    fun `progressPercent at 100 percent`() {
        val loan = XpLoan(amount = 100, amountRepaid = 100)
        assertEquals(1.0f, loan.progressPercent, 0.01f)
    }

    @Test
    fun `progressPercent zero when amount is zero`() {
        val loan = XpLoan(amount = 0, amountRepaid = 0)
        assertEquals(0f, loan.progressPercent, 0.01f)
    }

    // ── XpLoanStatus enum ───────────────────────────────────────────

    @Test
    fun `XpLoanStatus has 4 values`() {
        assertEquals(4, XpLoanStatus.entries.size)
    }

    @Test
    fun `default status is ACTIVE`() {
        val loan = XpLoan()
        assertEquals(XpLoanStatus.ACTIVE, loan.status)
    }

    // ── Default values ──────────────────────────────────────────────

    @Test
    fun `default repayment percentage is 20`() {
        val loan = XpLoan()
        assertEquals(20, loan.repaymentPercentage)
    }
}
