package com.rudra.smartworktracker.ui.screens.scheduler

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

data class ScheduleHistory(
    val id: String = System.currentTimeMillis().toString() + (0..1000).random(),
    val scheduleId: Long?,
    val type: HistoryType,
    val timestamp: LocalDateTime = LocalDateTime.now(),
    val details: String? = null,
    val scheduleTitle: String? = null,
    val scheduleTime: String? = null
) {
    fun formattedTime(): String {
        val now = LocalDateTime.now()
        val daysAgo = java.time.Duration.between(timestamp, now).toDays()
        return when {
            daysAgo == 0L -> "Today"
            daysAgo == 1L -> "Yesterday"
            daysAgo < 7L -> "$daysAgo days ago"
            else -> timestamp.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM))
        }
    }

    fun formattedTimestamp(): String {
        return timestamp.format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM, FormatStyle.SHORT))
    }
}

enum class HistoryType {
    CREATED, UPDATED, TRIGGERED, DELETED
}
