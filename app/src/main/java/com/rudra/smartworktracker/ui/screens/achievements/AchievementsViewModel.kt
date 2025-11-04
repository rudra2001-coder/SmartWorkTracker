package com.rudra.smartworktracker.ui.screens.achievements

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.rudra.smartworktracker.data.AppDatabase
import com.rudra.smartworktracker.data.repository.AchievementRepository
import com.rudra.smartworktracker.engine.AchievementManager
import com.rudra.smartworktracker.model.Achievement
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AchievementsViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val achievementRepository = AchievementRepository(db.achievementDao())
    private val achievementManager = AchievementManager(db.achievementDao(), db.habitDao(), db.focusSessionDao())

    val achievements: StateFlow<List<Achievement>> = achievementRepository.allAchievements
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _newlyUnlockedAchievement = MutableStateFlow<Achievement?>(null)
    val newlyUnlockedAchievement: StateFlow<Achievement?> = _newlyUnlockedAchievement.asStateFlow()

    init {
        viewModelScope.launch {
            // Initialize and check achievements on startup
            achievementRepository.initializeAchievements()
            checkAchievements()

            // Listen for changes in achievements to find newly unlocked ones
            achievements.map { it.filter { it.unlocked } }
                .distinctUntilChanged()
                .collect { unlockedAchievements ->
                    unlockedAchievements.maxByOrNull { it.unlockedTimestamp ?: 0 }?.let {
                        if (it.unlockedTimestamp != null && System.currentTimeMillis() - it.unlockedTimestamp < 5000) {
                            _newlyUnlockedAchievement.value = it
                        }
                    }
                }
        }
    }

    private suspend fun checkAchievements() {
        achievementManager.checkAndUnlockAchievements()
    }

    fun onAnimationShown() {
        _newlyUnlockedAchievement.value = null
    }
}
