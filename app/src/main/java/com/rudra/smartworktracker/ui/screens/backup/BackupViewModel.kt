package com.rudra.smartworktracker.ui.screens.backup

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.*
import com.rudra.smartworktracker.data.backup.AutoBackupWorker
import com.rudra.smartworktracker.data.backup.BackupEntry
import com.rudra.smartworktracker.data.backup.BackupManager
import com.rudra.smartworktracker.data.backup.ExportResult
import com.rudra.smartworktracker.data.backup.RestorePreview
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*
import java.util.concurrent.TimeUnit

sealed class BackupState {
    object Idle : BackupState()
    object InProgress : BackupState()
    data class Exporting(val progress: String) : BackupState()
    data class Restoring(val progress: String) : BackupState()
    data class Success(val message: String) : BackupState()
    data class Error(val message: String) : BackupState()
}

class BackupViewModel(private val context: Context) : ViewModel() {

    private val backupManager = BackupManager(context)
    private val prefs = context.getSharedPreferences("backup_prefs", Context.MODE_PRIVATE)

    private val _backupState = MutableStateFlow<BackupState>(BackupState.Idle)
    val backupState: StateFlow<BackupState> = _backupState.asStateFlow()

    private val _lastBackupTime = MutableStateFlow(0L)
    val lastBackupTime: StateFlow<Long> = _lastBackupTime.asStateFlow()

    private val _nextBackupTime = MutableStateFlow(0L)
    val nextBackupTime: StateFlow<Long> = _nextBackupTime.asStateFlow()

    private val _isAutoBackupEnabled = MutableStateFlow(false)
    val isAutoBackupEnabled: StateFlow<Boolean> = _isAutoBackupEnabled.asStateFlow()

    private val _lastExportResult = MutableStateFlow<ExportResult?>(null)
    val lastExportResult: StateFlow<ExportResult?> = _lastExportResult.asStateFlow()

    private val _restorePreview = MutableStateFlow<RestorePreview?>(null)
    val restorePreview: StateFlow<RestorePreview?> = _restorePreview.asStateFlow()

    private val _hasStoredBackup = MutableStateFlow(false)
    val hasStoredBackup: StateFlow<Boolean> = _hasStoredBackup.asStateFlow()

    private val _backupHistory = MutableStateFlow<List<BackupEntry>>(emptyList())
    val backupHistory: StateFlow<List<BackupEntry>> = _backupHistory.asStateFlow()

    private val _retentionLimit = MutableStateFlow(0)
    val retentionLimit: StateFlow<Int> = _retentionLimit.asStateFlow()

    private val _backupHour = MutableStateFlow(0)
    val backupHour: StateFlow<Int> = _backupHour.asStateFlow()

    private val _backupMinute = MutableStateFlow(5)
    val backupMinute: StateFlow<Int> = _backupMinute.asStateFlow()

    private val _backupTimeDisplay = MutableStateFlow("12:05 AM")
    val backupTimeDisplay: StateFlow<String> = _backupTimeDisplay.asStateFlow()

    init {
        loadBackupStatus()
        loadHistory()
    }

    fun loadBackupStatus() {
        _lastBackupTime.value = prefs.getLong("last_auto_backup_time", 0L)
        _isAutoBackupEnabled.value = prefs.getBoolean("auto_backup_enabled", false)
        _hasStoredBackup.value = prefs.getLong("last_auto_backup_time", 0L) > 0

        val lastResult = ExportResult(
            success = true,
            totalRows = prefs.getLong("last_backup_row_count", 0),
            fileSizeBytes = prefs.getLong("last_backup_file_size", 0),
            durationMs = prefs.getLong("last_backup_duration_ms", 0)
        )
        _lastExportResult.value = if (lastResult.totalRows > 0) lastResult else null

        if (_isAutoBackupEnabled.value) {
            calculateNextBackupTime()
        } else {
            _nextBackupTime.value = 0L
        }
    }

    fun loadHistory() {
        _backupHistory.value = backupManager.getBackupHistory()
        _retentionLimit.value = backupManager.getRetentionLimit()
        _backupHour.value = backupManager.getBackupHour()
        _backupMinute.value = backupManager.getBackupMinute()
        _backupTimeDisplay.value = backupManager.getBackupTimeDisplay()
    }

