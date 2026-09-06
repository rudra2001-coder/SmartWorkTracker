package com.rudra.smartworktracker.ui.screens.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.rudra.smartworktracker.data.AppDatabase
import com.rudra.smartworktracker.model.WorkLog
import com.rudra.smartworktracker.model.WorkType
import com.rudra.smartworktracker.data.repository.WorkLogRepository
import com.rudra.smartworktracker.ui.WorkLogUi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.util.Date
import java.util.Locale

class CalendarViewModel(private val repository: WorkLogRepository) : ViewModel() {

    private val _selectedDate = MutableStateFlow<LocalDate>(LocalDate.now())
    private val _workLogs = MutableStateFlow<List<WorkLogUi>>(emptyList())
    private val _selectedWorkLog = MutableStateFlow<WorkLogUi?>(null)
    private val _multiSelectMode = MutableStateFlow(false)
    private val _multiSelectedDates = MutableStateFlow<List<LocalDate>>(emptyList())
    private val _activeFilters = MutableStateFlow<List<WorkType>>(emptyList())
    private val _searchQuery = MutableStateFlow("")
    private val _monthlyStats = MutableStateFlow(MonthlyStats())
    private val _currentMonth = MutableStateFlow(YearMonth.now())

    val uiState: StateFlow<CalendarUiState> = combine(
        _selectedDate,
        _workLogs,
        _selectedWorkLog,
        _multiSelectMode,
        _multiSelectedDates,
        _activeFilters,
        _searchQuery,
        _monthlyStats,
        _currentMonth
    ) { values ->
        val selectedDate = values[0] as LocalDate
        val workLogs = values[1] as List<WorkLogUi>
        val selectedWorkLog = values[2] as WorkLogUi?
        val multiSelectMode = values[3] as Boolean
        @Suppress("UNCHECKED_CAST")
        val multiSelectedDates = values[4] as List<LocalDate>
        @Suppress("UNCHECKED_CAST")
        val activeFilters = values[5] as List<WorkType>
        val searchQuery = values[6] as String
        val monthlyStats = values[7] as MonthlyStats
        val currentMonth = values[8] as YearMonth

        val filteredWorkLogs = workLogs.filter { workLog ->
            val matchesFilter = activeFilters.isEmpty() ||
                    workLog.workType in activeFilters
            val matchesSearch = searchQuery.isEmpty() ||
                    workLog.formattedDate.contains(searchQuery, ignoreCase = true)
            matchesFilter && matchesSearch
        }

        CalendarUiState(
            selectedDate = selectedDate,
            workLogs = workLogs,
            filteredWorkLogs = filteredWorkLogs,
            selectedWorkLog = selectedWorkLog,
            isMultiSelectMode = multiSelectMode,
            multiSelectedDates = multiSelectedDates,
            activeFilters = activeFilters,
            searchQuery = searchQuery,
            monthlyStats = monthlyStats,
            currentMonth = currentMonth
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

    fun onMonthChanged(yearMonth: YearMonth) {
        _currentMonth.value = yearMonth
        updateMonthlyStats()
    }

    fun navigateMonth(delta: Int) {
        onMonthChanged(_currentMonth.value.plusMonths(delta.toLong()))
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

    fun onDateLongPress(date: LocalDate) {
        if (!_multiSelectMode.value) {
            toggleMultiSelectMode()
        }
        onDateSelected(date)
    }

    fun onQuickMonthSelect(yearMonth: YearMonth) {
        onMonthChanged(yearMonth)
    }

    fun toggleMultiSelectMode() {
        _multiSelectMode.value = !_multiSelectMode.value
        _multiSelectedDates.value = emptyList()
    }

    fun selectAllDatesInMonth(yearMonth: YearMonth) {
        val daysInMonth = yearMonth.lengthOfMonth()
        val allDates = (1..daysInMonth).map { yearMonth.atDay(it) }
        _multiSelectedDates.value = allDates
    }

    fun markSelectedDates(workType: WorkType) {
        viewModelScope.launch {
            _multiSelectedDates.value.forEach { date ->
                updateWorkLog(date, workType, isMultiSelect = true)
            }
            toggleMultiSelectMode()
        }
    }

    fun toggleFilter() {
        if (_activeFilters.value.isEmpty()) {
            _activeFilters.value = WorkType.entries.toList()
        } else {
            _activeFilters.value = emptyList()
        }
    }

    fun addFilter(workType: WorkType) {
        if (workType !in _activeFilters.value) {
            _activeFilters.value = _activeFilters.value + workType
        }
    }

    fun removeFilter(workType: WorkType) {
        _activeFilters.value = _activeFilters.value - workType
    }

    fun clearFilters() {
        _activeFilters.value = emptyList()
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun copyWorkLog(workLog: WorkLogUi) {
        viewModelScope.launch {
            val newWorkLog = WorkLog(
                date = Date.from(workLog.date.toInstant()),
                workType = workLog.workType,
                startTime = workLog.startTime,
                endTime = workLog.endTime
            )
            repository.insertWorkLog(newWorkLog)
        }
    }

    fun shareWorkLog(workLog: WorkLogUi) {
        viewModelScope.launch {
            _shareWorkLog.emit(workLog)
        }
    }

    fun saveAsTemplate(workLog: WorkLogUi) {
        viewModelScope.launch {
            _templateWorkLog.emit(workLog)
        }
    }

    private val _shareWorkLog = MutableSharedFlow<WorkLogUi>()
    val shareWorkLog: SharedFlow<WorkLogUi> = _shareWorkLog.asSharedFlow()

    private val _templateWorkLog = MutableSharedFlow<WorkLogUi>()
    val templateWorkLog: SharedFlow<WorkLogUi> = _templateWorkLog.asSharedFlow()

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
                    endTime = existingWorkLog.endTime,
                    isOvertime = workType == WorkType.OVERTIME,
                    overtimeRate = if (workType == WorkType.OVERTIME) 1.5 else null
                )
                repository.updateWorkLog(updatedWorkLog)
            } else {
                val workLog = WorkLog(
                    date = Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant()),
                    workType = workType,
                    startTime = "09:00",
                    endTime = "17:00",
                    isOvertime = workType == WorkType.OVERTIME,
                    overtimeRate = if (workType == WorkType.OVERTIME) 1.5 else null
                )
                repository.insertWorkLog(workLog)
            }
        }
    }

    fun quickAddWorkLog(date: LocalDate, workType: WorkType) {
        updateWorkLog(date, workType, isMultiSelect = false)
    }

    fun deleteWorkLog(id: Long) {
        viewModelScope.launch {
            repository.deleteWorkLogById(id)
        }
    }

    private fun updateMonthlyStats() {
        viewModelScope.launch {
            val yearMonth = _currentMonth.value
            val monthWorkLogs = _workLogs.value.filter { workLog ->
                val date = workLog.date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
                YearMonth.from(date) == yearMonth
            }

            val stats = MonthlyStats(
                officeDays = monthWorkLogs.count { it.workType == WorkType.OFFICE },
                homeDays = monthWorkLogs.count { it.workType == WorkType.HOME_OFFICE },
                offDays = monthWorkLogs.count { it.workType == WorkType.OFF_DAY },
                extraDays = monthWorkLogs.count { it.workType == WorkType.EXTRA_WORK },
                totalHours = calculateTotalHours(monthWorkLogs)
            )

            _monthlyStats.value = stats
        }
    }

    private fun calculateTotalHours(workLogs: List<WorkLogUi>): String {
        var totalMinutes = 0
        workLogs.forEach { workLog ->
            if (workLog.startTime != null && workLog.endTime != null) {
                try {
                    val startParts = workLog.startTime.split(":")
                    val endParts = workLog.endTime.split(":")
                    val startHour = startParts[0].toInt()
                    val startMinute = startParts[1].toInt()
                    val endHour = endParts[0].toInt()
                    val endMinute = endParts[1].toInt()

                    totalMinutes += (endHour * 60 + endMinute) - (startHour * 60 + startMinute)
                } catch (e: Exception) {
                    totalMinutes += 8 * 60
                }
            } else {
                totalMinutes += 8 * 60
            }
        }

        val hours = totalMinutes / 60
        val minutes = totalMinutes % 60

        return if (minutes > 0) "${hours}h ${minutes}m" else "${hours}h"
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
                _selectedWorkLog.value = _workLogs.value.find {
                    it.date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate() == _selectedDate.value
                }
                updateMonthlyStats()
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
            "8h"
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

data class CalendarUiState(
    val selectedDate: LocalDate = LocalDate.now(),
    val workLogs: List<WorkLogUi> = emptyList(),
    val filteredWorkLogs: List<WorkLogUi> = emptyList(),
    val selectedWorkLog: WorkLogUi? = null,
    val isMultiSelectMode: Boolean = false,
    val multiSelectedDates: List<LocalDate> = emptyList(),
    val activeFilters: List<WorkType> = emptyList(),
    val searchQuery: String = "",
    val monthlyStats: MonthlyStats = MonthlyStats(),
    val currentMonth: YearMonth = YearMonth.now()
)
