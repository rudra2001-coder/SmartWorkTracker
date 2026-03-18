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
    private val recurringEngine: RecurringEngine,
    private val incomeRepository: IncomeRepository? = null,
    private val expenseRepository: ExpenseRepository? = null
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(RecurringUiState())
    val uiState: StateFlow<RecurringUiState> = _uiState.asStateFlow()
    
    private val _executionHistory = MutableStateFlow<List<ExecutionHistory>>(emptyList())
    val executionHistory: StateFlow<List<ExecutionHistory>> = _executionHistory.asStateFlow()
    
    init {
        loadData()
    }
    
    private fun loadData() {
        viewModelScope.launch {
            // Load all rules
            recurringRepository.getAllRules().collect { rules ->
                _uiState.value = _uiState.value.copy(rules = rules)
                updateActiveRulesCount(rules)
            }
        }
        
        viewModelScope.launch {
            // Load active rules
            recurringRepository.getActiveRules().collect { activeRules ->
                _uiState.value = _uiState.value.copy(activeRules = activeRules)
            }
        }
        
        viewModelScope.launch {
            // Load all transactions
            recurringRepository.getAllTransactions().collect { transactions ->
                _uiState.value = _uiState.value.copy(allTransactions = transactions)
            }
        }
        
        viewModelScope.launch {
            // Load upcoming transactions (next 30 days)
            val now = System.currentTimeMillis()
            val thirtyDaysLater = now + (30L * 24 * 60 * 60 * 1000)
            recurringRepository.getTransactionsBetweenDates(now, thirtyDaysLater).collect { transactions ->
                _uiState.value = _uiState.value.copy(upcomingTransactions = transactions)
            }
        }
        
        viewModelScope.launch {
            // Calculate monthly totals
            calculateMonthlyTotals()
        }
    }
    
    private fun updateActiveRulesCount(rules: List<RecurringRule>) {
        val activeCount = rules.count { it.isActive }
        _uiState.value = _uiState.value.copy(activeRulesCount = activeCount)
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
        
        // Get active rules and calculate expected amounts
        val rules = recurringRepository.getActiveRules().first()
        
        var totalIncome = 0.0
        var totalExpenses = 0.0
        
        rules.forEach { rule ->
            when (rule.transactionType) {
                TransactionType.INCOME -> totalIncome += rule.amount
                TransactionType.EXPENSE -> totalExpenses += rule.amount
                else -> {}
            }
        }
        
        _uiState.value = _uiState.value.copy(
            totalIncomeThisMonth = totalIncome,
            totalExpensesThisMonth = totalExpenses
        )
    }
    
    fun addRule(rule: RecurringRule) {
        viewModelScope.launch {
            val id = recurringRepository.insertRule(rule)
            // The engine will handle calculating next execution date
        }
    }
    
    fun updateRule(rule: RecurringRule) {
        viewModelScope.launch {
            recurringRepository.updateRule(rule)
        }
    }
    
    fun deleteRule(rule: RecurringRule) {
        viewModelScope.launch {
            recurringRepository.deleteRule(rule)
        }
    }
    
    fun toggleRuleActive(rule: RecurringRule) {
        viewModelScope.launch {
            recurringRepository.updateRuleActiveStatus(rule.id, !rule.isActive)
        }
    }
    
    fun executeRuleNow(rule: RecurringRule) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val currentBalance = calculateCurrentBalance()
            val result = recurringEngine.executeRule(rule, currentBalance)
            _uiState.value = _uiState.value.copy(isLoading = false)
            if (!result.success) {
                _uiState.value = _uiState.value.copy(
                    lastExecutionError = result.reason
                )
            }
        }
    }
    
    fun checkAndExecuteDueRules() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val currentBalance = calculateCurrentBalance()
            val results = recurringEngine.processDueRules(currentBalance)
            _uiState.value = _uiState.value.copy(isLoading = false)
            
            val failedCount = results.count { !it.success }
            if (failedCount > 0) {
                _uiState.value = _uiState.value.copy(
                    lastExecutionError = "$failedCount transaction(s) failed"
                )
            }
        }
    }
    
    private suspend fun calculateCurrentBalance(): Double {
        return try {
            val now = System.currentTimeMillis()
            val startOfMonth = getStartOfMonth()
            
            val totalIncome = incomeRepository?.getTotalIncomeBetween(startOfMonth, now)?.first() ?: 0.0
            val totalExpenses = expenseRepository?.getTotalExpensesBetween(startOfMonth, now)?.first() ?: 0.0
            
            totalIncome - totalExpenses
        } catch (e: Exception) {
            0.0
        }
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
    
    fun skipTransaction(transaction: RecurringTransaction, reason: String? = "Skipped by user") {
        viewModelScope.launch {
            recurringRepository.skipTransaction(transaction.id, reason)
        }
    }
    
    fun refreshData() {
        loadData()
    }
    
    fun manualExecuteRules(rules: List<RecurringRule>) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            var totalIncome = 0.0
            var totalExpenses = 0.0
            val failedRules = mutableMapOf<String, String>()
            var successCount = 0
            
            rules.forEach { rule ->
                val currentBalance = calculateCurrentBalance()
                val result = recurringEngine.executeRule(rule, currentBalance)
                
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
        }
    }
    
    fun clearExecutionResult() {
        _uiState.value = _uiState.value.copy(lastExecutionResult = null)
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
        val allTransactions: List<RecurringTransaction> = emptyList(),
        val upcomingTransactions: List<RecurringTransaction> = emptyList(),
        val activeRulesCount: Int = 0,
        val totalIncomeThisMonth: Double = 0.0,
        val totalExpensesThisMonth: Double = 0.0,
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
            val incomeRepository = IncomeRepository(database.incomeDao())
            val expenseRepository = ExpenseRepository(database.expenseDao())
            val engine = RecurringEngine(repository, incomeRepository, expenseRepository)
            @Suppress("UNCHECKED_CAST")
            return RecurringViewModel(repository, engine, incomeRepository, expenseRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
