package com.rudra.smartworktracker.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.rudra.smartworktracker.data.entity.ManualMealEntry
import kotlinx.coroutines.flow.Flow

@Dao
interface ManualMealEntryDao {

    @Query("SELECT * FROM manual_meal_entries WHERE isDeleted = 0 ORDER BY date ASC")
    fun getAllEntries(): Flow<List<ManualMealEntry>>

    @Query("SELECT * FROM manual_meal_entries WHERE isDeleted = 0 ORDER BY date ASC")
    suspend fun getAllEntriesList(): List<ManualMealEntry>

    @Query("SELECT * FROM manual_meal_entries WHERE date >= :startOfMonth AND date < :endOfMonth AND isDeleted = 0 ORDER BY date ASC")
    fun getEntriesInRange(startOfMonth: Long, endOfMonth: Long): Flow<List<ManualMealEntry>>

    @Query("SELECT * FROM manual_meal_entries WHERE date >= :startOfMonth AND date < :endOfMonth AND isDeleted = 0 ORDER BY date ASC")
    suspend fun getEntriesInRangeOnce(startOfMonth: Long, endOfMonth: Long): List<ManualMealEntry>

    @Query("SELECT * FROM manual_meal_entries WHERE date = :date AND isDeleted = 0 LIMIT 1")
    suspend fun getEntryByDate(date: Long): ManualMealEntry?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: ManualMealEntry): Long

    @Query("DELETE FROM manual_meal_entries WHERE date = :date")
    suspend fun deleteByDate(date: Long)

    @Query("DELETE FROM manual_meal_entries WHERE date >= :startOfMonth AND date < :endOfMonth")
    suspend fun deleteInRange(startOfMonth: Long, endOfMonth: Long)
}
