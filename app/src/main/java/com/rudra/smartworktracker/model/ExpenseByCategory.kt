package com.rudra.smartworktracker.model

import androidx.room.Ignore

data class ExpenseByCategory(
    @Ignore val uuid: String? = null,
    val category: ExpenseCategory,
    val total: Double
) {
    // Primary constructor for Room to use
    constructor(category: ExpenseCategory, total: Double) : this(null, category, total)
}
