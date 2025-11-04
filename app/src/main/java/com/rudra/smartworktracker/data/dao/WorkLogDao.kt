package com.rudra.smartworktracker.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.rudra.smartworktracker.model.WorkLog
import com.rudra.smartworktracker.model.WorkType
import kotlinx.coroutines.flow.Flow
import java.util.Date

@Dao
interface WorkLogDao {
    @Query("SELECT * FROM work_logs ORDER BY date DESC")
    fun getAllWorkLogs(): Flow<List<WorkLog>>

    @Query("SELECT * FROM work_logs WHERE date = :date")
    suspend fun getWorkLogByDate(date: Date): WorkLog?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkLog(workLog: WorkLog)

    @Delete
    suspend fun deleteWorkLog(workLog: WorkLog)

    @Query("DELETE FROM work_logs")
    suspend fun clearAll()

    @Query("SELECT COUNT(*) FROM work_logs WHERE strftime('%Y-%m', date / 1000, 'unixepoch') = :monthYear AND workType = :workType")
    suspend fun countByType(monthYear: String, workType: WorkType): Int

    @Query("SELECT * FROM work_logs WHERE strftime('%Y-%m', date / 1000, 'unixepoch') = :monthYear")
    suspend fun getWorkLogsByMonth(monthYear: String): List<WorkLog>

    @Query("SELECT SUM((strftime('%s', endTime) - strftime('%s', startTime)) / 3600.0) FROM work_logs WHERE strftime('%Y-%m', date / 1000, 'unixepoch') = :monthYear AND workType = :workType")
    suspend fun getTotalExtraHours(monthYear: String, workType: WorkType = WorkType.EXTRA_WORK): Double?

}
