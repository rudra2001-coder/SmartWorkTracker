package com.rudra.smartworktracker.engine

import com.rudra.smartworktracker.data.entity.AccountType
import com.rudra.smartworktracker.data.entity.DayOfWeek
import com.rudra.smartworktracker.data.entity.Income
import com.rudra.smartworktracker.data.entity.RecurringFrequency
import com.rudra.smartworktracker.data.entity.RecurringPriority
import com.rudra.smartworktracker.data.entity.RecurringRule
import com.rudra.smartworktracker.data.entity.RecurringTransaction
import com.rudra.smartworktracker.data.entity.RecurringTransactionStatus
import com.rudra.smartworktracker.data.entity.SyncStatus
import com.rudra.smartworktracker.data.entity.TransactionType
import com.rudra.smartworktracker.data.repository.IncomeRepository
import com.rudra.smartworktracker.data.repository.RecurringRepository
import com.rudra.smartworktracker.model.Expense
import com.rudra.smartworktracker.model.ExpenseCategory
import kotlinx.coroutines.flow.first
import java.util.Calendar
import java.util.UUID

/**
 * Core engine for managing recurring transactions.
 * Handles scheduling, execution, rescheduling, and balance protection.
 */
class RecurringEngine(
    private val recurringRepository: RecurringRepository,
    private val incomeRepository: IncomeRepository,
    private val expenseRepository: com.rudra.smartworktracker.data.repository.ExpenseRepository,
    private val transactionRepository: com.rudra.smartworktracker.data.repository.TransactionRepository? = null
) {
    companion object {
        // Default balance placeholder - in production, this would come from account balance
        private const val DEFAULT_MINIMUM_BALANCE = 0.0
    }
    
    /**
     * Calculate the next execution date based on frequency and interval
     */
    fun calculateNextExecutionDate(
        currentDate: Long,
        frequency: RecurringFrequency,
        interval: Int = 1,
        preferredTime: com.rudra.smartworktracker.data.entity.PreferredTime = com.rudra.smartworktracker.data.entity.PreferredTime.MORNING
    ): Long {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = currentDate
        
        // Set preferred time
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
                calculateNextSpecificDay(calendar)
            }
        }
        
        return calendar.timeInMillis
    }
    
    /**
     * Calculate next execution date for specific days of week
     */
    fun calculateNextExecutionDateWithDays(
        currentDate: Long,
        selectedDays: List<DayOfWeek>,
        preferredTime: com.rudra.smartworktracker.data.entity.PreferredTime = com.rudra.smartworktracker.data.entity.PreferredTime.MORNING
    ): Long {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = currentDate
        return calculateNextSpecificDay(calendar, selectedDays, preferredTime)
    }
    
    private fun calculateNextSpecificDay(
        calendar: Calendar,
        selectedDays: List<DayOfWeek>? = null,
        preferredTime: com.rudra.smartworktracker.data.entity.PreferredTime = com.rudra.smartworktracker.data.entity.PreferredTime.MORNING
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
            return calendar.timeInMillis
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
        
        return calendar.timeInMillis
    }
    
    /**
     * Check if today is an execution day for weekly specific days
     */
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
    
    /**
     * Execute a recurring rule - generates a transaction and creates actual income/expense
     */
    suspend fun executeRule(rule: RecurringRule, currentBalance: Double = DEFAULT_MINIMUM_BALANCE): ExecutionResult {
        // Check if rule should be executed based on priority and balance
        val balanceCheck = checkBalanceProtection(rule, currentBalance)
        if (!balanceCheck.canExecute) {
            return ExecutionResult(
                success = false,
                reason = balanceCheck.reason ?: "Insufficient balance",
                shouldReschedule = balanceCheck.shouldReschedule,
                shouldSkip = balanceCheck.shouldSkip
            )
        }
        
        // Create the recurring transaction instance
        val transaction = createTransactionFromRule(rule)
        val transactionId = recurringRepository.insertTransaction(transaction)
        
        return try {
            // Execute the actual transaction based on type
            val result = when (rule.transactionType) {
                TransactionType.INCOME -> executeIncome(rule, transactionId)
                TransactionType.EXPENSE -> executeExpense(rule, transactionId)
                TransactionType.SAVINGS_ADD -> executeSavings(rule, transactionId)
                TransactionType.TRANSFER -> executeTransfer(rule, transactionId)
                else -> ExecutionResult(success = false, reason = "Unsupported transaction type")
            }
            
            // Update transaction status
            if (result.success) {
                recurringRepository.markTransactionExecuted(
                    transactionId,
                    RecurringTransactionStatus.EXECUTED
                )
                
                // Update next execution date for the rule
                val nextDate = calculateNextExecutionDate(
                    rule.nextExecutionDate,
                    rule.frequency,
                    rule.interval,
                    rule.preferredTime
                )
                
                // Check if we've passed the end date
                if (rule.endDate != null && nextDate > rule.endDate) {
                    recurringRepository.updateRuleActiveStatus(rule.id, false)
                } else {
                    recurringRepository.updateNextExecutionDate(rule.id, nextDate)
                }
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
    
    /**
     * Check if the transaction can be executed based on balance and priority
     */
    private fun checkBalanceProtection(rule: RecurringRule, currentBalance: Double): BalanceCheckResult {
        // Income always can be executed
        if (rule.transactionType == TransactionType.INCOME) {
            return BalanceCheckResult(canExecute = true, shouldReschedule = false, shouldSkip = false)
        }
        
        // For expenses, check balance
        val minimumRequired = rule.minimumBalanceRequired ?: (rule.amount + DEFAULT_MINIMUM_BALANCE)
        
        return when (rule.priority) {
            RecurringPriority.CRITICAL -> {
                // Critical transactions must execute regardless of balance
                BalanceCheckResult(canExecute = true, shouldReschedule = false, shouldSkip = false)
            }
            RecurringPriority.HIGH -> {
                if (currentBalance >= minimumRequired) {
                    BalanceCheckResult(canExecute = true, shouldReschedule = false, shouldSkip = false)
                } else {
                    BalanceCheckResult(
                        canExecute = false,
                        reason = "Insufficient balance for high priority transaction",
                        shouldReschedule = true,
                        shouldSkip = false
                    )
                }
            }
            RecurringPriority.MEDIUM, RecurringPriority.LOW, RecurringPriority.OPTIONAL -> {
                if (currentBalance >= minimumRequired) {
                    BalanceCheckResult(canExecute = true, shouldReschedule = false, shouldSkip = false)
                } else {
                    BalanceCheckResult(
                        canExecute = false,
                        reason = "Insufficient balance",
                        shouldReschedule = rule.priority != RecurringPriority.OPTIONAL,
                        shouldSkip = rule.priority == RecurringPriority.OPTIONAL
                    )
                }
            }
        }
    }
    
    /**
     * Create a recurring transaction instance from a rule
     */
    private fun createTransactionFromRule(rule: RecurringRule): RecurringTransaction {
        return RecurringTransaction(
            ruleId = rule.id,
            uuid = UUID.randomUUID().toString(),
            name = rule.name,
            description = rule.description,
            transactionType = rule.transactionType,
            amount = rule.amount,
            category = rule.category,
            sourceAccount = rule.sourceAccount,
            destinationAccount = rule.destinationAccount,
            scheduledDate = rule.nextExecutionDate,
            status = if (rule.autoExecute) RecurringTransactionStatus.PENDING else RecurringTransactionStatus.CONFIRMED
        )
    }
    
    /**
     * Execute income transaction - inserts actual Income to database
     */
    private suspend fun executeIncome(rule: RecurringRule, transactionId: Long): ExecutionResult {
        return try {
            val income = Income(
                amount = rule.amount,
                description = rule.name,
                category = rule.category ?: "Salary",
                timestamp = System.currentTimeMillis(),
                source = rule.sourceAccount.name,
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
    
    /**
     * Execute expense transaction - inserts actual Expense to database
     */
    private suspend fun executeExpense(rule: RecurringRule, transactionId: Long): ExecutionResult {
        return try {
            val expense = Expense(
                id = UUID.randomUUID().toString(),
                amount = rule.amount,
                category = mapCategory(rule.category),
                merchant = rule.name,
                notes = rule.description,
                timestamp = System.currentTimeMillis(),
                syncStatus = SyncStatus.LOCAL_ONLY
            )
            
            expenseRepository.insertExpense(expense)
            
            ExecutionResult(
                success = true,
                reason = "Expense added successfully",
                relatedExpenseId = expense.id.hashCode().toLong()
            )
        } catch (e: Exception) {
            ExecutionResult(success = false, reason = e.message)
        }
    }
    
    /**
     * Execute savings transaction
     */
    private suspend fun executeSavings(rule: RecurringRule, transactionId: Long): ExecutionResult {
        // Similar to income, but would create a savings entry
        return ExecutionResult(success = true, reason = "Savings executed successfully")
    }
    
    /**
     * Execute transfer transaction
     */
    private suspend fun executeTransfer(rule: RecurringRule, transactionId: Long): ExecutionResult {
        // Would create a financial transaction with TRANSFER type
        return ExecutionResult(success = true, reason = "Transfer executed successfully")
    }
    
    /**
     * Get upcoming transactions for the next N days
     */
    suspend fun getUpcomingTransactions(days: Int = 30): List<RecurringTransaction> {
        val now = System.currentTimeMillis()
        val endDate = now + (days.toLong() * 24 * 60 * 60 * 1000)
        
        return recurringRepository.getPendingTransactionsDue(endDate)
    }
    
    /**
     * Process all due rules
     */
    suspend fun processDueRules(currentBalance: Double = DEFAULT_MINIMUM_BALANCE): List<ExecutionResult> {
        val results = mutableListOf<ExecutionResult>()
        val now = System.currentTimeMillis()
        val allActiveRules = recurringRepository.getRulesDueForExecution(now)
        
        for (rule in allActiveRules) {
            if (rule.isActive) {
                // Check if today is an execution day for weekly specific days
                if (rule.frequency == RecurringFrequency.WEEKLY_SPECIFIC_DAYS) {
                    if (isExecutionDay(rule, now)) {
                        val result = executeRule(rule, currentBalance)
                        results.add(result)
                    }
                } else {
                    // For other frequencies, check if nextExecutionDate has passed
                    if (rule.nextExecutionDate <= now) {
                        val result = executeRule(rule, currentBalance)
                        results.add(result)
                    }
                }
            }
        }
        
        return results
    }
    
    /**
     * Smart reschedule - adjust execution date based on failure
     */
    suspend fun smartReschedule(rule: RecurringRule, failureReason: String?): Long {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = rule.nextExecutionDate
        
        // Add a grace period based on priority
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
    
    /**
     * Map string category to ExpenseCategory
     */
    private fun mapCategory(category: String?): ExpenseCategory {
        return when (category?.uppercase()) {
            "MEAL" -> ExpenseCategory.MEAL
            "TRANSPORT" -> ExpenseCategory.TRANSPORT
            "ENTERTAINMENT" -> ExpenseCategory.ENTERTAINMENT
            "BILLS" -> ExpenseCategory.BILLS
            "SHOPPING" -> ExpenseCategory.SHOPPING
            else -> ExpenseCategory.OTHER
        }
    }
    
    /**
     * Pattern detection - analyze transactions to suggest recurring rules
     */
    suspend fun detectPatterns(): List<PatternSuggestion> {
        val suggestions = mutableListOf<PatternSuggestion>()
        
        // Get recent transactions to analyze
        // This would analyze patterns like:
        // - Same amount from same source
        // - Regular intervals
        // - Suggest rules based on historical data
        
        return suggestions
    }
}

/**
 * Result of executing a recurring rule
 */
data class ExecutionResult(
    val success: Boolean,
    val reason: String? = null,
    val shouldReschedule: Boolean = false,
    val shouldSkip: Boolean = false,
    val relatedIncomeId: Long? = null,
    val relatedExpenseId: Long? = null,
    val relatedFinancialTransactionId: Int? = null
)

/**
 * Balance check result
 */
data class BalanceCheckResult(
    val canExecute: Boolean,
    val reason: String? = null,
    val shouldReschedule: Boolean = false,
    val shouldSkip: Boolean = false
)

/**
 * Pattern suggestion from analysis
 */
data class PatternSuggestion(
    val name: String,
    val amount: Double,
    val frequency: RecurringFrequency,
    val category: String,
    val confidence: Float
)
