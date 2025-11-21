package com.rudra.smartworktracker.ui.screens.dashboard

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.rudra.smartworktracker.data.AppDatabase
import com.rudra.smartworktracker.data.repository.ExpenseRepository
import com.rudra.smartworktracker.data.repository.IncomeRepository
import com.rudra.smartworktracker.data.repository.SettingsRepository
import com.rudra.smartworktracker.data.repository.WorkLogRepository
import com.rudra.smartworktracker.model.Expense
import com.rudra.smartworktracker.model.ExpenseByCategory
import com.rudra.smartworktracker.model.ExpenseCategory
import com.rudra.smartworktracker.model.WorkLog
import com.rudra.smartworktracker.model.WorkType
import com.rudra.smartworktracker.ui.DashboardUiState
import com.rudra.smartworktracker.ui.FinancialSummary
import com.rudra.smartworktracker.ui.MonthlyStats
import com.rudra.smartworktracker.ui.WorkLogUi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class DashboardViewModel(
    private val workLogRepository: WorkLogRepository,
    private val expenseRepository: ExpenseRepository,
    private val incomeRepository: IncomeRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _uiSate = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiSate.asStateFlow()

    init {
        loadDashboardData()
    }

    private fun loadDashboardData() {
        viewModelScope.launch {
            val today = Calendar.getInstance()
            val startTime = today.apply { set(Calendar.DAY_OF_MONTH, 1) }.timeInMillis
            val endTime = today.apply { add(Calendar.MONTH, 1); set(Calendar.DAY_OF_MONTH, 1); add(Calendar.DATE, -1) }.timeInMillis

            val flows = listOf(
                workLogRepository.getTodayWorkLog(),
                expenseRepository.getTotalExpensesBetween(startTime, endTime),
                expenseRepository.getMealExpensesBetween(startTime, endTime),
                incomeRepository.getTotalIncomeBetween(startTime, endTime),
                settingsRepository.mealRate,
                workLogRepository.getRecentActivities(),
                expenseRepository.getExpensesByCategoryBetween(startTime, endTime),
                workLogRepository.getMonthlyStats()
            )

            combine(flows) { array ->
                val todayWorkLog = array[0] as? WorkLog
                val totalExpense = array[1] as? Double ?: 0.0
                val monthlyMealExpenses = array[2] as? Double ?: 0.0
                val totalIncome = array[3] as? Double ?: 0.0
                val mealRate = array[4] as? Double // unused
                val recentActivities = array[5] as? List<WorkLog> ?: emptyList()
                val expensesByCategory = array[6] as? List<ExpenseByCategory> ?: emptyList()
                val monthlyStats = array[7] as MonthlyStats

                val netSavings = totalIncome - totalExpense
                val expensesByCategoryMap = expensesByCategory.associate { it.category to it.total }

                DashboardUiState(
                    userName = null, // Removed userProfileRepository
                    todayWorkType = todayWorkLog?.workType,
                    monthlyStats = monthlyStats,
                    recentActivities = recentActivities.map { it.toUiModel() },
                    financialSummary = FinancialSummary(
                        totalIncome = totalIncome,
                        totalExpense = totalExpense,
                        netSavings = netSavings,
                        totalMealCost = monthlyMealExpenses


                    ),
                    expensesByCategory = expensesByCategoryMap
                )
            }.collect { newState ->
                _uiSate.value = newState
            }
        }
    }

    fun updateTodayWorkType(workType: WorkType) {
        viewModelScope.launch {
            val today = Date()
            val workLog = WorkLog(
                date = today,
                workType = workType,
                startTime = "12:00", // Default start time
                endTime = "22:00"   // Default end time
            )
            workLogRepository.insertWorkLog(workLog)

            if (workType == WorkType.OFFICE) {
                val mealRate = settingsRepository.mealRate.first()
                val mealExpense = Expense(
                    amount = mealRate,
                    category = ExpenseCategory.MEAL,
                    timestamp = today.time,
                    currency = "BDT", // or your default currency
                    merchant = "Office Canteen", // or appropriate merchant
                    notes = "Auto-generated meal expense for office day",
                    imageUri = null
                )
                expenseRepository.insertExpense(mealExpense)
            }
        }
    }

    private fun WorkLog.toUiModel(): WorkLogUi {
        return WorkLogUi(
            id = this.id,
            date = this.date,
            workType = this.workType,
            formattedDate = formatDate(this.date),
            duration = calculateDuration(this.startTime, this.endTime),
            startTime = this.startTime,
            endTime = this.endTime
        )
    }

    private fun formatDate(date: Date): String {
        return SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(date)
    }

    private fun calculateDuration(startTime: String?, endTime: String?): String {
        if (startTime == null || endTime == null) return "-"
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
            "-" // Fallback
        }
    }

    companion object {
        fun factory(appDatabase: AppDatabase, context: Context): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    if (modelClass.isAssignableFrom(DashboardViewModel::class.java)) {
                        val workLogRepository = WorkLogRepository(appDatabase.workLogDao())
                        val expenseRepository = ExpenseRepository(appDatabase.expenseDao())
                        val incomeRepository = IncomeRepository(appDatabase.incomeDao())
                        val settingsRepository = SettingsRepository(context)
                        return DashboardViewModel(workLogRepository, expenseRepository, incomeRepository, settingsRepository) as T
                    }
                    throw IllegalArgumentException("Unknown ViewModel class")
                }
            }
        }
    }
}
