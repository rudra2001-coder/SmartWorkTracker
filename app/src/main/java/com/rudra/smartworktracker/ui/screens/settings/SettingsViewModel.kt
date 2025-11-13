package com.rudra.smartworktracker.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rudra.smartworktracker.data.entity.UserProfile
import com.rudra.smartworktracker.data.repository.UserProfileRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn

class SettingsViewModel(private val userProfileRepository: UserProfileRepository) : ViewModel() {

    val userProfile = userProfileRepository.userProfile.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    fun updateUserProfile(userProfile: UserProfile) {
        // To be implemented
    }
}
