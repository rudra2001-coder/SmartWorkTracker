package com.rudra.smartworktracker.ui.screens.dashboard

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.rudra.smartworktracker.data.AppDatabase
import com.rudra.smartworktracker.data.repository.ExpenseRepository
import com.rudra.smartworktracker.data.repository.SettingsRepository
import com.rudra.smartworktracker.data.repository.UserProfileRepository
import com.rudra.smartworktracker.model.WorkLog
import com.rudra.smartworktracker.model.WorkType
import com.rudra.smartworktracker.data.repository.WorkLogRepository
import com.rudra.smartworktracker.ui.DashboardUiState
import com.rudra.smartworktracker.ui.FinancialSummary
import com.rudra.smartworktracker.ui.WorkLogUi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class DashboardViewModel(
    private val workLogRepository: WorkLogRepository,
    private val userProfileRepository: UserProfileRepository,
    private val expenseRepository: ExpenseRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        loadDashboardData()
    }

    private fun loadDashboardData() {
        viewModelScope.launch {
            val today = Calendar.getInstance()
            val startTime = today.apply { set(Calendar.DAY_OF_MONTH, 1) }.timeInMillis
            val endTime = today.apply { add(Calendar.MONTH, 1); set(Calendar.DAY_OF_MONTH, 1); add(Calendar.DATE, -1) }.timeInMillis

            combine(
                workLogRepository.getTodayWorkLog(),
                userProfileRepository.userProfile,
                expenseRepository.getExpensesBetween(startTime, endTime),
                expenseRepository.getMealExpensesBetween(startTime, endTime),
                settingsRepository.mealRate
            ) { todayWorkLog, userProfile, monthlyExpenses, monthlyMealExpenses, mealRate ->
                val totalExpense = monthlyExpenses.sumOf { it.amount }
                val totalIncome = userProfile?.monthlySalary ?: 0.0
                val netSavings = totalIncome - totalExpense
                val monthlyStats = workLogRepository.getMonthlyStats()

                DashboardUiState(
                    userName = userProfile?.name,
                    todayWorkType = todayWorkLog?.workType,
                    monthlyStats = monthlyStats,
                    recentActivities = emptyList(),
                    financialSummary = FinancialSummary(
                        totalIncome = totalIncome,
                        totalExpense = totalExpense,
                        netSavings = netSavings,
                        totalMealCost = monthlyMealExpenses ?: 0.0 * mealRate
                    )
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

    companion object {
        fun factory(appDatabase: AppDatabase, context: Context): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    if (modelClass.isAssignableFrom(DashboardViewModel::class.java)) {
                        val workLogRepository = WorkLogRepository(appDatabase.workLogDao())
                        val userProfileRepository = UserProfileRepository(appDatabase.userProfileDao())
                        val expenseRepository = ExpenseRepository(appDatabase.expenseDao())
                        val settingsRepository = SettingsRepository(context)
                        return DashboardViewModel(workLogRepository, userProfileRepository, expenseRepository, settingsRepository) as T
                    }
                    throw IllegalArgumentException("Unknown ViewModel class")
                }
            }
        }
    }
}
