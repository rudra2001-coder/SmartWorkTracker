package com.rudra.smartworktracker.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "expenses")
data class Expense(
    @PrimaryKey val id: String,
    val amount: Double,
    val currency: String,
    val category: ExpenseCategory,
    val merchant: String?,
    val notes: String?,
    val timestamp: Long
)

enum class ExpenseCategory {
    MEAL,
    OTHER
}
