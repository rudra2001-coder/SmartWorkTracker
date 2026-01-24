package com.rudra.smartworktracker.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.rudra.smartworktracker.data.entity.BaseEntity
import com.rudra.smartworktracker.data.entity.SyncStatus

@Entity(tableName = "expenses")
data class Expense(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val amount: Double = 0.0,
    val currency: String = "",
    val category: ExpenseCategory = ExpenseCategory.OTHER,
    val merchant: String? = null,
    val notes: String? = null,
    val timestamp: Long = 0L,
    val imageUri: String? = null,

    // Audit fields - Rule 1.2
    override val createdAt: Long = System.currentTimeMillis(),
    override val updatedAt: Long = System.currentTimeMillis(),
    override val isDeleted: Boolean = false,
    override val syncStatus: SyncStatus = SyncStatus.LOCAL_ONLY
) : BaseEntity

enum class ExpenseCategory {
    MEAL,
    OTHER,
    TRANSPORT,
    ENTERTAINMENT,
    BILLS,
    SHOPPING
}
