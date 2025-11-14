package com.rudra.smartworktracker.ui.screens.report

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rudra.smartworktracker.data.repository.WorkLogRepository
import com.rudra.smartworktracker.model.WorkLog
import com.rudra.smartworktracker.model.WorkType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Locale

data class MonthlyReportUiState(
    val selectedMonth: String = "",
    val workLogs: List<WorkLog> = emptyList(),
    val officeCount: Int = 0,
    val homeCount: Int = 0,
    val offCount: Int = 0,
    val extraCount: Int = 0
)

class MonthlyReportViewModel(private val workLogRepository: WorkLogRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(MonthlyReportUiState())
    val uiState: StateFlow<MonthlyReportUiState> = _uiState.asStateFlow()

    private val calendar = Calendar.getInstance()
    val months = (0..11).map {
        calendar.set(Calendar.MONTH, it)
        calendar.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault())!!
    }

    init {
        val currentMonth = months[Calendar.getInstance().get(Calendar.MONTH)]
        onMonthSelected(currentMonth)
    }

    fun onMonthSelected(month: String) {
        viewModelScope.launch {
            val monthIndex = months.indexOf(month)
            workLogRepository.getAllWorkLogs().collect { allLogs ->
                val filteredLogs = allLogs.filter {
                    val logCalendar = Calendar.getInstance()
                    logCalendar.time = it.date
                    logCalendar.get(Calendar.MONTH) == monthIndex
                }
                _uiState.value = MonthlyReportUiState(
                    selectedMonth = month,
                    workLogs = filteredLogs,
                    officeCount = filteredLogs.count { it.workType == WorkType.OFFICE },
                    homeCount = filteredLogs.count { it.workType == WorkType.HOME_OFFICE },
                    offCount = filteredLogs.count { it.workType == WorkType.OFF_DAY },
                    extraCount = filteredLogs.count { it.workType == WorkType.EXTRA_WORK }
                )
            }
        }
    }
}
