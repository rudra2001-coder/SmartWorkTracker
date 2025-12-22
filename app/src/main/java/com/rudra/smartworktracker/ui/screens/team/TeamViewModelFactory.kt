package com.rudra.smartworktracker.ui.screens.team

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.rudra.smartworktracker.data.SharedPreferenceManager

class TeamViewModelFactory(private val sharedPreferenceManager: SharedPreferenceManager) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TeamViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TeamViewModel(sharedPreferenceManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
