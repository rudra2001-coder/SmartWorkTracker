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

enum class ExpenseCategory(val displayName: String) {
    MEAL("Food & Dining"),
    OTHER("Other"),
    TRANSPORT("Transport"),
    ENTERTAINMENT("Entertainment"),
    BILLS("Bills & Utilities"),
    SHOPPING("Shopping");

    val color: androidx.compose.ui.graphics.Color
        get() = when (this) {
            MEAL -> androidx.compose.ui.graphics.Color(0xFFFF9800)
            TRANSPORT -> androidx.compose.ui.graphics.Color(0xFF2196F3)
            SHOPPING -> androidx.compose.ui.graphics.Color(0xFF9C27B0)
            ENTERTAINMENT -> androidx.compose.ui.graphics.Color(0xFFE91E63)
            BILLS -> androidx.compose.ui.graphics.Color(0xFF4CAF50)
            OTHER -> androidx.compose.ui.graphics.Color(0xFF9E9E9E)
        }
}
