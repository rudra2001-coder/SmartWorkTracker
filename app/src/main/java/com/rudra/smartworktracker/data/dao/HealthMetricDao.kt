package com.rudra.smartworktracker.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.rudra.smartworktracker.model.HealthMetric
import kotlinx.coroutines.flow.Flow

@Dao
interface HealthMetricDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHealthMetric(metric: HealthMetric)
    @Query("SELECT * FROM health_metrics WHERE timestamp BETWEEN :start AND :end ORDER BY timestamp DESC")
    fun getMetricsBetweenTimestamps(start: Long, end: Long): Flow<List<HealthMetric>>

    @Query("SELECT * FROM health_metrics ORDER BY timestamp DESC")
    fun getAllHealthMetrics(): Flow<List<HealthMetric>>

    @Delete
    suspend fun deleteHealthMetric(metric: HealthMetric)
}
