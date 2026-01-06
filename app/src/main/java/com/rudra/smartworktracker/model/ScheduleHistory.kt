package com.rudra.smartworktracker.model

import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

enum class HistoryType {
    CREATED, UPDATED, TRIGGERED, DELETED
}

data class ScheduleHistory(
    val scheduleId: Long,
    val timestamp: Long = System.currentTimeMillis(),
    val type: HistoryType,
    val details: String? = null
) {
    fun formattedTime(): String {
        val instant = Instant.ofEpochMilli(timestamp)
        val localDateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault())
        return localDateTime.format(DateTimeFormatter.ofPattern("hh:mm a"))
    }
}
