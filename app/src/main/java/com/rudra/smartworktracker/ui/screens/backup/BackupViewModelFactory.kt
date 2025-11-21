package com.rudra.smartworktracker.ui.screens.backup

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.rudra.smartworktracker.data.AppDatabase

class BackupViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BackupViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return BackupViewModel(AppDatabase.getDatabase(context), context.applicationContext) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
