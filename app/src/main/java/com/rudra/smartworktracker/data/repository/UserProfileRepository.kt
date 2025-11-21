package com.rudra.smartworktracker.data.repository

import com.rudra.smartworktracker.data.dao.UserProfileDao
import com.rudra.smartworktracker.data.entity.UserProfile
import kotlinx.coroutines.flow.Flow

class UserProfileRepository(private val userProfileDao: UserProfileDao) {

    val userProfile: Flow<UserProfile?> = userProfileDao.getUserProfile()

    suspend fun saveUserProfile(userProfile: UserProfile) {
        userProfileDao.insertUserProfile(userProfile)
    }

    suspend fun clearAll() {
        userProfileDao.clearAll()
    }
}
