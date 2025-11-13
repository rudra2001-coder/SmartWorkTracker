package com.rudra.smartworktracker.ui.screens.reports

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.rudra.smartworktracker.data.AppDatabase
import com.rudra.smartworktracker.data.repository.ExpenseRepository

class ReportsViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ReportsViewModel::class.java)) {
            val db = AppDatabase.getDatabase(context.applicationContext)
            val expenseRepository = ExpenseRepository(db.expenseDao())
            @Suppress("UNCHECKED_CAST")
            return ReportsViewModel(expenseRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
