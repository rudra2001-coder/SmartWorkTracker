package com.rudra.smartworktracker.ui

import com.rudra.smartworktracker.data.entity.WorkType
import java.time.LocalDate
import java.util.Date

data class DashboardUiState(
    val todayWorkType: WorkType? = null,
    val monthlyStats: MonthlyStats = MonthlyStats(),
    val recentActivities: List<WorkLogUi> = emptyList(),
    val mealCount: Int = 0
)

data class MonthlyStats(
    val officeDays: Int = 0,
    val homeOfficeDays: Int = 0,
    val offDays: Int = 0,
    val extraHours: Double = 0.0
)

data class CalendarUiState(
    val selectedDate: LocalDate? = null,
    val workLogs: List<WorkLogUi> = emptyList()
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
