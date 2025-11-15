package com.rudra.smartworktracker.ui.screens.backup

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rudra.smartworktracker.data.AppDatabase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

class BackupViewModel(private val db: AppDatabase) : ViewModel() {

    private val _backupState = MutableStateFlow<BackupState>(BackupState.Idle)
    val backupState: StateFlow<BackupState> = _backupState.asStateFlow()

    fun backupDatabase(context: Context) {
        viewModelScope.launch {
            _backupState.value = BackupState.InProgress
            try {
                val dbFile = context.getDatabasePath(db.openHelper.databaseName)
                val backupDir = File(context.getExternalFilesDir(null), "backups")
                if (!backupDir.exists()) {
                    backupDir.mkdirs()
                }
                val backupFile = File(backupDir, "smart_work_tracker_backup.db")
                dbFile.copyTo(backupFile, overwrite = true)
                _backupState.value = BackupState.Success("Backup successful!")
            } catch (e: Exception) {
                _backupState.value = BackupState.Error("Backup failed: ${e.message}")
            }
        }
    }

    fun restoreDatabase(context: Context) {
        viewModelScope.launch {
            _backupState.value = BackupState.InProgress
            try {
                val backupDir = File(context.getExternalFilesDir(null), "backups")
                val backupFile = File(backupDir, "smart_work_tracker_backup.db")
                val dbFile = context.getDatabasePath(db.openHelper.databaseName)
                backupFile.copyTo(dbFile, overwrite = true)
                _backupState.value = BackupState.Success("Restore successful! Please restart the app.")
            } catch (e: Exception) {
                _backupState.value = BackupState.Error("Restore failed: ${e.message}")
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
