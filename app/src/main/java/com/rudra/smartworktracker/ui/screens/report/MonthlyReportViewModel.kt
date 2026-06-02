package com.rudra.smartworktracker.ui.screens.report

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rudra.smartworktracker.data.AppDatabase
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
import com.rudra.smartworktracker.model.ConsequenceDebt
import com.rudra.smartworktracker.model.DailyCheckIn
import com.rudra.smartworktracker.model.DailyJournal
import com.rudra.smartworktracker.model.Decision
import com.rudra.smartworktracker.model.Expense
import com.rudra.smartworktracker.model.ExpenseByCategory
import com.rudra.smartworktracker.model.ExpenseCategory
import com.rudra.smartworktracker.model.FocusSession
import com.rudra.smartworktracker.model.Habit
import com.rudra.smartworktracker.model.HealthMetric
import com.rudra.smartworktracker.model.IncomeByCategory
import com.rudra.smartworktracker.model.RealityEntry
import com.rudra.smartworktracker.model.Schedule
import com.rudra.smartworktracker.model.WeeklyReport
import com.rudra.smartworktracker.model.WorkLog
import com.rudra.smartworktracker.model.WorkSession
import com.rudra.smartworktracker.model.WorkType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class MonthlyReportUiState(
    val selectedMonth: String = "",
    val year: Int = Calendar.getInstance().get(Calendar.YEAR),
    val useCustomRange: Boolean = false,
    val customStartDate: Long? = null,
    val customEndDate: Long? = null,
    val compareWithPrevious: Boolean = false,
    val isLoading: Boolean = true,
    val workLogs: List<WorkLog> = emptyList(),
    val officeCount: Int = 0,
    val homeCount: Int = 0,
    val offCount: Int = 0,
    val extraCount: Int = 0,
    val overtimeCount: Int = 0,
    val totalIncome: Double = 0.0,
    val totalExpense: Double = 0.0,
    val netAmount: Double = 0.0,
    val mealExpense: Double = 0.0,
    val totalSavingsDeposited: Double = 0.0,
    val totalSavingsWithdrawn: Double = 0.0,
    val netSavings: Double = 0.0,
    val expenseByCategory: List<ExpenseByCategory> = emptyList(),
    val incomeByCategory: List<IncomeByCategory> = emptyList(),
    val previousPeriod: PeriodComparison? = null,
    val fullReport: FullReportData = FullReportData()
)

data class PeriodComparison(
    val label: String = "",
    val officeCount: Int = 0,
    val homeCount: Int = 0,
    val offCount: Int = 0,
    val extraCount: Int = 0,
    val overtimeCount: Int = 0,
    val totalIncome: Double = 0.0,
    val totalExpense: Double = 0.0,
    val netAmount: Double = 0.0
)

data class FullReportData(
    // Work domain
    val workSessionCount: Int = 0,
    val workSessionHours: Double = 0.0,
    val workDayCount: Int = 0,
    // Productivity domain
    val habitCount: Int = 0,
    val focusSessionCount: Int = 0,
    val focusSessionMinutes: Long = 0,
    val healthMetricCount: Int = 0,
    val achievementCount: Int = 0,
    val achievementsUnlocked: Int = 0,
    // Journal & Check-in
    val journalCount: Int = 0,
    val checkInCount: Int = 0,
    val decisionCount: Int = 0,
    val positiveDecisions: Int = 0,
    val negativeDecisions: Int = 0,
    val realityPlanned: Int = 0,
    val realityCompleted: Int = 0,
    // Loans
    val activeLoanCount: Int = 0,
    val totalLoanAmount: Double = 0.0,
    val totalRemainingLoan: Double = 0.0,
    val borrowedLoanCount: Int = 0,
    val lentLoanCount: Int = 0,
    val totalBorrowedRemaining: Double = 0.0,
    val totalLentRemaining: Double = 0.0,
    // EMIs
    val activeEmiCount: Int = 0,
    val pendingEmiCount: Int = 0,
    val overdueEmiCount: Int = 0,
    val totalPendingEmiAmount: Double = 0.0,
    // Credit Cards
    val creditCardCount: Int = 0,
    val totalCreditCardDebt: Double = 0.0,
    val totalCreditCardLimit: Double = 0.0,
    // Financial Transactions
    val financialTxCount: Int = 0,
    val financialTxIncome: Double = 0.0,
    val financialTxExpense: Double = 0.0,
    // Recurring
    val activeRecurringRules: Int = 0,
    val recurringTxCount: Int = 0,
    val pendingRecurringTxCount: Int = 0,
    // Meal
    val mealCount: Int = 0,
    val mealTotalCost: Double = 0.0,
    val specialMealDateCount: Int = 0,
    // Other
    val colleagueCount: Int = 0,
    val scheduleCount: Int = 0,
    val travelExpenseAmount: Double = 0.0,
    val totalDebtAmount: Double = 0.0,
    val weeklyReportCount: Int = 0,
    val monthlyInputCount: Int = 0
)

