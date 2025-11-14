package com.rudra.smartworktracker.ui.screens.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.rudra.smartworktracker.data.AppDatabase
import com.rudra.smartworktracker.model.WorkLog
import com.rudra.smartworktracker.model.WorkType
import com.rudra.smartworktracker.data.repository.WorkLogRepository
import com.rudra.smartworktracker.ui.CalendarUiState
import com.rudra.smartworktracker.ui.WorkLogUi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.ZoneId
import java.util.Date
import java.util.Locale
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.toDuration

class CalendarViewModel(private val repository: WorkLogRepository) : ViewModel() {

    private val _selectedDate = MutableStateFlow<LocalDate>(LocalDate.now())
    private val _workLogs = MutableStateFlow<List<WorkLogUi>>(emptyList())
    private val _selectedWorkLog = MutableStateFlow<WorkLogUi?>(null)
    private val _isLoading = MutableStateFlow(false)
    private val _errorMessage = MutableStateFlow<String?>(null)

    val uiState: StateFlow<CalendarUiState> = combine(
        _selectedDate,
        _workLogs,
        _selectedWorkLog,
        _isLoading,
        _errorMessage
    ) { selectedDate, workLogs, selectedWorkLog, isLoading, errorMessage ->
        CalendarUiState(
            selectedDate = selectedDate,
            workLogs = workLogs,
            selectedWorkLog = selectedWorkLog,
            isLoading = isLoading,
            errorMessage = errorMessage
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = CalendarUiState()
    )

    init {
        loadWorkLogs()
        observeSelectedDate()
    }

    fun onDateSelected(date: LocalDate) {
        _selectedDate.value = date
        _errorMessage.value = null // Clear error when user interacts
    }

    private fun observeSelectedDate() {
        viewModelScope.launch {
            _selectedDate.collect { date ->
                _selectedWorkLog.value = _workLogs.value.find { workLog ->
                    workLog.date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate() == date
                }
            }
        }
    }

    fun updateWorkLog(date: LocalDate, workType: WorkType) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _errorMessage.value = null

                val existingWorkLog = _workLogs.value.find {
                    it.date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate() == date
                }

                if (existingWorkLog != null) {
                    // Update existing work log
                    val updatedWorkLog = WorkLog(
                        id = existingWorkLog.id,
                        date = existingWorkLog.date,
                        workType = workType,
                        startTime = existingWorkLog.startTime,
                        endTime = existingWorkLog.endTime
                    )
                    repository.updateWorkLog(updatedWorkLog)
                } else {
                    // Create new work log with default times
                    val workLog = WorkLog(
                        date = Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant()),
                        workType = workType,
                        startTime = getDefaultStartTime(workType),
                        endTime = getDefaultEndTime(workType)
                    )
                    repository.insertWorkLog(workLog)
                }

                // Show success feedback or update UI state if needed
            } catch (e: Exception) {
                _errorMessage.value = "Failed to update work log: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteWorkLog(id: Long) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _errorMessage.value = null

                repository.deleteWorkLogById(id)

                // Clear selection if the deleted work log was selected
                _selectedWorkLog.value?.let { selected ->
                    if (selected.id == id) {
                        _selectedWorkLog.value = null
                    }
                }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to delete work log: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }

    fun refreshData() {
        loadWorkLogs()
    }

    private fun loadWorkLogs() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _errorMessage.value = null

                repository.getAllWorkLogs().collect { workLogs ->
                    _workLogs.value = workLogs.map { workLog ->
                        WorkLogUi(
                            id = workLog.id,
                            date = workLog.date,
                            workType = workLog.workType,
                            formattedDate = formatDate(workLog.date),
                            duration = calculateDuration(workLog.startTime, workLog.endTime),
                            startTime = workLog.startTime,
                            endTime = workLog.endTime
                        )
                    }

                    // Update selected work log after loading
                    _selectedWorkLog.value = _workLogs.value.find {
                        it.date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate() == _selectedDate.value
                    }
                }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to load work logs: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun getDefaultStartTime(workType: WorkType): String {
        return when (workType) {
            WorkType.OFF_DAY -> ""
            else -> "09:00"
        }
    }

    private fun getDefaultEndTime(workType: WorkType): String {
        return when (workType) {
            WorkType.OFF_DAY -> ""
            else -> "17:00"
        }
    }

    private fun formatDate(date: Date): String {
        return SimpleDateFormat("EEE, MMM dd, yyyy", Locale.getDefault()).format(date)
    }

    private fun calculateDuration(startTime: String?, endTime: String?): String {
        if (startTime.isNullOrEmpty() || endTime.isNullOrEmpty()) return "N/A"

        return try {
            val startParts = startTime.split(":")
            val endParts = endTime.split(":")

            if (startParts.size != 2 || endParts.size != 2) return "N/A"

            val startHour = startParts[0].toInt()
            val startMinute = startParts[1].toInt()
            val endHour = endParts[0].toInt()
            val endMinute = endParts[1].toInt()

            var totalMinutes = (endHour * 60 + endMinute) - (startHour * 60 + startMinute)

            // Handle overnight shifts (end time before start time)
            if (totalMinutes < 0) {
                totalMinutes += 24 * 60
            }

            val hours = totalMinutes / 60
            val minutes = totalMinutes % 60

            when {
                hours > 0 && minutes > 0 -> "${hours}h ${minutes}m"
                hours > 0 -> "${hours}h"
                minutes > 0 -> "${minutes}m"
                else -> "0h"
            }
        } catch (e: Exception) {
            "N/A"
        }
    }

    // Helper method to get work logs for a specific month
    fun getWorkLogsForMonth(yearMonth: java.time.YearMonth): List<WorkLogUi> {
        return _workLogs.value.filter { workLog ->
            val localDate = workLog.date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
            localDate.year == yearMonth.year && localDate.month == yearMonth.month
        }
    }

    // Helper method to check if a date has a work log
    fun hasWorkLog(date: LocalDate): Boolean {
        return _workLogs.value.any { workLog ->
            workLog.date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate() == date
        }
    }

    // Get work type for a specific date
    fun getWorkTypeForDate(date: LocalDate): WorkType? {
        return _workLogs.value.find { workLog ->
            workLog.date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate() == date
        }?.workType
    }

    companion object {
        fun factory(appDatabase: AppDatabase): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    if (modelClass.isAssignableFrom(CalendarViewModel::class.java)) {
                        val workLogRepository = WorkLogRepository(appDatabase.workLogDao())
                        return CalendarViewModel(workLogRepository) as T
                    }
                    throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
                }
            }
        }
    }
}