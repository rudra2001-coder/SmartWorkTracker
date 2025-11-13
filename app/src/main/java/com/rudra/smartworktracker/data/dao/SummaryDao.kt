package com.rudra.smartworktracker.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.rudra.smartworktracker.data.entity.MonthlySummary

@Dao
interface SummaryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSummary(summary: MonthlySummary)

    @Query("SELECT * FROM monthly_summaries WHERE month = :month")
    suspend fun getSummary(month: String): MonthlySummary?
}
