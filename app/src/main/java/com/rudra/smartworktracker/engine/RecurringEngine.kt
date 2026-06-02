package com.rudra.smartworktracker.engine

import com.rudra.smartworktracker.data.dao.AccountDao
import com.rudra.smartworktracker.data.entity.DayOfWeek
import com.rudra.smartworktracker.data.entity.Income
import com.rudra.smartworktracker.data.entity.RecurringFrequency
import com.rudra.smartworktracker.data.entity.RecurringPriority
import com.rudra.smartworktracker.data.entity.RecurringRule
import com.rudra.smartworktracker.data.entity.RecurringTransaction
import com.rudra.smartworktracker.data.entity.RecurringTransactionStatus
import com.rudra.smartworktracker.data.entity.SyncStatus
import com.rudra.smartworktracker.data.entity.TransactionType
import com.rudra.smartworktracker.data.entity.WeekdayAdjustment
import com.rudra.smartworktracker.data.repository.ExpenseRepository
import com.rudra.smartworktracker.data.repository.IncomeRepository
import com.rudra.smartworktracker.data.repository.RecurringRepository
import com.rudra.smartworktracker.data.repository.SavingsRepository
import com.rudra.smartworktracker.model.Expense
import com.rudra.smartworktracker.model.ExpenseCategory
import kotlinx.coroutines.flow.first
import java.util.Calendar
import java.util.UUID

