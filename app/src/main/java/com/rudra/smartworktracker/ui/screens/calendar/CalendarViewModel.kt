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
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.ZoneId
import java.util.Date
import java.util.Locale

class CalendarViewModel(private val repository: WorkLogRepository) : ViewModel() {

    private val _selectedDate = MutableStateFlow<LocalDate?>(null)
    private val _workLogs = MutableStateFlow<List<WorkLogUi>>(emptyList())
    private val _errorMessage = MutableStateFlow<String?>(null)
    private val _isLoading = MutableStateFlow(false)

    val uiState: StateFlow<CalendarUiState> = combine(
        _selectedDate,
        _workLogs,
        _errorMessage,
        _isLoading
    ) { selectedDate, workLogs, errorMessage, isLoading ->
        CalendarUiState(
            selectedDate = selectedDate,
            workLogs = workLogs,
            errorMessage = errorMessage,
            isLoading = isLoading
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = CalendarUiState()
    )

    init {
        loadWorkLogs()
    }

    fun selectDate(date: LocalDate) {
        if (canSelectDate(date)) {
            _selectedDate.value = date
            _errorMessage.value = null
        } else {
            _errorMessage.value = "Cannot select future dates"
        }
    }

    fun markSelectedDate(workType: WorkType) {
        _selectedDate.value?.let { date ->
            updateWorkLog(date, workType)
        }
    }

    fun markDateWithWorkType(date: LocalDate, workType: WorkType) {
        updateWorkLog(date, workType)
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
                    repository.insertWorkLog(updatedWorkLog)
                } else {
                    // Create new work log
                    val workLog = WorkLog(
                        date = Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant()),
                        workType = workType,
                        startTime = "09:00",
                        endTime = "17:00"
                    )
                    repository.insertWorkLog(workLog)
                }
                loadWorkLogs()
            } catch (e: Exception) {
                _errorMessage.value = "Failed to save work log: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteWorkLog(date: LocalDate) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val workLog = _workLogs.value.find {
                    it.date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate() == date
                }
                workLog?.let {
                    repository.deleteWorkLog(WorkLog(it.id, it.date, it.workType, it.startTime, it.endTime))
                    loadWorkLogs()
                    _selectedDate.value = null
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

    private fun canSelectDate(date: LocalDate): Boolean {
        val today = LocalDate.now()
        return !date.isAfter(today)
    }

    private fun loadWorkLogs() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
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
                }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to load work logs: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun formatDate(date: Date): String {
        return SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(date)
    }

    private fun calculateDuration(startTime: String?, endTime: String?): String {
        return try {
            val startParts = startTime!!.split(":")
            val endParts = endTime!!.split(":")
            val startHour = startParts[0].toInt()
            val startMinute = startParts[1].toInt()
            val endHour = endParts[0].toInt()
            val endMinute = endParts[1].toInt()

            val totalMinutes = (endHour * 60 + endMinute) - (startHour * 60 + startMinute)
            val hours = totalMinutes / 60
            val minutes = totalMinutes % 60

            if (minutes > 0) "${hours}h ${minutes}m" else "${hours}h"
        } catch (e: Exception) {
            "8h" // Fallback
        }
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
                    throw IllegalArgumentException("Unknown ViewModel class")
                }
            }
        }
    }
}
