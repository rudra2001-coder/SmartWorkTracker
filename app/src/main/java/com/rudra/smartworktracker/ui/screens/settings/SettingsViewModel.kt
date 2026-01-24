package com.rudra.smartworktracker.ui.screens.settings

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.rudra.smartworktracker.data.backup.BackupManager
import com.rudra.smartworktracker.data.entity.UserProfile
import com.rudra.smartworktracker.data.repository.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    application: Application,
    private val userProfileRepository: UserProfileRepository,
    private val workLogRepository: WorkLogRepository,
    private val incomeRepository: IncomeRepository,
    private val expenseRepository: ExpenseRepository,
    private val settingsRepository: SettingsRepository
) : AndroidViewModel(application) {

    private val backupManager = BackupManager(application)

    private val _backupResult = MutableSharedFlow<String>()
    val backupResult: SharedFlow<String> = _backupResult

    private val _restoreResult = MutableSharedFlow<Result<Unit>>()
    val restoreResult: SharedFlow<Result<Unit>> = _restoreResult

    val userProfile = userProfileRepository.userProfile.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )
    val mealRate = settingsRepository.mealRate.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 60.0
    )
    val isDarkTheme = settingsRepository.darkTheme.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false
    )
    val notificationsEnabled = settingsRepository.notifications.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = true
    )

    fun setMealRate(rate: Double) {
        viewModelScope.launch {
            settingsRepository.setMealRate(rate)
        }
    }

    fun setDarkTheme(isDark: Boolean) {
        viewModelScope.launch {
            settingsRepository.setDarkTheme(isDark)
        }
    }

    fun setNotifications(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setNotifications(enabled)
        }
    }

    fun createBackup(uri: Uri) {
        viewModelScope.launch {
            val context = getApplication<Application>()
            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                val success = backupManager.exportToJson(outputStream)
                if (success) {
                    _backupResult.emit(uri.path ?: "Backup successful")
                }
            }
        }
    }

    fun restoreBackup(uri: Uri) {
        viewModelScope.launch {
            val context = getApplication<Application>()
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                val result = backupManager.importFromJson(inputStream)
                _restoreResult.emit(result)
            }
        }
    }

    fun clearAllData() {
        viewModelScope.launch {
            userProfileRepository.clearAll()
            workLogRepository.clearAll()
            incomeRepository.clearAll()
            expenseRepository.clearAll()
            settingsRepository.clearAll()
        }
    }
}
