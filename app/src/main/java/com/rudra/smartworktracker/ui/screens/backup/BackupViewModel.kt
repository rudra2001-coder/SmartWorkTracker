package com.rudra.smartworktracker.ui.screens.backup

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.*
import com.rudra.smartworktracker.data.backup.AutoBackupWorker
import com.rudra.smartworktracker.data.backup.BackupManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.concurrent.TimeUnit

class BackupViewModel(private val context: Context) : ViewModel() {

    private val backupManager = BackupManager(context)
    private val prefs = context.getSharedPreferences("backup_prefs", Context.MODE_PRIVATE)

    private val _backupState = MutableStateFlow<BackupState>(BackupState.Idle)
    val backupState: StateFlow<BackupState> = _backupState.asStateFlow()

    private val _lastBackupTime = MutableStateFlow<Long>(0L)
    val lastBackupTime: StateFlow<Long> = _lastBackupTime.asStateFlow()

    private val _nextBackupTime = MutableStateFlow<Long>(0L)
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
            _backupState.value = BackupState.InProgress
            try {
                context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                    val success = backupManager.exportToJson(outputStream)
                    if (success) {
                        _backupState.value = BackupState.Success("Manual backup created successfully")
                        loadBackupStatus()
                    } else {
                        _backupState.value = BackupState.Error("Failed to create backup")
                    }
                } ?: run {
                    _backupState.value = BackupState.Error("Could not open output stream")
                }
            } catch (e: Exception) {
                _backupState.value = BackupState.Error("Backup failed: ${e.localizedMessage}")
            }
        }
    }

    fun restoreBackup(uri: Uri) {
        viewModelScope.launch {
            _backupState.value = BackupState.InProgress
            try {
                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    val result = backupManager.importFromJson(inputStream)
                    if (result.isSuccess) {
                        _backupState.value = BackupState.Success("Data restored successfully")
                    } else {
                        _backupState.value = BackupState.Error("Restore failed: ${result.exceptionOrNull()?.message}")
                    }
                } ?: run {
                    _backupState.value = BackupState.Error("Could not open input stream")
                }
            } catch (e: Exception) {
                _backupState.value = BackupState.Error("Restore failed: ${e.localizedMessage}")
            }
        }
    }
}

sealed class BackupState {
    object Idle : BackupState()
    object InProgress : BackupState()
    data class Success(val message: String) : BackupState()
    data class Error(val message: String) : BackupState()
}
