package com.rudra.smartworktracker.ui.screens.reports

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rudra.smartworktracker.data.repository.ExpenseRepository
import com.rudra.smartworktracker.model.Expense
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.Calendar


data class ReportsUiState(
    val totalExpenses: Double = 0.0,
    val expenses: List<Expense> = emptyList(),
    val selectedDate: Calendar = Calendar.getInstance()
)

class ReportsViewModel(private val expenseRepository: ExpenseRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(ReportsUiState())
    val uiState = _uiState.asStateFlow()

    init {
        fetchExpensesForMonth(Calendar.getInstance())
    }

    fun onDateChange(newDate: Calendar) {
        fetchExpensesForMonth(newDate)
    }

    private fun fetchExpensesForMonth(date: Calendar) {
        viewModelScope.launch {
            val startOfMonth = date.clone() as Calendar
            startOfMonth.set(Calendar.DAY_OF_MONTH, 1)
            val endOfMonth = date.clone() as Calendar
            endOfMonth.add(Calendar.MONTH, 1)
            endOfMonth.set(Calendar.DAY_OF_MONTH, 1)
            endOfMonth.add(Calendar.DATE, -1)

            expenseRepository.getExpensesBetween(
                startTime = startOfMonth.timeInMillis,
                endTime = endOfMonth.timeInMillis
            ).collectLatest { expenses ->
                _uiState.value = ReportsUiState(
                    totalExpenses = expenses.sumOf { it.amount },
                    expenses = expenses,
                    selectedDate = date
                )
            }
        }
    }
}
