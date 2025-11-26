package com.rudra.smartworktracker.ui.screens.habit

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.rudra.smartworktracker.data.AppDatabase
import com.rudra.smartworktracker.model.Habit
import com.rudra.smartworktracker.model.HabitDifficulty
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.UUID

class HabitViewModel(application: Application) : AndroidViewModel(application) {

    private val habitDao = AppDatabase.getDatabase(application).habitDao()

    val habits: StateFlow<List<Habit>> = habitDao.getAllHabits()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addHabit(name: String, description: String, difficulty: HabitDifficulty) {
        viewModelScope.launch {
            val newHabit = Habit(
                id = UUID.randomUUID().toString(),
                name = name,
                description = description,
                streak = 0,
                difficulty = difficulty,
                triggerHabitId = null, // Not implemented yet
                createdAt = System.currentTimeMillis()
            )
            habitDao.insertHabit(newHabit)
        }
    }

    fun completeHabit(habit: Habit) {
        viewModelScope.launch {
            if (isEligibleForCompletion(habit.lastCompleted)) {
                val updatedHabit = habit.copy(
                    streak = habit.streak + 1,
                    lastCompleted = System.currentTimeMillis()
                )
                habitDao.updateHabit(updatedHabit)
            } else if (isStreakBroken(habit.lastCompleted)) {
                val updatedHabit = habit.copy(
                    streak = 1, // Reset to 1 for today's completion
                    lastCompleted = System.currentTimeMillis()
                )
                habitDao.updateHabit(updatedHabit)
            }
            // If already completed today, do nothing.
        }
    }

    fun heavyDeleteHabit(habit: Habit) {
        viewModelScope.launch {
            deleteHabitAndItsTriggers(habit)
        }
    }

    private suspend fun deleteHabitAndItsTriggers(habit: Habit) {
        val triggeredHabits = habitDao.getHabitsByTriggerId(habit.id)
        for (triggeredHabit in triggeredHabits) {
            deleteHabitAndItsTriggers(triggeredHabit)
        }
        habitDao.deleteHabit(habit)
    }

    private fun isEligibleForCompletion(lastCompleted: Long?): Boolean {
        if (lastCompleted == null) return true // First time completion

        val today = Calendar.getInstance()
        val lastCompletionDate = Calendar.getInstance().apply { timeInMillis = lastCompleted }

        // Eligible if last completion was yesterday
        today.add(Calendar.DAY_OF_YEAR, -1)
        return today.get(Calendar.YEAR) == lastCompletionDate.get(Calendar.YEAR) &&
                today.get(Calendar.DAY_OF_YEAR) == lastCompletionDate.get(Calendar.DAY_OF_YEAR)
    }

    private fun isStreakBroken(lastCompleted: Long?): Boolean {
        if (lastCompleted == null) return false

        val today = Calendar.getInstance()
        val lastCompletionDate = Calendar.getInstance().apply { timeInMillis = lastCompleted }

        // Not eligible if it's the same day
        if (today.get(Calendar.YEAR) == lastCompletionDate.get(Calendar.YEAR) &&
            today.get(Calendar.DAY_OF_YEAR) == lastCompletionDate.get(Calendar.DAY_OF_YEAR)) {
            return false
        }

        // Eligible for reset if last completion was before yesterday
        today.add(Calendar.DAY_OF_YEAR, -1)
        return lastCompletionDate.before(today)
    }
}
