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

    private val _selectedDate = MutableStateFlow<LocalDate>(LocalDate.now())
    private val _workLogs = MutableStateFlow<List<WorkLogUi>>(emptyList())
    private val _selectedWorkLog = MutableStateFlow<WorkLogUi?>(null)
    private val _multiSelectMode = MutableStateFlow(false)
    private val _multiSelectedDates = MutableStateFlow<List<LocalDate>>(emptyList())

    val uiState: StateFlow<CalendarUiState> = combine(
        _selectedDate,
        _workLogs,
        _selectedWorkLog,
        _multiSelectMode,
        _multiSelectedDates
    ) { selectedDate, workLogs, selectedWorkLog, multiSelectMode, multiSelectedDates ->
        CalendarUiState(
            selectedDate = selectedDate,
            workLogs = workLogs,
            selectedWorkLog = selectedWorkLog,
            isMultiSelectMode = multiSelectMode,
            multiSelectedDates = multiSelectedDates
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
        if (_multiSelectMode.value) {
            val currentDates = _multiSelectedDates.value.toMutableList()
            if (currentDates.contains(date)) {
                currentDates.remove(date)
            } else {
                currentDates.add(date)
            }
            _multiSelectedDates.value = currentDates
        } else {
            _selectedDate.value = date
        }
    }

    fun toggleMultiSelectMode() {
        _multiSelectMode.value = !_multiSelectMode.value
        _multiSelectedDates.value = emptyList() // Clear selection on toggle
    }

    fun markSelectedDates(workType: WorkType) {
        viewModelScope.launch {
            _multiSelectedDates.value.forEach { date ->
                updateWorkLog(date, workType, isMultiSelect = true)
            }
            toggleMultiSelectMode() // Exit multi-select mode after marking
        }
    }

    private fun observeSelectedDate() {
        viewModelScope.launch {
            _selectedDate.collect { date ->
                _selectedWorkLog.value = _workLogs.value.find {
                    it.date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate() == date
                }
            }
        }
    }

    fun updateWorkLog(date: LocalDate, workType: WorkType, isMultiSelect: Boolean = false) {
        viewModelScope.launch {
            val existingWorkLog = _workLogs.value.find {
                it.date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate() == date
            }

            if (existingWorkLog != null) {
                val updatedWorkLog = WorkLog(
                    id = existingWorkLog.id,
                    date = existingWorkLog.date,
                    workType = workType,
                    startTime = existingWorkLog.startTime,
                    endTime = existingWorkLog.endTime
                )
                repository.updateWorkLog(updatedWorkLog)
            } else {
                val workLog = WorkLog(
                    date = Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant()),
                    workType = workType,
                    startTime = "09:00",
                    endTime = "17:00"
                )
                repository.insertWorkLog(workLog)
            }
        }
    }

    fun deleteWorkLog(id: Long) {
        viewModelScope.launch {
            repository.deleteWorkLogById(id)
        }
    }

    private fun loadWorkLogs() {
        viewModelScope.launch {
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
                // Refresh single-day selection as well
                _selectedWorkLog.value = _workLogs.value.find {
                    it.date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate() == _selectedDate.value
                }
            }
        }
    }

    private fun formatDate(date: Date): String {
        return SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(date)
    }

    private fun calculateDuration(startTime: String?, endTime: String?): String {
        if (startTime == null || endTime == null) return "8h"
        return try {
            val startParts = startTime.split(":")
            val endParts = endTime.split(":")
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
