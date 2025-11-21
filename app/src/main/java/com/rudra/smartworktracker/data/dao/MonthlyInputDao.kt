package com.rudra.smartworktracker.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.rudra.smartworktracker.data.entity.MonthlyInput

@Dao
interface MonthlyInputDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMonthlyInput(monthlyInput: MonthlyInput)

    @Query("SELECT * FROM monthly_inputs WHERE month = :month")
    suspend fun getMonthlyInput(month: String): MonthlyInput?




}
