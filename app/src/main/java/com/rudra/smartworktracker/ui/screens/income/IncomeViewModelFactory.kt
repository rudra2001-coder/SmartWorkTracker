package com.rudra.smartworktracker.ui.screens.income

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.rudra.smartworktracker.data.AppDatabase

class IncomeViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(IncomeViewModel::class.java)) {
            val database = AppDatabase.getDatabase(context)
            return IncomeViewModel(database) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}