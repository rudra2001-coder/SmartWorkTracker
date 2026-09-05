package com.rudra.smartworktracker.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "execution_history",
    indices = [
        Index(value = ["timestamp"]),
        Index(value = ["ruleId"])
    ]
)
data class ExecutionHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val ruleId: Long? = null,
    val ruleName: String,
    val transactionType: String,
    val amount: Double,
    val success: Boolean,
    val failureReason: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)
