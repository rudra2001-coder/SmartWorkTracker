package com.rudra.smartworktracker.ui.screens.reports

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rudra.smartworktracker.data.AppDatabase
import com.rudra.smartworktracker.model.ConsequenceDebt
import com.rudra.smartworktracker.data.entity.CreditCard
import com.rudra.smartworktracker.data.entity.Emi
import com.rudra.smartworktracker.data.entity.EmiStatus
import com.rudra.smartworktracker.data.entity.FinancialTransaction
import com.rudra.smartworktracker.data.entity.Income
import com.rudra.smartworktracker.data.entity.Loan
import com.rudra.smartworktracker.data.entity.LoanType
import com.rudra.smartworktracker.data.entity.Meal
import com.rudra.smartworktracker.data.entity.MealSettings
import com.rudra.smartworktracker.data.entity.MonthlyInput
import com.rudra.smartworktracker.data.entity.RecurringRule
import com.rudra.smartworktracker.data.entity.RecurringTransaction
import com.rudra.smartworktracker.data.entity.RecurringTransactionStatus
import com.rudra.smartworktracker.data.entity.Savings
import com.rudra.smartworktracker.data.entity.Settings
import com.rudra.smartworktracker.data.entity.SpecialMealDate
import com.rudra.smartworktracker.data.entity.TravelAndExpense
import com.rudra.smartworktracker.data.entity.UserProfile
import com.rudra.smartworktracker.data.entity.WorkDay
import com.rudra.smartworktracker.data.repository.ExpenseRepository
import com.rudra.smartworktracker.data.repository.IncomeRepository
import com.rudra.smartworktracker.data.repository.WorkLogRepository
import com.rudra.smartworktracker.model.Achievement
import com.rudra.smartworktracker.model.Colleague
import com.rudra.smartworktracker.model.DailyCheckIn
import com.rudra.smartworktracker.model.DailyJournal
import com.rudra.smartworktracker.model.Decision
import com.rudra.smartworktracker.model.Expense
import com.rudra.smartworktracker.model.FocusSession
import com.rudra.smartworktracker.model.Habit
import com.rudra.smartworktracker.model.HealthMetric
import com.rudra.smartworktracker.model.RealityEntry
import com.rudra.smartworktracker.model.Schedule
import com.rudra.smartworktracker.model.WeeklyReport
import com.rudra.smartworktracker.model.WorkLog
import com.rudra.smartworktracker.model.WorkSession
import com.rudra.smartworktracker.utils.DateTimeUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

enum class ReportCategory { All, Work, Income, Expense }
enum class DateRange { Today, Yesterday, ThisWeek, LastWeek, ThisMonth, LastMonth, ThisYear, Custom }
enum class SortOption { DateNewest, DateOldest, AmountHighest, AmountLowest }

// Add this data class for custom date range
data class CustomDateRange(
    val startDate: Long? = null,
    val endDate: Long? = null
)

