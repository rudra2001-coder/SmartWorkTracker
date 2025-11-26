package com.rudra.smartworktracker.data.repository

import com.rudra.smartworktracker.data.dao.HabitDao
import com.rudra.smartworktracker.model.Habit
import kotlinx.coroutines.flow.Flow

class HabitRepository(private val habitDao: HabitDao) {

    fun getAllHabits(): Flow<List<Habit>> = habitDao.getAllHabits()

    suspend fun insertHabit(habit: Habit) {
        habitDao.insertHabit(habit)
    }

    suspend fun updateHabit(habit: Habit) {
        habitDao.updateHabit(habit)
    }

    suspend fun deleteHabit(habit: Habit) {
        habitDao.deleteHabit(habit)
    }
}
