package com.rudra.smartworktracker.ui.screens.financials

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rudra.smartworktracker.data.entity.FinancialTransaction
import com.rudra.smartworktracker.data.entity.TransactionType
import com.rudra.smartworktracker.data.repository.TransactionRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

class FinancialStatementViewModel(private val transactionRepository: TransactionRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(FinancialsUiState())
    val uiState: StateFlow<FinancialsUiState> = _uiState.asStateFlow()

    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage: StateFlow<String?> = _snackbarMessage.asStateFlow()

    init {
        loadTransactions()
    }

    private fun loadTransactions() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            combine(
                transactionRepository.getAllTransactions(),
                _uiState.map { it.filter }.distinctUntilChanged(),
                _uiState.map { it.startDate }.distinctUntilChanged(),
                _uiState.map { it.endDate }.distinctUntilChanged(),
                _uiState.map { it.limit }.distinctUntilChanged()
            ) { transactions, filter, startDate, endDate, limit ->
                
                // 1. Calculate totals from ALL transactions (unfiltered) for the summary card
                val totalIncome = transactions.filter { 
                    it.type == TransactionType.INCOME || it.type == TransactionType.LOAN_RECEIVE 
                }.sumOf { it.amount }
                
                val totalExpenses = transactions.filter { 
                    it.type == TransactionType.EXPENSE || it.type == TransactionType.EMI_PAID 
                }.sumOf { it.amount }

                // 2. Apply Filters to the list
                var filtered = transactions

                // Type Filter
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

                // 3. Apply Limit (50 Income + 50 Expense by default, or simple limit)
                // If filter is ALL, we take up to 50 of each type if limit is default
                val limitedTransactions = if (filter == TransactionFilter.ALL && limit == 100) {
                    val incomes = filtered.filter { it.type == TransactionType.INCOME || it.type == TransactionType.LOAN_RECEIVE }.take(50)
                    val expenses = filtered.filter { it.type == TransactionType.EXPENSE || it.type == TransactionType.EMI_PAID }.take(50)
                    (incomes + expenses).sortedByDescending { it.date }
                } else {
                    filtered.take(limit)
                }

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
        _uiState.value = _uiState.value.copy(filter = filter)
    }

    fun setDateRange(start: LocalDate?, end: LocalDate?) {
        _uiState.value = _uiState.value.copy(
            startDate = start,
            endDate = end,
            filter = TransactionFilter.DATE_RANGE
        )
    }

    fun setLimit(newLimit: Int) {
        _uiState.value = _uiState.value.copy(limit = newLimit)
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
