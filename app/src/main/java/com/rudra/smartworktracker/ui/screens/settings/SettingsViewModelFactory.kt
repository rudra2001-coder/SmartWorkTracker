package com.rudra.smartworktracker.ui.screens.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.rudra.smartworktracker.data.AppDatabase
import com.rudra.smartworktracker.data.repository.UserProfileRepository

class SettingsViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
            val db = AppDatabase.getDatabase(context.applicationContext)
            val userProfileRepository = UserProfileRepository(db.userProfileDao())
            @Suppress("UNCHECKED_CAST")
            return SettingsViewModel(userProfileRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
