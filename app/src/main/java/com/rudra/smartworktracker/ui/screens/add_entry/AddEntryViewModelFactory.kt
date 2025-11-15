package com.rudra.smartworktracker.ui.screens.add_entry

import android.content.Context
import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.rudra.smartworktracker.data.AppDatabase
import com.rudra.smartworktracker.data.repository.ExpenseRepository
import com.rudra.smartworktracker.data.repository.WorkLogRepository

class AddEntryViewModelFactory(private val context: Context) : AbstractSavedStateViewModelFactory() {
    override fun <T : ViewModel> create(key: String, modelClass: Class<T>, handle: SavedStateHandle): T {
        if (modelClass.isAssignableFrom(AddEntryViewModel::class.java)) {
            val db = AppDatabase.getDatabase(context.applicationContext)
            val expenseRepository = ExpenseRepository(db.expenseDao())
            val workLogRepository = WorkLogRepository(db.workLogDao())
            @Suppress("UNCHECKED_CAST")
            return AddEntryViewModel(expenseRepository, workLogRepository, handle) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
