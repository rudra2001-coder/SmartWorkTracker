package com.rudra.smartworktracker.ui.screens.backup

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.*
import com.rudra.smartworktracker.data.backup.AutoBackupWorker
import com.rudra.smartworktracker.data.backup.BackupEntry
import com.rudra.smartworktracker.data.backup.BackupManager
import com.rudra.smartworktracker.data.backup.BackupOptions
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

data class BackupUiState(
    val isAutoBackupEnabled: Boolean = false,
    val lastBackupTime: Long = 0,
    val lastExportResult: ExportResult? = null,
    val restorePreview: RestorePreview? = null,
    val backupHistory: List<BackupEntry> = emptyList(),
    val retentionLimit: Int = 0,
    val retentionDays: Int = 0,
    val backupHour: Int = 0,
    val backupMinute: Int = 5,
    val backupTimeDisplay: String = "12:05 AM",
    val backupFrequency: String = "daily",

    val compressEnabled: Boolean = false,
    val encryptionEnabled: Boolean = false,
    val encryptionPassword: String = "",
    val showPassword: Boolean = false,
    val restorePassword: String = "",
    val showRestorePassword: Boolean = false,

    val selectedTypes: Set<String> = emptySet(),
    val selectAllTypes: Boolean = true,

    val availableTypes: List<String> = listOf(
        "Accounts", "Expenses", "Incomes", "Work Logs", "Loans", "EMIs",
        "Credit Cards", "Credit Card Tx", "Savings", "Fin. Transactions",
        "Habits", "Focus Sessions", "Work Sessions", "Health Metrics",
        "Journals", "Work Days", "Achievements", "Colleagues",
        "Schedules", "Recurring Rules", "Recurring Tx",
        "Reality Entries", "Decisions", "Check-ins", "Debts",
        "Weekly Reports", "Meal Settings", "Manual Meals", "Notifications"
    )
)

class BackupViewModel(private val context: Context) : ViewModel() {

    private val backupManager = BackupManager(context)
    private val prefs = context.getSharedPreferences("backup_prefs", Context.MODE_PRIVATE)

    private val _backupState = MutableStateFlow<BackupState>(BackupState.Idle)
    val backupState: StateFlow<BackupState> = _backupState.asStateFlow()

    private val _uiState = MutableStateFlow(BackupUiState())
    val uiState: StateFlow<BackupUiState> = _uiState.asStateFlow()

    init {
        loadAll()
    }

    private fun loadAll() {
        val lastTime = prefs.getLong("last_auto_backup_time", 0L)
        val enabled = prefs.getBoolean("auto_backup_enabled", false)
        val frequency = backupManager.getBackupFrequency()

        val lastResult = ExportResult(
            success = true,
            totalRows = prefs.getLong("last_backup_row_count", 0),
            fileSizeBytes = prefs.getLong("last_backup_file_size", 0),
            durationMs = prefs.getLong("last_backup_duration_ms", 0)
        )

        _uiState.value = _uiState.value.copy(
            lastBackupTime = lastTime,
            isAutoBackupEnabled = enabled,
            lastExportResult = if (lastResult.totalRows > 0) lastResult else null,
            backupHistory = backupManager.getBackupHistory(),
            retentionLimit = backupManager.getRetentionLimit(),
            retentionDays = backupManager.getRetentionDays(),
            backupHour = backupManager.getBackupHour(),
            backupMinute = backupManager.getBackupMinute(),
            backupTimeDisplay = backupManager.getBackupTimeDisplay(),
            backupFrequency = frequency,
            selectAllTypes = true,
            selectedTypes = emptySet()
        )
    }

    fun refreshHistory() {
        _uiState.value = _uiState.value.copy(
            backupHistory = backupManager.getBackupHistory()
        )
    }

    fun toggleAutoBackup(enabled: Boolean) {
        viewModelScope.launch {
            prefs.edit().putBoolean("auto_backup_enabled", enabled).apply()
            _uiState.value = _uiState.value.copy(isAutoBackupEnabled = enabled)
            if (enabled) {
                schedulePeriodicBackup()
            } else {
                cancelPeriodicBackup()
            }
        }
    }

    fun setBackupFrequency(frequency: String) {
        viewModelScope.launch {
            backupManager.setBackupFrequency(frequency)
            _uiState.value = _uiState.value.copy(backupFrequency = frequency)
            if (_uiState.value.isAutoBackupEnabled) {
                cancelPeriodicBackup()
                schedulePeriodicBackup()
            }
        }
    }

