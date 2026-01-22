package com.rudra.smartworktracker.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "health_metrics")
data class HealthMetric(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val type: HealthMetricType,
    val value: Double,
    val timestamp: Long,
    val notes: String? = null,
    val tags: String? = null // Comma-separated tags like "work", "exercise", "nutrition"
)

enum class HealthMetricType {
    WEIGHT,
    HEIGHT,
    WATER,
    SLEEP,
    BREAKS,      // New: Number of breaks taken
    EXERCISE,    // New: Exercise minutes
    SCREEN_TIME, // New: Screen time in minutes
    POSTURE,     // New: Posture check (1 = good, 0 = bad)
    FOCUS,       // New: Focus time in minutes
    CALORIES,
    PROTEIN,
    CARBS,
    FIBER
}
