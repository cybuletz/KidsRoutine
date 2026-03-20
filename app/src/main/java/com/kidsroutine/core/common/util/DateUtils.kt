package com.kidsroutine.core.common.util

import java.text.SimpleDateFormat
import java.util.*

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
}
