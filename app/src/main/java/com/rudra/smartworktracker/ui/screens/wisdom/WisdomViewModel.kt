package com.rudra.smartworktracker.ui.screens.wisdom

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rudra.smartworktracker.data.repository.WisdomRepository
import com.rudra.smartworktracker.model.Goal
import com.rudra.smartworktracker.model.GoalCategory
import com.rudra.smartworktracker.model.Target
import com.rudra.smartworktracker.model.Wisdom
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

enum class BoosterType {
    XP_2X, XP_1_5X, TARGET_SPEED
}

data class Booster(
    val id: String = UUID.randomUUID().toString(),
    val type: BoosterType,
    val durationHours: Int,
    var activatedAt: LocalDateTime? = null
)

data class UserStats(
    val experiencePoints: Int = 0,
    val level: Int = 1,
    val streak: Int = 1,
    val totalGoalsCompleted: Int = 0,
    val lastActiveDate: LocalDate = LocalDate.now(),
    val streakProtectionAvailable: Int = 0,
    val xpMultiplier: Float = 1.0f
)

class WisdomViewModel : ViewModel() {
    private val repository = WisdomRepository()
    
    // Goals State
    var goals by mutableStateOf(listOf<Goal>())
        private set

    // Simple in-memory map for targets (goalId -> list of Targets)
    var targetsMap by mutableStateOf(mapOf<String, List<Target>>())
        private set
    
    var selectedGoal by mutableStateOf<Goal?>(null)
        private set
    
    var showGoalCelebration by mutableStateOf(false)
        private set
    
    var showTargetCelebration by mutableStateOf(false)
        private set
    
    var lastAchievedItemName by mutableStateOf("")
        private set

    var userStats by mutableStateOf(UserStats())
        private set

    var inventoryBoosters by mutableStateOf(listOf<Booster>())
        private set

    var activeBoosters by mutableStateOf(listOf<Booster>())
        private set

    private var boosterJobs = mutableMapOf<String, Job>()
    
    init {
        loadGoals()
        checkStreak()
    }
    
    fun getWisdom(): List<Wisdom> {
        return emptyList()
    }
    
    private fun loadGoals() {
        val g1 = Goal(
            id = "1",
            title = "Become Senior Developer",
            description = "Reach senior developer position within 2 years",
            category = GoalCategory.CAREER,
            totalTargets = 3,
            completedTargets = 1
        )
        val g2 = Goal(
            id = "2",
            title = "Learn Modern Android",
            description = "Master Jetpack Compose and advanced Kotlin features",
            category = GoalCategory.LEARNING,
            totalTargets = 2,
            completedTargets = 0
        )
        goals = listOf(g1, g2)

        // Sample targets
        targetsMap = mapOf(
            "1" to listOf(
                Target(id = "t1", goalId = "1", title = "Complete React Course", description = "", order = 1, isCompleted = true),
                Target(id = "t2", goalId = "1", title = "Lead a Project", description = "", order = 2, isCompleted = false),
                Target(id = "t3", goalId = "1", title = "Master CI/CD", description = "", order = 3, isCompleted = false)
            ),
            "2" to listOf(
                Target(id = "t4", goalId = "2", title = "Finish Compose Basics", description = "", order = 1, isCompleted = false),
                Target(id = "t5", goalId = "2", title = "Implement Navigation", description = "", order = 2, isCompleted = false)
            )
        )
    }

    private fun checkStreak() {
        val today = LocalDate.now()
        val lastActive = userStats.lastActiveDate
        
        if (lastActive.isBefore(today)) {
            if (lastActive.plusDays(1) == today) {
                userStats = userStats.copy(streak = userStats.streak + 1, lastActiveDate = today)
                awardXP(20)
                checkStreakMilestones(userStats.streak)
            } else if (lastActive.plusDays(1).isBefore(today)) {
                if (!useStreakProtection()) {
                    userStats = userStats.copy(streak = 1, lastActiveDate = today)
                }
            }
        }
    }

    private fun checkStreakMilestones(streak: Int) {
        when (streak) {
            7 -> awardXP(100)
            30 -> {
                awardXP(500)
                userStats = userStats.copy(streakProtectionAvailable = userStats.streakProtectionAvailable + 1)
            }
        }
    }
    
    fun addGoal(goal: Goal) {
        goals = goals + goal
        targetsMap = targetsMap + (goal.id to emptyList())
    }

    fun deleteGoal(goalId: String) {
        goals = goals.filter { it.id != goalId }
        targetsMap = targetsMap - goalId
        if (selectedGoal?.id == goalId) {
            selectedGoal = null
        }
    }

    fun addTargetToGoal(goalId: String, title: String) {
        val currentTargets = targetsMap[goalId] ?: emptyList()
        val newTarget = Target(
            id = UUID.randomUUID().toString(),
            goalId = goalId,
            title = title,
            description = "",
            order = currentTargets.size + 1
        )
        targetsMap = targetsMap + (goalId to (currentTargets + newTarget))
        
        // Update goal total count
        goals = goals.map { if (it.id == goalId) it.copy(totalTargets = it.totalTargets + 1) else it }
    }

