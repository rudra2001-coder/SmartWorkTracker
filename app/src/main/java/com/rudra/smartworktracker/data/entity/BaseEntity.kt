package com.rudra.smartworktracker.data.entity

/**
 * Base interface for all entities to ensure consistency in audit and sync fields.
 * Rule 1.2: Every table MUST contain these columns.
 */
interface BaseEntity {
    val createdAt: Long
    val updatedAt: Long
    val isDeleted: Boolean
    val syncStatus: SyncStatus
}
