package com.rudra.smartworktracker.ui.screens.dashboard

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.rudra.smartworktracker.data.AppDatabase
import com.rudra.smartworktracker.data.repository.*
import com.rudra.smartworktracker.model.Expense
import com.rudra.smartworktracker.model.ExpenseByCategory
import com.rudra.smartworktracker.model.ExpenseCategory
import com.rudra.smartworktracker.model.IncomeByCategory
import com.rudra.smartworktracker.model.WorkLog
import com.rudra.smartworktracker.model.WorkType
import com.rudra.smartworktracker.ui.DashboardUiState
import com.rudra.smartworktracker.ui.FinancialSummary
import com.rudra.smartworktracker.ui.MonthlyStats
import com.rudra.smartworktracker.ui.WorkLogUi
import com.rudra.smartworktracker.utils.DateTimeUtils
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class DashboardViewModel(
    private val workLogRepository: WorkLogRepository,
    private val expenseRepository: ExpenseRepository,
    private val incomeRepository: IncomeRepository,
    private val savingsRepository: SavingsRepository,
    private val settingsRepository: SettingsRepository,
    private val userProfileRepository: UserProfileRepository,
    private val transactionRepository: TransactionRepository,
    private val loanRepository: LoanRepository,
    private val accountRepository: AccountRepository
) : ViewModel() {

    private val _uiSate = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiSate.asStateFlow()

    init {
        loadDashboardData()
    }

    private fun loadDashboardData() {
        viewModelScope.launch {
            val calendar = Calendar.getInstance()
            
            val startTime = (calendar.clone() as Calendar).apply {
                set(Calendar.DAY_OF_MONTH, 1)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis
            
            val endTime = (calendar.clone() as Calendar).apply {
                add(Calendar.MONTH, 1)
                set(Calendar.DAY_OF_MONTH, 1)
                add(Calendar.DATE, -1)
                set(Calendar.HOUR_OF_DAY, 23)
                set(Calendar.MINUTE, 59)
                set(Calendar.SECOND, 59)
                set(Calendar.MILLISECOND, 999)
            }.timeInMillis

            val monthYear = SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(Calendar.getInstance().time)

            val todayStart = (calendar.clone() as Calendar).apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis
            
            val todayEnd = (calendar.clone() as Calendar).apply {
                set(Calendar.HOUR_OF_DAY, 23)
                set(Calendar.MINUTE, 59)
                set(Calendar.SECOND, 59)
                set(Calendar.MILLISECOND, 999)
            }.timeInMillis
            
            val flows = listOf(
                workLogRepository.getTodayWorkLog(),
                expenseRepository.getTotalExpensesBetween(startTime, endTime),
                expenseRepository.getMealExpensesBetween(startTime, endTime),
                incomeRepository.getTotalIncomeBetween(startTime, endTime),
                savingsRepository.getSavingsBetween(startTime, endTime),
                settingsRepository.mealRate,
                workLogRepository.getRecentActivities(),
                expenseRepository.getExpensesByCategoryBetween(startTime, endTime),
                incomeRepository.getIncomesByCategoryBetween(startTime, endTime),
                workLogRepository.getMonthlyStats(),
                incomeRepository.getIncomes(1, 50),
                expenseRepository.getExpenses(1, 50),
                workLogRepository.getOvertimeLogsByMonth(monthYear),
                userProfileRepository.userProfile,
                transactionRepository.getTotalIncome(),
                transactionRepository.getTotalExpenses(),
                incomeRepository.getTotalIncomeBetween(todayStart, todayEnd),
                expenseRepository.getTotalExpensesBetween(todayStart, todayEnd),
                transactionRepository.getTotalIncomeBetween(startTime, endTime),
                transactionRepository.getTotalExpensesBetween(startTime, endTime),
                transactionRepository.getTotalIncomeBetween(todayStart, todayEnd),
                transactionRepository.getTotalExpensesBetween(todayStart, todayEnd),
                incomeRepository.getTotalIncome(),
                expenseRepository.getTotalExpenses(),
                transactionRepository.getExpenseByCategoryBetween(startTime, endTime),
                transactionRepository.getIncomeByCategoryBetween(startTime, endTime),
                accountRepository.getTotalBalance(),
                loanRepository.getTotalBorrowed(),
                loanRepository.getTotalLent()
            )

            combine(flows) { array ->
                val todayWorkLog = array[0] as? WorkLog
                val monthlyExpense = (array[1] as? Double ?: 0.0) + (array[19] as? Double ?: 0.0)
                val monthlyMealExpenses = array[2] as? Double ?: 0.0
                val monthlyIncome = (array[3] as? Double ?: 0.0) + (array[18] as? Double ?: 0.0)
                val totalSavings = array[4] as? Double ?: 0.0
                val recentActivities = array[6] as? List<WorkLog> ?: emptyList()
                val expensesByCategory = array[7] as? List<ExpenseByCategory> ?: emptyList()
                val incomesByCategory = array[8] as? List<IncomeByCategory> ?: emptyList()
                val monthlyStats = array[9] as MonthlyStats
                val incomes = array[10] as? List<com.rudra.smartworktracker.data.entity.Income> ?: emptyList()
                val expenses = array[11] as? List<Expense> ?: emptyList()
                val overtimeLogs = array[12] as? List<WorkLog> ?: emptyList()
                val userProfile = array[13] as? com.rudra.smartworktracker.data.entity.UserProfile

                val allTimeIncome = (array[14] as? Double ?: 0.0) + (array[22] as? Double ?: 0.0)
                val allTimeExpense = (array[15] as? Double ?: 0.0) + (array[23] as? Double ?: 0.0)
                val todayIncome = (array[16] as? Double ?: 0.0) + (array[20] as? Double ?: 0.0)
                val todayExpense = (array[17] as? Double ?: 0.0) + (array[21] as? Double ?: 0.0)

                val dailyIncome = todayIncome
                val dailyExpense = todayExpense
                val dailySavings = dailyIncome - dailyExpense

                val overtimeHours = monthlyStats.extraHours

                val overtimeEarnings = overtimeLogs.sumOf { log ->
                    val hours = (log.endTime?.let { DateTimeUtils.parseTime(it) } ?: 0L) - (log.startTime?.let { DateTimeUtils.parseTime(it) } ?: 0L)
                    (hours.toDouble() / (1000 * 60 * 60)) * (log.overtimeRate ?: 0.0)
                }

                val ftExpenseCategories = array[24] as? List<IncomeByCategory> ?: emptyList()
                val ftIncomeCategories = array[25] as? List<IncomeByCategory> ?: emptyList()

                val totalBalance = array[26] as? Double ?: 0.0
                val totalBorrowed = array[27] as? Double ?: 0.0
                val totalLent = array[28] as? Double ?: 0.0
                val totalLoan = totalBorrowed + totalLent

                val ftExpenseByCategory = ftExpenseCategories.map {
                    ExpenseByCategory(
                        category = it.category.toExpenseCategory(),
                        total = it.total
                    )
                }

                val combinedExpensesByCategory = expensesByCategory + ftExpenseByCategory
                val combinedIncomesByCategory = incomesByCategory + ftIncomeCategories

                val expensesByCategoryMap = combinedExpensesByCategory
                    .groupBy { it.category }
                    .mapValues { it.value.sumOf { e -> e.total } }
                val incomesByCategoryMap = combinedIncomesByCategory
                    .groupBy { it.category }
                    .mapValues { it.value.sumOf { e -> e.total } }

                val netSavings = monthlyIncome - monthlyExpense
                val allTimeNetSavings = allTimeIncome - allTimeExpense

                DashboardUiState(
                    userName = userProfile?.name,
                    todayWorkType = todayWorkLog?.workType,
                    monthlyStats = monthlyStats,
                    recentActivities = recentActivities.map { it.toUiModel() },
                    financialSummary = FinancialSummary(
                        totalIncome = monthlyIncome,
                        totalExpense = monthlyExpense,
                        totalSavings = totalSavings,
                        netSavings = allTimeNetSavings,
                        monthlyNetSavings = netSavings,
                        dailyIncome = dailyIncome,
                        dailyExpense = dailyExpense,
                        dailySavings = dailySavings,
                        totalMealCost = monthlyMealExpenses,
                        overtimeHours = overtimeHours,
                        overtimeEarnings = overtimeEarnings,
                        allTimeIncome = allTimeIncome,
                        allTimeExpense = allTimeExpense,
                        totalBalance = totalBalance,
                        totalLoan = totalLoan
                    ),
                    expensesByCategory = expensesByCategoryMap,
                    incomesByCategory = incomesByCategoryMap,
                    incomes = incomes,
                    expenses = expenses,
                    workLogs = overtimeLogs
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

    private fun String.toExpenseCategory(): ExpenseCategory {
        return try {
            ExpenseCategory.valueOf(this.uppercase())
        } catch (e: IllegalArgumentException) {
            ExpenseCategory.entries.find { it.displayName.equals(this, ignoreCase = true) }
                ?: ExpenseCategory.OTHER
        }
    }

    companion object {
        fun factory(appDatabase: AppDatabase, context: Context): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    if (modelClass.isAssignableFrom(DashboardViewModel::class.java)) {
                        val workLogRepository = WorkLogRepository(appDatabase.workLogDao())
                        val expenseRepository = ExpenseRepository(appDatabase.expenseDao(), appDatabase.accountDao())
                        val incomeRepository = IncomeRepository(appDatabase.incomeDao(), appDatabase.accountDao())
                        val savingsRepository = SavingsRepository(appDatabase.savingsDao(), appDatabase.accountDao(), appDatabase.financialTransactionDao())
                        val settingsRepository = SettingsRepository(context)
                        val userProfileRepository = UserProfileRepository(appDatabase.userProfileDao())
                        val transactionRepository = TransactionRepository(appDatabase.financialTransactionDao())
                        val accountRepository = AccountRepository(appDatabase.accountDao())
                        val loanRepository = LoanRepository(appDatabase.loanDao(), appDatabase.financialTransactionDao(), accountRepository)
                        return DashboardViewModel(
                            workLogRepository,
                            expenseRepository,
                            incomeRepository,
                            savingsRepository,
                            settingsRepository,
                            userProfileRepository,
                            transactionRepository,
                            loanRepository,
                            accountRepository
                        ) as T
                    }
                    throw IllegalArgumentException("Unknown ViewModel class")
                }
            }
        }
    }
}
