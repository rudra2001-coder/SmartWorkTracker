package com.rudra.smartworktracker.engine

import com.rudra.smartworktracker.data.dao.AchievementDao
import com.rudra.smartworktracker.data.dao.FocusSessionDao
import com.rudra.smartworktracker.data.dao.HabitDao
import com.rudra.smartworktracker.model.AchievementType
import kotlinx.coroutines.flow.first

class AchievementManager(
    private val achievementDao: AchievementDao,
    private val habitDao: HabitDao,
    private val focusSessionDao: FocusSessionDao
) {

    suspend fun checkAndUnlockAchievements() {
        val achievements = achievementDao.getAllAchievements().first()
        val unlockedAchievements = achievements.filter { it.unlocked }.map { it.id }.toSet()

        // --- Check Streak Achievements ---
        val habits = habitDao.getAllHabits().first()
        val maxStreak = habits.maxOfOrNull { it.streak } ?: 0
        achievements.filter { it.type == AchievementType.STREAK && it.id !in unlockedAchievements }.forEach { achievement ->
            if (maxStreak >= achievement.threshold) {
                achievementDao.unlockAchievement(achievement.copy(unlocked = true, unlockedTimestamp = System.currentTimeMillis()))
            }
        }

        // --- Check Focus Achievements ---
        val focusSessionCount = focusSessionDao.getAllFocusSessions().first().size
        achievements.filter { it.type == AchievementType.FOCUS && it.id !in unlockedAchievements }.forEach { achievement ->
            if (focusSessionCount >= achievement.threshold) {
                achievementDao.unlockAchievement(achievement.copy(unlocked = true, unlockedTimestamp = System.currentTimeMillis()))
            }
        }
    }
}
