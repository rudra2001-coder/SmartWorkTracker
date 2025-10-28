package com.rudra.smartworktracker.ui.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rudra.smartworktracker.data.entity.Meal
import com.rudra.smartworktracker.data.entity.WorkLog
import com.rudra.smartworktracker.data.entity.WorkType
import com.rudra.smartworktracker.data.repository.MealRepository
import com.rudra.smartworktracker.data.repository.WorkLogRepository
import com.rudra.smartworktracker.ui.DashboardUiState
import com.rudra.smartworktracker.ui.WorkLogUi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val workLogRepository: WorkLogRepository,
    private val mealRepository: MealRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        loadDashboardData()
    }

    private fun loadDashboardData() {
        viewModelScope.launch {
            combine(
                workLogRepository.getTodayWorkLog(),
                workLogRepository.getMonthlyStats(),
                workLogRepository.getRecentActivities(),
                mealRepository.getAllMeals()
            ) { todayWorkLog, monthlyStats, recentActivities, meals ->
                DashboardUiState(
                    todayWorkType = todayWorkLog?.workType,
                    monthlyStats = monthlyStats,
                    recentActivities = recentActivities.map {
                        val format = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                        WorkLogUi(
                            id = it.id,
                            date = it.date,
                            workType = it.workType,
                            formattedDate = format.format(it.date),
                            duration = "8h",
                            startTime = it.startTime,
                            endTime = it.endTime
                        )
                    },
                    mealCount = meals.size
                )
            }.collect { newState ->
                _uiState.value = newState
            }
        }
    }

    fun updateTodayWorkType(workType: WorkType) {
        viewModelScope.launch {
            val today = Date()
            val workLog = WorkLog(
                date = today,
                workType = workType,
                startTime = "09:00", // Default start time
                endTime = "17:00"   // Default end time
            )
            workLogRepository.insertWorkLog(workLog)
        }
    }

    fun addMeal() {
        viewModelScope.launch {
            val meal = Meal(date = Date(), mealCount = 1)
            mealRepository.insertMeal(meal)
        }
    }
}
