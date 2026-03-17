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
import com.rudra.smartworktracker.data.repository.RecurringRepository
import com.rudra.smartworktracker.engine.RecurringEngine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Calendar

class RecurringViewModel(
    private val recurringRepository: RecurringRepository,
    private val recurringEngine: RecurringEngine
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(RecurringUiState())
    val uiState: StateFlow<RecurringUiState> = _uiState.asStateFlow()
    
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
            val result = recurringEngine.executeRule(rule)
            if (!result.success) {
                // Handle failure - could show a toast or update UI state
                _uiState.value = _uiState.value.copy(
                    lastExecutionError = result.reason
                )
            }
        }
    }
    
    fun skipTransaction(transaction: RecurringTransaction, reason: String? = "Skipped by user") {
        viewModelScope.launch {
            recurringRepository.skipTransaction(transaction.id, reason)
        }
    }
    
    fun refreshData() {
        loadData()
    }
    
    data class RecurringUiState(
        val rules: List<RecurringRule> = emptyList(),
        val activeRules: List<RecurringRule> = emptyList(),
        val allTransactions: List<RecurringTransaction> = emptyList(),
        val upcomingTransactions: List<RecurringTransaction> = emptyList(),
        val activeRulesCount: Int = 0,
        val totalIncomeThisMonth: Double = 0.0,
        val totalExpensesThisMonth: Double = 0.0,
        val lastExecutionError: String? = null,
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
            val engine = RecurringEngine(repository)
            @Suppress("UNCHECKED_CAST")
            return RecurringViewModel(repository, engine) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
