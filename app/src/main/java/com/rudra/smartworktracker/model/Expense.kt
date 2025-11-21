package com.rudra.smartworktracker.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "expenses")
data class Expense(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val amount: Double = 0.0,
    val currency: String = "",
    val category: ExpenseCategory = ExpenseCategory.OTHER,
    val merchant: String? = null,
    val notes: String? = null,
    val timestamp: Long = 0L,
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
