package com.rudra.smartworktracker.data.repository

import com.rudra.smartworktracker.data.dao.WorkLogDao
import com.rudra.smartworktracker.data.entity.WorkLog
import com.rudra.smartworktracker.data.entity.WorkType
import com.rudra.smartworktracker.ui.MonthlyStats
import com.rudra.smartworktracker.ui.WorkLogUi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class WorkLogRepository @Inject constructor(
    private val workLogDao: WorkLogDao
) {

    fun getTodayWorkLog(): Flow<WorkLog?> = flow {
        emit(workLogDao.getWorkLogByDate(Date()))
    }

    fun getMonthlyStats(): Flow<MonthlyStats> = flow {
        val calendar = Calendar.getInstance()
        val monthYear = SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(calendar.time)
        val officeDays = workLogDao.countByType(monthYear, WorkType.OFFICE)
        val homeOfficeDays = workLogDao.countByType(monthYear, WorkType.HOME_OFFICE)
        val offDays = workLogDao.countByType(monthYear, WorkType.OFF_DAY)
        val extraHours = workLogDao.getTotalExtraHours(monthYear, WorkType.EXTRA_WORK)
        emit(MonthlyStats(officeDays, homeOfficeDays, offDays, extraHours))
    }

    fun getRecentActivities(): Flow<List<WorkLog>> = workLogDao.getAllWorkLogs()

    suspend fun insertWorkLog(workLog: WorkLog) {
        workLogDao.insertWorkLog(workLog)
    }

    fun getAllWorkLogs(): Flow<List<WorkLog>> = workLogDao.getAllWorkLogs()

    suspend fun clearAllWorkLogs() {
        workLogDao.clearAll()
    }
}