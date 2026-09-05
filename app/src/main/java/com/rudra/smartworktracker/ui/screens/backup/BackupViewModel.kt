package com.rudra.smartworktracker.ui.screens.backup

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.*
import com.rudra.smartworktracker.data.backup.AutoBackupWorker
import com.rudra.smartworktracker.data.backup.BackupManager
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.concurrent.TimeUnit

class BackupViewModel(private val context: Context) : ViewModel() {

    private val backupManager = BackupManager(context)
    private val prefs = context.getSharedPreferences("backup_prefs", Context.MODE_PRIVATE)

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _backupResult = MutableSharedFlow<BackupResult>()
    val backupResult: SharedFlow<BackupResult> = _backupResult.asSharedFlow()

    private val _lastBackupTime = MutableStateFlow(0L)
    val lastBackupTime: StateFlow<Long> = _lastBackupTime.asStateFlow()

    private val _nextBackupTime = MutableStateFlow(0L)
    val nextBackupTime: StateFlow<Long> = _nextBackupTime.asStateFlow()

    private val _isAutoBackupEnabled = MutableStateFlow(false)
    val isAutoBackupEnabled: StateFlow<Boolean> = _isAutoBackupEnabled.asStateFlow()

    init {
        loadBackupStatus()
    }

    fun loadBackupStatus() {
        _lastBackupTime.value = prefs.getLong("last_auto_backup_time", 0L)
        _isAutoBackupEnabled.value = prefs.getBoolean("auto_backup_enabled", false)

        if (_isAutoBackupEnabled.value) {
            calculateNextBackupTime()
        } else {
            _nextBackupTime.value = 0L
        }
    }

    private fun calculateNextBackupTime() {
        val nextBackup = Calendar.getInstance()
        nextBackup.set(Calendar.HOUR_OF_DAY, 0)
        nextBackup.set(Calendar.MINUTE, 5)
        nextBackup.set(Calendar.SECOND, 0)

        if (nextBackup.before(Calendar.getInstance())) {
            nextBackup.add(Calendar.DAY_OF_YEAR, 1)
        }
        _nextBackupTime.value = nextBackup.timeInMillis
    }

    fun toggleAutoBackup(enabled: Boolean) {
        viewModelScope.launch {
            prefs.edit().putBoolean("auto_backup_enabled", enabled).apply()
            _isAutoBackupEnabled.value = enabled

            if (enabled) {
                scheduleDailyBackup()
                calculateNextBackupTime()
            } else {
                cancelDailyBackup()
                _nextBackupTime.value = 0L
            }
        }
    }

    private fun scheduleDailyBackup() {
        val constraints = Constraints.Builder()
            .setRequiresStorageNotLow(true)
            .build()

        val currentDate = Calendar.getInstance()
        val dueDate = Calendar.getInstance()
        dueDate.set(Calendar.HOUR_OF_DAY, 0)
        dueDate.set(Calendar.MINUTE, 5)
        dueDate.set(Calendar.SECOND, 0)

        if (dueDate.before(currentDate)) {
            dueDate.add(Calendar.HOUR_OF_DAY, 24)
        }

        val initialDelay = dueDate.timeInMillis - currentDate.timeInMillis

        val dailyBackupRequest = PeriodicWorkRequestBuilder<AutoBackupWorker>(24, TimeUnit.HOURS)
            .setConstraints(constraints)
            .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
            .addTag("daily_backup")
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "daily_backup_work",
            ExistingPeriodicWorkPolicy.UPDATE,
            dailyBackupRequest
        )
    }

    private fun cancelDailyBackup() {
        WorkManager.getInstance(context).cancelUniqueWork("daily_backup_work")
    }

    fun createBackup(uri: Uri) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                    val success = backupManager.exportToJson(outputStream)
                    if (success) {
                        _backupResult.emit(BackupResult.Success("Manual backup created successfully"))
                        loadBackupStatus()
                    } else {
                        _backupResult.emit(BackupResult.Error("Failed to create backup"))
                    }
                } ?: run {
                    _backupResult.emit(BackupResult.Error("Could not open output stream"))
                }
            } catch (e: Exception) {
                _backupResult.emit(BackupResult.Error("Backup failed: ${e.localizedMessage}"))
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun restoreBackup(uri: Uri) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    val result = backupManager.importFromJson(inputStream)
                    if (result.isSuccess) {
                        _backupResult.emit(BackupResult.Success("Data restored successfully"))
                    } else {
                        _backupResult.emit(BackupResult.Error("Restore failed: ${result.exceptionOrNull()?.message}"))
                    }
                } ?: run {
                    _backupResult.emit(BackupResult.Error("Could not open input stream"))
                }
            } catch (e: Exception) {
                _backupResult.emit(BackupResult.Error("Restore failed: ${e.localizedMessage}"))
            } finally {
                _isLoading.value = false
            }
        }
    }
}

sealed class BackupResult {
    data class Success(val message: String) : BackupResult()
    data class Error(val message: String) : BackupResult()
}
