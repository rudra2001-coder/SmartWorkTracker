package com.rudra.smartworktracker.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.rudra.smartworktracker.data.entity.MonthlySummary
import kotlinx.coroutines.flow.Flow

@Dao
interface SummaryDao {

    // 1️⃣ Get all summaries
    @Query("SELECT * FROM MonthlySummary ORDER BY month DESC")
    fun getAllSummaries(): Flow<List<MonthlySummary>>

    // 2️⃣ Get specific month summary
    @Query("SELECT * FROM MonthlySummary WHERE month = :month LIMIT 1")
    fun getSummary(month: String): Flow<MonthlySummary?>



}
