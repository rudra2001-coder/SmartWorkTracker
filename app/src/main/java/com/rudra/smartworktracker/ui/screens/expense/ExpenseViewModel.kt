package com.rudra.smartworktracker.ui.screens.expense

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.rudra.smartworktracker.data.AppDatabase
import com.rudra.smartworktracker.model.Expense
import com.rudra.smartworktracker.model.ExpenseCategory
import kotlinx.coroutines.launch

class ExpenseViewModel(application: Application) : AndroidViewModel(application) {

    private val expenseDao = AppDatabase.getDatabase(application).expenseDao()

    fun saveExpense(
        amount: Double,
        currency: String,
        category: ExpenseCategory,
        merchant: String?,
        notes: String?
    ) {
        viewModelScope.launch {
            val expense = Expense(
                amount = amount,
                currency = currency,
                category = category,
                merchant = merchant,
                notes = notes,
                timestamp = System.currentTimeMillis()
            )
            expenseDao.insertExpense(expense)
        }
    }
}
