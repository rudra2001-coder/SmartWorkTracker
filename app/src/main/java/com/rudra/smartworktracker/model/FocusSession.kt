package com.rudra.smartworktracker.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "focus_sessions")
data class FocusSession(
    @PrimaryKey val id: String,
    val type: FocusType,
    val duration: Long,
    val interruptions: Int,
    val focusScore: Int,
    val timestamp: Long
)

enum class FocusType {
    DEEP_WORK,
    POMODORO
}