class MonthlyReportViewModel(
    private val workLogRepository: WorkLogRepository,
    private val expenseRepository: ExpenseRepository,
    private val incomeRepository: IncomeRepository,
    private val db: AppDatabase
) : ViewModel() {

    private val _selectedMonthIndex = MutableStateFlow(Calendar.getInstance().get(Calendar.MONTH))
    private val _selectedYear = MutableStateFlow(Calendar.getInstance().get(Calendar.YEAR))
    private val _useCustomRange = MutableStateFlow(false)
    private val _customStartDate = MutableStateFlow<Long?>(null)
    private val _customEndDate = MutableStateFlow<Long?>(null)
    private val _compareWithPrevious = MutableStateFlow(false)

    val months: List<String> = (0..11).map {
        val cal = Calendar.getInstance()
        cal.set(Calendar.MONTH, it)
        cal.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault())!!
    }

    val uiState: StateFlow<MonthlyReportUiState> = combine(
        _selectedMonthIndex,
        _selectedYear,
        _useCustomRange,
        _customStartDate,
        _customEndDate,
        _compareWithPrevious,
        workLogRepository.getAllWorkLogs(),
        expenseRepository.getAllExpenses(),
        incomeRepository.getAllIncomes(),
        db.savingsDao().getAllSavings(),
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
    ) { array ->
        val monthIndex = array[0] as Int
        val year = array[1] as Int
        val useCustom = array[2] as Boolean
        val customStart = array[3] as Long?
        val customEnd = array[4] as Long?
        val compare = array[5] as Boolean
        val allWorkLogs = array[6] as List<WorkLog>
        val allExpenses = array[7] as List<Expense>
        val allIncomes = array[8] as List<Income>
        val allSavings = array[9] as List<Savings>
        val allWorkSessions = array[10] as List<WorkSession>
        val allWorkDays = array[11] as List<WorkDay>
        val allHabits = array[12] as List<Habit>
        val allFocusSessions = array[13] as List<FocusSession>
        val allHealthMetrics = array[14] as List<HealthMetric>
        val allAchievements = array[15] as List<Achievement>
        val allJournals = array[16] as List<DailyJournal>
        val allLoans = array[17] as List<Loan>
        val allEmis = array[18] as List<Emi>
        val allCreditCards = array[19] as List<CreditCard>
        val allFinancialTxs = array[20] as List<FinancialTransaction>
        val allRecurringRules = array[21] as List<RecurringRule>
        val allRecurringTxs = array[22] as List<RecurringTransaction>
        val allMeals = array[23] as List<Meal>
        val allSpecialDates = array[24] as List<SpecialMealDate>
        val allColleagues = array[25] as List<Colleague>
        val allSchedules = array[26] as List<Schedule>
        val travelExpense = array[27] as TravelAndExpense?
        val allDecisions = array[28] as List<Decision>
        val allCheckIns = array[29] as List<DailyCheckIn>
        val allRealityEntries = array[30] as List<RealityEntry>
        val allMonthlyInputs = array[31] as List<MonthlyInput>
        val allWeeklyReports = array[32] as List<WeeklyReport>
        val allDebts = array[33] as List<ConsequenceDebt>
        val allSettings = array[34] as List<Settings>
        val userProfile = array[35] as UserProfile?
        val mealSettings = array[36] as MealSettings?

        val (startTime, endTime) = computeDateRange(monthIndex, year, useCustom, customStart, customEnd)

        val filteredLogs = allWorkLogs.filter { it.date.time in startTime..endTime }
        val filteredExpenses = allExpenses.filter { it.timestamp in startTime..endTime }
        val filteredIncomes = allIncomes.filter { it.timestamp in startTime..endTime }
        val filteredSavings = allSavings.filter { it.timestamp in startTime..endTime }

        val officeCount = filteredLogs.count { it.workType == WorkType.OFFICE }
        val homeCount = filteredLogs.count { it.workType == WorkType.HOME_OFFICE }
        val offCount = filteredLogs.count { it.workType == WorkType.OFF_DAY }
        val extraCount = filteredLogs.count { it.workType == WorkType.EXTRA_WORK }
        val overtimeCount = filteredLogs.count { it.workType == WorkType.OVERTIME }

        val totalIncome = filteredIncomes.sumOf { it.amount }
        val totalExpense = filteredExpenses.sumOf { it.amount }
        val netAmount = totalIncome - totalExpense

        val mealExpense = filteredExpenses.filter { it.category == ExpenseCategory.MEAL }.sumOf { it.amount }

        val deposits = filteredSavings.filter { it.amount > 0 }.sumOf { it.amount }
        val withdrawals = filteredSavings.filter { it.amount < 0 }.sumOf { -it.amount }

        val expenseByCategory = filteredExpenses.groupBy { it.category }.map { (cat, items) ->
            ExpenseByCategory(cat, items.sumOf { it.amount })
        }.sortedByDescending { it.total }

        val incomeByCategory = filteredIncomes.groupBy { it.category }.map { (cat, items) ->
            IncomeByCategory(cat, items.sumOf { it.amount })
        }.sortedByDescending { it.total }

        val previousPeriod = if (compare) {
            val duration = endTime - startTime
            val prevEnd = startTime - 1
            val prevStart = prevEnd - duration

            val prevLogs = allWorkLogs.filter { it.date.time in prevStart..prevEnd }
            val prevExpenses = allExpenses.filter { it.timestamp in prevStart..prevEnd }
            val prevIncomes = allIncomes.filter { it.timestamp in prevStart..prevEnd }

            PeriodComparison(
                label = formatDateLabel(prevStart, prevEnd),
                officeCount = prevLogs.count { it.workType == WorkType.OFFICE },
                homeCount = prevLogs.count { it.workType == WorkType.HOME_OFFICE },
                offCount = prevLogs.count { it.workType == WorkType.OFF_DAY },
                extraCount = prevLogs.count { it.workType == WorkType.EXTRA_WORK },
                overtimeCount = prevLogs.count { it.workType == WorkType.OVERTIME },
                totalIncome = prevIncomes.sumOf { it.amount },
                totalExpense = prevExpenses.sumOf { it.amount },
                netAmount = prevIncomes.sumOf { it.amount } - prevExpenses.sumOf { it.amount }
            )
        } else null

        val filteredWorkSessions = allWorkSessions.filter { it.startTime in startTime..endTime }
        val workSessionHours = filteredWorkSessions.sumOf {
            val end = it.endTime ?: it.startTime
            (end - it.startTime).toDouble() / 3_600_000
        }
        val workDayCount = allWorkDays.size
        val activeLoans = allLoans.filter { it.isActive }
        val borrowedLoans = activeLoans.filter { it.loanType == LoanType.BORROWED }
        val lentLoans = activeLoans.filter { it.loanType == LoanType.LENT }
        val filteredEmis = allEmis.filter { it.nextDueDate in startTime..endTime || it.lastPaymentDate?.let { d -> d in startTime..endTime } == true }
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

        val fullReport = FullReportData(
            workSessionCount = filteredWorkSessions.size,
            workSessionHours = workSessionHours,
            workDayCount = workDayCount,
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

        MonthlyReportUiState(
            selectedMonth = months[monthIndex],
            year = year,
            useCustomRange = useCustom,
            customStartDate = customStart,
            customEndDate = customEnd,
            compareWithPrevious = compare,
            isLoading = false,
            workLogs = filteredLogs,
            officeCount = officeCount,
            homeCount = homeCount,
            offCount = offCount,
            extraCount = extraCount,
            overtimeCount = overtimeCount,
            totalIncome = totalIncome,
            totalExpense = totalExpense,
            netAmount = netAmount,
            mealExpense = mealExpense,
            totalSavingsDeposited = deposits,
            totalSavingsWithdrawn = withdrawals,
            netSavings = deposits - withdrawals,
            expenseByCategory = expenseByCategory,
            incomeByCategory = incomeByCategory,
            previousPeriod = previousPeriod,
            fullReport = fullReport
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = MonthlyReportUiState()
    )

    fun onMonthSelected(month: String) {
        val index = months.indexOf(month)
        if (index >= 0) {
            _selectedMonthIndex.value = index
        }
    }

    fun onYearChanged(year: Int) {
        _selectedYear.value = year
    }

    fun onYearIncrement() {
        _selectedYear.value = _selectedYear.value + 1
    }

    fun onYearDecrement() {
        _selectedYear.value = (_selectedYear.value - 1).coerceAtLeast(2000)
    }

    fun onUseCustomRangeChanged(use: Boolean) {
        _useCustomRange.value = use
        if (!use) {
            _customStartDate.value = null
            _customEndDate.value = null
        }
    }

    fun onCustomStartDateChanged(date: Long?) {
        _customStartDate.value = date
    }

    fun onCustomEndDateChanged(date: Long?) {
        _customEndDate.value = date
    }

    fun onCompareWithPreviousChanged(compare: Boolean) {
        _compareWithPrevious.value = compare
    }

    private fun computeDateRange(
        monthIndex: Int,
        year: Int,
        useCustom: Boolean,
        customStart: Long?,
        customEnd: Long?
    ): Pair<Long, Long> {
        if (useCustom && customStart != null && customEnd != null) {
            val startCal = Calendar.getInstance().apply {
                timeInMillis = customStart
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val endCal = Calendar.getInstance().apply {
                timeInMillis = customEnd
                set(Calendar.HOUR_OF_DAY, 23)
                set(Calendar.MINUTE, 59)
                set(Calendar.SECOND, 59)
                set(Calendar.MILLISECOND, 999)
            }
            return startCal.timeInMillis to endCal.timeInMillis
        }

        val startCal = Calendar.getInstance().apply {
            set(year, monthIndex, 1, 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val endCal = Calendar.getInstance().apply {
            set(year, monthIndex, 1, 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
            set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }
        return startCal.timeInMillis to endCal.timeInMillis
    }

    private fun formatDateLabel(start: Long, end: Long): String {
        val fmt = SimpleDateFormat("MMM d", Locale.getDefault())
        val startStr = fmt.format(Date(start))
        val endFmt = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
        val endStr = endFmt.format(Date(end))
        return "$startStr - $endStr"
    }

    companion object {
        fun formatCurrency(amount: Double): String {
            return if (amount >= 0) {
                "\u09F3${"%,.0f".format(amount)}"
            } else {
                "-\u09F3${"%,.0f".format(-amount)}"
            }
        }
    }
}
