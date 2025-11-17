package com.rudra.smartworktracker.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.rudra.smartworktracker.data.entity.Savings
import kotlinx.coroutines.flow.Flow

@Dao
interface SavingsDao {

    @Insert
    suspend fun insert(savings: Savings)

    @Query("SELECT SUM(amount) FROM savings")
    fun getTotalSavings(): Flow<Double>

    @Query("SELECT * FROM savings ORDER BY timestamp ASC")
    fun getSavingsHistory(): Flow<List<Savings>>
}
