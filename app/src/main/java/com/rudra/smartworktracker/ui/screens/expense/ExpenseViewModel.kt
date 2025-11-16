package com.rudra.smartworktracker.ui.screens.expense

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.rudra.smartworktracker.data.AppDatabase
import com.rudra.smartworktracker.data.entity.AccountType
import com.rudra.smartworktracker.data.entity.FinancialTransaction
import com.rudra.smartworktracker.data.entity.TransactionType
import com.rudra.smartworktracker.model.Expense
import com.rudra.smartworktracker.model.ExpenseCategory
import kotlinx.coroutines.launch

class ExpenseViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val expenseDao = db.expenseDao()
    private val financialTransactionDao = db.financialTransactionDao()

    fun saveExpense(
        amount: Double,
        currency: String,
        category: ExpenseCategory,
        merchant: String?,
        notes: String?
    ) {
        viewModelScope.launch {
            val timestamp = System.currentTimeMillis()
            val expense = Expense(
                amount = amount,
                currency = currency,
                category = category,
                merchant = merchant,
                notes = notes,
                timestamp = timestamp
            )
            expenseDao.insertExpense(expense)

            // Create and save the corresponding financial transaction
            val transaction = FinancialTransaction(
                type = TransactionType.EXPENSE,
                amount = amount,
                source = AccountType.BALANCE, // Or determine dynamically
                destination = null,
                note = notes ?: "",
                date = timestamp
            )
            financialTransactionDao.insertTransaction(transaction)
        }
    }
}
