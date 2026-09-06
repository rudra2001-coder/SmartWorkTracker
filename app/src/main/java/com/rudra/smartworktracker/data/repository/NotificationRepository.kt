package com.rudra.smartworktracker.data.repository

import com.rudra.smartworktracker.data.dao.InAppNotificationDao
import com.rudra.smartworktracker.data.entity.InAppNotification
import com.rudra.smartworktracker.data.entity.NotificationType
import kotlinx.coroutines.flow.Flow

class NotificationRepository(
    private val dao: InAppNotificationDao
) {
    suspend fun insert(notification: InAppNotification): Long = dao.insert(notification)

    fun getAllNotifications(): Flow<List<InAppNotification>> = dao.getAllNotifications()

    fun getUnreadNotifications(): Flow<List<InAppNotification>> = dao.getUnreadNotifications()

    fun getNotificationsByType(type: NotificationType): Flow<List<InAppNotification>> =
        dao.getNotificationsByType(type)

    fun getNotificationsByTypeAndReadStatus(isRead: Boolean, type: NotificationType): Flow<List<InAppNotification>> =
        dao.getNotificationsByTypeAndReadStatus(isRead, type)

    suspend fun getNotificationById(id: Long): InAppNotification? = dao.getNotificationById(id)

    suspend fun markAsRead(id: Long) = dao.markAsRead(id)

    suspend fun markAllAsRead() = dao.markAllAsRead()

    suspend fun deleteById(id: Long) = dao.deleteById(id)

    suspend fun deleteAll() = dao.deleteAll()

    suspend fun deleteRead() = dao.deleteRead()

    fun getUnreadCount(): Flow<Int> = dao.getUnreadCount()

    fun getTotalCount(): Flow<Int> = dao.getTotalCount()

    fun getRecentUnread(limit: Int = 10): Flow<List<InAppNotification>> =
        dao.getRecentUnread(limit)

    fun getNotificationsBetween(startTime: Long, endTime: Long): Flow<List<InAppNotification>> =
        dao.getNotificationsBetween(startTime, endTime)
}
