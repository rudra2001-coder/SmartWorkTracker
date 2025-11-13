package com.rudra.smartworktracker.ui.screens.meal_overtime

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.rudra.smartworktracker.data.AppDatabase
import com.rudra.smartworktracker.data.repository.MealOvertimeRepository

class MealOvertimeViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MealOvertimeViewModel::class.java)) {
            val db = AppDatabase.getDatabase(context.applicationContext)
            val repository = MealOvertimeRepository(
                workDayDao = db.workDayDao(),
                settingsDao = db.settingsDao(),
                summaryDao = db.summaryDao(),
                monthlyInputDao = db.monthlyInputDao()
            )
            @Suppress("UNCHECKED_CAST")
            return MealOvertimeViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}