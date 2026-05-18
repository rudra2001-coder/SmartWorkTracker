package com.rudra.smartworktracker.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.rudra.smartworktracker.model.HealthMetric
import com.rudra.smartworktracker.model.HealthMetricType
import kotlinx.coroutines.flow.Flow

@Dao
interface HealthMetricDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHealthMetric(metric: HealthMetric): Long

    @Update
    suspend fun updateHealthMetric(metric: HealthMetric)

    @Delete
    suspend fun deleteHealthMetric(metric: HealthMetric)

    @Query("SELECT * FROM health_metrics WHERE isDeleted = 0 ORDER BY timestamp DESC")
    fun getAllHealthMetrics(): Flow<List<HealthMetric>>

    @Query("SELECT * FROM health_metrics WHERE isDeleted = 0 AND timestamp BETWEEN :start AND :end ORDER BY timestamp DESC")
    fun getMetricsBetweenTimestamps(start: Long, end: Long): Flow<List<HealthMetric>>

    @Query("SELECT * FROM health_metrics WHERE isDeleted = 0 AND type = :type ORDER BY timestamp DESC")
    fun getMetricsByType(type: HealthMetricType): Flow<List<HealthMetric>>

    @Query("SELECT * FROM health_metrics WHERE isDeleted = 0 AND type = :type AND timestamp BETWEEN :start AND :end ORDER BY timestamp DESC")
    fun getMetricsByTypeAndTimeRange(type: HealthMetricType, start: Long, end: Long): Flow<List<HealthMetric>>

    @Query("SELECT * FROM health_metrics WHERE isDeleted = 0 AND timestamp >= :startOfDay AND timestamp < :endOfDay ORDER BY timestamp DESC")
    fun getTodaysMetrics(startOfDay: Long, endOfDay: Long): Flow<List<HealthMetric>>

    @Query("SELECT * FROM health_metrics WHERE isDeleted = 0 ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentMetrics(limit: Int): Flow<List<HealthMetric>>

    @Query("SELECT * FROM health_metrics WHERE isDeleted = 0 AND id = :id")
    fun getMetricById(id: Int): Flow<HealthMetric?>

    @Query("SELECT * FROM health_metrics WHERE isDeleted = 0 AND type = :type ORDER BY timestamp DESC LIMIT 1")
    fun getLatestMetricByType(type: HealthMetricType): Flow<HealthMetric?>

    @Query("SELECT SUM(value) FROM health_metrics WHERE isDeleted = 0 AND type = :type AND timestamp BETWEEN :start AND :end")
    fun getSumOfMetricByTypeAndTimeRange(type: HealthMetricType, start: Long, end: Long): Flow<Double?>

    @Query("SELECT AVG(value) FROM health_metrics WHERE isDeleted = 0 AND type = :type AND timestamp BETWEEN :start AND :end")
    fun getAverageOfMetricByTypeAndTimeRange(type: HealthMetricType, start: Long, end: Long): Flow<Double?>

    @Query("SELECT COUNT(*) FROM health_metrics WHERE isDeleted = 0 AND type = :type AND timestamp BETWEEN :start AND :end")
    fun getCountOfMetricByTypeAndTimeRange(type: HealthMetricType, start: Long, end: Long): Flow<Int>

    @Query("SELECT * FROM health_metrics WHERE isDeleted = 0 AND type = :type ORDER BY timestamp DESC LIMIT :limit")
    fun getMetricsByTypeWithLimit(type: HealthMetricType, limit: Int): Flow<List<HealthMetric>>

    @Query("SELECT * FROM health_metrics WHERE isDeleted = 0 AND tags LIKE '%' || :tag || '%' ORDER BY timestamp DESC")
    fun getMetricsByTag(tag: String): Flow<List<HealthMetric>>

    @Query("SELECT DISTINCT tags FROM health_metrics WHERE isDeleted = 0 AND tags IS NOT NULL")
    fun getAllTags(): Flow<List<String>>

    @Query("UPDATE health_metrics SET isDeleted = 1, updatedAt = :timestamp WHERE id = :id")
    suspend fun softDeleteMetric(id: Int, timestamp: Long = System.currentTimeMillis())

    @Query("DELETE FROM health_metrics WHERE id = :id")
    suspend fun deleteMetricById(id: Int)

    @Query("SELECT * FROM health_metrics WHERE isDeleted = 0 AND (notes LIKE '%' || :query || '%' OR tags LIKE '%' || :query || '%') ORDER BY timestamp DESC")
    fun searchMetrics(query: String): Flow<List<HealthMetric>>
}
