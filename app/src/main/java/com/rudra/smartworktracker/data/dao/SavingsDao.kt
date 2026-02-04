package com.rudra.smartworktracker.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.rudra.smartworktracker.data.entity.Savings
import kotlinx.coroutines.flow.Flow

@Dao
interface SavingsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(savings: Savings)

    @Query("SELECT SUM(amount) FROM savings")
    fun getTotalSavings(): Flow<Double>

    @Query("SELECT * FROM savings ORDER BY timestamp ASC")
    fun getSavingsHistory(): Flow<List<Savings>>

    @Query("SELECT * FROM savings")
    fun getAllSavings(): Flow<List<Savings>>

    @Query("SELECT SUM(amount) FROM savings WHERE timestamp BETWEEN :startTime AND :endTime")
    fun getSavingsBetween(startTime: Long, endTime: Long): Flow<Double?>

    @Query("DELETE FROM savings")
    suspend fun deleteAll()
}
