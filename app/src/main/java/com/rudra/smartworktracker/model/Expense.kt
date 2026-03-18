package com.rudra.smartworktracker.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.rudra.smartworktracker.data.entity.BaseEntity
import com.rudra.smartworktracker.data.entity.SyncStatus
import java.util.UUID

@Entity(tableName = "expenses")
data class Expense(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val amount: Double = 0.0,
    val currency: String = "",
    val category: ExpenseCategory = ExpenseCategory.OTHER,
    val merchant: String? = null,
    val notes: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val imageUri: String? = null,
    
    // UUID for future sync preparation - Rule 1.1
    val uuid: String = id,

    // Audit fields - Rule 1.2
    override val createdAt: Long = System.currentTimeMillis(),
    override val updatedAt: Long = System.currentTimeMillis(),
    override val isDeleted: Boolean = false,
    override val syncStatus: SyncStatus = SyncStatus.LOCAL_ONLY
) : BaseEntity

enum class ExpenseCategory(val displayName: String) {
    MEAL("Food & Dining"),
    TRANSPORT("Transportation"),
    SHOPPING("Shopping"),
    ENTERTAINMENT("Entertainment"),
    BILLS("Bills & Utilities"),
    HEALTHCARE("Healthcare"),
    EDUCATION("Education"),
    PERSONAL_CARE("Personal Care"),
    GIFTS("Gifts"),
    TRAVEL("Travel"),
    SUBSCRIPTIONS("Subscriptions"),
    OTHER("Other")
}
