package com.rudra.smartworktracker.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.rudra.smartworktracker.data.entity.InAppNotification
import com.rudra.smartworktracker.data.entity.NotificationType
import kotlinx.coroutines.flow.Flow

@Dao
interface InAppNotificationDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(notification: InAppNotification): Long

    @Query("SELECT * FROM in_app_notifications ORDER BY timestamp DESC")
    fun getAllNotifications(): Flow<List<InAppNotification>>

    @Query("SELECT * FROM in_app_notifications WHERE isRead = 0 ORDER BY timestamp DESC")
    fun getUnreadNotifications(): Flow<List<InAppNotification>>

    @Query("SELECT * FROM in_app_notifications WHERE type = :type ORDER BY timestamp DESC")
    fun getNotificationsByType(type: NotificationType): Flow<List<InAppNotification>>

    @Query("SELECT * FROM in_app_notifications WHERE isRead = :isRead AND type = :type ORDER BY timestamp DESC")
    fun getNotificationsByTypeAndReadStatus(isRead: Boolean, type: NotificationType): Flow<List<InAppNotification>>

    @Query("SELECT * FROM in_app_notifications WHERE id = :id")
    suspend fun getNotificationById(id: Long): InAppNotification?

    @Query("UPDATE in_app_notifications SET isRead = 1 WHERE id = :id")
    suspend fun markAsRead(id: Long)

    @Query("UPDATE in_app_notifications SET isRead = 1 WHERE isRead = 0")
    suspend fun markAllAsRead()

    @Query("DELETE FROM in_app_notifications WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM in_app_notifications")
    suspend fun deleteAll()

    @Query("DELETE FROM in_app_notifications WHERE isRead = 1")
    suspend fun deleteRead()

    @Query("SELECT COUNT(*) FROM in_app_notifications WHERE isRead = 0")
    fun getUnreadCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM in_app_notifications")
    fun getTotalCount(): Flow<Int>

    @Query("SELECT * FROM in_app_notifications WHERE isRead = 0 ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentUnread(limit: Int = 10): Flow<List<InAppNotification>>

    @Query("SELECT * FROM in_app_notifications WHERE timestamp BETWEEN :startTime AND :endTime ORDER BY timestamp DESC")
    fun getNotificationsBetween(startTime: Long, endTime: Long): Flow<List<InAppNotification>>
}
