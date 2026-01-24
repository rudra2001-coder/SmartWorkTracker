package com.rudra.smartworktracker.data.entity

/**
 * Represents the synchronization status of an entity with the remote server.
 */
enum class SyncStatus {
    LOCAL_ONLY,
    PENDING_UPLOAD,
    SYNCED,
    CONFLICT
}
