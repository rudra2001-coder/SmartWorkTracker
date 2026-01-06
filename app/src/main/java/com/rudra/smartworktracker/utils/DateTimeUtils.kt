
package com.rudra.smartworktracker.utils

import android.util.Log
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object DateTimeUtils {

    fun parseTime(time: String): Long {
        // Handle null/empty input
        val cleanTime = time.trim()
        if (cleanTime.isEmpty()) {
            return getTimeInMillis(0, 0)
        }

        // Parse with safety
        val parts = cleanTime.split(":")
        val hour = parts.getOrElse(0) { "0" }.toIntOrNull()?.coerceIn(0..23) ?: 0
        val minute = parts.getOrElse(1) { "0" }.toIntOrNull()?.coerceIn(0..59) ?: 0

        return getTimeInMillis(hour, minute)
    }

    private fun getTimeInMillis(hour: Int, minute: Int): Long {
        return Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    fun parseDate(dateString: String, format: String = "yyyy-MM-dd"): Date? {
        return try {
            SimpleDateFormat(format, Locale.getDefault()).parse(dateString)
        } catch (e: ParseException) {
            Log.e("DateTimeUtils", "Error parsing date: '$dateString' with format: '$format'", e)
            null
        }
    }
}
