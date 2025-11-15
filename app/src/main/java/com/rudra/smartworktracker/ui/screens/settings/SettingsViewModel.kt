package com.rudra.smartworktracker.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rudra.smartworktracker.data.entity.UserProfile
import com.rudra.smartworktracker.data.repository.ExpenseRepository
import com.rudra.smartworktracker.data.repository.IncomeRepository
import com.rudra.smartworktracker.data.repository.UserProfileRepository
import com.rudra.smartworktracker.data.repository.WorkLogRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val userProfileRepository: UserProfileRepository,
    private val workLogRepository: WorkLogRepository,
    private val incomeRepository: IncomeRepository,
    private val expenseRepository: ExpenseRepository
) : ViewModel() {

    val userProfile = userProfileRepository.userProfile.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    fun updateUserProfile(userProfile: UserProfile) {
        // To be implemented
    }

    fun clearAllData() {
        viewModelScope.launch {
            workLogRepository.clearAll()
            incomeRepository.clearAll()
            expenseRepository.clearAll()
        }
    }
}
