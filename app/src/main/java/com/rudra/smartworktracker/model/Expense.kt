package com.rudra.smartworktracker.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "expenses")
data class Expense(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val amount: Double,
    val currency: String,
    val category: ExpenseCategory,
    val merchant: String?,
    val notes: String?,
    val timestamp: Long,
    val imageUri: String? = null
)

enum class ExpenseCategory {
    MEAL,
    OTHER,
    TRANSPORT,
    ENTERTAINMENT,
    BILLS,
    SHOPPING
}
