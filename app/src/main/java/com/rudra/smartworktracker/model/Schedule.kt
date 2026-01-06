package com.rudra.smartworktracker.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalTime
import java.time.LocalDateTime
@Entity(tableName = "schedules")
data class Schedule(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val hour: Int,
    val minute: Int,
    val isRepeating: Boolean,
    val isEnabled: Boolean = true,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
) {
    val time: LocalTime
        get() = LocalTime.of(hour, minute)
}