    private fun schedulePeriodicBackup() {
        val constraints = Constraints.Builder()
            .setRequiresStorageNotLow(true)
            .build()
        val currentDate = Calendar.getInstance()
        val dueDate = Calendar.getInstance()
        dueDate.set(Calendar.HOUR_OF_DAY, _uiState.value.backupHour)
        dueDate.set(Calendar.MINUTE, _uiState.value.backupMinute)
        dueDate.set(Calendar.SECOND, 0)
        if (dueDate.before(currentDate)) {
            dueDate.add(Calendar.HOUR_OF_DAY, 24)
        }
        val initialDelay = dueDate.timeInMillis - currentDate.timeInMillis

        val intervalHours = when (_uiState.value.backupFrequency) {
            "weekly" -> 168L
            "monthly" -> 720L
            else -> 24L
        }

        val request = PeriodicWorkRequestBuilder<AutoBackupWorker>(intervalHours, TimeUnit.HOURS)
            .setConstraints(constraints)
            .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
            .addTag("periodic_backup")
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "periodic_backup_work",
            ExistingPeriodicWorkPolicy.UPDATE,
            request
        )
    }

    private fun cancelPeriodicBackup() {
        WorkManager.getInstance(context).cancelUniqueWork("periodic_backup_work")
    }

    fun toggleCompress(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(compressEnabled = enabled)
    }

    fun toggleEncryption(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(
            encryptionEnabled = enabled,
            encryptionPassword = if (!enabled) "" else _uiState.value.encryptionPassword
        )
    }

    fun setEncryptionPassword(password: String) {
        _uiState.value = _uiState.value.copy(encryptionPassword = password)
    }

    fun setRestorePassword(password: String) {
        _uiState.value = _uiState.value.copy(restorePassword = password)
    }

    fun toggleShowPassword() {
        _uiState.value = _uiState.value.copy(showPassword = !_uiState.value.showPassword)
    }

    fun toggleShowRestorePassword() {
        _uiState.value = _uiState.value.copy(showRestorePassword = !_uiState.value.showRestorePassword)
    }

    fun toggleType(type: String) {
        val current = _uiState.value.selectedTypes.toMutableSet()
        if (type in current) current.remove(type) else current.add(type)
        _uiState.value = _uiState.value.copy(
            selectedTypes = current,
            selectAllTypes = false
        )
    }

    fun selectAllTypes() {
        _uiState.value = _uiState.value.copy(
            selectAllTypes = true,
            selectedTypes = emptySet()
        )
    }

    fun createBackup(uri: Uri) {
        viewModelScope.launch {
            _backupState.value = BackupState.Exporting("Starting export...")
            try {
                val state = _uiState.value
                val options = BackupOptions(
                    compress = state.compressEnabled,
                    password = if (state.encryptionEnabled && state.encryptionPassword.isNotBlank())
                        state.encryptionPassword else null,
                    selectedTypes = if (!state.selectAllTypes && state.selectedTypes.isNotEmpty())
                        state.selectedTypes else null
                )

                context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                    val result = backupManager.exportToJson(outputStream,
                        onProgress = { _backupState.value = BackupState.Exporting(it) },
                        options = options
                    )
                    if (result.success) {
                        _uiState.value = _uiState.value.copy(lastExportResult = result)
                        val fileName = uri.lastPathSegment ?: "backup.json"
                        backupManager.recordBackup(
                            fileName = fileName,
                            totalRows = result.totalRows,
                            fileSizeBytes = result.fileSizeBytes,
                            isManual = true,
                            fileUri = uri.toString()
                        )
                        refreshHistory()
                        _backupState.value = BackupState.Success(
                            "Exported ${result.totalRows} records (${formatSize(result.fileSizeBytes)})"
                        )
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
                val password = _uiState.value.restorePassword.takeIf { it.isNotBlank() }
                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    val preview = backupManager.previewBackup(inputStream, password = password)
                    _uiState.value = _uiState.value.copy(restorePreview = preview)
                    _backupState.value = BackupState.Idle
                }
            } catch (e: Exception) {
                _backupState.value = BackupState.Error(
                    if (e.message?.contains("tag") == true) "Wrong password" else "Cannot read file: ${e.localizedMessage}"
                )
            }
        }
    }

    fun restoreBackup(uri: Uri) {
        viewModelScope.launch {
            _backupState.value = BackupState.Restoring("Starting restore...")
            try {
                val password = _uiState.value.restorePassword.takeIf { it.isNotBlank() }
                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    val result = backupManager.importFromJson(
                        inputStream = inputStream,
                        onProgress = { _backupState.value = BackupState.Restoring(it) },
                        password = password
                    )
                    if (result.isSuccess) {
                        _uiState.value = _uiState.value.copy(restorePreview = null)
                        _backupState.value = BackupState.Success("Data restored successfully")
                        loadAll()
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
            _uiState.value = _uiState.value.copy(
                backupHour = hour,
                backupMinute = minute,
                backupTimeDisplay = backupManager.getBackupTimeDisplay()
            )
            if (_uiState.value.isAutoBackupEnabled) {
                cancelPeriodicBackup()
                schedulePeriodicBackup()
            }
            _backupState.value = BackupState.Success(
                "Backup time set to ${backupManager.getBackupTimeDisplay()}"
            )
        }
    }

    fun deleteBackupEntry(entry: BackupEntry) {
        viewModelScope.launch {
            _backupState.value = BackupState.Exporting("Deleting backup...")
            try {
                withContext(Dispatchers.IO) {
                    backupManager.deleteBackupEntry(entry)
                }
                refreshHistory()
                _backupState.value = BackupState.Success("Deleted: ${entry.fileName}")
            } catch (e: Exception) {
                _backupState.value = BackupState.Error("Delete failed: ${e.localizedMessage}")
            }
        }
    }

    fun setRetentionLimit(limit: Int) {
        viewModelScope.launch {
            backupManager.setRetentionLimit(limit)
            _uiState.value = _uiState.value.copy(retentionLimit = limit)
            refreshHistory()
            _backupState.value = BackupState.Success(
                "Count retention set to $limit"
            )
        }
    }

    fun setRetentionDays(days: Int) {
        viewModelScope.launch {
            backupManager.setRetentionDays(days)
            _uiState.value = _uiState.value.copy(retentionDays = days)
            refreshHistory()
            _backupState.value = BackupState.Success(
                "Age retention set to $days days"
            )
        }
    }

    fun clearRestorePreview() {
        _uiState.value = _uiState.value.copy(restorePreview = null)
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
