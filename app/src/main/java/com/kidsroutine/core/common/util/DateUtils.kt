package com.kidsroutine.core.common.util

import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

object DateUtils {
    private val dayFormat  = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

    fun todayString(): String = dayFormat.format(Date())

    fun dateString(date: Date): String = dayFormat.format(date)

    fun daysBetween(from: Date, to: Date): Int {
        val diff = to.time - from.time
        return (diff / (1000 * 60 * 60 * 24)).toInt()
    }

    fun parseDate(dateStr: String): Date = dayFormat.parse(dateStr) ?: Date()

    /**
     * Returns the next day's date string (YYYY-MM-DD format).
     */
    fun nextDay(dateStr: String): String {
        val calendar = Calendar.getInstance()
        calendar.time = parseDate(dateStr)
        calendar.add(Calendar.DAY_OF_MONTH, 1)
        return dayFormat.format(calendar.time)
    }

    /**
     * Returns the previous day's date string (YYYY-MM-DD format).
     */
    fun previousDay(dateStr: String): String {
        val calendar = Calendar.getInstance()
        calendar.time = parseDate(dateStr)
        calendar.add(Calendar.DAY_OF_MONTH, -1)
        return dayFormat.format(calendar.time)
    }

    /**
     * Adds days to a date string and returns the new date string.
     */
    fun addDays(dateStr: String, daysToAdd: Int): String {
        val calendar = Calendar.getInstance()
        calendar.time = parseDate(dateStr)
        calendar.add(Calendar.DAY_OF_MONTH, daysToAdd)
        return dayFormat.format(calendar.time)
    }

    /**
     * Checks if date is in the past.
     */
    fun isPast(dateStr: String): Boolean {
        return parseDate(dateStr).before(Date())
    }

    /**
     * Checks if date is today.
     */
    fun isToday(dateStr: String): Boolean {
        return dateStr == todayString()
    }

    /**
     * Gets day of week (1=Monday, 7=Sunday).
     */
    fun getDayOfWeek(dateStr: String): Int {
        val calendar = Calendar.getInstance()
        calendar.time = parseDate(dateStr)
        return calendar.get(Calendar.DAY_OF_WEEK)
    }
}