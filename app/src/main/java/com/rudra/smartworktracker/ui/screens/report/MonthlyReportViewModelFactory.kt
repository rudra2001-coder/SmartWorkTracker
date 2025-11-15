package com.rudra.smartworktracker.ui.screens.report

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.rudra.smartworktracker.data.AppDatabase
import com.rudra.smartworktracker.data.repository.WorkLogRepository

class MonthlyReportViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MonthlyReportViewModel::class.java)) {
            val database = AppDatabase.getDatabase(application)
            val workLogRepository = WorkLogRepository(database.workLogDao())
            @Suppress("UNCHECKED_CAST")
            return MonthlyReportViewModel(workLogRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
