package com.rudra.smartworktracker.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.rudra.smartworktracker.data.entity.WorkDay

@Dao
interface WorkDayDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkDay(workDay: WorkDay)

    @Query("SELECT * FROM work_days WHERE date = :date")
    suspend fun getWorkDay(date: String): WorkDay?

    @Query("SELECT * FROM work_days WHERE date LIKE :month || '%' ORDER BY date ASC")
    suspend fun getWorkDaysForMonth(month: String): List<WorkDay>
}
