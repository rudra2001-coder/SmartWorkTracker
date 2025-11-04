package com.rudra.smartworktracker.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "health_metrics")
data class HealthMetric(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val type: HealthMetricType,
    val value: Double,
    val timestamp: Long
)

enum class HealthMetricType {
    WEIGHT,
    HEIGHT
}
