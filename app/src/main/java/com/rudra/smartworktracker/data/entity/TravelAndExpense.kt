
package com.rudra.smartworktracker.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "travel_expenses")
data class TravelAndExpense(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val dailyTravelCost: Double = 0.0,
    val otherExpenses: Double = 0.0,
    val otherExpenseDescription: String = "",
    val lastUpdated: Long = System.currentTimeMillis()
) {
    // Helper function to get formatted last updated date
    fun getFormattedLastUpdated(): String {
        return try {
            val date = Date(lastUpdated)
            val formatter = java.text.SimpleDateFormat("dd MMM yyyy HH:mm", java.util.Locale.getDefault())
            formatter.format(date)
        } catch (e: Exception) {
            "Unknown"
        }
    }

    // Check if any expense data exists
    fun hasData(): Boolean {
        return dailyTravelCost > 0 || otherExpenses > 0
    }
}