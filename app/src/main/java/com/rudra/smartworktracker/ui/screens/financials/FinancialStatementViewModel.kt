package com.rudra.smartworktracker.ui.screens.financials

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rudra.smartworktracker.data.entity.FinancialTransaction
import com.rudra.smartworktracker.data.entity.TransactionType
import com.rudra.smartworktracker.data.repository.TransactionRepository
import com.rudra.smartworktracker.data.repository.IncomeRepository
import com.rudra.smartworktracker.data.repository.ExpenseRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

class FinancialStatementViewModel(
    private val transactionRepository: TransactionRepository,
    private val incomeRepository: IncomeRepository,
    private val expenseRepository: ExpenseRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(FinancialsUiState())
    val uiState: StateFlow<FinancialsUiState> = _uiState.asStateFlow()

    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage: StateFlow<String?> = _snackbarMessage.asStateFlow()

    private val filterFlow = MutableStateFlow(TransactionFilter.ALL)
    private val startDateFlow = MutableStateFlow<LocalDate?>(null)
    private val endDateFlow = MutableStateFlow<LocalDate?>(null)
    private val limitFlow = MutableStateFlow(100)

    init {
        loadTransactions()
    }

    private fun loadTransactions() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            combine(
                transactionRepository.getAllTransactions(),
                incomeRepository.getAllIncomes(),
                expenseRepository.getAllExpenses(),
                filterFlow,
                startDateFlow,
                endDateFlow,
                limitFlow
            ) { values ->
                @Suppress("UNCHECKED_CAST")
                val transactions = values[0] as List<FinancialTransaction>
                @Suppress("UNCHECKED_CAST")
                val incomes = values[1] as List<com.rudra.smartworktracker.data.entity.Income>
                @Suppress("UNCHECKED_CAST")
                val expenses = values[2] as List<com.rudra.smartworktracker.model.Expense>
                val filter = values[3] as TransactionFilter
                val startDate = values[4] as LocalDate?
                val endDate = values[5] as LocalDate?
                val limit = values[6] as Int
                
                // Convert incomes to unified transactions
                val incomeTransactions = incomes.map { income ->
                    UnifiedTransaction(
                        id = "income_${income.id}",
                        amount = income.amount,
                        type = TransactionType.INCOME,
                        description = income.description,
                        category = income.category,
                        date = income.timestamp,
                        source = "Income"
                    )
                }
                
                // Convert expenses to unified transactions
                val expenseTransactions = expenses.map { expense ->
                    UnifiedTransaction(
                        id = "expense_${expense.id}",
                        amount = expense.amount,
                        type = TransactionType.EXPENSE,
                        description = expense.merchant ?: expense.notes ?: "Expense",
                        category = expense.category.displayName,
                        date = expense.timestamp,
                        source = "Expense"
                    )
                }
                
                // Convert financial transactions
                val financialTransactions = transactions.map { ft ->
                    UnifiedTransaction(
                        id = ft.id.toString(),
                        amount = ft.amount,
                        type = ft.type,
                        description = ft.note.ifEmpty { ft.type.name },
                        category = ft.category ?: "Other",
                        date = ft.date,
                        source = "Financial"
                    )
                }
                
                // Combine all transactions
                val allTransactions = (incomeTransactions + expenseTransactions + financialTransactions)
                    .sortedByDescending { it.date }
                
                // Calculate totals
                val totalIncome = allTransactions.filter { 
                    it.type == TransactionType.INCOME || it.type == TransactionType.LOAN_RECEIVE 
                }.sumOf { it.amount }
                
                val totalExpenses = allTransactions.filter { 
                    it.type == TransactionType.EXPENSE || it.type == TransactionType.EMI_PAID 
                }.sumOf { it.amount }

                // Apply Filters
                var filtered = allTransactions

                filtered = when (filter) {
                    TransactionFilter.INCOME -> filtered.filter { 
                        it.type == TransactionType.INCOME || it.type == TransactionType.LOAN_RECEIVE 
                    }
                    TransactionFilter.EXPENSE -> filtered.filter { 
                        it.type == TransactionType.EXPENSE || it.type == TransactionType.EMI_PAID 
                    }
                    else -> filtered
                }

                // Date Filter
                if (filter == TransactionFilter.DATE_RANGE && startDate != null && endDate != null) {
                    val startMillis = startDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
                    val endMillis = endDate.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
                    filtered = filtered.filter { it.date in startMillis until endMillis }
                }

                // Apply Limit
                val limitedTransactions = filtered.take(limit)

                FinancialsUiState(
                    transactions = limitedTransactions,
                    totalIncome = totalIncome,
                    totalExpenses = totalExpenses,
                    netFlow = totalIncome - totalExpenses,
                    isLoading = false,
                    filter = filter,
                    startDate = startDate,
                    endDate = endDate,
                    limit = limit
                )
            }.catch { exception ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = exception.message
                )
            }.collect { 
                _uiState.value = it
            }
        }
    }

    fun setFilter(filter: TransactionFilter) {
        filterFlow.value = filter
    }

    fun setDateRange(start: LocalDate?, end: LocalDate?) {
        startDateFlow.value = start
        endDateFlow.value = end
        filterFlow.value = TransactionFilter.DATE_RANGE
    }

    fun setLimit(newLimit: Int) {
        limitFlow.value = newLimit
    }

    fun deleteTransaction(transaction: FinancialTransaction) {
        viewModelScope.launch {
            try {
                transactionRepository.deleteTransaction(transaction)
                _snackbarMessage.value = "Transaction deleted successfully"
            } catch (e: Exception) {
                _snackbarMessage.value = "Failed to delete transaction: ${e.message}"
            }
        }
    }

    fun clearSnackbar() {
        _snackbarMessage.value = null
    }
}

/**
 * Unified transaction model combining Income, Expense, and FinancialTransaction
 */
data class UnifiedTransaction(
    val id: String,
    val amount: Double,
    val type: TransactionType,
    val description: String,
    val category: String,
    val date: Long,
    val source: String
)
