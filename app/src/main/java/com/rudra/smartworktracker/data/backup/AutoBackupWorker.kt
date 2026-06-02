package com.rudra.smartworktracker.data.backup

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.rudra.smartworktracker.engine.InAppNotificationManager
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class AutoBackupWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val backupManager = BackupManager(applicationContext)
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmm", Locale.getDefault()).format(Date())
        val fileName = "SmartWork_Auto_$timestamp.json"

        return try {
            val success = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                saveToDownloadsMediaStore(fileName, backupManager)
            } else {
                saveToDownloadsLegacy(fileName, backupManager)
            }

            if (success) {
                val prefs = applicationContext.getSharedPreferences("backup_prefs", Context.MODE_PRIVATE)
                prefs.edit().putLong("last_auto_backup_time", System.currentTimeMillis()).apply()
                InAppNotificationManager.getInstance(applicationContext).showBackup(
                    "Auto Backup Successful",
                    "Data backed up successfully to $fileName"
                )
                Result.success()
            } else {
                InAppNotificationManager.getInstance(applicationContext).showBackup(
                    "Auto Backup Failed",
                    "Failed to create backup file"
                )
                Result.retry()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            InAppNotificationManager.getInstance(applicationContext).showBackup(
                "Auto Backup Error",
                "Backup failed: ${e.localizedMessage ?: "Unknown error"}"
            )
            Result.failure()
        }
    }

    private suspend fun saveToDownloadsMediaStore(fileName: String, backupManager: BackupManager): Boolean {
        val resolver = applicationContext.contentResolver
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(MediaStore.MediaColumns.MIME_TYPE, "application/json")
            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
        }

        val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
        return uri?.let {
            resolver.openOutputStream(it)?.use { outputStream ->
                backupManager.exportToJson(outputStream)
            } ?: false
        } ?: false
    }

    private suspend fun saveToDownloadsLegacy(fileName: String, backupManager: BackupManager): Boolean {
        val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        if (!downloadsDir.exists()) downloadsDir.mkdirs()
        val file = File(downloadsDir, fileName)
        return FileOutputStream(file).use { outputStream ->
            backupManager.exportToJson(outputStream)
        }
    }
}
