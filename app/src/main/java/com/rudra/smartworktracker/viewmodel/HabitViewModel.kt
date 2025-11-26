package com.rudra.smartworktracker.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rudra.smartworktracker.data.repository.HabitRepository
import com.rudra.smartworktracker.model.Habit
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HabitViewModel(private val habitRepository: HabitRepository) : ViewModel() {

    val habits: StateFlow<List<Habit>> = habitRepository.getAllHabits()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun deleteHabit(habit: Habit) {
        viewModelScope.launch {
            habitRepository.deleteHabit(habit)
        }
    }
}
