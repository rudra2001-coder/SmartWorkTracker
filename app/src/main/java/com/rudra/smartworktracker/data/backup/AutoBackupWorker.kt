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
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fileName = "SmartWork_Auto_$timestamp.json"
        val prefs = applicationContext.getSharedPreferences("backup_prefs", Context.MODE_PRIVATE)
        val attemptCount = prefs.getInt("auto_backup_attempts", 0)

        return try {
            var result = ExportResult(success = false)
            var savedUri = ""
            var savedMediaStoreId = 0L

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val resolver = applicationContext.contentResolver
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, "application/json")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                }
                val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                result = uri?.let {
                    savedUri = it.toString()
                    try { savedMediaStoreId = it.lastPathSegment?.toLong() ?: 0L } catch (_: Exception) {}
                    resolver.openOutputStream(it)?.use { outputStream ->
                        backupManager.exportToJson(
                            outputStream = outputStream,
                            options = BackupOptions(compress = true)
                        )
                    }
                } ?: ExportResult(success = false, errorMessage = "Failed to create MediaStore entry")
            } else {
                val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                if (!downloadsDir.exists()) downloadsDir.mkdirs()
                result = FileOutputStream(File(downloadsDir, fileName)).use { outputStream ->
                    backupManager.exportToJson(
                        outputStream = outputStream,
                        options = BackupOptions(compress = true)
                    )
                }
            }

            prefs.edit().putInt("auto_backup_attempts", 0).apply()

            if (result.success) {
                prefs.edit().apply {
                    putLong("last_auto_backup_time", System.currentTimeMillis())
                    putLong("last_backup_file_size", result.fileSizeBytes)
                    putLong("last_backup_row_count", result.totalRows)
                    putLong("last_backup_duration_ms", result.durationMs)
                    putString("last_backup_file_name", fileName)
                    apply()
                }

                backupManager.recordBackup(
                    fileName = fileName,
                    totalRows = result.totalRows,
                    fileSizeBytes = result.fileSizeBytes,
                    isManual = false,
                    fileUri = savedUri,
                    mediaStoreId = savedMediaStoreId
                )

                InAppNotificationManager.getInstance(applicationContext).showBackup(
                    "Auto Backup Successful",
                    "Exported ${result.totalRows} records (${formatSize(result.fileSizeBytes)}, compressed)"
                )
                Result.success()
            } else {
                InAppNotificationManager.getInstance(applicationContext).showBackup(
                    "Auto Backup Failed",
                    result.errorMessage ?: "Unknown error"
                )
                if (attemptCount < 3) Result.retry() else Result.failure()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            prefs.edit().putInt("auto_backup_attempts", attemptCount + 1).apply()
            InAppNotificationManager.getInstance(applicationContext).showBackup(
                "Auto Backup Error",
                "Attempt ${attemptCount + 1} failed: ${e.localizedMessage ?: "Unknown error"}"
            )
            if (attemptCount < 3) Result.retry() else Result.failure()
        }
    }

    private fun formatSize(bytes: Long): String = when {
        bytes < 1024 -> "$bytes B"
        bytes < 1024 * 1024 -> "%.1f KB".format(bytes / 1024.0)
        else -> "%.1f MB".format(bytes / (1024.0 * 1024.0))
    }
}
