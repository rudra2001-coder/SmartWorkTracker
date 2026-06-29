package com.rudra.smartworktracker.engine

import com.rudra.smartworktracker.data.dao.AccountDao
import com.rudra.smartworktracker.data.entity.DayOfWeek
import com.rudra.smartworktracker.data.entity.Income
import com.rudra.smartworktracker.data.entity.MonthlyDayOption
import com.rudra.smartworktracker.data.entity.PreferredTime
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
import java.time.DayOfWeek as JavaDayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import java.time.temporal.IsoFields
import java.time.temporal.TemporalAdjusters
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
        private val SYSTEM_ZONE: ZoneId = ZoneId.systemDefault()
    }

    private fun getPreferredHour(preferredTime: PreferredTime): Int = when (preferredTime) {
        PreferredTime.MORNING -> 9
        PreferredTime.AFTERNOON -> 14
        PreferredTime.EVENING -> 19
        PreferredTime.NIGHT -> 22
    }

    private fun toZonedDateTime(millis: Long): ZonedDateTime =
        ZonedDateTime.ofInstant(java.time.Instant.ofEpochMilli(millis), SYSTEM_ZONE)

    private fun toMillis(zdt: ZonedDateTime): Long =
        zdt.toInstant().toEpochMilli()

    private fun startOfDayZoned(date: LocalDate, preferredTime: PreferredTime): ZonedDateTime {
        val hour = getPreferredHour(preferredTime)
        return date.atTime(LocalTime.of(hour, 0, 0)).atZone(SYSTEM_ZONE)
    }

    private fun getIsoWeek(date: LocalDate): Long {
        val year = date.get(IsoFields.WEEK_BASED_YEAR).toLong()
        val week = date.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR).toLong()
        return year * 100 + week
    }

    fun calculateNextExecutionDate(
        currentDate: Long,
        frequency: RecurringFrequency,
        interval: Int = 1,
        preferredTime: PreferredTime = PreferredTime.MORNING,
        weekdayAdjustment: WeekdayAdjustment = WeekdayAdjustment.SKIP,
        selectedDaysOfWeek: List<DayOfWeek>? = null,
        selectedDaysOfMonth: List<Int>? = null,
        monthlyDayOption: MonthlyDayOption = MonthlyDayOption.DAY_OF_MONTH,
        weeklyInterval: Int = 1
    ): Long {
        val zdt = toZonedDateTime(currentDate)
        val date = zdt.toLocalDate()

        val result = when (frequency) {
            RecurringFrequency.DAILY -> {
                startOfDayZoned(date.plusDays(interval.toLong()), preferredTime)
            }
            RecurringFrequency.WEEKLY -> {
                startOfDayZoned(date.plusWeeks(interval.toLong()), preferredTime)
            }
            RecurringFrequency.BIWEEKLY -> {
                startOfDayZoned(date.plusWeeks(2L * interval), preferredTime)
            }
            RecurringFrequency.MONTHLY -> {
                startOfDayZoned(date.plusMonths(interval.toLong()), preferredTime)
            }
            RecurringFrequency.QUARTERLY -> {
                startOfDayZoned(date.plusMonths(3L * interval), preferredTime)
            }
            RecurringFrequency.YEARLY -> {
                startOfDayZoned(date.plusYears(interval.toLong()), preferredTime)
            }
            RecurringFrequency.CUSTOM -> {
                startOfDayZoned(date.plusDays(interval.toLong()), preferredTime)
            }
            RecurringFrequency.WEEKLY_SPECIFIC_DAYS -> {
                val days = selectedDaysOfWeek ?: return toMillis(startOfDayZoned(date.plusWeeks(1), preferredTime))
                if (days.isEmpty()) return toMillis(startOfDayZoned(date.plusWeeks(1), preferredTime))
                return calculateNextSpecificDay(zdt, days, preferredTime, weekdayAdjustment, weeklyInterval)
            }
            RecurringFrequency.MONTHLY_SPECIFIC_DAYS -> {
                val days = selectedDaysOfMonth ?: return toMillis(startOfDayZoned(date.plusMonths(1), preferredTime))
                if (days.isEmpty()) return toMillis(startOfDayZoned(date.plusMonths(1), preferredTime))
                return calculateNextMonthlySpecificDay(zdt, days, monthlyDayOption, preferredTime, weekdayAdjustment)
            }
        }

        return applyWeekdayAdjustment(toMillis(result), weekdayAdjustment)
    }

    fun calculateNextExecutionDateWithDays(
        currentDate: Long,
        selectedDays: List<DayOfWeek>,
        preferredTime: PreferredTime = PreferredTime.MORNING,
        weekdayAdjustment: WeekdayAdjustment = WeekdayAdjustment.SKIP,
        weeklyInterval: Int = 1
    ): Long {
        val zdt = toZonedDateTime(currentDate)
        return calculateNextSpecificDay(zdt, selectedDays, preferredTime, weekdayAdjustment, weeklyInterval)
    }

    fun calculateNextMonthlySpecificDayWithDays(
        currentDate: Long,
        selectedDays: List<Int>,
        monthlyDayOption: MonthlyDayOption = MonthlyDayOption.DAY_OF_MONTH,
        preferredTime: PreferredTime = PreferredTime.MORNING,
        weekdayAdjustment: WeekdayAdjustment = WeekdayAdjustment.SKIP
    ): Long {
        val zdt = toZonedDateTime(currentDate)
        return calculateNextMonthlySpecificDay(zdt, selectedDays, monthlyDayOption, preferredTime, weekdayAdjustment)
    }

    private fun calculateNextSpecificDay(
        zdt: ZonedDateTime,
        selectedDays: List<DayOfWeek>,
        preferredTime: PreferredTime,
        weekdayAdjustment: WeekdayAdjustment,
        weeklyInterval: Int = 1
    ): Long {
        val date = zdt.toLocalDate()
        val selectedJavaDays = selectedDays.map { d -> java.time.DayOfWeek.valueOf(d.name) }.sorted()

        val currentJavaDay = date.dayOfWeek

        var nextDate: LocalDate = date
        for (day in selectedJavaDays) {
            if (day > currentJavaDay) {
                nextDate = date.with(TemporalAdjusters.next(day))
                break
            }
        }

        if (nextDate == date) {
            nextDate = date.with(TemporalAdjusters.next(selectedJavaDays.first()))
        }

        if (weeklyInterval > 1) {
            val currentWeek = getIsoWeek(date)
            val nextWeek = getIsoWeek(nextDate)
            val isoWeeksDiff = nextWeek - currentWeek

            if (isoWeeksDiff > 0) {
                val mod = isoWeeksDiff % weeklyInterval
                if (mod != 0L) {
                    nextDate = nextDate.plusWeeks((weeklyInterval - mod).toLong())
                }
            }
        }

        return toMillis(startOfDayZoned(nextDate, preferredTime))
    }

    private fun calculateNextMonthlySpecificDay(
        zdt: ZonedDateTime,
        selectedDays: List<Int>,
        monthlyDayOption: MonthlyDayOption,
        preferredTime: PreferredTime,
        weekdayAdjustment: WeekdayAdjustment
    ): Long {
        val date = zdt.toLocalDate()
        val sortedDays = selectedDays.sorted()

        val candidateDays = mutableListOf<LocalDate>()

        for (dayNum in sortedDays) {
            val dayInMonth = when (monthlyDayOption) {
                MonthlyDayOption.DAY_OF_MONTH -> {
                    if (dayNum > 0) {
                        try { date.withDayOfMonth(dayNum.coerceIn(1, date.lengthOfMonth())) } catch (_: Exception) { null }
                    } else {
                        try { date.withDayOfMonth(date.lengthOfMonth() + 1 + dayNum) } catch (_: Exception) { null }
                    }
                }
                MonthlyDayOption.FIRST_DAY -> date.withDayOfMonth(1)
                MonthlyDayOption.LAST_DAY -> date.withDayOfMonth(date.lengthOfMonth())
                MonthlyDayOption.FIRST_WEEKDAY -> {
                    val firstDay = date.withDayOfMonth(1)
                    when (firstDay.dayOfWeek) {
                        JavaDayOfWeek.SATURDAY -> firstDay.plusDays(2)
                        JavaDayOfWeek.FRIDAY -> firstDay.plusDays(3)
                        else -> firstDay
                    }
                }
                MonthlyDayOption.LAST_WEEKDAY -> {
                    val lastDay = date.withDayOfMonth(date.lengthOfMonth())
                    when (lastDay.dayOfWeek) {
                        JavaDayOfWeek.SATURDAY -> lastDay.minusDays(1)
                        JavaDayOfWeek.FRIDAY -> lastDay.minusDays(1)
                        else -> lastDay
                    }
                }
            }
            if (dayInMonth != null) candidateDays.add(dayInMonth)
        }

        val today = date
        for (candidate in candidateDays.sorted()) {
            if (!candidate.isBefore(today)) {
                return toMillis(startOfDayZoned(candidate, preferredTime))
            }
        }

        val nextMonth = date.plusMonths(1)
        val nextCandidates = candidateDays.map {
            when (monthlyDayOption) {
                MonthlyDayOption.DAY_OF_MONTH -> {
                    val dayNum = if (it.dayOfMonth > nextMonth.lengthOfMonth()) nextMonth.lengthOfMonth() else it.dayOfMonth
                    nextMonth.withDayOfMonth(dayNum)
                }
                MonthlyDayOption.FIRST_DAY -> nextMonth.withDayOfMonth(1)
                MonthlyDayOption.LAST_DAY -> nextMonth.withDayOfMonth(nextMonth.lengthOfMonth())
                MonthlyDayOption.FIRST_WEEKDAY -> {
                    val firstDay = nextMonth.withDayOfMonth(1)
                    when (firstDay.dayOfWeek) {
                        JavaDayOfWeek.SATURDAY -> firstDay.plusDays(2)
                        JavaDayOfWeek.FRIDAY -> firstDay.plusDays(3)
                        else -> firstDay
                    }
                }
                MonthlyDayOption.LAST_WEEKDAY -> {
                    val lastDay = nextMonth.withDayOfMonth(nextMonth.lengthOfMonth())
                    when (lastDay.dayOfWeek) {
                        JavaDayOfWeek.SATURDAY -> lastDay.minusDays(1)
                        JavaDayOfWeek.FRIDAY -> lastDay.minusDays(1)
                        else -> lastDay
                    }
                }
            }
        }
        val earliestNext = nextCandidates.minOrNull() ?: nextMonth.withDayOfMonth(1)
        return toMillis(startOfDayZoned(earliestNext, preferredTime))
    }

    private fun applyWeekdayAdjustment(millis: Long, adjustment: WeekdayAdjustment): Long {
        if (adjustment == WeekdayAdjustment.SKIP) return millis
        var date = toZonedDateTime(millis).toLocalDate()
        when (adjustment) {
            WeekdayAdjustment.PREVIOUS_WORKDAY -> {
                while (date.dayOfWeek == JavaDayOfWeek.SATURDAY || date.dayOfWeek == JavaDayOfWeek.FRIDAY) {
                    date = date.minusDays(1)
                }
            }
            WeekdayAdjustment.NEXT_WORKDAY -> {
                while (date.dayOfWeek == JavaDayOfWeek.SATURDAY || date.dayOfWeek == JavaDayOfWeek.FRIDAY) {
                    date = date.plusDays(1)
                }
            }
            WeekdayAdjustment.SKIP -> {}
        }
        return toMillis(date.atTime(LocalTime.of(toZonedDateTime(millis).hour, 0, 0)).atZone(SYSTEM_ZONE))
    }

    fun isExecutionDay(rule: RecurringRule, date: Long = System.currentTimeMillis()): Boolean {
        val zdt = toZonedDateTime(date)
        val localDate = zdt.toLocalDate()
        return when (rule.frequency) {
            RecurringFrequency.WEEKLY_SPECIFIC_DAYS -> {
                val todayJava = localDate.dayOfWeek
                rule.selectedDaysOfWeek?.any { java.time.DayOfWeek.valueOf(it.name) == todayJava } ?: false
            }
            RecurringFrequency.MONTHLY_SPECIFIC_DAYS -> {
                val today = localDate.dayOfMonth
                rule.selectedDaysOfMonth?.any {
                    val targetDay = if (it > 0) it else localDate.lengthOfMonth() + 1 + it
                    today == targetDay
                } ?: false
            }
            else -> true
        }
    }

    private fun isWeekend(date: Long): Boolean {
        val day = toZonedDateTime(date).dayOfWeek
        return day == JavaDayOfWeek.SATURDAY || day == JavaDayOfWeek.FRIDAY
    }

    suspend fun executeRule(rule: RecurringRule): ExecutionResult {
        val balanceCheck = checkBalanceProtection(rule)
        if (!balanceCheck.canExecute) {
            if (balanceCheck.shouldReschedule) {
                val newRetryCount = rule.retryCount + 1
                if (newRetryCount >= rule.maxRetries) {
                    recurringRepository.updateRuleActiveStatus(rule.id, false)
                    return ExecutionResult(
                        success = false,
                        reason = "Deactivated after $newRetryCount failed retries: ${balanceCheck.reason}",
                        shouldReschedule = false,
                        shouldSkip = true
                    )
                }
                val now = System.currentTimeMillis()
                recurringRepository.updateRetryState(
                    rule.id, pendingRetry = true,
                    retryCount = newRetryCount,
                    nextDate = now - 600_000
                )
                return ExecutionResult(
                    success = false,
                    reason = balanceCheck.reason ?: "Insufficient balance (retry $newRetryCount/${rule.maxRetries})",
                    shouldReschedule = false,
                    shouldSkip = false
                )
            }
            return ExecutionResult(
                success = false,
                reason = balanceCheck.reason ?: "Balance protection triggered",
                shouldReschedule = false,
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

        val now = System.currentTimeMillis()
        val localDate = toZonedDateTime(now).toLocalDate()
        val executionDate = toZonedDateTime(rule.nextExecutionDate).toLocalDate()

        if (rule.strictMode) {
            if (localDate.isAfter(executionDate)) {
                val daysLate = ChronoUnit.DAYS.between(executionDate, localDate)
                if (daysLate > 0) {
                    val nextDate = calculateNextExecutionDate(
                        rule.nextExecutionDate, rule.frequency, rule.interval,
                        rule.preferredTime, rule.weekdayAdjustment,
                        rule.selectedDaysOfWeek, rule.selectedDaysOfMonth,
                        rule.monthlyDayOption, rule.weeklyInterval
                    )
                    recurringRepository.updateNextExecutionAndChecked(rule.id, nextDate, now)
                    return ExecutionResult(
                        success = true,
                        reason = "Skipped (strict mode: missed window by ${daysLate} day(s))",
                        shouldReschedule = false,
                        shouldSkip = true
                    )
                }
            }
        }

        if (rule.skipIfHoliday && isWeekend(rule.nextExecutionDate)) {
            val nextDate = calculateNextExecutionDate(
                rule.nextExecutionDate, rule.frequency, rule.interval,
                rule.preferredTime, rule.weekdayAdjustment,
                rule.selectedDaysOfWeek, rule.selectedDaysOfMonth,
                rule.monthlyDayOption, rule.weeklyInterval
            )
            if (rule.endDate != null && nextDate > rule.endDate) {
                recurringRepository.updateRuleActiveStatus(rule.id, false)
            } else {
                recurringRepository.updateNextExecutionAndChecked(rule.id, nextDate, now)
            }
            return ExecutionResult(
                success = true,
                reason = "Skipped (weekend/holiday)",
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
                    transactionId, RecurringTransactionStatus.EXECUTED
                )

                val newExecutedCount = rule.executedCount + 1
                val newTotalAmount = rule.totalExecutedAmount + rule.amount

                val nextDate = calculateNextExecutionDate(
                    now, rule.frequency, rule.interval,
                    rule.preferredTime, rule.weekdayAdjustment,
                    rule.selectedDaysOfWeek, rule.selectedDaysOfMonth,
                    rule.monthlyDayOption, rule.weeklyInterval
                )

                val reachedMax = rule.maxExecutions != null && newExecutedCount >= rule.maxExecutions
                val pastEndDate = rule.endDate != null && nextDate > rule.endDate

                if (reachedMax || pastEndDate) {
                    recurringRepository.updateRuleActiveStatus(rule.id, false)
                }

                if (rule.pendingRetry || rule.retryCount > 0) {
                    recurringRepository.clearRetryState(rule.id)
                }

                recurringRepository.updateExecutionStats(
                    rule.id, newExecutedCount, newTotalAmount, now,
                    if (reachedMax || pastEndDate) now else nextDate
                )

                recurringRepository.updateLastCheckedTimestamp(rule.id, now)
            } else {
                recurringRepository.markTransactionFailed(transactionId, result.reason)
                if (!result.shouldSkip) {
                    smartReschedule(rule, result.reason)
                }
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

    suspend fun checkForMissedExecutions(): List<ExecutionResult> {
        val results = mutableListOf<ExecutionResult>()
        val now = System.currentTimeMillis()
        val localDateNow = toZonedDateTime(now).toLocalDate()
        val nowHour = toZonedDateTime(now).hour

        val allActiveRules = recurringRepository.getActiveRules().first()

        for (rule in allActiveRules) {
            if (!rule.isActive || rule.isPaused) continue

            val lastChecked = rule.lastCheckedTimestamp ?: rule.startDate
            val lastCheckedDate = toZonedDateTime(lastChecked).toLocalDate()
            val nextExecDate = toZonedDateTime(rule.nextExecutionDate).toLocalDate()

            if (nextExecDate.isBefore(localDateNow) && lastCheckedDate.isBefore(localDateNow)) {
                val daysMissed = ChronoUnit.DAYS.between(nextExecDate, localDateNow)
                if (daysMissed <= 0) continue

                val maxCatchUp = rule.maxCatchUpDays
                if (maxCatchUp <= 0) continue

                val daysToCatchUp = daysMissed.coerceAtMost(maxCatchUp.toLong())

                var currentCheckDate = nextExecDate
                var caughtUp = 0
                var lastCatchUpExecDate: LocalDate? = null

                while (caughtUp < daysToCatchUp && (currentCheckDate.isBefore(localDateNow) || currentCheckDate.isEqual(localDateNow))) {
                    val validDay = when (rule.frequency) {
                        RecurringFrequency.WEEKLY_SPECIFIC_DAYS -> {
                            val todayJava = currentCheckDate.dayOfWeek
                            val dayMatch = rule.selectedDaysOfWeek?.any { java.time.DayOfWeek.valueOf(it.name) == todayJava } ?: false
                            if (dayMatch && rule.weeklyInterval > 1 && lastCatchUpExecDate != null) {
                                val isoWeeksDiff = getIsoWeek(currentCheckDate) - getIsoWeek(lastCatchUpExecDate)
                                isoWeeksDiff % rule.weeklyInterval.toLong() == 0L
                            } else dayMatch
                        }
                        RecurringFrequency.MONTHLY_SPECIFIC_DAYS -> {
                            rule.selectedDaysOfMonth?.any {
                                val targetDay = if (it > 0) it else currentCheckDate.lengthOfMonth() + 1 + it
                                currentCheckDate.dayOfMonth == targetDay
                            } ?: true
                        }
                        else -> true
                    }

                    if (validDay) {
                        val preferredHour = getPreferredHour(rule.preferredTime)
                        if (currentCheckDate.isEqual(localDateNow) && nowHour < preferredHour) {
                            currentCheckDate = currentCheckDate.plusDays(1)
                            caughtUp++
                            continue
                        }
                        val tempRule = rule.copy(nextExecutionDate = toMillis(startOfDayZoned(currentCheckDate, rule.preferredTime)))
                        val result = executeRule(tempRule)
                        results.add(result)
                        if (result.success) lastCatchUpExecDate = currentCheckDate
                    }

                    currentCheckDate = currentCheckDate.plusDays(1)
                    caughtUp++
                }

                recurringRepository.updateLastCheckedTimestamp(rule.id, now)
            }
        }

        return results
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
                        shouldReschedule = true, shouldSkip = false
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
                        shouldReschedule = true, shouldSkip = false
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
                        shouldReschedule = false, shouldSkip = true
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

        val catchUpResults = checkForMissedExecutions()
        results.addAll(catchUpResults)

        val allActiveRules = recurringRepository.getRulesDueForExecution(now)

        for (rule in allActiveRules) {
            if (!rule.isActive || rule.isPaused) continue

            val zdtNow = toZonedDateTime(now)
            val localDateNow = zdtNow.toLocalDate()
            val nowHour = zdtNow.hour
            val execDate = toZonedDateTime(rule.nextExecutionDate).toLocalDate()

            val shouldExecute = when (rule.frequency) {
                RecurringFrequency.WEEKLY_SPECIFIC_DAYS -> {
                    val todayJava = localDateNow.dayOfWeek
                    val dayMatches = rule.selectedDaysOfWeek?.any { java.time.DayOfWeek.valueOf(it.name) == todayJava } ?: false

                    if (dayMatches) {
                        val preferredHour = getPreferredHour(rule.preferredTime)
                        val timeReached = nowHour >= preferredHour

                        if (!timeReached) {
                            false
                        } else if (execDate.isEqual(localDateNow) || execDate.isBefore(localDateNow)) {
                            if (rule.lastExecutedDate != null) {
                                val lastWeek = getIsoWeek(toZonedDateTime(rule.lastExecutedDate).toLocalDate())
                                val currentWeek = getIsoWeek(localDateNow)
                                val isoWeeksDiff = currentWeek - lastWeek
                                isoWeeksDiff % rule.weeklyInterval.toLong() == 0L || isoWeeksDiff == 0L
                            } else true
                        } else false
                    } else {
                        if (rule.nextExecutionDate <= now && !rule.pendingRetry) {
                            val nextOccurrence = calculateNextExecutionDate(
                                now, rule.frequency, rule.interval,
                                rule.preferredTime, rule.weekdayAdjustment,
                                rule.selectedDaysOfWeek, rule.selectedDaysOfMonth,
                                rule.monthlyDayOption, rule.weeklyInterval
                            )
                            recurringRepository.updateNextExecutionAndChecked(rule.id, nextOccurrence, now)
                        }
                        false
                    }
                }
                RecurringFrequency.MONTHLY_SPECIFIC_DAYS -> {
                    val dayMatches = isExecutionDay(rule, now)
                    if (!dayMatches && rule.nextExecutionDate <= now && !rule.pendingRetry) {
                        val nextOccurrence = calculateNextExecutionDate(
                            now, rule.frequency, rule.interval,
                            rule.preferredTime, rule.weekdayAdjustment,
                            rule.selectedDaysOfWeek, rule.selectedDaysOfMonth,
                            rule.monthlyDayOption, rule.weeklyInterval
                        )
                        recurringRepository.updateNextExecutionAndChecked(rule.id, nextOccurrence, now)
                        false
                    } else dayMatches
                }
                else -> rule.nextExecutionDate <= now
            }

            if (shouldExecute) {
                val result = executeRule(rule)
                results.add(result)
            }
        }

        return results
    }

    suspend fun processDueRulesWithBalance(currentBalance: Double): List<ExecutionResult> {
        return processDueRules()
    }

    suspend fun smartReschedule(rule: RecurringRule, failureReason: String?): Long {
        val gracePeriodHours = when (rule.priority) {
            RecurringPriority.CRITICAL -> 2
            RecurringPriority.HIGH -> 6
            RecurringPriority.MEDIUM -> 24
            RecurringPriority.LOW -> 48
            RecurringPriority.OPTIONAL -> 72
        }

        val newDate = toZonedDateTime(rule.nextExecutionDate)
            .plusHours(gracePeriodHours.toLong())
            .toInstant().toEpochMilli()

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
                    val weeksPerMonth = 4.0 / rule.weeklyInterval.coerceAtLeast(1)
                    rule.amount * daysPerWeek * weeksPerMonth
                }
                RecurringFrequency.MONTHLY_SPECIFIC_DAYS -> {
                    val daysPerMonth = (rule.selectedDaysOfMonth?.size ?: 1).coerceAtLeast(1)
                    rule.amount * daysPerMonth
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