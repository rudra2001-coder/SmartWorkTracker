package com.rudra.smartworktracker.ui.screens.backup

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rudra.smartworktracker.data.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream
import kotlin.system.exitProcess

class BackupViewModel(private val db: AppDatabase, private val context: Context) : ViewModel() {

    private val _backupState = MutableStateFlow<BackupState>(BackupState.Idle)
    val backupState: StateFlow<BackupState> = _backupState.asStateFlow()

    companion object {
        private const val TEMP_RESTORE_FILE = "restore_backup.zip"
        private const val PREFS_EXTENSION = ".xml"
        private const val APP_FILES_DIR = "app_files"
        private const val BUFFER_SIZE = 8192
    }

    private fun getDatabaseFiles(context: Context): List<File> {
        val dbName = db.openHelper.databaseName ?: "app_database"
        val dbPath = context.getDatabasePath(dbName)

        val databaseFiles = mutableListOf<File>()

        // Main database file
        if (dbPath.exists()) {
            databaseFiles.add(dbPath)
        }

        // Journal files (if they exist)
        val shmFile = File(dbPath.parent, "$dbName-shm")
        val walFile = File(dbPath.parent, "$dbName-wal")

        if (shmFile.exists()) databaseFiles.add(shmFile)
        if (walFile.exists()) databaseFiles.add(walFile)

        return databaseFiles
    }

    private fun getSharedPrefsFiles(context: Context): List<File> {
        val prefsDir = File(context.filesDir.parent + "/shared_prefs/")
        return if (prefsDir.exists() && prefsDir.isDirectory) {
            prefsDir.listFiles { file ->
                file.name.startsWith("smart_work_tracker") && file.name.endsWith(PREFS_EXTENSION)
            }?.toList() ?: emptyList()
        } else {
            emptyList()
        }
    }

    private fun getAppFiles(): List<File> {
        val appFilesDir = File(context.filesDir, APP_FILES_DIR)
        return if (appFilesDir.exists() && appFilesDir.isDirectory) {
            appFilesDir.walk().filter { it.isFile }.toList()
        } else {
            emptyList()
        }
    }

    fun createBackup(backupFileUri: Uri) {
        viewModelScope.launch {
            _backupState.value = BackupState.InProgress
            try {
                val result = withContext(Dispatchers.IO) {
                    performBackup(backupFileUri)
                }
                _backupState.value = result
            } catch (e: Exception) {
                _backupState.value = BackupState.Error("Backup failed: ${e.localizedMessage ?: "Unknown error"}")
            }
        }
    }

    private suspend fun performBackup(backupFileUri: Uri): BackupState {
        return try {
            val filesToBackup = getFilesForBackup()

            if (filesToBackup.isEmpty()) {
                return BackupState.Error("No data found to backup")
            }

            context.contentResolver.openOutputStream(backupFileUri)?.use { outputStream ->
                ZipOutputStream(BufferedOutputStream(outputStream)).use { zos ->
                    for (file in filesToBackup) {
                        if (file.exists()) {
                            addFileToZip(zos, file, getRelativePath(file))
                        }
                    }
                }
            } ?: return BackupState.Error("Failed to open output stream")

            BackupState.Success("Backup created successfully")
        } catch (e: SecurityException) {
            BackupState.Error("Permission denied: Cannot create backup file")
        } catch (e: Exception) {
            BackupState.Error("Backup failed: ${e.localizedMessage ?: "Unknown error"}")
        }
    }

    private fun getFilesForBackup(): List<File> {
        val files = mutableListOf<File>()

        // Database files
        files.addAll(getDatabaseFiles(context))

        // Shared preferences
        files.addAll(getSharedPrefsFiles(context))

        // App files
        files.addAll(getAppFiles())

        return files.filter { it.exists() && it.canRead() }
    }

    private fun getRelativePath(file: File): String {
        return when {
            file.absolutePath.contains("/databases/") -> "databases/${file.name}"
            file.absolutePath.contains("/shared_prefs/") -> "shared_prefs/${file.name}"
            file.absolutePath.contains("/$APP_FILES_DIR/") -> {
                val relativePath = file.absolutePath.substringAfter("$APP_FILES_DIR/")
                "$APP_FILES_DIR/$relativePath"
            }
            else -> file.name
        }
    }

