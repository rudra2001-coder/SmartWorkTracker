package com.rudra.smartworktracker.data.backup

import android.content.Context
import android.os.Build
import android.provider.MediaStore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File

data class BackupEntry(
    val id: String = java.util.UUID.randomUUID().toString(),
    val fileName: String,
    val timestamp: Long = System.currentTimeMillis(),
    val fileSizeBytes: Long = 0,
    val totalRows: Long = 0,
    val isManual: Boolean = false,
    val fileUri: String = "",
    val mediaStoreId: Long = 0
) {
    val displayType: String get() = if (isManual) "Manual" else "Auto"
    val displaySize: String get() = formatSize(fileSizeBytes)
    val displayRows: String get() = formatCount(totalRows)

    companion object {
        private fun formatSize(bytes: Long): String = when {
            bytes < 1024 -> "$bytes B"
            bytes < 1024 * 1024 -> "%.1f KB".format(bytes / 1024.0)
            else -> "%.1f MB".format(bytes / (1024.0 * 1024.0))
        }
        private fun formatCount(count: Long): String = when {
            count < 1000 -> "$count"
            count < 1_000_000 -> "${count / 1000}K"
            else -> "%.1fM".format(count / 1_000_000.0)
        }
    }
}

class BackupHistoryStore(private val context: Context) {

    private val prefs = context.getSharedPreferences("backup_history_v2", Context.MODE_PRIVATE)
    private val gson = Gson()

    fun getAll(): List<BackupEntry> {
        val json = prefs.getString("entries", "[]") ?: "[]"
        val type = object : TypeToken<List<BackupEntry>>() {}.type
        return try {
            val list: List<BackupEntry> = gson.fromJson(json, type)
            list.sortedByDescending { it.timestamp }
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun add(entry: BackupEntry) {
        val list = getAll().toMutableList()
        list.add(entry)
        saveAll(list)
    }

    fun remove(id: String): Boolean {
        val list = getAll().toMutableList()
        val removed = list.removeAll { it.id == id }
        if (removed) saveAll(list)
        return removed
    }

    fun size(): Int = getAll().size

    fun clear() {
        prefs.edit().remove("entries").apply()
    }

    fun getRetentionLimit(): Int = prefs.getInt("retention_limit", 0)

    fun setRetentionLimit(limit: Int) {
        prefs.edit().putInt("retention_limit", maxOf(0, limit)).apply()
    }

    fun getRetentionDays(): Int = prefs.getInt("retention_days", 0)

    fun setRetentionDays(days: Int) {
        prefs.edit().putInt("retention_days", maxOf(0, days)).apply()
    }

    fun getBackupFrequency(): String = prefs.getString("backup_frequency", "daily") ?: "daily"

    fun setBackupFrequency(frequency: String) {
        prefs.edit().putString("backup_frequency", frequency).apply()
    }

    fun getBackupHour(): Int = prefs.getInt("backup_hour", 0)

    fun getBackupMinute(): Int = prefs.getInt("backup_minute", 5)

    fun setBackupTime(hour: Int, minute: Int) {
        prefs.edit().putInt("backup_hour", hour.coerceIn(0, 23))
            .putInt("backup_minute", minute.coerceIn(0, 59)).apply()
    }

    fun getExcessEntries(): List<BackupEntry> {
        val limit = getRetentionLimit()
        if (limit <= 0) return emptyList()
        val all = getAll()
        if (all.size <= limit) return emptyList()
        return all.drop(limit)
    }

    fun cleanupByAge() {
        val maxDays = getRetentionDays()
        if (maxDays <= 0) return
        val cutoff = System.currentTimeMillis() - maxDays * 86400000L
        val all = getAll().toMutableList()
        val before = all.size
        all.removeAll { it.timestamp < cutoff }
        if (all.size < before) saveAll(all)
    }

    private fun saveAll(entries: List<BackupEntry>) {
        val json = gson.toJson(entries)
        prefs.edit().putString("entries", json).apply()
    }

    companion object {
        fun deleteFile(context: Context, entry: BackupEntry): Boolean {
            return try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && entry.mediaStoreId > 0) {
                    val uri = android.net.Uri.withAppendedPath(
                        MediaStore.Downloads.EXTERNAL_CONTENT_URI,
                        entry.mediaStoreId.toString()
                    )
                    val rows = context.contentResolver.delete(uri, null, null)
                    rows > 0
                } else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                    val downloadsDir = android.os.Environment.getExternalStoragePublicDirectory(
                        android.os.Environment.DIRECTORY_DOWNLOADS
                    )
                    val file = File(downloadsDir, entry.fileName)
                    if (file.exists()) file.delete() else false
                } else {
                    false
                }
            } catch (e: Exception) {
                try {
                    if (entry.fileUri.isNotEmpty()) {
                        val uri = android.net.Uri.parse(entry.fileUri)
                        context.contentResolver.delete(uri, null, null) > 0
                    } else false
                } catch (e2: Exception) {
                    false
                }
            }
        }
    }
}
