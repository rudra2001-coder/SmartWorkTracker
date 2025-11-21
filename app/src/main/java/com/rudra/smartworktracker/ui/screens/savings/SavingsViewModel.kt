package com.rudra.smartworktracker.ui.screens.savings

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.rudra.smartworktracker.data.AppDatabase
import com.rudra.smartworktracker.data.entity.Savings
import com.rudra.smartworktracker.data.repository.SavingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

data class SavingsUiState(
    val savings: Double = 0.0,
    val savingsHistory: List<Savings> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class SavingsViewModel(private val savingsRepository: SavingsRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(SavingsUiState())
    val uiState: StateFlow<SavingsUiState> = _uiState.asStateFlow()

    init {
        loadSavingsData()
    }

    private fun loadSavingsData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            combine(
                savingsRepository.getSavings(),
                savingsRepository.getSavingsHistory()
            ) { savings, history ->
                SavingsUiState(
                    savings = savings ?: 0.0,
                    savingsHistory = history,
                    isLoading = false
                )
            }.catch { exception ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = exception.message
                )
            }.collect {
                _uiState.value = it
            }
        }
    }

    fun addToSavings(amount: Double) {
        viewModelScope.launch {
            savingsRepository.addToSavings(amount)
        }
    }

    fun withdrawFromSavings(amount: Double) {
        viewModelScope.launch {
            savingsRepository.withdrawFromSavings(amount)
        }
    }
}
