package com.rudra.smartworktracker.data.repository

import com.rudra.smartworktracker.data.dao.WorkLogDao
import com.rudra.smartworktracker.model.WorkLog
import com.rudra.smartworktracker.model.WorkType
import com.rudra.smartworktracker.ui.MonthlyStats
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class WorkLogRepository(private val workLogDao: WorkLogDao) {

    fun getTodayWorkLog(): Flow<WorkLog?> {
        val today = Calendar.getInstance()
        val startOfDay = today.apply { set(Calendar.HOUR_OF_DAY, 0) }.timeInMillis
        val endOfDay = today.apply { set(Calendar.HOUR_OF_DAY, 23) }.timeInMillis
        return workLogDao.getWorkLogByDate(startOfDay, endOfDay)
    }

    suspend fun getMonthlyStats(): MonthlyStats {
        val monthYear = SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(Calendar.getInstance().time)
        val officeDays = workLogDao.countByType(monthYear, WorkType.OFFICE)
        val homeOfficeDays = workLogDao.countByType(monthYear, WorkType.HOME_OFFICE)
        val offDays = workLogDao.countByType(monthYear, WorkType.OFF_DAY)
        val extraHours = workLogDao.getTotalExtraHours(monthYear) ?: 0.0
        return MonthlyStats(
            officeDays = officeDays,
            homeOfficeDays = homeOfficeDays,
            offDays = offDays,
            extraHours = extraHours,
            totalWorkDays = officeDays + homeOfficeDays
        )
    }

    fun getRecentActivities(): Flow<List<WorkLog>> {
        return workLogDao.getRecentWorkLogs()
    }

    suspend fun insertWorkLog(workLog: WorkLog) {
        workLogDao.insertWorkLog(workLog)
    }

    fun getAllWorkLogs(): Flow<List<WorkLog>> {
        return workLogDao.getAllWorkLogs()
    }

    suspend fun deleteWorkLog(workLog: WorkLog) {
        workLogDao.deleteWorkLog(workLog)
    }

    fun getWorkLogById(id: Long): Flow<WorkLog?> {
        return workLogDao.getWorkLogById(id)
    }
}
