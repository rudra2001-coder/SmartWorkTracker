package com.rudra.smartworktracker.ui.screens.appearance

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.rudra.smartworktracker.data.repository.SettingsRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class AppearanceUiState(
    val isDarkTheme: Boolean = false,
    val fontSize: Float = 1.0f,
    val accentColorIndex: Int = 0,
    val isDynamicColor: Boolean = true
)

class AppearanceViewModel(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AppearanceUiState())
    val uiState: StateFlow<AppearanceUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                settingsRepository.darkTheme,
                settingsRepository.fontSize,
                settingsRepository.accentColor,
                settingsRepository.dynamicColor
            ) { dark, size, color, dynamic ->
                AppearanceUiState(
                    isDarkTheme = dark,
                    fontSize = size.toFloat(),
                    accentColorIndex = color,
                    isDynamicColor = dynamic
                )
            }.collect { _uiState.value = it }
        }
    }

    fun setDarkTheme(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.setDarkTheme(enabled) }
    }

    fun setFontSize(size: Float) {
        viewModelScope.launch { settingsRepository.setFontSize(size.toDouble()) }
    }

    fun setAccentColor(index: Int) {
        viewModelScope.launch { settingsRepository.setAccentColor(index) }
    }

    fun setDynamicColor(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.setDynamicColor(enabled) }
    }

    class Factory(private val context: Context) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return AppearanceViewModel(SettingsRepository(context)) as T
        }
    }
}
