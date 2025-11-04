package com.rudra.smartworktracker.ui

import com.rudra.smartworktracker.model.WorkType
import java.time.LocalDate
import java.util.Date

data class DashboardUiState(
    val todayWorkType: WorkType? = null,
    val monthlyStats: MonthlyStats = MonthlyStats(),
    val recentActivities: List<WorkLogUi> = emptyList(),
)

data class MonthlyStats(
    val officeDays: Int = 0,
    val homeOfficeDays: Int = 0,
    val offDays: Int = 0,
    val extraHours: Double = 0.0
)


data class CalendarUiState(
    val selectedDate: LocalDate? = null,
    val workLogs: List<WorkLogUi> = emptyList(),
    val errorMessage: String? = null,
    val isLoading: Boolean = false
)
data class WorkLogUi(
    val id: Long,
    val date: Date,
    val workType: WorkType,
    val formattedDate: String,
    val duration: String,
    val startTime: String? = null,
    val endTime: String? = null
)
