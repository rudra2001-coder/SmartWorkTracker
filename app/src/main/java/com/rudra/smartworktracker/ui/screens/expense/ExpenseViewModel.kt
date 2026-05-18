package com.rudra.smartworktracker.ui.screens.expense

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.rudra.smartworktracker.data.AppDatabase
import com.rudra.smartworktracker.data.entity.Account
import com.rudra.smartworktracker.model.Expense
import com.rudra.smartworktracker.model.ExpenseCategory
import com.rudra.smartworktracker.data.repository.AccountRepository
import com.rudra.smartworktracker.data.repository.ExpenseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ExpenseViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val expenseRepository = ExpenseRepository(db.expenseDao(), db.accountDao())
    private val accountRepository = AccountRepository(db.accountDao())

    private val _recentExpenses = MutableStateFlow<List<Expense>>(emptyList())
    val recentExpenses: StateFlow<List<Expense>> = _recentExpenses.asStateFlow()
    
    private val _accounts = MutableStateFlow<List<Account>>(emptyList())
    val accounts: StateFlow<List<Account>> = _accounts.asStateFlow()

    init {
        loadRecentExpenses()
        loadAccounts()
    }
    
    private fun loadAccounts() {
        viewModelScope.launch {
            accountRepository.initializeDefaultAccounts()
            accountRepository.getAllAccounts().collect { accountList ->
                _accounts.value = accountList
            }
        }
    }

    private fun loadRecentExpenses() {
        viewModelScope.launch {
            db.expenseDao().getLatest5Expenses().collect { expenses ->
                _recentExpenses.value = expenses
            }
        }
    }

    fun deleteExpense(expense: Expense) {
        viewModelScope.launch {
            expenseRepository.deleteExpense(expense)
        }
    }

    fun saveExpense(
        amount: Double,
        currency: String,
        category: ExpenseCategory,
        merchant: String?,
        notes: String?,
        timestamp: Long,
        selectedAccountId: Long
    ) {
        viewModelScope.launch {
            val expense = Expense(
                amount = amount,
                currency = currency,
                category = category,
                merchant = merchant,
                notes = notes,
                timestamp = timestamp,
                accountId = selectedAccountId
            )
            expenseRepository.insertExpense(expense)
        }
    }
    
    fun hasSufficientBalance(accountId: Long, amount: Double): Boolean {
        val account = _accounts.value.find { it.id == accountId }
        return account != null && account.balance >= amount
    }
}