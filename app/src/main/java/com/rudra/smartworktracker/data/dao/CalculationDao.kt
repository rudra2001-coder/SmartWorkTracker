package com.rudra.smartworktracker.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.rudra.smartworktracker.data.entity.Calculation
import kotlinx.coroutines.flow.Flow

@Dao
interface CalculationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(calculation: Calculation)

    @Query("SELECT * FROM calculations WHERE id = 1")
    fun getCalculation(): Flow<Calculation?>
}
