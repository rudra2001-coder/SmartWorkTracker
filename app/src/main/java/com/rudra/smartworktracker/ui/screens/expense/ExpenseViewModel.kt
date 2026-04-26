package com.rudra.smartworktracker.ui.screens.expense

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.rudra.smartworktracker.data.AppDatabase
import com.rudra.smartworktracker.data.entity.AccountType
import com.rudra.smartworktracker.model.Expense
import com.rudra.smartworktracker.model.ExpenseCategory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class ExpenseViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val expenseDao = db.expenseDao()

    private val _recentExpenses = MutableStateFlow<List<Expense>>(emptyList())
    val recentExpenses: StateFlow<List<Expense>> = _recentExpenses.asStateFlow()

    init {
        loadRecentExpenses()
    }
    private fun loadRecentExpenses() {
        viewModelScope.launch {
            expenseDao.getLatest5Expenses().collect { expenses ->
                _recentExpenses.value = expenses
            }
        }
    }

    fun deleteExpense(expense: Expense) {
        viewModelScope.launch {
            expenseDao.deleteExpense(expense)
        }
    }

    fun saveExpense(
        amount: Double,
        currency: String,
        category: ExpenseCategory,
        merchant: String?,
        notes: String?,
        accountType: AccountType,
        timestamp: Long
    ) {
        viewModelScope.launch {
            val expense = Expense(
                amount = amount,
                currency = currency,
                category = category,
                merchant = merchant,
                notes = notes,
                timestamp = timestamp
            )
            expenseDao.insertExpense(expense)
        }
    }
}
