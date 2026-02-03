package com.rudra.smartworktracker.data.repository

import com.rudra.smartworktracker.data.dao.WorkLogDao
import com.rudra.smartworktracker.model.WorkLog
import com.rudra.smartworktracker.model.WorkType
import com.rudra.smartworktracker.ui.MonthlyStats
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class WorkLogRepository(private val workLogDao: WorkLogDao) {

    fun getTodayWorkLog(): Flow<WorkLog?> {
        return workLogDao.getTodayWorkLog()
    }

    fun getMonthlyStats(): Flow<MonthlyStats> = flow {
        val monthYear = SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(Calendar.getInstance().time)
        val officeDays = workLogDao.countByType(monthYear, WorkType.OFFICE)
        val homeOfficeDays = workLogDao.countByType(monthYear, WorkType.HOME_OFFICE)
        val offDays = workLogDao.countByType(monthYear, WorkType.OFF_DAY)
        val extraHours = workLogDao.getTotalExtraHours(monthYear) ?: 0.0
        emit(
            MonthlyStats(
                officeDays = officeDays,
                homeOfficeDays = homeOfficeDays,
                offDays = offDays,
                extraHours = extraHours,
                totalWorkDays = officeDays + homeOfficeDays
            )
        )
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
