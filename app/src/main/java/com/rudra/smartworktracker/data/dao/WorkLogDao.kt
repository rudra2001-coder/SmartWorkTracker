package com.rudra.smartworktracker.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.rudra.smartworktracker.model.WorkLog
import com.rudra.smartworktracker.model.WorkType
import kotlinx.coroutines.flow.Flow
import java.util.Date

@Dao
interface WorkLogDao {
    @Query("SELECT * FROM work_logs ORDER BY date DESC")
    fun getAllWorkLogs(): Flow<List<WorkLog>>

    @Query("SELECT * FROM work_logs WHERE date BETWEEN :startOfDay AND :endOfDay")
    fun getWorkLogByDate(startOfDay: Long, endOfDay: Long): Flow<WorkLog?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkLog(workLog: WorkLog)

    @Update
    suspend fun updateWorkLog(workLog: WorkLog)

    @Delete
    suspend fun deleteWorkLog(workLog: WorkLog)

    @Query("DELETE FROM work_logs WHERE id = :id")
    suspend fun deleteWorkLogById(id: Long)

    @Query("DELETE FROM work_logs")
    suspend fun clearAll()

    @Query("SELECT COUNT(*) FROM work_logs WHERE strftime('%Y-%m', date / 1000, 'unixepoch') = :monthYear AND workType = :workType")
    suspend fun countByType(monthYear: String, workType: WorkType): Int

    @Query("SELECT COUNT(*) FROM work_logs WHERE strftime('%Y-%m', date / 1000, 'unixepoch') = :monthYear AND workType = :workType")
    fun countByTypeFlow(monthYear: String, workType: WorkType): Flow<Int>

    @Query("SELECT * FROM work_logs WHERE strftime('%Y-%m', date / 1000, 'unixepoch') = :monthYear")
    suspend fun getWorkLogsByMonth(monthYear: String): List<WorkLog>

    @Query("""
        SELECT COALESCE(SUM(
            CAST(
                (strftime('%s', '2000-01-01 ' || endTime || ':00') - strftime('%s', '2000-01-01 ' || startTime || ':00'))
            AS REAL) / 3600.0
        ), 0) FROM work_logs 
        WHERE strftime('%Y-%m', date / 1000, 'unixepoch') = :monthYear 
        AND workType = :workType 
        AND startTime IS NOT NULL 
        AND endTime IS NOT NULL
    """)
    suspend fun getTotalExtraHours(monthYear: String, workType: WorkType = WorkType.EXTRA_WORK): Double

    @Query("""
        SELECT COALESCE(SUM(
            CAST(
                (strftime('%s', '2000-01-01 ' || endTime || ':00') - strftime('%s', '2000-01-01 ' || startTime || ':00'))
            AS REAL) / 3600.0
        ), 0) FROM work_logs 
        WHERE strftime('%Y-%m', date / 1000, 'unixepoch') = :monthYear 
        AND workType = :workType 
        AND startTime IS NOT NULL 
        AND endTime IS NOT NULL
    """)
    fun getTotalExtraHoursFlow(monthYear: String, workType: WorkType = WorkType.EXTRA_WORK): Flow<Double>

    @Query("SELECT * FROM work_logs WHERE isOvertime = 1 ORDER BY date DESC")
    fun getOvertimeLogs(): Flow<List<WorkLog>>

    @Query("SELECT * FROM work_logs WHERE isOvertime = 1 AND strftime('%Y-%m', date / 1000, 'unixepoch') = :monthYear ORDER BY date DESC")
    fun getOvertimeLogsByMonth(monthYear: String): Flow<List<WorkLog>>

    @Query("SELECT * FROM work_logs WHERE isOvertime = 1 AND strftime('%Y', date / 1000, 'unixepoch') = :year ORDER BY date DESC")
    fun getOvertimeLogsByYear(year: String): Flow<List<WorkLog>>

    @Query("SELECT * FROM work_logs ORDER BY date DESC LIMIT 5")
    fun getRecentWorkLogs(): Flow<List<WorkLog>>

    @Query("SELECT * FROM work_logs WHERE id = :id")
    fun getWorkLogById(id: Long): Flow<WorkLog?>

    @Query("SELECT * FROM work_logs WHERE date >= :startOfDay AND date < :endOfDay")
    fun getTodayWorkLog(startOfDay: Long, endOfDay: Long): Flow<WorkLog?>

    @Query("SELECT * FROM work_logs ORDER BY date DESC LIMIT :pageSize OFFSET :offset")
    fun getPaginatedWorkLogs(offset: Int, pageSize: Int): Flow<List<WorkLog>>
}
