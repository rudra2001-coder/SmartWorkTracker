package com.rudra.smartworktracker.ui.screens.financials

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rudra.smartworktracker.data.entity.FinancialTransaction
import com.rudra.smartworktracker.data.entity.Income
import com.rudra.smartworktracker.data.entity.TransactionType
import com.rudra.smartworktracker.data.repository.TransactionRepository
import com.rudra.smartworktracker.data.repository.IncomeRepository
import com.rudra.smartworktracker.data.repository.ExpenseRepository
import com.rudra.smartworktracker.model.Expense
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
                val incomes = values[1] as List<Income>
                @Suppress("UNCHECKED_CAST")
                val expenses = values[2] as List<Expense>
                val filter = values[3] as TransactionFilter
                val startDate = values[4] as LocalDate?
                val endDate = values[5] as LocalDate?
                val limit = values[6] as Int
                
                // Convert incomes to unified transactions (double-entry: Cash/Bank DEBIT, Income CREDIT)
                val incomeTransactions = incomes.flatMap { income ->
                    listOf(
                        // DEBIT entry (money comes in - increases asset/liability)
                        UnifiedTransaction(
                            id = "income_${income.id}",
                            amount = income.amount,
                            type = TransactionType.INCOME,
                            description = income.description,
                            category = income.category,
                            date = income.timestamp,
                            source = "Income",
                            entryType = EntryType.DEBIT,
                            debitAccount = "Cash/Bank",
                            creditAccount = "Income"
                        ),
                        // CREDIT entry (income earned)
                        UnifiedTransaction(
                            id = "income_${income.id}_credit",
                            amount = income.amount,
                            type = TransactionType.INCOME,
                            description = "${income.description} (Credit)",
                            category = income.category,
                            date = income.timestamp,
                            source = "Income",
                            entryType = EntryType.CREDIT,
                            debitAccount = null,
                            creditAccount = "Income"
                        )
                    )
                }
                
                // Convert expenses to unified transactions (double-entry: Expense DEBIT, Cash/Credit CREDIT)
                val expenseTransactions = expenses.flatMap { expense ->
                    listOf(
                        // DEBIT entry (expense incurred)
                        UnifiedTransaction(
                            id = "expense_${expense.id}",
                            amount = expense.amount,
                            type = TransactionType.EXPENSE,
                            description = expense.merchant ?: expense.notes ?: "Expense",
                            category = expense.category.displayName,
                            date = expense.timestamp,
                            source = "Expense",
                            entryType = EntryType.DEBIT,
                            debitAccount = "Expense",
                            creditAccount = null
                        ),
                        // CREDIT entry (money goes out - decreases asset)
                        UnifiedTransaction(
                            id = "expense_${expense.id}_credit",
                            amount = expense.amount,
                            type = TransactionType.EXPENSE,
                            description = "${expense.merchant ?: expense.notes ?: "Expense"} (Credit)",
                            category = expense.category.displayName,
                            date = expense.timestamp,
                            source = "Expense",
                            entryType = EntryType.CREDIT,
                            debitAccount = null,
                            creditAccount = "Cash/Bank"
                        )
                    )
                }
                
                // Convert financial transactions (double-entry)
                val financialTransactions = transactions.flatMap { ft ->
                    val entries = mutableListOf<UnifiedTransaction>()
                    
                    when (ft.type) {
                        TransactionType.INCOME -> {
                            entries.add(UnifiedTransaction(
                                id = "ft_${ft.id}",
                                amount = ft.amount,
                                type = ft.type,
                                description = ft.note.ifEmpty { ft.type.name },
                                category = ft.category ?: "Other",
                                date = ft.date,
                                source = "Financial",
                                entryType = EntryType.DEBIT,
                                debitAccount = ft.source.name,
                                creditAccount = null
                            ))
                            entries.add(UnifiedTransaction(
                                id = "ft_${ft.id}_credit",
                                amount = ft.amount,
                                type = ft.type,
                                description = "${ft.note.ifEmpty { ft.type.name }} (Credit)",
                                category = ft.category ?: "Other",
                                date = ft.date,
                                source = "Financial",
                                entryType = EntryType.CREDIT,
                                debitAccount = null,
                                creditAccount = "Income"
                            ))
                        }
                        TransactionType.EXPENSE -> {
                            entries.add(UnifiedTransaction(
                                id = "ft_${ft.id}",
                                amount = ft.amount,
                                type = ft.type,
                                description = ft.note.ifEmpty { ft.type.name },
                                category = ft.category ?: "Other",
                                date = ft.date,
                                source = "Financial",
                                entryType = EntryType.DEBIT,
                                debitAccount = "Expense",
                                creditAccount = null
                            ))
                            entries.add(UnifiedTransaction(
                                id = "ft_${ft.id}_credit",
                                amount = ft.amount,
                                type = ft.type,
                                description = "${ft.note.ifEmpty { ft.type.name }} (Credit)",
                                category = ft.category ?: "Other",
                                date = ft.date,
                                source = "Financial",
                                entryType = EntryType.CREDIT,
                                debitAccount = null,
                                creditAccount = ft.source.name
                            ))
                        }
                        TransactionType.TRANSFER -> {
                            entries.add(UnifiedTransaction(
                                id = "ft_${ft.id}",
                                amount = ft.amount,
                                type = ft.type,
                                description = "${ft.note.ifEmpty { "Transfer" }} - Debit",
                                category = ft.category ?: "Transfer",
                                date = ft.date,
                                source = "Financial",
                                entryType = EntryType.DEBIT,
                                debitAccount = ft.destination?.name ?: "Destination",
                                creditAccount = null
                            ))
                            entries.add(UnifiedTransaction(
                                id = "ft_${ft.id}_credit",
                                amount = ft.amount,
                                type = ft.type,
                                description = "${ft.note.ifEmpty { "Transfer" }} - Credit",
                                category = ft.category ?: "Transfer",
                                date = ft.date,
                                source = "Financial",
                                entryType = EntryType.CREDIT,
                                debitAccount = null,
                                creditAccount = ft.source.name
                            ))
                        }
                        else -> {
                            entries.add(UnifiedTransaction(
                                id = "ft_${ft.id}",
                                amount = ft.amount,
                                type = ft.type,
                                description = ft.note.ifEmpty { ft.type.name },
                                category = ft.category ?: "Other",
                                date = ft.date,
                                source = "Financial",
                                entryType = EntryType.DEBIT,
                                debitAccount = ft.source.name,
                                creditAccount = ft.destination?.name
                            ))
                        }
                    }
                    entries
                }
                
                // Combine all transactions
                val allTransactions = (incomeTransactions + expenseTransactions + financialTransactions)
                    .sortedByDescending { it.date }
                
                // Calculate totals
                val totalIncome = allTransactions.filter { 
                    it.type == TransactionType.INCOME || it.type == TransactionType.LOAN_RECEIVE 
                }.filter { it.entryType == EntryType.CREDIT }.sumOf { it.amount }
                
                val totalExpenses = allTransactions.filter { 
                    it.type == TransactionType.EXPENSE || it.type == TransactionType.EMI_PAID 
                }.filter { it.entryType == EntryType.DEBIT }.sumOf { it.amount }

                // Apply Filters
                var filtered = allTransactions

                filtered = when (filter) {
                    TransactionFilter.INCOME -> filtered.filter { 
                        (it.type == TransactionType.INCOME || it.type == TransactionType.LOAN_RECEIVE) && it.entryType == EntryType.CREDIT
                    }
                    TransactionFilter.EXPENSE -> filtered.filter { 
                        (it.type == TransactionType.EXPENSE || it.type == TransactionType.EMI_PAID) && it.entryType == EntryType.DEBIT
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

    fun deleteTransaction(transaction: UnifiedTransaction) {
        viewModelScope.launch {
            try {
                when {
                    transaction.source == "Financial" && !transaction.id.endsWith("_credit") -> {
                        val ftId = transaction.id.removePrefix("ft_").toIntOrNull()
                        if (ftId != null) {
                            transactionRepository.deleteTransactionById(ftId)
                        }
                    }
                    transaction.source == "Income" && !transaction.id.endsWith("_credit") -> {
                        val incomeId = transaction.id.removePrefix("income_").toLongOrNull()
                        if (incomeId != null) {
                            incomeRepository.deleteIncomeById(incomeId)
                        }
                    }
                    transaction.source == "Expense" && !transaction.id.endsWith("_credit") -> {
                        val expenseId = transaction.id.removePrefix("expense_").toLongOrNull()
                        if (expenseId != null) {
                            expenseRepository.deleteExpenseById(expenseId)
                        }
                    }
                }
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
 * for double-entry accounting display
 */
data class UnifiedTransaction(
    val id: String,
    val amount: Double,
    val type: TransactionType,
    val description: String,
    val category: String,
    val date: Long,
    val source: String,
    val entryType: EntryType = EntryType.DEBIT,
    val debitAccount: String? = null,
    val creditAccount: String? = null
)

enum class EntryType {
    DEBIT,
    CREDIT
}
