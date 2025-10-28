package com.rudra.smartworktracker.ui.screens.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rudra.smartworktracker.data.entity.WorkLog
import com.rudra.smartworktracker.data.entity.WorkType
import com.rudra.smartworktracker.data.repository.WorkLogRepository
import com.rudra.smartworktracker.ui.CalendarUiState
import com.rudra.smartworktracker.ui.WorkLogUi
import dagger.hilt.android.lifecycle.HiltViewModel
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
import javax.inject.Inject

@HiltViewModel
class CalendarViewModel @Inject constructor(
    private val repository: WorkLogRepository
) : ViewModel() {

    private val _selectedDate = MutableStateFlow<LocalDate?>(null)
    private val _workLogs = MutableStateFlow<List<WorkLogUi>>(emptyList())

    val uiState: StateFlow<CalendarUiState> = combine(
        _selectedDate,
        _workLogs
    ) { selectedDate, workLogs ->
        CalendarUiState(
            selectedDate = selectedDate,
            workLogs = workLogs
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
        _selectedDate.value = date
    }

    fun markSelectedDate(workType: WorkType) {
        _selectedDate.value?.let { date ->
            markDateWithWorkType(date, workType)
        }
    }

    fun markDateWithWorkType(date: LocalDate, workType: WorkType) {
        viewModelScope.launch {
            val workLog = WorkLog(
                date = Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant()),
                workType = workType,
                startTime = "09:00",
                endTime = "17:00"
            )
            repository.insertWorkLog(workLog)
            loadWorkLogs() // Refresh the list
        }
    }

    private fun loadWorkLogs() {
        viewModelScope.launch {
            repository.getAllWorkLogs().collect { workLogs ->
                _workLogs.value = workLogs.map { workLog ->
                    val format = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    WorkLogUi(
                        id = workLog.id,
                        date = workLog.date,
                        workType = workLog.workType,
                        formattedDate = format.format(workLog.date),
                        duration = "8h",
                        startTime = workLog.startTime,
                        endTime = workLog.endTime
                    )
                }
            }
        }
    }
}