    fun deleteTarget(goalId: String, targetId: String) {
        val currentTargets = targetsMap[goalId] ?: return
        val updatedTargets = currentTargets.filter { it.id != targetId }
        targetsMap = targetsMap + (goalId to updatedTargets)
        
        val completedCount = updatedTargets.count { it.isCompleted }
        goals = goals.map { goal ->
            if (goal.id == goalId) {
                val total = updatedTargets.size
                val isGoalComplete = total > 0 && completedCount >= total
                goal.copy(totalTargets = total, completedTargets = completedCount, isCompleted = isGoalComplete)
            } else goal
        }
    }
    
    fun completeTarget(goalId: String, targetId: String) {
        val currentTargets = targetsMap[goalId] ?: return
        var targetTitle = ""
        var newlyCompleted = false
        
        val updatedTargets = currentTargets.map { target ->
            if (target.id == targetId && !target.isCompleted) {
                newlyCompleted = true
                targetTitle = target.title
                target.copy(isCompleted = true, completedAt = Date())
            } else target
        }

        if (newlyCompleted) {
            targetsMap = targetsMap + (goalId to updatedTargets)
            awardXP(50)
            
            lastAchievedItemName = targetTitle
            triggerTargetCelebration()

            val completedCount = updatedTargets.count { it.isCompleted }
            goals = goals.map { goal ->
                if (goal.id == goalId) {
                    val isGoalComplete = completedCount >= goal.totalTargets
                    if (isGoalComplete && !goal.isCompleted) {
                        awardXP(200)
                        userStats = userStats.copy(totalGoalsCompleted = userStats.totalGoalsCompleted + 1)
                        lastAchievedItemName = goal.title
                        triggerGoalCelebration()
                    }
                    goal.copy(completedTargets = completedCount, isCompleted = isGoalComplete)
                } else goal
            }
        }
    }

    private fun awardXP(amount: Int) {
        val actualAmount = (amount * userStats.xpMultiplier).toInt()
        val newXP = userStats.experiencePoints + actualAmount
        val xpForNextLevel = userStats.level * 500
        
        if (newXP >= xpForNextLevel) {
            userStats = userStats.copy(level = userStats.level + 1, experiencePoints = newXP - xpForNextLevel)
        } else {
            userStats = userStats.copy(experiencePoints = newXP)
        }
    }
    
    fun selectGoal(goal: Goal?) {
        selectedGoal = goal
    }
    
    private fun triggerGoalCelebration() {
        viewModelScope.launch {
            showGoalCelebration = true
            delay(4000)
            showGoalCelebration = false
        }
    }

    private fun triggerTargetCelebration() {
        viewModelScope.launch {
            showTargetCelebration = true
            delay(2000)
            showTargetCelebration = false
        }
    }

    fun purchaseStreakProtection(amount: Int, cost: Int) {
        if (userStats.experiencePoints >= cost) {
            userStats = userStats.copy(experiencePoints = userStats.experiencePoints - cost, streakProtectionAvailable = userStats.streakProtectionAvailable + amount)
        }
    }

    fun purchaseBooster(type: BoosterType, cost: Int) {
        if (userStats.experiencePoints >= cost) {
            userStats = userStats.copy(experiencePoints = userStats.experiencePoints - cost)
            val newBooster = Booster(type = type, durationHours = 1)
            inventoryBoosters = inventoryBoosters + newBooster
        }
    }

    fun activateBooster(booster: Booster) {
        val activatedBooster = booster.copy(activatedAt = LocalDateTime.now())
        inventoryBoosters = inventoryBoosters.filter { it.id != booster.id }
        activeBoosters = activeBoosters + activatedBooster
        recalculateMultipliers()
        val job = viewModelScope.launch {
            delay(activatedBooster.durationHours * 3600L * 1000L)
            deactivateBooster(activatedBooster)
        }
        boosterJobs[activatedBooster.id] = job
    }

    private fun deactivateBooster(booster: Booster) {
        activeBoosters = activeBoosters.filter { it.id != booster.id }
        boosterJobs.remove(booster.id)?.cancel()
        recalculateMultipliers()
    }

    private fun recalculateMultipliers() {
        var multiplier = 1.0f
        activeBoosters.forEach { 
            when (it.type) {
                BoosterType.XP_2X -> multiplier *= 2.0f
                BoosterType.XP_1_5X -> multiplier *= 1.5f
                else -> {}
            }
        }
        userStats = userStats.copy(xpMultiplier = multiplier)
    }

    private fun useStreakProtection(): Boolean {
        if (userStats.streakProtectionAvailable > 0) {
            userStats = userStats.copy(streakProtectionAvailable = userStats.streakProtectionAvailable - 1, lastActiveDate = LocalDate.now())
            return true
        }
        return false
    }
}