package com.rudra.smartworktracker.data.repository

import com.rudra.smartworktracker.data.dao.AchievementDao
import com.rudra.smartworktracker.model.Achievement
import com.rudra.smartworktracker.model.AchievementType

class AchievementRepository(private val achievementDao: AchievementDao) {

    val allAchievements = achievementDao.getAllAchievements()

    suspend fun initializeAchievements() {
        achievementDao.insertAchievements(predefinedAchievements)
    }

    companion object {
        val predefinedAchievements = listOf(
            // Streak Achievements
            Achievement(
                id = "streak_1", name = "First Step",
                description = "Complete a habit for the first time.",
                type = AchievementType.STREAK, threshold = 1
            ),
            Achievement(
                id = "streak_7", name = "Weekly Warrior",
                description = "Maintain a 7-day streak on any habit.",
                type = AchievementType.STREAK, threshold = 7
            ),
            Achievement(
                id = "streak_30", name = "Monthly Master",
                description = "Maintain a 30-day streak on any habit.",
                type = AchievementType.STREAK, threshold = 30
            ),
            Achievement(
                id = "streak_100", name = "Habit Legend",
                description = "Maintain an epic 100-day streak!",
                type = AchievementType.STREAK, threshold = 100
            ),

            // Focus Achievements
            Achievement(
                id = "focus_1", name = "Getting Focused",
                description = "Complete your first focus session.",
                type = AchievementType.FOCUS, threshold = 1
            ),
            Achievement(
                id = "focus_10", name = "Deep Worker",
                description = "Complete 10 focus sessions.",
                type = AchievementType.FOCUS, threshold = 10
            ),
            Achievement(
                id = "focus_50", name = "Focus Grandmaster",
                description = "Complete 50 focus sessions.",
                type = AchievementType.FOCUS, threshold = 50
            ),
            Achievement(
                id = "focus_200", name = "Zen Master",
                description = "Complete an incredible 200 focus sessions.",
                type = AchievementType.FOCUS, threshold = 200
            )
        )
    }
}
