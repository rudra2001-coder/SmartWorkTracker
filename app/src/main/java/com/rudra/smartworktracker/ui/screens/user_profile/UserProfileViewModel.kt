package com.rudra.smartworktracker.ui.screens.user_profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rudra.smartworktracker.data.entity.UserProfile
import com.rudra.smartworktracker.data.repository.UserProfileRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class UserProfileViewModel(private val repository: UserProfileRepository) : ViewModel() {

    val userProfile: Flow<UserProfile?> = repository.userProfile

    fun saveUserProfile(userProfile: UserProfile) {
        viewModelScope.launch {
            repository.saveUserProfile(userProfile)
        }
    }
}
