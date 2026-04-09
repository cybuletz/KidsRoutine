package com.kidsroutine.core.common.util

import org.junit.Assert.*
import org.junit.Test
import java.text.SimpleDateFormat
import java.util.*

class DateUtilsTest {

    private val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    // ── todayString ─────────────────────────────────────────────────

    @Test
    fun `todayString returns current date in yyyy-MM-dd format`() {
        val today = DateUtils.todayString()
        // Should parse without error
        val parsed = format.parse(today)
        assertNotNull(parsed)
        // Should be today
        assertEquals(format.format(Date()), today)
    }

    // ── dateString ──────────────────────────────────────────────────

    @Test
    fun `dateString formats a known date correctly`() {
        val cal = Calendar.getInstance()
        cal.set(2026, Calendar.APRIL, 9)
        assertEquals("2026-04-09", DateUtils.dateString(cal.time))
    }

    // ── daysBetween ─────────────────────────────────────────────────

    @Test
    fun `daysBetween for same date is 0`() {
        val date = format.parse("2026-04-09")!!
        assertEquals(0, DateUtils.daysBetween(date, date))
    }

    @Test
    fun `daysBetween for consecutive days is 1`() {
        val from = format.parse("2026-04-08")!!
        val to = format.parse("2026-04-09")!!
        assertEquals(1, DateUtils.daysBetween(from, to))
    }

    @Test
    fun `daysBetween for a week apart is 7`() {
        val from = format.parse("2026-04-02")!!
        val to = format.parse("2026-04-09")!!
        assertEquals(7, DateUtils.daysBetween(from, to))
    }

    // ── parseDate ───────────────────────────────────────────────────

    @Test
    fun `parseDate parses valid date string`() {
        val date = DateUtils.parseDate("2026-04-09")
        val cal = Calendar.getInstance()
        cal.time = date
        assertEquals(2026, cal.get(Calendar.YEAR))
        assertEquals(Calendar.APRIL, cal.get(Calendar.MONTH))
        assertEquals(9, cal.get(Calendar.DAY_OF_MONTH))
    }

    // ── nextDay ─────────────────────────────────────────────────────

    @Test
    fun `nextDay increments by one`() {
        assertEquals("2026-04-10", DateUtils.nextDay("2026-04-09"))
    }

    @Test
    fun `nextDay crosses month boundary`() {
        assertEquals("2026-05-01", DateUtils.nextDay("2026-04-30"))
    }

    @Test
    fun `nextDay crosses year boundary`() {
        assertEquals("2027-01-01", DateUtils.nextDay("2026-12-31"))
    }

    // ── previousDay ─────────────────────────────────────────────────

    @Test
    fun `previousDay decrements by one`() {
        assertEquals("2026-04-08", DateUtils.previousDay("2026-04-09"))
    }

    @Test
    fun `previousDay crosses month boundary`() {
        assertEquals("2026-03-31", DateUtils.previousDay("2026-04-01"))
    }

    // ── addDays ─────────────────────────────────────────────────────

    @Test
    fun `addDays adds positive days`() {
        assertEquals("2026-04-14", DateUtils.addDays("2026-04-09", 5))
    }

    @Test
    fun `addDays with 0 returns same date`() {
        assertEquals("2026-04-09", DateUtils.addDays("2026-04-09", 0))
    }

    @Test
    fun `addDays with negative subtracts days`() {
        assertEquals("2026-04-04", DateUtils.addDays("2026-04-09", -5))
    }

    // ── isToday ─────────────────────────────────────────────────────

    @Test
    fun `isToday returns true for today`() {
        assertTrue(DateUtils.isToday(DateUtils.todayString()))
    }

    @Test
    fun `isToday returns false for yesterday`() {
        val yesterday = DateUtils.previousDay(DateUtils.todayString())
        assertFalse(DateUtils.isToday(yesterday))
    }

    // ── getDayOfWeek ────────────────────────────────────────────────

    @Test
    fun `getDayOfWeek returns expected value`() {
        // Calendar.DAY_OF_WEEK: 1=Sunday, 2=Monday, etc.
        val dayOfWeek = DateUtils.getDayOfWeek("2026-04-09") // Thursday
        assertTrue(dayOfWeek in 1..7)
    }
}
