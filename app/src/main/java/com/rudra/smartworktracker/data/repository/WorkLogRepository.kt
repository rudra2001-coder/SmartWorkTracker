package com.rudra.smartworktracker.data.repository

import com.rudra.smartworktracker.data.dao.WorkLogDao
import com.rudra.smartworktracker.data.entity.WorkLog
import com.rudra.smartworktracker.data.entity.WorkType
import com.rudra.smartworktracker.ui.MonthlyStats
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.text.SimpleDateFormat
import java.util.*

open class WorkLogRepository(private val workLogDao: WorkLogDao) {

    open fun getTodayWorkLog(): Flow<WorkLog?> = flow {
        emit(workLogDao.getWorkLogByDate(Date()))
    }

    open fun getMonthlyStats(): Flow<MonthlyStats> = flow {
        val calendar = Calendar.getInstance()
        val monthYear = SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(calendar.time)
        val officeDays = workLogDao.countByType(monthYear, WorkType.OFFICE)
        val homeOfficeDays = workLogDao.countByType(monthYear, WorkType.HOME_OFFICE)
        val offDays = workLogDao.countByType(monthYear, WorkType.OFF_DAY)
        val extraHours = workLogDao.getTotalExtraHours(monthYear, WorkType.EXTRA_WORK)
        emit(MonthlyStats(officeDays, homeOfficeDays, offDays, extraHours))
    }

    open fun getRecentActivities(): Flow<List<WorkLog>> = workLogDao.getAllWorkLogs()

    open suspend fun insertWorkLog(workLog: WorkLog) {
        workLogDao.insertWorkLog(workLog)
    }

    open fun getAllWorkLogs(): Flow<List<WorkLog>> = workLogDao.getAllWorkLogs()

    open suspend fun clearAllWorkLogs() {
        workLogDao.clearAll()
    }

    fun deleteWorkLog(workLog: com.rudra.smartworktracker.data.entity.WorkLog) {}
}