class ReportsViewModel(
    private val workLogRepository: WorkLogRepository,
    private val expenseRepository: ExpenseRepository,
    private val incomeRepository: IncomeRepository,
    private val db: AppDatabase
) : ViewModel() {

    private val _selectedCategory = MutableStateFlow(ReportCategory.All)
    private val _selectedDateRange = MutableStateFlow(DateRange.ThisMonth)
    private val _customDateRange = MutableStateFlow(CustomDateRange())
    private val _workTypeFilter = MutableStateFlow<String?>(null)
    private val _incomeCategoryFilter = MutableStateFlow<String?>(null)
    private val _expenseCategoryFilter = MutableStateFlow<String?>(null)
    private val _sortOption = MutableStateFlow(SortOption.DateNewest)

    // Add new states for custom date picker
    private val _showCustomDatePicker = MutableStateFlow(false)
    private val _customStartDate = MutableStateFlow<Long?>(null)
    private val _customEndDate = MutableStateFlow<Long?>(null)

    val showCustomDatePicker: StateFlow<Boolean> = _showCustomDatePicker
    val customStartDate: StateFlow<Long?> = _customStartDate
    val customEndDate: StateFlow<Long?> = _customEndDate
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    val uiState: StateFlow<ReportUiState> = combine(
        workLogRepository.getAllWorkLogs(),
        expenseRepository.getAllExpenses(),
        incomeRepository.getAllIncomes(),
        db.savingsDao().getAllSavings(),
        _selectedCategory,
        _selectedDateRange,
        _customDateRange,
        _workTypeFilter,
        _incomeCategoryFilter,
        _expenseCategoryFilter,
        _sortOption,
        db.workSessionDao().getAllWorkSessions(),
        db.workDayDao().getAllWorkDays(),
        db.habitDao().getAllHabits(),
        db.focusSessionDao().getAllFocusSessions(),
        db.healthMetricDao().getAllHealthMetrics(),
        db.achievementDao().getAllAchievements(),
        db.dailyJournalDao().getAllJournals(),
        db.loanDao().getAllLoans(),
        db.emiDao().getAllEmis(),
        db.creditCardDao().getAllCreditCards(),
        db.financialTransactionDao().getAllTransactions(),
        db.recurringRuleDao().getAllRules(),
        db.recurringTransactionDao().getAllTransactions(),
        db.mealDao().getAllMeals(),
        db.specialMealDateDao().getAllSpecialDates(),
        db.colleagueDao().getAllColleagues(),
        db.scheduleDao().getAllSchedules(),
        db.travelExpenseDao().getTravelExpense(),
        db.decisionDao().getAllDecisions(),
        db.checkInDao().getAllCheckIns(),
        db.realityTrackerDao().getAllEntries(),
        db.monthlyInputDao().getAllMonthlyInputs(),
        db.weeklyReportDao().getAllReports(),
        db.consequenceDebtDao().getAllDebts(),
        db.settingsDao().getAllSettings(),
        db.userProfileDao().getUserProfile(),
        db.mealSettingsDao().getMealSettings()
    ) { values ->
        withContext(Dispatchers.IO) {
            val workLogs = values[0] as List<WorkLog>
            val expenses = values[1] as List<Expense>
            val incomes = values[2] as List<Income>
            val allSavings = values[3] as List<Savings>
            val category = values[4] as ReportCategory
            val dateRange = values[5] as DateRange
            val customDateRange = values[6] as CustomDateRange
            val workTypeFilter = values[7] as String?
            val incomeCategoryFilter = values[8] as String?
            val expenseCategoryFilter = values[9] as String?
            val sortOption = values[10] as SortOption
            val allWorkSessions = values[11] as List<WorkSession>
            val allWorkDays = values[12] as List<WorkDay>
            val allHabits = values[13] as List<Habit>
            val allFocusSessions = values[14] as List<FocusSession>
            val allHealthMetrics = values[15] as List<HealthMetric>
            val allAchievements = values[16] as List<Achievement>
            val allJournals = values[17] as List<DailyJournal>
            val allLoans = values[18] as List<Loan>
            val allEmis = values[19] as List<Emi>
            val allCreditCards = values[20] as List<CreditCard>
            val allFinancialTxs = values[21] as List<FinancialTransaction>
            val allRecurringRules = values[22] as List<RecurringRule>
            val allRecurringTxs = values[23] as List<RecurringTransaction>
            val allMeals = values[24] as List<Meal>
            val allSpecialDates = values[25] as List<SpecialMealDate>
            val allColleagues = values[26] as List<Colleague>
            val allSchedules = values[27] as List<Schedule>
            val travelExpense = values[28] as TravelAndExpense?
            val allDecisions = values[29] as List<Decision>
            val allCheckIns = values[30] as List<DailyCheckIn>
            val allRealityEntries = values[31] as List<RealityEntry>
            val allMonthlyInputs = values[32] as List<MonthlyInput>
            val allWeeklyReports = values[33] as List<WeeklyReport>
            val allDebts = values[34] as List<ConsequenceDebt>
            val allSettings = values[35] as List<Settings>
            val userProfile = values[36] as UserProfile?
            val mealSettings = values[37] as MealSettings?

            val allItems = workLogs.map { WorkLogReportItem(it) } +
                    incomes.map { IncomeReportItem(it) } +
                    expenses.map { ExpenseReportItem(it) }

            val calendar = Calendar.getInstance()
            val (startTime, endTime) = when (dateRange) {
                DateRange.Today -> {
                    val start = calendar.apply {
                        set(Calendar.HOUR_OF_DAY, 0)
                        set(Calendar.MINUTE, 0)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }.timeInMillis
                    val end = calendar.apply {
                        set(Calendar.HOUR_OF_DAY, 23)
                        set(Calendar.MINUTE, 59)
                        set(Calendar.SECOND, 59)
                        set(Calendar.MILLISECOND, 999)
                    }.timeInMillis
                    start to end
                }

                DateRange.Yesterday -> {
                    calendar.add(Calendar.DATE, -1)
                    val start = calendar.apply {
                        set(Calendar.HOUR_OF_DAY, 0)
                        set(Calendar.MINUTE, 0)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }.timeInMillis
                    val end = calendar.apply {
                        set(Calendar.HOUR_OF_DAY, 23)
                        set(Calendar.MINUTE, 59)
                        set(Calendar.SECOND, 59)
                        set(Calendar.MILLISECOND, 999)
                    }.timeInMillis
                    start to end
                }

                DateRange.ThisWeek -> {
                    calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
                    val start = calendar.apply {
                        set(Calendar.HOUR_OF_DAY, 0)
                        set(Calendar.MINUTE, 0)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }.timeInMillis
                    calendar.add(Calendar.WEEK_OF_YEAR, 1)
                    calendar.add(Calendar.DATE, -1)
                    val end = calendar.apply {
                        set(Calendar.HOUR_OF_DAY, 23)
                        set(Calendar.MINUTE, 59)
                        set(Calendar.SECOND, 59)
                        set(Calendar.MILLISECOND, 999)
                    }.timeInMillis
                    start to end
                }

                DateRange.LastWeek -> {
                    calendar.add(Calendar.WEEK_OF_YEAR, -1)
                    calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
                    val start = calendar.apply {
                        set(Calendar.HOUR_OF_DAY, 0)
                        set(Calendar.MINUTE, 0)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }.timeInMillis
                    calendar.add(Calendar.WEEK_OF_YEAR, 1)
                    calendar.add(Calendar.DATE, -1)
                    val end = calendar.apply {
                        set(Calendar.HOUR_OF_DAY, 23)
                        set(Calendar.MINUTE, 59)
                        set(Calendar.SECOND, 59)
                        set(Calendar.MILLISECOND, 999)
                    }.timeInMillis
                    start to end
                }

                DateRange.ThisMonth -> {
                    calendar.set(Calendar.DAY_OF_MONTH, 1)
                    val start = calendar.apply {
                        set(Calendar.HOUR_OF_DAY, 0)
                        set(Calendar.MINUTE, 0)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }.timeInMillis
                    calendar.add(Calendar.MONTH, 1)
                    calendar.set(Calendar.DAY_OF_MONTH, 1)
                    calendar.add(Calendar.DATE, -1)
                    val end = calendar.apply {
                        set(Calendar.HOUR_OF_DAY, 23)
                        set(Calendar.MINUTE, 59)
                        set(Calendar.SECOND, 59)
                        set(Calendar.MILLISECOND, 999)
                    }.timeInMillis
                    start to end
                }

                DateRange.LastMonth -> {
                    calendar.add(Calendar.MONTH, -1)
                    calendar.set(Calendar.DAY_OF_MONTH, 1)
                    val start = calendar.apply {
                        set(Calendar.HOUR_OF_DAY, 0)
                        set(Calendar.MINUTE, 0)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }.timeInMillis
                    calendar.add(Calendar.MONTH, 1)
                    calendar.set(Calendar.DAY_OF_MONTH, 1)
                    calendar.add(Calendar.DATE, -1)
                    val end = calendar.apply {
                        set(Calendar.HOUR_OF_DAY, 23)
                        set(Calendar.MINUTE, 59)
                        set(Calendar.SECOND, 59)
                        set(Calendar.MILLISECOND, 999)
                    }.timeInMillis
                    start to end
                }

                DateRange.ThisYear -> {
                    calendar.set(Calendar.DAY_OF_YEAR, 1)
                    val start = calendar.apply {
                        set(Calendar.HOUR_OF_DAY, 0)
                        set(Calendar.MINUTE, 0)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }.timeInMillis
                    calendar.set(Calendar.MONTH, 11) // December
                    calendar.set(Calendar.DAY_OF_MONTH, 31)
                    val end = calendar.apply {
                        set(Calendar.HOUR_OF_DAY, 23)
                        set(Calendar.MINUTE, 59)
                        set(Calendar.SECOND, 59)
                        set(Calendar.MILLISECOND, 999)
                    }.timeInMillis
                    start to end
                }

                DateRange.Custom -> {
                    val start = customDateRange.startDate ?: 0L
                    val end = customDateRange.endDate ?: Long.MAX_VALUE
                    // Adjust end date to end of day
                    val adjustedEnd = if (end != Long.MAX_VALUE) {
                        Calendar.getInstance().apply {
                            timeInMillis = end
                            set(Calendar.HOUR_OF_DAY, 23)
                            set(Calendar.MINUTE, 59)
                            set(Calendar.SECOND, 59)
                            set(Calendar.MILLISECOND, 999)
                        }.timeInMillis
                    } else end
                    // Adjust start date to beginning of day
                    val adjustedStart = if (start != 0L) {
                        Calendar.getInstance().apply {
                            timeInMillis = start
                            set(Calendar.HOUR_OF_DAY, 0)
                            set(Calendar.MINUTE, 0)
                            set(Calendar.SECOND, 0)
                            set(Calendar.MILLISECOND, 0)
                        }.timeInMillis
                    } else start
                    adjustedStart to adjustedEnd
                }
            }

            val dateFilteredItems = allItems.filter {
                it.date in startTime..endTime
            }

            var categoryFilteredItems = when (category) {
                ReportCategory.All -> dateFilteredItems
                ReportCategory.Work -> dateFilteredItems.filterIsInstance<WorkLogReportItem>()
                ReportCategory.Income -> dateFilteredItems.filterIsInstance<IncomeReportItem>()
                ReportCategory.Expense -> dateFilteredItems.filterIsInstance<ExpenseReportItem>()
            }

            if (workTypeFilter != null) {
                categoryFilteredItems = categoryFilteredItems.filter {
                    it is WorkLogReportItem && it.workLog.workType.name == workTypeFilter
                }
            }
            if (incomeCategoryFilter != null) {
                categoryFilteredItems = categoryFilteredItems.filter {
                    it is IncomeReportItem && it.income.category == incomeCategoryFilter
                }
            }
            if (expenseCategoryFilter != null) {
                categoryFilteredItems = categoryFilteredItems.filter {
                    it is ExpenseReportItem && it.expense.category.name == expenseCategoryFilter
                }
            }

            val sortedItems = when (sortOption) {
                SortOption.DateNewest -> categoryFilteredItems.sortedByDescending { it.date }
                SortOption.DateOldest -> categoryFilteredItems.sortedBy { it.date }
                SortOption.AmountHighest -> categoryFilteredItems.sortedByDescending { it.amount }
                SortOption.AmountLowest -> categoryFilteredItems.sortedBy { it.amount }
            }

            val totalWorkHours =
                categoryFilteredItems.filterIsInstance<WorkLogReportItem>().sumOf {
                    val start = it.workLog.startTime?.let { time -> DateTimeUtils.parseTime(time) } ?: 0L
                    val end = it.workLog.endTime?.let { time -> DateTimeUtils.parseTime(time) } ?: 0L
                    (end - start).toDouble() / (1000 * 60 * 60)
                }.toLong()

            val totalIncome =
                categoryFilteredItems.filterIsInstance<IncomeReportItem>().sumOf { it.income.amount }
            val totalExpense =
                categoryFilteredItems.filterIsInstance<ExpenseReportItem>()
                    .sumOf { it.expense.amount }
            val netProfit = totalIncome - totalExpense

            val filteredWorkSessions = allWorkSessions.filter { it.startTime in startTime..endTime }
            val workSessionHours = filteredWorkSessions.sumOf {
                val end = it.endTime ?: it.startTime
                (end - it.startTime).toDouble() / 3_600_000
            }
            val activeLoans = allLoans.filter { it.isActive }
            val borrowedLoans = activeLoans.filter { it.loanType == LoanType.BORROWED }
            val lentLoans = activeLoans.filter { it.loanType == LoanType.LENT }
            val filteredFinancialTxs = allFinancialTxs.filter { it.date in startTime..endTime }
            val filteredMeals = allMeals.filter { it.date.time in startTime..endTime }
            val filteredDecisions = allDecisions.filter { it.createdAt in startTime..endTime }
            val filteredCheckIns = allCheckIns.filter { it.date in startTime..endTime }
            val filteredReality = allRealityEntries.filter { it.createdAt in startTime..endTime }
            val filteredSpecialDates = allSpecialDates.filter { it.date in startTime..endTime }
            val filteredMonthlyInputs = allMonthlyInputs.filter { mi ->
                try {
                    val cal = Calendar.getInstance()
                    val parts = mi.month.split("-")
                    if (parts.size == 2) {
                        cal.set(Calendar.YEAR, parts[0].toInt())
                        cal.set(Calendar.MONTH, parts[1].toInt() - 1)
                        val monthStart = cal.apply { set(Calendar.DAY_OF_MONTH, 1); set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0) }.timeInMillis
                        val monthEnd = cal.apply { set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH)); set(Calendar.HOUR_OF_DAY, 23); set(Calendar.MINUTE, 59); set(Calendar.SECOND, 59); set(Calendar.MILLISECOND, 999) }.timeInMillis
                        monthStart in startTime..endTime || monthEnd in startTime..endTime
                    } else false
                } catch (_: Exception) { false }
            }

            val fullReport = ReportsFullData(
                workSessionCount = filteredWorkSessions.size,
                workSessionHours = workSessionHours,
                workDayCount = allWorkDays.size,
                habitCount = allHabits.size,
                focusSessionCount = allFocusSessions.filter { it.timestamp in startTime..endTime }.size,
                focusSessionMinutes = allFocusSessions.filter { it.timestamp in startTime..endTime }.sumOf { it.elapsedTime } / 60_000,
                healthMetricCount = allHealthMetrics.filter { it.timestamp in startTime..endTime }.size,
                achievementCount = allAchievements.size,
                achievementsUnlocked = allAchievements.count { it.unlocked },
                journalCount = allJournals.size,
                checkInCount = filteredCheckIns.size,
                decisionCount = filteredDecisions.size,
                positiveDecisions = filteredDecisions.count { it.isPositive },
                negativeDecisions = filteredDecisions.count { !it.isPositive },
                realityPlanned = filteredReality.size,
                realityCompleted = filteredReality.count { it.isCompleted },
                activeLoanCount = activeLoans.size,
                totalLoanAmount = activeLoans.sumOf { it.initialAmount },
                totalRemainingLoan = activeLoans.sumOf { it.remainingAmount },
                borrowedLoanCount = borrowedLoans.size,
                lentLoanCount = lentLoans.size,
                totalBorrowedRemaining = borrowedLoans.sumOf { it.remainingAmount },
                totalLentRemaining = lentLoans.sumOf { it.remainingAmount },
                activeEmiCount = allEmis.count { it.isActive },
                pendingEmiCount = allEmis.count { it.isActive && !it.isPaid && !it.isSkipped },
                overdueEmiCount = allEmis.count { it.status == EmiStatus.OVERDUE },
                totalPendingEmiAmount = allEmis.filter { it.isActive && !it.isPaid && !it.isSkipped }.sumOf { it.totalPayable },
                creditCardCount = allCreditCards.size,
                totalCreditCardDebt = allCreditCards.sumOf { it.currentBalance.coerceAtLeast(0.0) },
                totalCreditCardLimit = allCreditCards.sumOf { it.cardLimit },
                financialTxCount = filteredFinancialTxs.size,
                financialTxIncome = filteredFinancialTxs.filter { it.amount > 0 }.sumOf { it.amount },
                financialTxExpense = filteredFinancialTxs.filter { it.amount < 0 }.sumOf { -it.amount },
                activeRecurringRules = allRecurringRules.count { it.isActive },
                recurringTxCount = allRecurringTxs.filter { it.executedDate in startTime..endTime }.size,
                pendingRecurringTxCount = allRecurringTxs.count { it.status == RecurringTransactionStatus.PENDING },
                mealCount = filteredMeals.size,
                mealTotalCost = filteredMeals.sumOf { it.mealCount * it.costPerMeal },
                specialMealDateCount = filteredSpecialDates.size,
                colleagueCount = allColleagues.size,
                scheduleCount = allSchedules.size,
                travelExpenseAmount = travelExpense?.dailyTravelCost ?: 0.0,
                totalDebtAmount = allDebts.sumOf { it.debtAmount.toDouble() },
                weeklyReportCount = allWeeklyReports.filter { it.weekStartDate in startTime..endTime }.size,
                monthlyInputCount = filteredMonthlyInputs.size
            )

            ReportUiState(
                selectedCategory = category,
                selectedDateRange = dateRange,
                customDateRange = customDateRange,
                filteredItems = sortedItems.take(100),
                totalWorkHours = totalWorkHours,
                totalIncome = totalIncome,
                totalExpense = totalExpense,
                netProfit = netProfit,
                workTypeFilter = workTypeFilter,
                incomeCategoryFilter = incomeCategoryFilter,
                expenseCategoryFilter = expenseCategoryFilter,
                sortOption = sortOption,
                fullReport = fullReport
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ReportUiState()
    )

    // Existing functions remain the same
    fun onCategoryChange(category: ReportCategory) {
        _selectedCategory.value = category
    }

    fun onDateRangeChange(dateRange: DateRange) {
        _selectedDateRange.value = dateRange
    }

    // NEW: Function to open custom date picker
    fun showCustomDatePicker(show: Boolean) {
        _showCustomDatePicker.value = show
    }

    // NEW: Function to set custom dates
    fun setCustomStartDate(date: Long?) {
        _customStartDate.value = date
    }

    fun setCustomEndDate(date: Long?) {
        _customEndDate.value = date
    }

    // NEW: Function to apply custom date filter
    fun applyCustomDateFilter() {
        val start = _customStartDate.value
        val end = _customEndDate.value

        if (start != null && end != null) {
            _customDateRange.value = CustomDateRange(start, end)
            _selectedDateRange.value = DateRange.Custom
        }
    }

    // NEW: Function to clear custom date filter
    fun clearCustomDateFilter() {
        _customDateRange.value = CustomDateRange()
        _customStartDate.value = null
        _customEndDate.value = null
        _selectedDateRange.value = DateRange.ThisMonth
    }

    fun onWorkTypeFilterChange(workType: String?) {
        _workTypeFilter.value = workType
    }

    fun onIncomeCategoryFilterChange(category: String?) {
        _incomeCategoryFilter.value = category
    }

    fun onExpenseCategoryFilterChange(category: String?) {
        _expenseCategoryFilter.value = category
    }

    fun onSortOptionChange(sortOption: SortOption) {
        _sortOption.value = sortOption
    }

    fun deleteIncome(income: Income) {
        viewModelScope.launch {
            incomeRepository.deleteIncome(income)
        }
    }

    fun deleteExpense(expense: Expense) {
        viewModelScope.launch {
            expenseRepository.deleteExpense(expense)
        }
    }

    fun generateTextReport(): String {
        val reportBuilder = StringBuilder()

        val uiStateValue = uiState.value

        reportBuilder.append("Report (${uiStateValue.selectedDateRange.name})\n")

        if (uiStateValue.selectedDateRange == DateRange.Custom &&
            uiStateValue.customDateRange != null
        ) {
            val startDate = if (uiStateValue.customDateRange.startDate != null)
                dateFormat.format(Date(uiStateValue.customDateRange.startDate!!))
            else "N/A"
            val endDate = if (uiStateValue.customDateRange.endDate != null)
                dateFormat.format(Date(uiState.value.customDateRange!!.endDate!!))
            else "N/A"
            reportBuilder.append("Date Range: $startDate to $endDate\n")
        }

        reportBuilder.append("Total Work Hours: ${uiStateValue.totalWorkHours} hrs\n")
        reportBuilder.append("Total Income: ${uiStateValue.totalIncome} TK\n")
        reportBuilder.append("Total Expense: ${uiStateValue.totalExpense} TK\n")
        reportBuilder.append("Net Profit: ${uiStateValue.netProfit} TK\n\n")
        reportBuilder.append("Details:\n")

        uiStateValue.filteredItems.forEach { item ->
            when (item) {
                is WorkLogReportItem -> {
                    reportBuilder.append("Work Log: ${item.workLog.workType} - ${
                        dateFormat.format(
                            Date(item.workLog.date.time)
                        )
                    }\n")
                }

                is IncomeReportItem -> {
                    reportBuilder.append("Income: ${item.income.amount} TK - ${item.income.category} - ${
                        dateFormat.format(
                            Date(item.income.timestamp)
                        )
                    }\n")
                }

                is ExpenseReportItem -> {
                    reportBuilder.append("Expense: ${item.expense.amount} TK - ${item.expense.category} - ${
                        dateFormat.format(
                            Date(item.expense.timestamp)
                        )
                    }\n")
                }
            }
        }

        return reportBuilder.toString()
    }
}
