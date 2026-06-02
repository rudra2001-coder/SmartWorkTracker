package com.rudra.smartworktracker.ui.screens.overtime

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.rudra.smartworktracker.data.AppDatabase
import com.rudra.smartworktracker.data.repository.WorkLogRepository
import com.rudra.smartworktracker.model.WorkLog
import com.rudra.smartworktracker.model.WorkType
import com.rudra.smartworktracker.utils.DateTimeUtils
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.ZoneId
import java.util.*

class OvertimeViewModel(application: Application) : AndroidViewModel(application) {

    private val workLogRepository: WorkLogRepository

    private val _monthlyOvertimeLogs = MutableStateFlow<List<WorkLog>>(emptyList())
    val monthlyOvertimeLogs: StateFlow<List<WorkLog>> = _monthlyOvertimeLogs.asStateFlow()

    private val _yearlyOvertimeLogs = MutableStateFlow<List<WorkLog>>(emptyList())
    val yearlyOvertimeLogs: StateFlow<List<WorkLog>> = _yearlyOvertimeLogs.asStateFlow()

    val monthlySummary: Flow<OvertimeSummary> = monthlyOvertimeLogs.map { logs -> calculateSummary(logs) }
    val yearlySummary: Flow<OvertimeSummary> = yearlyOvertimeLogs.map { logs -> calculateSummary(logs) }

    var selectedTab by mutableStateOf(0)

    init {
        val workLogDao = AppDatabase.getDatabase(application).workLogDao()
        workLogRepository = WorkLogRepository(workLogDao)
        loadOvertimeData()
    }

    private fun loadOvertimeData() {
        val now = LocalDate.now()
        val startOfMonth = Date.from(now.withDayOfMonth(1).atStartOfDay(ZoneId.systemDefault()).toInstant()).time
        val endOfMonth = Date.from(now.plusMonths(1).withDayOfMonth(1).atStartOfDay(ZoneId.systemDefault()).toInstant()).time
        val startOfYear = Date.from(now.withDayOfYear(1).atStartOfDay(ZoneId.systemDefault()).toInstant()).time
        val endOfYear = Date.from(now.plusYears(1).withDayOfYear(1).atStartOfDay(ZoneId.systemDefault()).toInstant()).time

        viewModelScope.launch {
            workLogRepository.getOvertimeLogsInRange(startOfMonth, endOfMonth).collect { _monthlyOvertimeLogs.value = it }
        }
        viewModelScope.launch {
            workLogRepository.getOvertimeLogsInYearRange(startOfYear, endOfYear).collect { _yearlyOvertimeLogs.value = it }
        }
    }

    fun saveOvertime(date: Date, startTime: String, endTime: String, overtimeRate: Double): Boolean {
        if (!isValidTime(startTime) || !isValidTime(endTime)) return false
        
        viewModelScope.launch {
            val workLog = WorkLog(
                date = date,
                workType = WorkType.OVERTIME,
                startTime = startTime,
                endTime = endTime,
                isOvertime = true,
                overtimeRate = overtimeRate
            )
            workLogRepository.insertWorkLog(workLog)
        }
        return true
    }

    private fun isValidTime(time: String): Boolean {
        val regex = Regex("^([01]?[0-9]|2[0-3]):[0-5][0-9]$")
        return regex.matches(time)
    }

    fun deleteOvertime(workLog: WorkLog) {
        viewModelScope.launch {
            workLogRepository.deleteWorkLog(workLog)
        }
    }

    fun calculateDuration(startTime: String?, endTime: String?): Double {
        if (startTime == null || endTime == null) return 0.0
        val startMillis = DateTimeUtils.parseTime(startTime)
        var endMillis = DateTimeUtils.parseTime(endTime)
        
        // Handle cross-midnight (e.g., 22:00 to 02:00)
        if (endMillis < startMillis) {
            endMillis += 24 * 60 * 60 * 1000 // Add 24 hours
        }
        
        return (endMillis - startMillis).toDouble() / (1000 * 60 * 60)
    }

    private fun calculateSummary(logs: List<WorkLog>): OvertimeSummary {
        var totalHours = 0.0
        var totalEarnings = 0.0
        logs.forEach { log ->
            val duration = calculateDuration(log.startTime, log.endTime)
            totalHours += duration
            totalEarnings += duration * (log.overtimeRate ?: 0.0)
        }
        return OvertimeSummary(totalHours, totalEarnings)
    }

    fun getMonthName() = SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(Date())
    fun getYearString() = SimpleDateFormat("yyyy", Locale.getDefault()).format(Date())
}

data class OvertimeSummary(val totalHours: Double = 0.0, val totalEarnings: Double = 0.0)
