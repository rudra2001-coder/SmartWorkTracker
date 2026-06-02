package com.rudra.smartworktracker.data.repository

import com.rudra.smartworktracker.data.dao.WorkLogDao
import com.rudra.smartworktracker.model.WorkLog
import com.rudra.smartworktracker.model.WorkType
import com.rudra.smartworktracker.ui.MonthlyStats
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.ZoneId
import java.util.Calendar
import java.util.Date
import java.util.Locale

class WorkLogRepository(private val workLogDao: WorkLogDao) {

    fun getTodayWorkLog(): Flow<WorkLog?> {
        val today = LocalDate.now()
        val startOfDay = Date.from(today.atStartOfDay(ZoneId.systemDefault()).toInstant()).time
        val endOfDay = Date.from(today.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant()).time
        return workLogDao.getTodayWorkLog(startOfDay, endOfDay)
    }

    fun getMonthlyStats(): Flow<MonthlyStats> {
        val monthYear = SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(Calendar.getInstance().time)
        return combine(
            listOf(
                workLogDao.countByTypeFlow(monthYear, WorkType.OFFICE).map { it.toDouble() },
                workLogDao.countByTypeFlow(monthYear, WorkType.HOME_OFFICE).map { it.toDouble() },
                workLogDao.countByTypeFlow(monthYear, WorkType.OFF_DAY).map { it.toDouble() },
                workLogDao.countByTypeFlow(monthYear, WorkType.EXTRA_WORK).map { it.toDouble() },
                workLogDao.countByTypeFlow(monthYear, WorkType.OVERTIME).map { it.toDouble() },
                workLogDao.getTotalExtraHoursFlow(monthYear),
                workLogDao.getTotalExtraHoursFlow(monthYear, WorkType.OVERTIME)
            )
        ) { array ->
            val officeDays = array[0].toInt()
            val homeOfficeDays = array[1].toInt()
            val offDays = array[2].toInt()
            val extraWorkDays = array[3].toInt()
            val overtimeDays = array[4].toInt()
            val extraHours = array[5]
            val overtimeHours = array[6]
            MonthlyStats(
                officeDays = officeDays,
                homeOfficeDays = homeOfficeDays,
                offDays = offDays,
                extraHours = extraHours + overtimeHours,
                totalWorkDays = officeDays + homeOfficeDays + extraWorkDays + overtimeDays
            )
        }
    }

    fun getRecentActivities(): Flow<List<WorkLog>> {
        return workLogDao.getRecentWorkLogs()
    }

    fun getOvertimeLogs(): Flow<List<WorkLog>> {
        return workLogDao.getOvertimeLogs()
    }

    fun getOvertimeLogsByMonth(monthYear: String): Flow<List<WorkLog>> {
        return workLogDao.getOvertimeLogsByMonth(monthYear)
    }

    fun getOvertimeLogsByYear(year: String): Flow<List<WorkLog>> {
        return workLogDao.getOvertimeLogsByYear(year)
    }

    suspend fun insertWorkLog(workLog: WorkLog) {
        workLogDao.insertWorkLog(workLog)
    }

    suspend fun updateWorkLog(workLog: WorkLog) {
        workLogDao.updateWorkLog(workLog)
    }

    fun getAllWorkLogs(): Flow<List<WorkLog>> {
        return workLogDao.getAllWorkLogs()
    }

    fun getWorkLogs(page: Int, pageSize: Int): Flow<List<WorkLog>> {
        val offset = (page - 1) * pageSize
        return workLogDao.getPaginatedWorkLogs(offset, pageSize)
    }

    suspend fun deleteWorkLog(workLog: WorkLog) {
        workLogDao.deleteWorkLog(workLog)
    }

    suspend fun deleteWorkLogById(id: Long) {
        workLogDao.deleteWorkLogById(id)
    }

    fun getWorkLogById(id: Long): Flow<WorkLog?> {
        return workLogDao.getWorkLogById(id)
    }

    suspend fun clearAll() {
        workLogDao.clearAll()
    }
}
