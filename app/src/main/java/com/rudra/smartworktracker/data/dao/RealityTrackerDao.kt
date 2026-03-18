package com.rudra.smartworktracker.data.dao

import androidx.room.*
import com.rudra.smartworktracker.model.RealityEntry
import kotlinx.coroutines.flow.Flow

@Dao
interface RealityTrackerDao {
    @Query("SELECT * FROM reality_entries ORDER BY createdAt DESC")
    fun getAllEntries(): Flow<List<RealityEntry>>

    @Query("SELECT * FROM reality_entries WHERE type = :type ORDER BY createdAt DESC")
    fun getEntriesByType(type: String): Flow<List<RealityEntry>>

    @Query("SELECT * FROM reality_entries WHERE category = :category ORDER BY createdAt DESC")
    fun getEntriesByCategory(category: String): Flow<List<RealityEntry>>

    @Query("SELECT * FROM reality_entries WHERE createdAt >= :startTime AND createdAt <= :endTime ORDER BY createdAt DESC")
    fun getEntriesInRange(startTime: Long, endTime: Long): Flow<List<RealityEntry>>

    @Query("SELECT COUNT(*) FROM reality_entries WHERE createdAt >= :startTime AND createdAt <= :endTime")
    fun getTotalPlannedCount(startTime: Long, endTime: Long): Flow<Int>

    @Query("SELECT COUNT(*) FROM reality_entries WHERE isCompleted = 1 AND completedAt >= :startTime AND completedAt <= :endTime")
    fun getCompletedCount(startTime: Long, endTime: Long): Flow<Int>

    @Query("SELECT COUNT(*) FROM reality_entries WHERE type = :type AND createdAt >= :startTime AND createdAt <= :endTime")
    fun getPlannedCountByType(type: String, startTime: Long, endTime: Long): Flow<Int>

    @Query("SELECT COUNT(*) FROM reality_entries WHERE type = :type AND isCompleted = 1 AND completedAt >= :startTime AND completedAt <= :endTime")
    fun getCompletedCountByType(type: String, startTime: Long, endTime: Long): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntry(entry: RealityEntry)

    @Update
    suspend fun updateEntry(entry: RealityEntry)

    @Query("DELETE FROM reality_entries WHERE id = :id")
    suspend fun deleteEntry(id: String)

    @Query("UPDATE reality_entries SET isCompleted = :isCompleted, completedAt = :completedAt WHERE id = :id")
    suspend fun markAsCompleted(id: String, isCompleted: Boolean, completedAt: Long?)
}
