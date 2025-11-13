package com.rudra.smartworktracker.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.rudra.smartworktracker.data.entity.Income
import kotlinx.coroutines.flow.Flow

@Dao
interface IncomeDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(income: Income)

    @Query("SELECT * FROM income ORDER BY id DESC LIMIT 1")
    fun getLatestIncome(): Flow<Income?>
}