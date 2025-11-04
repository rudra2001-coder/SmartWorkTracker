package com.rudra.smartworktracker.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "work_sessions")
data class WorkSession(
    @PrimaryKey val id: String,
    val startTime: Long,
    val endTime: Long?,
    val type: SessionType,
    val breaks: List<BreakPeriod>,
    val productivityScore: Int?
)

data class BreakPeriod(
    val startTime: Long,
    val endTime: Long
)

enum class SessionType {
    WORK,
    BREAK,
    LUNCH
}