    private fun calculateNextBackupTime() {
        val nextBackup = Calendar.getInstance()
        nextBackup.set(Calendar.HOUR_OF_DAY, _backupHour.value)
        nextBackup.set(Calendar.MINUTE, _backupMinute.value)
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
        dueDate.set(Calendar.HOUR_OF_DAY, _backupHour.value)
        dueDate.set(Calendar.MINUTE, _backupMinute.value)
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
            _backupState.value = BackupState.Exporting("Starting export...")
            try {
                context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                    val result = backupManager.exportToJson(outputStream) { progress ->
                        _backupState.value = BackupState.Exporting(progress)
                    }
                    if (result.success) {
                        _lastExportResult.value = result
                        val fileName = uri.lastPathSegment ?: "manual_backup.json"
                        backupManager.recordBackup(
                            fileName = fileName,
                            totalRows = result.totalRows,
                            fileSizeBytes = result.fileSizeBytes,
                            isManual = true,
                            fileUri = uri.toString()
                        )
                        loadHistory()
                        _backupState.value = BackupState.Success(
                            "Exported ${result.totalRows} records (${formatSize(result.fileSizeBytes)})"
                        )
                        loadBackupStatus()
                    } else {
                        _backupState.value = BackupState.Error(
                            result.errorMessage ?: "Export failed"
                        )
                    }
                } ?: run {
                    _backupState.value = BackupState.Error("Could not open file")
                }
            } catch (e: Exception) {
                _backupState.value = BackupState.Error("Export failed: ${e.localizedMessage}")
            }
        }
    }

    fun previewBackup(uri: Uri) {
        viewModelScope.launch {
            _backupState.value = BackupState.Exporting("Reading backup file...")
            try {
                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    val preview = backupManager.previewBackup(inputStream)
                    _restorePreview.value = preview
                    _backupState.value = BackupState.Idle
                }
            } catch (e: Exception) {
                _backupState.value = BackupState.Error("Cannot read file: ${e.localizedMessage}")
            }
        }
    }

    fun restoreBackup(uri: Uri) {
        viewModelScope.launch {
            _backupState.value = BackupState.Restoring("Starting restore...")
            try {
                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    val result = backupManager.importFromJson(inputStream) { progress ->
                        _backupState.value = BackupState.Restoring(progress)
                    }
                    if (result.isSuccess) {
                        _restorePreview.value = null
                        _backupState.value = BackupState.Success("Data restored successfully")
                        loadBackupStatus()
                    } else {
                        _backupState.value = BackupState.Error(
                            result.exceptionOrNull()?.message ?: "Restore failed"
                        )
                    }
                } ?: run {
                    _backupState.value = BackupState.Error("Could not open file")
                }
            } catch (e: Exception) {
                _backupState.value = BackupState.Error("Restore failed: ${e.localizedMessage}")
            }
        }
    }

    fun updateBackupTime(hour: Int, minute: Int) {
        viewModelScope.launch {
            backupManager.setBackupTime(hour, minute)
            _backupHour.value = hour
            _backupMinute.value = minute
            _backupTimeDisplay.value = backupManager.getBackupTimeDisplay()
            if (_isAutoBackupEnabled.value) {
                cancelDailyBackup()
                scheduleDailyBackup()
                calculateNextBackupTime()
            }
            _backupState.value = BackupState.Success("Backup time set to ${backupManager.getBackupTimeDisplay()}")
        }
    }

    fun deleteBackupEntry(entry: BackupEntry) {
        viewModelScope.launch {
            _backupState.value = BackupState.Exporting("Deleting backup...")
            try {
                withContext(Dispatchers.IO) {
                    backupManager.deleteBackupEntry(entry)
                }
                loadHistory()
                _backupState.value = BackupState.Success("Deleted: ${entry.fileName}")
            } catch (e: Exception) {
                _backupState.value = BackupState.Error("Delete failed: ${e.localizedMessage}")
            }
        }
    }

    fun setRetentionLimit(limit: Int) {
        viewModelScope.launch {
            backupManager.setRetentionLimit(limit)
            _retentionLimit.value = limit
            loadHistory()
            _backupState.value = BackupState.Success(
                "Retention limit set to $limit. ${if (limit > 0) "Oldest backups cleaned up." else "All backups kept."}"
            )
        }
    }

    fun clearRestorePreview() {
        _restorePreview.value = null
    }

    fun onStateConsumed() {
        if (_backupState.value !is BackupState.InProgress &&
            _backupState.value !is BackupState.Exporting &&
            _backupState.value !is BackupState.Restoring) {
            _backupState.value = BackupState.Idle
        }
    }

    private fun formatSize(bytes: Long): String = when {
        bytes < 1024 -> "$bytes B"
        bytes < 1024 * 1024 -> "%.1f KB".format(bytes / 1024.0)
        else -> "%.1f MB".format(bytes / (1024.0 * 1024.0))
    }
}