class RecurringEngine(
    private val recurringRepository: RecurringRepository,
    private val incomeRepository: IncomeRepository,
    private val expenseRepository: ExpenseRepository,
    private val accountDao: AccountDao,
    private val savingsRepository: SavingsRepository? = null,
    private val fusionEngine: FusionEngine? = null
) {
    companion object {
        private const val DEFAULT_MINIMUM_BALANCE = 0.0
    }

    fun calculateNextExecutionDate(
        currentDate: Long,
        frequency: RecurringFrequency,
        interval: Int = 1,
        preferredTime: com.rudra.smartworktracker.data.entity.PreferredTime = com.rudra.smartworktracker.data.entity.PreferredTime.MORNING,
        weekdayAdjustment: WeekdayAdjustment = WeekdayAdjustment.SKIP
    ): Long {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = currentDate

        val hour = when (preferredTime) {
            com.rudra.smartworktracker.data.entity.PreferredTime.MORNING -> 9
            com.rudra.smartworktracker.data.entity.PreferredTime.AFTERNOON -> 14
            com.rudra.smartworktracker.data.entity.PreferredTime.EVENING -> 19
            com.rudra.smartworktracker.data.entity.PreferredTime.NIGHT -> 22
        }
        calendar.set(Calendar.HOUR_OF_DAY, hour)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)

        when (frequency) {
            RecurringFrequency.DAILY -> calendar.add(Calendar.DAY_OF_MONTH, interval)
            RecurringFrequency.WEEKLY -> calendar.add(Calendar.WEEK_OF_YEAR, interval)
            RecurringFrequency.BIWEEKLY -> calendar.add(Calendar.WEEK_OF_YEAR, 2 * interval)
            RecurringFrequency.MONTHLY -> calendar.add(Calendar.MONTH, interval)
            RecurringFrequency.QUARTERLY -> calendar.add(Calendar.MONTH, 3 * interval)
            RecurringFrequency.YEARLY -> calendar.add(Calendar.YEAR, interval)
            RecurringFrequency.CUSTOM -> {
                calendar.add(Calendar.DAY_OF_MONTH, interval)
            }
            RecurringFrequency.WEEKLY_SPECIFIC_DAYS -> {
                return calculateNextSpecificDay(calendar, null, preferredTime, weekdayAdjustment)
            }
        }

        return applyWeekdayAdjustment(calendar.timeInMillis, weekdayAdjustment)
    }

    fun calculateNextExecutionDateWithDays(
        currentDate: Long,
        selectedDays: List<DayOfWeek>,
        preferredTime: com.rudra.smartworktracker.data.entity.PreferredTime = com.rudra.smartworktracker.data.entity.PreferredTime.MORNING,
        weekdayAdjustment: WeekdayAdjustment = WeekdayAdjustment.SKIP
    ): Long {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = currentDate
        return calculateNextSpecificDay(calendar, selectedDays, preferredTime, weekdayAdjustment)
    }

    private fun calculateNextSpecificDay(
        calendar: Calendar,
        selectedDays: List<DayOfWeek>? = null,
        preferredTime: com.rudra.smartworktracker.data.entity.PreferredTime = com.rudra.smartworktracker.data.entity.PreferredTime.MORNING,
        weekdayAdjustment: WeekdayAdjustment = WeekdayAdjustment.SKIP
    ): Long {
        val hour = when (preferredTime) {
            com.rudra.smartworktracker.data.entity.PreferredTime.MORNING -> 9
            com.rudra.smartworktracker.data.entity.PreferredTime.AFTERNOON -> 14
            com.rudra.smartworktracker.data.entity.PreferredTime.EVENING -> 19
            com.rudra.smartworktracker.data.entity.PreferredTime.NIGHT -> 22
        }

        if (selectedDays.isNullOrEmpty()) {
            calendar.add(Calendar.WEEK_OF_YEAR, 1)
            calendar.set(Calendar.HOUR_OF_DAY, hour)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            return applyWeekdayAdjustment(calendar.timeInMillis, weekdayAdjustment)
        }

        val currentDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        val selectedCalendarDays = selectedDays.map { DayOfWeek.toCalendarDay(it) }.sorted()

        var nextDay: Int? = null
        for (day in selectedCalendarDays) {
            if (day > currentDayOfWeek) {
                nextDay = day
                break
            }
        }

        if (nextDay != null) {
            val daysToAdd = nextDay - currentDayOfWeek
            calendar.add(Calendar.DAY_OF_YEAR, daysToAdd)
        } else {
            val daysToAdd = (7 - currentDayOfWeek) + selectedCalendarDays.first()
            calendar.add(Calendar.DAY_OF_YEAR, daysToAdd)
        }

        calendar.set(Calendar.HOUR_OF_DAY, hour)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)

        return applyWeekdayAdjustment(calendar.timeInMillis, weekdayAdjustment)
    }

    private fun applyWeekdayAdjustment(millis: Long, adjustment: WeekdayAdjustment): Long {
        if (adjustment == WeekdayAdjustment.SKIP) return millis
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = millis
        var dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)

        if (adjustment == WeekdayAdjustment.PREVIOUS_WORKDAY) {
            while (dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.FRIDAY) {
                calendar.add(Calendar.DAY_OF_YEAR, -1)
                dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
            }
        } else if (adjustment == WeekdayAdjustment.NEXT_WORKDAY) {
            while (dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.FRIDAY) {
                calendar.add(Calendar.DAY_OF_YEAR, 1)
                dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
            }
        }

        return calendar.timeInMillis
    }

    fun isExecutionDay(rule: RecurringRule, date: Long = System.currentTimeMillis()): Boolean {
        if (rule.frequency != RecurringFrequency.WEEKLY_SPECIFIC_DAYS) {
            return true
        }
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = date
        val currentDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        val today = DayOfWeek.fromCalendarDay(currentDayOfWeek)
        return rule.selectedDaysOfWeek?.contains(today) ?: false
    }

    private fun isWeekend(date: Long): Boolean {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = date
        val day = calendar.get(Calendar.DAY_OF_WEEK)
        return day == Calendar.SATURDAY || day == Calendar.FRIDAY
    }

    suspend fun executeRule(rule: RecurringRule): ExecutionResult {
        val balanceCheck = checkBalanceProtection(rule)
        if (!balanceCheck.canExecute) {
            return ExecutionResult(
                success = false,
                reason = balanceCheck.reason ?: "Balance protection triggered",
                shouldReschedule = balanceCheck.shouldReschedule,
                shouldSkip = balanceCheck.shouldSkip
            )
        }

        if (rule.maxExecutions != null && rule.executedCount >= rule.maxExecutions) {
            recurringRepository.updateRuleActiveStatus(rule.id, false)
            return ExecutionResult(
                success = false,
                reason = "Max executions (${rule.maxExecutions}) reached",
                shouldReschedule = false,
                shouldSkip = true
            )
        }

        if (rule.skipIfHoliday && isWeekend(rule.nextExecutionDate)) {
            val nextDate = calculateNextExecutionDate(
                rule.nextExecutionDate,
                rule.frequency,
                rule.interval,
                rule.preferredTime,
                rule.weekdayAdjustment
            )
            if (rule.endDate != null && nextDate > rule.endDate) {
                recurringRepository.updateRuleActiveStatus(rule.id, false)
            } else {
                recurringRepository.updateNextExecutionDate(rule.id, nextDate)
            }
            return ExecutionResult(
                success = true,
                reason = "Skipped (weekend)",
                shouldReschedule = false,
                shouldSkip = true
            )
        }

        val transaction = createTransactionFromRule(rule)
        val transactionId = recurringRepository.insertTransaction(transaction)

        return try {
            val result = when (rule.transactionType) {
                TransactionType.INCOME -> executeIncome(rule, transactionId)
                TransactionType.EXPENSE -> executeExpense(rule, transactionId)
                TransactionType.SAVINGS_ADD, TransactionType.SAVINGS_WITHDRAW -> executeSavings(rule, transactionId)
                TransactionType.TRANSFER -> executeTransfer(rule, transactionId)
                else -> ExecutionResult(success = false, reason = "Unsupported transaction type")
            }

            if (result.success) {
                recurringRepository.markTransactionExecuted(
                    transactionId,
                    RecurringTransactionStatus.EXECUTED
                )

                val newExecutedCount = rule.executedCount + 1
                val newTotalAmount = rule.totalExecutedAmount + rule.amount
                val now = System.currentTimeMillis()

                val nextDate = calculateNextExecutionDate(
                    rule.nextExecutionDate,
                    rule.frequency,
                    rule.interval,
                    rule.preferredTime,
                    rule.weekdayAdjustment
                )

                val reachedMax = rule.maxExecutions != null && newExecutedCount >= rule.maxExecutions
                val pastEndDate = rule.endDate != null && nextDate > rule.endDate

                if (reachedMax || pastEndDate) {
                    recurringRepository.updateRuleActiveStatus(rule.id, false)
                }

                recurringRepository.updateExecutionStats(
                    rule.id,
                    newExecutedCount,
                    newTotalAmount,
                    now,
                    if (reachedMax || pastEndDate) now else nextDate
                )
            } else {
                recurringRepository.markTransactionFailed(transactionId, result.reason)
            }

            result
        } catch (e: Exception) {
            recurringRepository.markTransactionFailed(transactionId, e.message)
            ExecutionResult(
                success = false,
                reason = e.message ?: "Unknown error",
                shouldReschedule = true
            )
        }
    }

    private suspend fun checkBalanceProtection(rule: RecurringRule): BalanceCheckResult {
        if (rule.transactionType == TransactionType.INCOME ||
            rule.transactionType == TransactionType.SAVINGS_WITHDRAW) {
            return BalanceCheckResult(canExecute = true, shouldReschedule = false, shouldSkip = false)
        }

        val accountBalance = getAccountBalance(rule)

        val minimumRequired = rule.minimumBalanceRequired ?: (rule.amount + DEFAULT_MINIMUM_BALANCE)

        return when (rule.priority) {
            RecurringPriority.CRITICAL -> {
                BalanceCheckResult(canExecute = true, shouldReschedule = false, shouldSkip = false)
            }
            RecurringPriority.HIGH -> {
                if (accountBalance >= minimumRequired) {
                    BalanceCheckResult(canExecute = true, shouldReschedule = false, shouldSkip = false)
                } else {
                    BalanceCheckResult(
                        canExecute = false,
                        reason = "Insufficient balance. Account has ৳${"%,.0f".format(accountBalance)}, needs ৳${"%,.0f".format(minimumRequired)}",
                        shouldReschedule = true,
                        shouldSkip = false
                    )
                }
            }
            RecurringPriority.MEDIUM, RecurringPriority.LOW -> {
                if (accountBalance >= minimumRequired) {
                    BalanceCheckResult(canExecute = true, shouldReschedule = false, shouldSkip = false)
                } else {
                    BalanceCheckResult(
                        canExecute = false,
                        reason = "Insufficient balance for ${rule.priority.name.lowercase()} priority rule. Available: ৳${"%,.0f".format(accountBalance)}",
                        shouldReschedule = true,
                        shouldSkip = false
                    )
                }
            }
            RecurringPriority.OPTIONAL -> {
                if (accountBalance >= minimumRequired) {
                    BalanceCheckResult(canExecute = true, shouldReschedule = false, shouldSkip = false)
                } else {
                    BalanceCheckResult(
                        canExecute = false,
                        reason = "Insufficient balance for optional rule. Skipping.",
                        shouldReschedule = false,
                        shouldSkip = true
                    )
                }
            }
        }
    }

    private suspend fun getAccountBalance(rule: RecurringRule): Double {
        val accountId = when (rule.transactionType) {
            TransactionType.EXPENSE, TransactionType.SAVINGS_ADD -> rule.sourceAccountId
            TransactionType.TRANSFER -> rule.sourceAccountId
            else -> return Double.MAX_VALUE
        }
        if (accountId <= 0) return Double.MAX_VALUE
        val account = accountDao.getAccountById(accountId)
        return account?.balance ?: Double.MAX_VALUE
    }

    private fun createTransactionFromRule(rule: RecurringRule): RecurringTransaction {
        return RecurringTransaction(
            ruleId = rule.id,
            uuid = UUID.randomUUID().toString(),
            name = rule.name,
            description = rule.description,
            transactionType = rule.transactionType,
            amount = rule.amount,
            category = rule.category,
            sourceAccountId = rule.sourceAccountId,
            destinationAccountId = rule.destinationAccountId,
            scheduledDate = rule.nextExecutionDate,
            status = if (rule.autoExecute) RecurringTransactionStatus.PENDING else RecurringTransactionStatus.CONFIRMED
        )
    }

    private suspend fun executeIncome(rule: RecurringRule, transactionId: Long): ExecutionResult {
        return try {
            val income = Income(
                amount = rule.amount,
                description = rule.name,
                category = rule.category ?: "Salary",
                timestamp = System.currentTimeMillis(),
                source = "Account ${rule.sourceAccountId}",
                accountId = rule.sourceAccountId,
                syncStatus = SyncStatus.LOCAL_ONLY
            )
            incomeRepository.insertIncome(income)
            ExecutionResult(
                success = true,
                reason = "Income added successfully",
                relatedIncomeId = income.id
            )
        } catch (e: Exception) {
            ExecutionResult(success = false, reason = e.message)
        }
    }

    private suspend fun executeExpense(rule: RecurringRule, transactionId: Long): ExecutionResult {
        return try {
            val account = accountDao.getAccountById(rule.sourceAccountId)
            if (account == null) {
                return ExecutionResult(success = false, reason = "Source account not found for expense")
            }
            if (account.balance < rule.amount) {
                return ExecutionResult(
                    success = false,
                    reason = "Insufficient balance in ${account.name}. Available: ৳${"%,.0f".format(account.balance)}",
                    shouldReschedule = rule.priority != RecurringPriority.OPTIONAL
                )
            }

            val expense = Expense(
                id = UUID.randomUUID().toString(),
                amount = rule.amount,
                category = mapCategory(rule.category),
                merchant = rule.name,
                notes = rule.description ?: "Recurring expense: ${rule.name}",
                timestamp = System.currentTimeMillis(),
                accountId = rule.sourceAccountId,
                syncStatus = SyncStatus.LOCAL_ONLY
            )
            expenseRepository.insertExpense(expense)
            ExecutionResult(
                success = true,
                reason = "Expense added successfully",
                relatedExpenseId = expense.id.hashCode().toLong()
            )
        } catch (e: Exception) {
            ExecutionResult(success = false, reason = e.message, shouldReschedule = true)
        }
    }

    private suspend fun executeSavings(rule: RecurringRule, transactionId: Long): ExecutionResult {
        val repo = savingsRepository ?: return ExecutionResult(success = false, reason = "SavingsRepository not available")
        return try {
            if (rule.transactionType == TransactionType.SAVINGS_ADD) {
                val account = accountDao.getAccountById(rule.sourceAccountId)
                if (account == null) {
                    return ExecutionResult(success = false, reason = "Source account not found")
                }
                if (account.balance < rule.amount) {
                    return ExecutionResult(
                        success = false,
                        reason = "Insufficient balance for savings. Available: ৳${"%,.0f".format(account.balance)}",
                        shouldReschedule = true
                    )
                }
                repo.addToSavings(
                    amount = rule.amount,
                    note = rule.name,
                    category = "Recurring Savings",
                    accountId = rule.sourceAccountId
                )
            } else {
                val destId = rule.destinationAccountId
                    ?: return ExecutionResult(success = false, reason = "No destination account configured")
                repo.withdrawFromSavings(
                    amount = rule.amount,
                    note = rule.name,
                    category = "Recurring Withdrawal",
                    accountId = destId
                )
            }
            ExecutionResult(
                success = true,
                reason = "Savings ${if (rule.transactionType == TransactionType.SAVINGS_ADD) "deposit" else "withdrawal"} executed"
            )
        } catch (e: Exception) {
            ExecutionResult(success = false, reason = e.message, shouldReschedule = true)
        }
    }

    private suspend fun executeTransfer(rule: RecurringRule, transactionId: Long): ExecutionResult {
        val engine = fusionEngine ?: return ExecutionResult(success = false, reason = "FusionEngine not available")
        if (rule.sourceAccountId <= 0 || rule.destinationAccountId == null || rule.destinationAccountId <= 0) {
            return ExecutionResult(success = false, reason = "Transfer requires valid source and destination accounts")
        }
        return try {
            val fromAccount = accountDao.getAccountById(rule.sourceAccountId)
            if (fromAccount == null) {
                return ExecutionResult(success = false, reason = "Source account not found")
            }
            if (fromAccount.balance < rule.amount) {
                return ExecutionResult(
                    success = false,
                    reason = "Insufficient balance in ${fromAccount.name}. Available: ৳${"%,.0f".format(fromAccount.balance)}",
                    shouldReschedule = true
                )
            }

            val result = engine.processTransfer(
                fromAccountId = rule.sourceAccountId,
                toAccountId = rule.destinationAccountId,
                amount = rule.amount,
                note = rule.name
            )
            when (result) {
                is FusionResult.Success -> ExecutionResult(
                    success = true,
                    reason = "Transfer executed successfully"
                )
                is FusionResult.Error -> ExecutionResult(
                    success = false,
                    reason = result.message,
                    shouldReschedule = true
                )
            }
        } catch (e: Exception) {
            ExecutionResult(success = false, reason = e.message, shouldReschedule = true)
        }
    }

    suspend fun getUpcomingTransactions(days: Int = 30): List<RecurringTransaction> {
        val now = System.currentTimeMillis()
        val endDate = now + (days.toLong() * 24 * 60 * 60 * 1000)
        return recurringRepository.getPendingTransactionsDue(endDate)
    }

    suspend fun processDueRules(): List<ExecutionResult> {
        val results = mutableListOf<ExecutionResult>()
        val now = System.currentTimeMillis()

        val rulesPastEndDate = recurringRepository.getRulesPastEndDate(now)
        for (rule in rulesPastEndDate) {
            recurringRepository.updateRuleActiveStatus(rule.id, false)
        }

        val rulesAtMax = recurringRepository.getRulesThatReachedMaxExecutions()
        for (rule in rulesAtMax) {
            recurringRepository.updateRuleActiveStatus(rule.id, false)
        }

        val allActiveRules = recurringRepository.getRulesDueForExecution(now)

        for (rule in allActiveRules) {
            if (rule.isActive && !rule.isPaused) {
                if (rule.frequency == RecurringFrequency.WEEKLY_SPECIFIC_DAYS) {
                    if (isExecutionDay(rule, now)) {
                        val result = executeRule(rule)
                        results.add(result)
                    }
                } else {
                    if (rule.nextExecutionDate <= now) {
                        val result = executeRule(rule)
                        results.add(result)
                    }
                }
            }
        }

        return results
    }

    suspend fun processDueRulesWithBalance(currentBalance: Double): List<ExecutionResult> {
        return processDueRules()
    }

    suspend fun smartReschedule(rule: RecurringRule, failureReason: String?): Long {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = rule.nextExecutionDate

        val gracePeriodHours = when (rule.priority) {
            RecurringPriority.CRITICAL -> 2
            RecurringPriority.HIGH -> 6
            RecurringPriority.MEDIUM -> 24
            RecurringPriority.LOW -> 48
            RecurringPriority.OPTIONAL -> 72
        }

        calendar.add(Calendar.HOUR_OF_DAY, gracePeriodHours)
        val newDate = calendar.timeInMillis
        recurringRepository.updateNextExecutionDate(rule.id, newDate)
        return newDate
    }

    suspend fun executePendingTransactions(): List<ExecutionResult> {
        val results = mutableListOf<ExecutionResult>()
        val pendingTransactions = recurringRepository.getPendingTransactionsDue(System.currentTimeMillis())

        for (transaction in pendingTransactions) {
            try {
                val rule = recurringRepository.getRuleById(transaction.ruleId)
                if (rule != null && rule.isActive && !rule.isPaused) {
                    val result = executeRule(rule)
                    results.add(result)
                }
            } catch (e: Exception) {
                recurringRepository.markTransactionFailed(transaction.id, e.message)
                results.add(ExecutionResult(success = false, reason = e.message))
            }
        }

        return results
    }

    suspend fun calculateTotalMonthlyImpact(): MonthlyImpact {
        val allActiveRules = recurringRepository.getActiveRules().first()
        var totalIncome = 0.0
        var totalExpense = 0.0
        var totalSavings = 0.0
        var totalTransfer = 0.0

        for (rule in allActiveRules) {
            val monthlyAmount = when (rule.frequency) {
                RecurringFrequency.DAILY -> rule.amount * 30
                RecurringFrequency.WEEKLY -> rule.amount * 4
                RecurringFrequency.BIWEEKLY -> rule.amount * 2
                RecurringFrequency.MONTHLY -> rule.amount
                RecurringFrequency.QUARTERLY -> rule.amount / 3
                RecurringFrequency.YEARLY -> rule.amount / 12
                RecurringFrequency.CUSTOM -> rule.amount * (30.0 / rule.interval)
                RecurringFrequency.WEEKLY_SPECIFIC_DAYS -> {
                    val daysPerWeek = (rule.selectedDaysOfWeek?.size ?: 1).coerceAtLeast(1)
                    rule.amount * daysPerWeek * 4
                }
            }

            when (rule.transactionType) {
                TransactionType.INCOME -> totalIncome += monthlyAmount
                TransactionType.EXPENSE -> totalExpense += monthlyAmount
                TransactionType.SAVINGS_ADD -> totalSavings += monthlyAmount
                TransactionType.SAVINGS_WITHDRAW -> totalSavings -= monthlyAmount
                TransactionType.TRANSFER -> totalTransfer += monthlyAmount
                else -> {}
            }
        }

        return MonthlyImpact(
            totalIncome = totalIncome,
            totalExpense = totalExpense,
            totalSavings = totalSavings,
            totalTransfer = totalTransfer,
            netCashflow = totalIncome - totalExpense - totalSavings
        )
    }

    suspend fun detectPatterns(): List<PatternSuggestion> {
        val suggestions = mutableListOf<PatternSuggestion>()

        try {
            val recentExpenses = expenseRepository.getAllExpenses().first()
            val expenseFrequency = mutableMapOf<String, MutableList<Expense>>()
            for (expense in recentExpenses) {
                val key = "${expense.merchant}:${expense.amount}"
                expenseFrequency.getOrPut(key) { mutableListOf() }.add(expense)
            }

            for ((key, expenses) in expenseFrequency) {
                if (expenses.size >= 3) {
                    val intervals = mutableListOf<Long>()
                    for (i in 1 until expenses.size) {
                        intervals.add(expenses[i].timestamp - expenses[i - 1].timestamp)
                    }
                    if (intervals.isNotEmpty()) {
                        val avgInterval = intervals.average()
                        val frequency = when {
                            avgInterval < 1.5 * 24 * 60 * 60 * 1000 -> RecurringFrequency.DAILY
                            avgInterval < 10 * 24 * 60 * 60 * 1000 -> RecurringFrequency.WEEKLY
                            avgInterval < 45 * 24 * 60 * 60 * 1000 -> RecurringFrequency.MONTHLY
                            avgInterval < 120 * 24 * 60 * 60 * 1000 -> RecurringFrequency.QUARTERLY
                            else -> RecurringFrequency.YEARLY
                        }
                        suggestions.add(
                            PatternSuggestion(
                                name = "Auto: ${expenses.first().merchant ?: "Expense"}",
                                amount = expenses.first().amount,
                                frequency = frequency,
                                category = expenses.first().category.name,
                                confidence = (expenses.size.toFloat() / 10f).coerceAtMost(1f)
                            )
                        )
                    }
                }
            }
        } catch (_: Exception) {}

        return suggestions
    }

    private fun mapCategory(category: String?): ExpenseCategory {
        return when (category?.uppercase()) {
            "MEAL" -> ExpenseCategory.MEAL
            "TRANSPORT" -> ExpenseCategory.TRANSPORT
            "ENTERTAINMENT" -> ExpenseCategory.ENTERTAINMENT
            "BILLS" -> ExpenseCategory.BILLS
            "SHOPPING" -> ExpenseCategory.SHOPPING
            "HEALTHCARE" -> ExpenseCategory.HEALTHCARE
            "EDUCATION" -> ExpenseCategory.EDUCATION
            "SUBSCRIPTIONS" -> ExpenseCategory.SUBSCRIPTIONS
            "TRAVEL" -> ExpenseCategory.TRAVEL
            else -> ExpenseCategory.OTHER
        }
    }
}

data class ExecutionResult(
    val success: Boolean,
    val reason: String? = null,
    val shouldReschedule: Boolean = false,
    val shouldSkip: Boolean = false,
    val relatedIncomeId: Long? = null,
    val relatedExpenseId: Long? = null,
    val relatedFinancialTransactionId: Int? = null
)

data class BalanceCheckResult(
    val canExecute: Boolean,
    val reason: String? = null,
    val shouldReschedule: Boolean = false,
    val shouldSkip: Boolean = false
)

data class PatternSuggestion(
    val name: String,
    val amount: Double,
    val frequency: RecurringFrequency,
    val category: String,
    val confidence: Float
)

data class MonthlyImpact(
    val totalIncome: Double,
    val totalExpense: Double,
    val totalSavings: Double,
    val totalTransfer: Double,
    val netCashflow: Double
)