    private fun addFileToZip(zos: ZipOutputStream, file: File, entryPath: String) {
        if (!file.exists() || !file.canRead()) return

        try {
            val entry = ZipEntry(entryPath).apply {
                time = file.lastModified()
                size = file.length()
            }
            zos.putNextEntry(entry)
            file.inputStream().buffered(BUFFER_SIZE).use { input ->
                input.copyTo(zos, BUFFER_SIZE)
            }
            zos.closeEntry()
        } catch (e: Exception) {
            throw RuntimeException("Failed to add file to zip: ${file.name}", e)
        }
    }

    fun restoreBackup(backupZipUri: Uri) {
        viewModelScope.launch {
            _backupState.value = BackupState.InProgress
            try {
                val result = withContext(Dispatchers.IO) {
                    performRestore(backupZipUri)
                }
                _backupState.value = result
            } catch (e: Exception) {
                _backupState.value = BackupState.Error("Restore failed: ${e.localizedMessage ?: "Unknown error"}")
            }
        }
    }

    private suspend fun performRestore(backupZipUri: Uri): BackupState {
        var tempFile: File? = null
        return try {
            tempFile = backupZipUri.toTempFile(context)

            if (!isValidBackupFile(tempFile)) {
                return BackupState.Error("Invalid backup file format")
            }

            val restoredFiles = restoreFilesFromZip(tempFile)

            if (restoredFiles > 0) {
                BackupState.Success("$restoredFiles files restored successfully. Restarting app...").also {
                    // Small delay to ensure message is shown
                    kotlinx.coroutines.delay(1000)
                    restartApp()
                }
            } else {
                BackupState.Error("No files were restored from backup")
            }
        } catch (e: SecurityException) {
            BackupState.Error("Permission denied: Cannot restore backup")
        } catch (e: Exception) {
            BackupState.Error("Restore failed: ${e.localizedMessage ?: "Unknown error"}")
        } finally {
            // Clean up temp file
            tempFile?.delete()
        }
    }

    private fun isValidBackupFile(file: File): Boolean {
        if (!file.exists() || file.length() == 0L) return false

        return try {
            ZipFile(file).use { zip ->
                zip.entries().asSequence().any { entry ->
                    !entry.isDirectory && (
                            entry.name.contains("databases/") ||
                                    entry.name.contains("shared_prefs/") ||
                                    entry.name.startsWith(APP_FILES_DIR)
                            )
                }
            }
        } catch (e: Exception) {
            false
        }
    }

    private fun restoreFilesFromZip(backupZip: File): Int {
        var restoredCount = 0

        ZipFile(backupZip).use { zip ->
            for (entry in zip.entries()) {
                if (!entry.isDirectory) {
                    val outputFile = getOutputFileForEntry(entry.name)
                    if (outputFile != null && restoreSingleFile(zip, entry, outputFile)) {
                        restoredCount++
                    }
                }
            }
        }

        return restoredCount
    }

    private fun getOutputFileForEntry(entryName: String): File? {
        return when {
            entryName.startsWith("databases/") -> {
                val dbName = entryName.substringAfter("databases/")
                File(context.getDatabasePath(dbName).parent, dbName)
            }
            entryName.startsWith("shared_prefs/") -> {
                val prefsName = entryName.substringAfter("shared_prefs/")
                File(context.filesDir.parent + "/shared_prefs/", prefsName)
            }
            entryName.startsWith("$APP_FILES_DIR/") -> {
                val relativePath = entryName.substringAfter("$APP_FILES_DIR/")
                File(File(context.filesDir, APP_FILES_DIR), relativePath)
            }
            else -> null
        }
    }

    private fun restoreSingleFile(zip: ZipFile, entry: ZipEntry, outputFile: File): Boolean {
        return try {
            outputFile.parentFile?.mkdirs()

            zip.getInputStream(entry).use { input ->
                FileOutputStream(outputFile).use { output ->
                    input.copyTo(output, BUFFER_SIZE)
                }
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    private fun Uri.toTempFile(context: Context): File {
        val tempFile = File(context.cacheDir, TEMP_RESTORE_FILE)

        context.contentResolver.openInputStream(this)?.use { input ->
            FileOutputStream(tempFile).use { output ->
                input.copyTo(output, BUFFER_SIZE)
            }
        } ?: throw IllegalArgumentException("Cannot open backup file")

        return tempFile
    }

    private fun restartApp() {
        // This will restart the app
        exitProcess(0)
    }
}

sealed class BackupState {
    object Idle : BackupState()
    object InProgress : BackupState()
    data class Success(val message: String) : BackupState()
    data class Error(val message: String) : BackupState()
}