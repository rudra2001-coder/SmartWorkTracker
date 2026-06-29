package com.rudra.smartworktracker.ui.screens.recurring

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.rudra.smartworktracker.data.AppDatabase
import com.rudra.smartworktracker.data.entity.RecurringRule
import com.rudra.smartworktracker.data.entity.RecurringTransaction
import com.rudra.smartworktracker.data.entity.RecurringTransactionStatus
import com.rudra.smartworktracker.data.entity.TransactionType
import com.rudra.smartworktracker.data.repository.ExpenseRepository
import com.rudra.smartworktracker.data.repository.IncomeRepository
import com.rudra.smartworktracker.data.repository.RecurringRepository
import com.rudra.smartworktracker.data.repository.SavingsRepository
import com.rudra.smartworktracker.engine.FusionEngine
import com.rudra.smartworktracker.engine.MonthlyImpact
import com.rudra.smartworktracker.engine.PatternSuggestion
import com.rudra.smartworktracker.engine.RecurringEngine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar

class RecurringViewModel(
    private val recurringRepository: RecurringRepository,
    private val recurringEngine: RecurringEngine
) : ViewModel() {

    private val _uiState = MutableStateFlow(RecurringUiState())
    val uiState: StateFlow<RecurringUiState> = _uiState.asStateFlow()

    private val _executionHistory = MutableStateFlow<List<ExecutionHistory>>(emptyList())
    val executionHistory: StateFlow<List<ExecutionHistory>> = _executionHistory.asStateFlow()

    init {
        loadData()
        loadMonthlyImpact()
        loadPatternSuggestions()
    }

    private fun loadData() {
        viewModelScope.launch {
            recurringRepository.getAllRules().collect { rules ->
                _uiState.value = _uiState.value.copy(rules = rules)
                updateActiveRulesCount(rules)
            }
        }
        viewModelScope.launch {
            recurringRepository.getActiveRules().collect { activeRules ->
                _uiState.value = _uiState.value.copy(activeRules = activeRules)
            }
        }
        viewModelScope.launch {
            recurringRepository.getPausedRules().collect { pausedRules ->
                _uiState.value = _uiState.value.copy(pausedRules = pausedRules)
            }
        }
        viewModelScope.launch {
            recurringRepository.getAllTransactions().collect { transactions ->
                _uiState.value = _uiState.value.copy(allTransactions = transactions)
            }
        }
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val thirtyDaysLater = now + (30L * 24 * 60 * 60 * 1000)
            recurringRepository.getTransactionsBetweenDates(now, thirtyDaysLater).collect { transactions ->
                _uiState.value = _uiState.value.copy(upcomingTransactions = transactions)
            }
        }
        viewModelScope.launch {
            calculateMonthlyTotals()
        }
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val startOfMonth = getStartOfMonth()
            val cal = Calendar.getInstance().apply { add(Calendar.MONTH, 1) }
            val endOfMonth = cal.timeInMillis

            recurringRepository.getExecutedTransactionCount(startOfMonth, endOfMonth).collect { count ->
                _uiState.value = _uiState.value.copy(executedThisMonth = count)
            }
        }
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val startOfMonth = getStartOfMonth()
            val cal = Calendar.getInstance().apply { add(Calendar.MONTH, 1) }
            val endOfMonth = cal.timeInMillis

            recurringRepository.getFailedTransactionCount(startOfMonth, endOfMonth).collect { count ->
                _uiState.value = _uiState.value.copy(failedThisMonth = count)
            }
        }
    }

    private fun updateActiveRulesCount(rules: List<RecurringRule>) {
        val activeCount = rules.count { it.isActive && !it.isPaused }
        val pausedCount = rules.count { it.isActive && it.isPaused }
        _uiState.value = _uiState.value.copy(
            activeRulesCount = activeCount,
            pausedRulesCount = pausedCount
        )
    }

    private fun loadMonthlyImpact() {
        viewModelScope.launch {
            try {
                val impact = recurringEngine.calculateTotalMonthlyImpact()
                _uiState.value = _uiState.value.copy(monthlyImpact = impact)
            } catch (_: Exception) {}
        }
    }

    private fun loadPatternSuggestions() {
        viewModelScope.launch {
            try {
                val suggestions = recurringEngine.detectPatterns()
                _uiState.value = _uiState.value.copy(patternSuggestions = suggestions)
            } catch (_: Exception) {}
        }
    }

    private suspend fun calculateMonthlyTotals() {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        val startOfMonth = calendar.timeInMillis
        calendar.add(Calendar.MONTH, 1)
        val endOfMonth = calendar.timeInMillis

        val rules = recurringRepository.getActiveRules().first()

        var totalIncome = 0.0
        var totalExpenses = 0.0
        var totalTransfers = 0.0

        rules.forEach { rule ->
            when (rule.transactionType) {
                TransactionType.INCOME -> totalIncome += rule.amount
                TransactionType.EXPENSE -> totalExpenses += rule.amount
                TransactionType.TRANSFER -> totalTransfers += rule.amount
                else -> {}
            }
        }

        _uiState.value = _uiState.value.copy(
            totalIncomeThisMonth = totalIncome,
            totalExpensesThisMonth = totalExpenses,
            totalTransfersThisMonth = totalTransfers
        )
    }

    fun addRule(rule: RecurringRule) {
        viewModelScope.launch {
            recurringRepository.insertRule(rule)
            loadMonthlyImpact()
        }
    }

    fun updateRule(rule: RecurringRule) {
        viewModelScope.launch {
            recurringRepository.updateRule(rule)
            loadMonthlyImpact()
        }
    }

    fun deleteRule(rule: RecurringRule) {
        viewModelScope.launch {
            recurringRepository.deleteRule(rule)
            loadMonthlyImpact()
        }
    }

    fun toggleRuleActive(rule: RecurringRule) {
        viewModelScope.launch {
            recurringRepository.updateRuleActiveStatus(rule.id, !rule.isActive)
            loadMonthlyImpact()
        }
    }

    fun toggleRulePaused(rule: RecurringRule) {
        viewModelScope.launch {
            val newPaused = !rule.isPaused
            recurringRepository.updateRulePausedStatus(rule.id, newPaused)
            if (!newPaused) {
                val nextDate = recurringEngine.calculateNextExecutionDate(
                    currentDate = System.currentTimeMillis(),
                    frequency = rule.frequency,
                    interval = rule.interval,
                    preferredTime = rule.preferredTime,
                    weekdayAdjustment = rule.weekdayAdjustment,
                    selectedDaysOfWeek = rule.selectedDaysOfWeek,
                    selectedDaysOfMonth = rule.selectedDaysOfMonth,
                    monthlyDayOption = rule.monthlyDayOption,
                    weeklyInterval = rule.weeklyInterval
                )
                if (rule.endDate != null && nextDate > rule.endDate) {
                    recurringRepository.updateRuleActiveStatus(rule.id, false)
                } else {
                    recurringRepository.updateNextExecutionDate(rule.id, nextDate)
                }
            }
            loadMonthlyImpact()
        }
    }

    fun executeRuleNow(rule: RecurringRule) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val result = recurringEngine.executeRule(rule)
            _uiState.value = _uiState.value.copy(isLoading = false)
            if (!result.success) {
                _uiState.value = _uiState.value.copy(
                    lastExecutionError = result.reason
                )
            } else {
                loadMonthlyImpact()
            }
        }
    }

    fun checkAndExecuteDueRules() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val results = recurringEngine.processDueRules()
            _uiState.value = _uiState.value.copy(isLoading = false)

            val failedCount = results.count { !it.success }
            if (failedCount > 0) {
                _uiState.value = _uiState.value.copy(
                    lastExecutionError = "$failedCount transaction(s) failed"
                )
            }
            loadMonthlyImpact()
        }
    }

    fun skipTransaction(transaction: RecurringTransaction, reason: String? = "Skipped by user") {
        viewModelScope.launch {
            recurringRepository.skipTransaction(transaction.id, reason)
        }
    }

    fun refreshData() {
        loadData()
        loadMonthlyImpact()
    }

    fun manualExecuteRules(rules: List<RecurringRule>) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            var totalIncome = 0.0
            var totalExpenses = 0.0
            val failedRules = mutableMapOf<String, String>()
            var successCount = 0

            rules.forEach { rule ->
                val result = recurringEngine.executeRule(rule)

                if (result.success) {
                    successCount++
                    when (rule.transactionType) {
                        TransactionType.INCOME -> totalIncome += rule.amount
                        TransactionType.EXPENSE -> totalExpenses += rule.amount
                        else -> {}
                    }
                } else {
                    failedRules[rule.name] = result.reason ?: "Unknown error"
                }
            }
            val totalAmount = totalIncome + totalExpenses

            val executionResult = ExecutionResult(
                success = successCount == rules.size,
                successCount = successCount,
                failureCount = rules.size - successCount,
                totalAmount = totalAmount,
                totalIncome = totalIncome,
                totalExpenses = totalExpenses,
                failedRules = failedRules,
                timestamp = System.currentTimeMillis()
            )

            val history = ExecutionHistory(
                id = System.currentTimeMillis(),
                timestamp = System.currentTimeMillis(),
                success = executionResult.success,
                successCount = executionResult.successCount,
                totalCount = rules.size,
                totalAmount = executionResult.totalAmount,
                failedRules = failedRules
            )

            _executionHistory.update { listOf(history) + it }

            _uiState.value = _uiState.value.copy(
                isLoading = false,
                lastExecutionResult = executionResult
            )

            loadMonthlyImpact()
        }
    }

    fun clearExecutionResult() {
        _uiState.value = _uiState.value.copy(lastExecutionResult = null)
    }

    fun clearExecutionError() {
        _uiState.value = _uiState.value.copy(lastExecutionError = null)
    }

    private fun getStartOfMonth(): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    data class ExecutionResult(
        val success: Boolean,
        val successCount: Int,
        val failureCount: Int,
        val totalAmount: Double,
        val totalIncome: Double,
        val totalExpenses: Double,
        val failedRules: Map<String, String>,
        val timestamp: Long,
        val errorMessage: String? = null
    )

    data class ExecutionHistory(
        val id: Long,
        val timestamp: Long,
        val success: Boolean,
        val successCount: Int,
        val totalCount: Int,
        val totalAmount: Double,
        val failedRules: Map<String, String>
    )

    data class RecurringUiState(
        val rules: List<RecurringRule> = emptyList(),
        val activeRules: List<RecurringRule> = emptyList(),
        val pausedRules: List<RecurringRule> = emptyList(),
        val allTransactions: List<RecurringTransaction> = emptyList(),
        val upcomingTransactions: List<RecurringTransaction> = emptyList(),
        val activeRulesCount: Int = 0,
        val pausedRulesCount: Int = 0,
        val totalIncomeThisMonth: Double = 0.0,
        val totalExpensesThisMonth: Double = 0.0,
        val totalTransfersThisMonth: Double = 0.0,
        val executedThisMonth: Int = 0,
        val failedThisMonth: Int = 0,
        val monthlyImpact: MonthlyImpact? = null,
        val patternSuggestions: List<PatternSuggestion> = emptyList(),
        val lastExecutionError: String? = null,
        val lastExecutionResult: ExecutionResult? = null,
        val isLoading: Boolean = false
    )
}

class RecurringViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RecurringViewModel::class.java)) {
            val database = AppDatabase.getDatabase(context)
            val repository = RecurringRepository(
                database.recurringRuleDao(),
                database.recurringTransactionDao()
            )
            val incomeRepository = IncomeRepository(database.incomeDao(), database.accountDao())
            val expenseRepository = ExpenseRepository(database.expenseDao(), database.accountDao())
            val savingsRepository = SavingsRepository(database.savingsDao(), database.accountDao(), database.financialTransactionDao())
            val fusionEngine = FusionEngine(database.accountDao(), database.financialTransactionDao())
            val engine = RecurringEngine(
                repository, incomeRepository, expenseRepository,
                database.accountDao(), savingsRepository, fusionEngine
            )
            @Suppress("UNCHECKED_CAST")
            return RecurringViewModel(repository, engine) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
