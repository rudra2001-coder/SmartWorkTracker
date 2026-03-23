package com.rudra.smartworktracker.ui.screens.spendadvisor

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.rudra.smartworktracker.data.AppDatabase
import com.rudra.smartworktracker.data.repository.SpendAdvisorRepository

class SpendAdvisorViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SpendAdvisorViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            val database = AppDatabase.getDatabase(application)
            return SpendAdvisorViewModel(
                application = application,
                repository = SpendAdvisorRepository(
                    database.incomeDao(),
                    database.expenseDao(),
                    application
                )
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
