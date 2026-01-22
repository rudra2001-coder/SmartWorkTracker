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
    val scheduleTime: String? = null,
    val ringtoneName: String? = null, // Added for ringtone tracking
    val changesSummary: Map<String, String>? = null // Detailed changes for updates
) {
    fun formattedTime(): String {
        val now = LocalDateTime.now()
        val minutesAgo = java.time.Duration.between(timestamp, now).toMinutes()
        val hoursAgo = java.time.Duration.between(timestamp, now).toHours()
        val daysAgo = java.time.Duration.between(timestamp, now).toDays()
        
        return when {
            minutesAgo < 1 -> "Just now"
            minutesAgo < 60 -> "$minutesAgo minutes ago"
            hoursAgo < 24 -> "$hoursAgo hours ago"
            daysAgo == 1L -> "Yesterday"
            daysAgo < 7L -> "$daysAgo days ago"
            else -> timestamp.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM))
        }
    }

    fun formattedTimestamp(): String {
        return timestamp.format(DateTimeFormatter.ofPattern("MMM dd, yyyy • hh:mm a"))
    }
    
    fun getIcon(): String {
        return when (type) {
            HistoryType.CREATED -> "📝"
            HistoryType.UPDATED -> "🔄"
            HistoryType.TRIGGERED -> "🔔"
            HistoryType.DELETED -> "🗑️"
            HistoryType.RINGTONE_CHANGED -> "🎵"
        }
    }
    
    fun getColorCode(): Int {
        return when (type) {
            HistoryType.CREATED -> 0xFF4CAF50
            HistoryType.UPDATED -> 0xFF2196F3
            HistoryType.TRIGGERED -> 0xFFFF9800
            HistoryType.DELETED -> 0xFFF44336
            HistoryType.RINGTONE_CHANGED -> 0xFF9C27B0
        }.toInt()
    }
}

enum class HistoryType {
    CREATED, UPDATED, TRIGGERED, DELETED, RINGTONE_CHANGED
}
