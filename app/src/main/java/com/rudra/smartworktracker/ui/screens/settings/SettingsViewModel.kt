package com.rudra.smartworktracker.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rudra.smartworktracker.data.repository.WorkLogRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repository: WorkLogRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    fun toggleNotifications(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(notificationsEnabled = enabled)
        // Save to preferences
    }

    fun toggleDarkTheme(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(darkThemeEnabled = enabled)
        // Save to preferences and apply theme
    }

    fun toggleVibration(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(vibrationEnabled = enabled)
        // Save to preferences
    }

    fun backupData() {
        viewModelScope.launch {
            // Implement backup logic
            _uiState.value = _uiState.value.copy(showBackupSuccess = true)
        }
    }

    fun restoreData() {
        viewModelScope.launch {
            // Implement restore logic
            _uiState.value = _uiState.value.copy(showRestoreSuccess = true)
        }
    }

    fun showClearDataDialog() {
        _uiState.value = _uiState.value.copy(showClearDataDialog = true)
    }

    fun hideClearDataDialog() {
        _uiState.value = _uiState.value.copy(showClearDataDialog = false)
    }

    fun clearAllData() {
        viewModelScope.launch {
            repository.clearAllWorkLogs()
            _uiState.value = _uiState.value.copy(showClearDataSuccess = true)
        }
    }

    fun openHelp() {
        // Navigate to help screen or open documentation
    }

    fun openAbout() {
        // Navigate to about screen
    }
}

data class SettingsUiState(
    val notificationsEnabled: Boolean = true,
    val darkThemeEnabled: Boolean = false,
    val vibrationEnabled: Boolean = true,
    val showClearDataDialog: Boolean = false,
    val showBackupSuccess: Boolean = false,
    val showRestoreSuccess: Boolean = false,
    val showClearDataSuccess: Boolean = false
)