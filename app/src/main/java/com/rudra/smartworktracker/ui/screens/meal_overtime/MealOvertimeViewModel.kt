package com.rudra.smartworktracker.ui.screens.meal_overtime

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rudra.smartworktracker.data.entity.MonthlyInput
import com.rudra.smartworktracker.data.entity.MonthlySummary
import com.rudra.smartworktracker.data.entity.Settings
import com.rudra.smartworktracker.data.repository.FirstWeekData
import com.rudra.smartworktracker.data.repository.MealOvertimeRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

class MealOvertimeViewModel(private val repository: MealOvertimeRepository) : ViewModel() {

    private val _uiState = MutableStateFlow<MealOvertimeUiState>(MealOvertimeUiState.Loading)
    val uiState: StateFlow<MealOvertimeUiState> = _uiState.asStateFlow()

    private val _settings = MutableStateFlow<Settings?>(null)
    val settings: StateFlow<Settings?> = _settings.asStateFlow()

    init {
        loadSettings()
        initializeCurrentMonth()
    }

    private fun initializeCurrentMonth() {
        viewModelScope.launch(Dispatchers.IO) {
            val year = LocalDate.now().year.toString()
            val month = LocalDate.now().monthValue.toString().padStart(2, '0')
            initializeMonth(year, month)
        }
    }

    private fun loadSettings() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _settings.value = repository.getSettings()
            } catch (e: Exception) {
                // Handle settings loading error if needed
                e.printStackTrace()
            }
        }
    }

    fun initializeMonth(year: String, month: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.value = MealOvertimeUiState.Loading
            try {
                repository.initializeMonth(year, month)
                loadMonthData(year, month)
            } catch (e: Exception) {
                _uiState.value = MealOvertimeUiState.Error(
                    message = "Failed to initialize month: ${e.message ?: "Unknown error"}"
                )
            }
        }
    }

    fun calculateFromFirstWeek(year: String, month: String, firstWeekData: FirstWeekData) {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { MealOvertimeUiState.Loading }
            try {
                val summary = repository.calculateFromFirstWeek(year, month, firstWeekData)
                val input = repository.getMonthlyInput(summary.month)

                if (input != null) {
                    _uiState.value = MealOvertimeUiState.Success(
                        summary = summary,
                        input = input
                    )
                } else {
                    _uiState.value = MealOvertimeUiState.Error("Failed to load monthly input after calculation")
                }
            } catch (e: Exception) {
                _uiState.value = MealOvertimeUiState.Error(
                    message = "Calculation failed: ${e.message ?: "Unknown error"}"
                )
            }
        }
    }

    fun updateMonthlyInput(updatedInput: MonthlyInput) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val summary = repository.updateMonthlyInput(updatedInput)
                _uiState.value = MealOvertimeUiState.Success(
                    summary = summary,
                    input = updatedInput
                )
            } catch (e: Exception) {
                // Update UI state with error but keep current data
                val currentState = _uiState.value
                if (currentState is MealOvertimeUiState.Success) {
                    _uiState.value = currentState.copy(
                        summary = currentState.summary, // You might want to handle this differently
                        input = currentState.input
                    )
                }
                // Optionally show error message in UI
                _uiState.value = MealOvertimeUiState.Error("Update failed: ${e.message}")
            }
        }
    }

    fun updateSettings(updatedSettings: Settings) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                repository.saveSettings(updatedSettings)
                _settings.value = updatedSettings

                // Recalculate current month with new settings
                val currentState = _uiState.value
                if (currentState is MealOvertimeUiState.Success) {
                    val monthKey = currentState.summary.month
                    val year = monthKey.substring(0, 4)
                    val month = monthKey.substring(5, 7)
                    loadMonthData(year, month)
                }
            } catch (e: Exception) {
                // Handle settings update error
                e.printStackTrace()
            }
        }
    }

    fun reloadCurrentMonth() {
        viewModelScope.launch(Dispatchers.IO) {
            val currentState = _uiState.value
            if (currentState is MealOvertimeUiState.Success) {
                val monthKey = currentState.summary.month
                val year = monthKey.substring(0, 4)
                val month = monthKey.substring(5, 7)
                loadMonthData(year, month)
            }
        }
    }

    private suspend fun loadMonthData(year: String, month: String) {
        val monthKey = "$year-${month.padStart(2, '0')}"
        try {
            val input = repository.getMonthlyInput(monthKey)
            val summary = repository.getSummary(monthKey)

            if (input != null && summary != null) {
                _uiState.value = MealOvertimeUiState.Success(summary, input)
            } else {
                _uiState.value = MealOvertimeUiState.FirstTimeSetup
            }
        } catch (e: Exception) {
            _uiState.value = MealOvertimeUiState.Error(
                message = "Failed to load month data: ${e.message ?: "Unknown error"}"
            )
        }
    }
}

sealed class MealOvertimeUiState {
    object Loading : MealOvertimeUiState()
    object FirstTimeSetup : MealOvertimeUiState()
    data class Success(val summary: MonthlySummary, val input: MonthlyInput) : MealOvertimeUiState()
    data class Error(val message: String) : MealOvertimeUiState()
}
