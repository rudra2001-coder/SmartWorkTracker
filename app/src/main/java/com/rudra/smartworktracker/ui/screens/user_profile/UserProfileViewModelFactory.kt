package com.rudra.smartworktracker.ui.screens.user_profile

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.rudra.smartworktracker.data.AppDatabase
import com.rudra.smartworktracker.data.repository.UserProfileRepository

class UserProfileViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(UserProfileViewModel::class.java)) {
            val db = AppDatabase.getDatabase(context.applicationContext)
            val repository = UserProfileRepository(userProfileDao = db.userProfileDao())
            @Suppress("UNCHECKED_CAST")
            return UserProfileViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
