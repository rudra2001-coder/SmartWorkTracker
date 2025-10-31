package com.rudra.smartworktracker.ui.screens.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.rudra.smartworktracker.data.AppDatabase
import com.rudra.smartworktracker.data.repository.SettingsRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

open class SettingsViewModel(
    private val repository: SettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    open val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            repository.getSettings().collect { settings ->
                _uiState.value = _uiState.value.copy(
                    notificationsEnabled = settings.notificationsEnabled,
                    darkThemeEnabled = settings.darkThemeEnabled,
                    vibrationEnabled = settings.vibrationEnabled
                )
            }
        }
    }

    fun toggleNotifications(enabled: Boolean) {
        updateSettings(_uiState.value.copy(notificationsEnabled = enabled))
    }

    fun toggleDarkTheme(enabled: Boolean) {
        updateSettings(_uiState.value.copy(darkThemeEnabled = enabled))
    }

    fun toggleVibration(enabled: Boolean) {
        updateSettings(_uiState.value.copy(vibrationEnabled = enabled))
    }

    private fun updateSettings(newState: SettingsUiState) {
        _uiState.value = newState
        viewModelScope.launch {
            repository.saveSettings(
                notificationsEnabled = newState.notificationsEnabled,
                darkThemeEnabled = newState.darkThemeEnabled,
                vibrationEnabled = newState.vibrationEnabled
            )
        }
    }

    fun backupData() {
        viewModelScope.launch {
            repository.backupData()
            _uiState.value = _uiState.value.copy(showBackupSuccess = true)
        }
    }

    fun restoreData() {
        viewModelScope.launch {
            repository.restoreData()
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
        // Navigation handled by UI
    }

    fun openAbout() {
        // Navigation handled by UI
    }

    fun resetSuccessFlags() {
        _uiState.value = _uiState.value.copy(
            showBackupSuccess = false,
            showRestoreSuccess = false,
            showClearDataSuccess = false
        )
    }

    companion object {
        fun factory(appDatabase: AppDatabase, context: Context): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
                        val settingsRepository = SettingsRepository(
                            context.dataStore, appDatabase.workLogDao()
                        )
                        return SettingsViewModel(settingsRepository) as T
                    }
                    throw IllegalArgumentException("Unknown ViewModel class")
                }
            }
        }
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
