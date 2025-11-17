package com.rudra.smartworktracker.ui.screens.financials

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rudra.smartworktracker.data.entity.FinancialTransaction
import com.rudra.smartworktracker.data.entity.TransactionType
import com.rudra.smartworktracker.data.repository.TransactionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

//enum class TransactionFilter {
//    ALL, INCOME, EXPENSE
//}

data class FinancialsUiState(
    val transactions: List<FinancialTransaction> = emptyList(),
    val totalIncome: Double = 0.0,
    val totalExpenses: Double = 0.0,
    val netFlow: Double = 0.0,
    val isLoading: Boolean = false,
    val filter: TransactionFilter = TransactionFilter.ALL,
    val errorMessage: String? = null
)

class FinancialStatementViewModel(private val transactionRepository: TransactionRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(FinancialsUiState())
    val uiState: StateFlow<FinancialsUiState> = _uiState.asStateFlow()

    init {
        loadTransactions()
    }

    private fun loadTransactions() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            combine(
                transactionRepository.getAllTransactions(),
                transactionRepository.getTotalIncome(),
                transactionRepository.getTotalExpenses(),
                _uiState.map { it.filter }
            ) { transactions, totalIncome, totalExpenses, filter ->
                val filteredTransactions = when (filter) {
                    TransactionFilter.INCOME -> transactions.filter { it.type == TransactionType.INCOME || it.type == TransactionType.LOAN_RECEIVE }
                    TransactionFilter.EXPENSE -> transactions.filter { it.type == TransactionType.EXPENSE || it.type == TransactionType.EMI_PAID }
                    TransactionFilter.ALL -> transactions
                }
                val netFlow = totalIncome - totalExpenses
                FinancialsUiState(
                    transactions = filteredTransactions,
                    totalIncome = totalIncome,
                    totalExpenses = totalExpenses,
                    netFlow = netFlow,
                    isLoading = false,
                    filter = filter
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
}
