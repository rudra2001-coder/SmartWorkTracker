package com.rudra.smartworktracker.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime
import java.time.LocalTime

@Entity(tableName = "schedules")
data class Schedule(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val time: LocalTime, // Changed from hour/minute to LocalTime
    val isEnabled: Boolean = true,
    val isRepeating: Boolean = false,
    val repeatingDays: Set<Int> = emptySet(), // 1=Monday, 7=Sunday
    val createdAt: LocalDateTime = LocalDateTime.now()
)

// Add a helper function to convert to hour/minute
fun Schedule.getHour(): Int = time.hour
fun Schedule.getMinute(): Int = time.minute
