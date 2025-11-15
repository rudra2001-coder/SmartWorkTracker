package com.rudra.smartworktracker.ui.screens.analytics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rudra.smartworktracker.data.AppDatabase
import com.rudra.smartworktracker.model.Expense
import com.rudra.smartworktracker.data.entity.Income
import com.rudra.smartworktracker.model.FocusSession
import com.rudra.smartworktracker.model.Habit
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class AnalyticsData(
    val productivityScore: Int = 0,
    val focusSessions: List<FocusSession> = emptyList(),
    val habits: List<Habit> = emptyList(),
    val incomes: List<Income> = emptyList(),
    val expenses: List<Expense> = emptyList()
)

class AnalyticsViewModel(private val db: AppDatabase) : ViewModel() {

    val analyticsData: StateFlow<AnalyticsData> = combine(
        db.focusSessionDao().getAllFocusSessions(),
        db.habitDao().getAllHabits(),
        db.incomeDao().getAllIncomes(),
        db.expenseDao().getAllExpenses()
    ) { focusSessions, habits, incomes, expenses ->
        // --- Productivity Score Calculation (placeholder logic) ---
        val totalFocusMinutes = focusSessions.sumOf { it.duration } / 60
        val completedHabits = habits.count { it.streak > 0 }
        val score = (totalFocusMinutes / 10).toInt() + (completedHabits * 10)

        AnalyticsData(
            productivityScore = score.coerceIn(0, 100), // Ensure score is between 0 and 100
            focusSessions = focusSessions,
            habits = habits,
            incomes = incomes,
            expenses = expenses
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AnalyticsData()
    )
}
