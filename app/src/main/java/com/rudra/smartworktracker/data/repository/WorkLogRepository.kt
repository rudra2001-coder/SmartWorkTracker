package com.rudra.smartworktracker.data.repository

import com.rudra.smartworktracker.data.dao.WorkLogDao
import com.rudra.smartworktracker.model.WorkLog
import com.rudra.smartworktracker.model.WorkType
import com.rudra.smartworktracker.ui.MonthlyStats
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.ZoneId
import java.util.Date

class WorkLogRepository(private val workLogDao: WorkLogDao) {

    fun getTodayWorkLog(): Flow<WorkLog?> {
        val today = LocalDate.now()
        val startOfDay = Date.from(today.atStartOfDay(ZoneId.systemDefault()).toInstant()).time
        val endOfDay = Date.from(today.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant()).time
        return workLogDao.getTodayWorkLog(startOfDay, endOfDay)
    }

    fun getMonthlyStats(): Flow<MonthlyStats> {
        val now = LocalDate.now()
        val startOfMonth = Date.from(now.withDayOfMonth(1).atStartOfDay(ZoneId.systemDefault()).toInstant()).time
        val endOfMonth = Date.from(now.plusMonths(1).withDayOfMonth(1).atStartOfDay(ZoneId.systemDefault()).toInstant()).time
        return combine(
            listOf(
                workLogDao.countByTypeInRange(startOfMonth, endOfMonth, WorkType.OFFICE).map { it.toDouble() },
                workLogDao.countByTypeInRange(startOfMonth, endOfMonth, WorkType.HOME_OFFICE).map { it.toDouble() },
                workLogDao.countByTypeInRange(startOfMonth, endOfMonth, WorkType.OFF_DAY).map { it.toDouble() },
                workLogDao.countByTypeInRange(startOfMonth, endOfMonth, WorkType.EXTRA_WORK).map { it.toDouble() },
                workLogDao.countByTypeInRange(startOfMonth, endOfMonth, WorkType.OVERTIME).map { it.toDouble() },
                workLogDao.getTotalExtraHoursInRange(startOfMonth, endOfMonth),
                workLogDao.getTotalExtraHoursInRange(startOfMonth, endOfMonth, WorkType.OVERTIME)
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

    suspend fun getWorkLogsInRange(startOfMonth: Long, endOfMonth: Long): List<WorkLog> {
        return workLogDao.getWorkLogsInRange(startOfMonth, endOfMonth)
    }

    fun getOvertimeLogsInRange(startOfMonth: Long, endOfMonth: Long): Flow<List<WorkLog>> {
        return workLogDao.getOvertimeLogsInRange(startOfMonth, endOfMonth)
    }

    fun getOvertimeLogsByMonth(monthYear: String): Flow<List<WorkLog>> {
        return workLogDao.getOvertimeLogsByMonth(monthYear)
    }

    fun getOvertimeLogsInYearRange(startOfYear: Long, endOfYear: Long): Flow<List<WorkLog>> {
        return workLogDao.getOvertimeLogsInYearRange(startOfYear, endOfYear)
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
