package com.rudra.smartworktracker.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.rudra.smartworktracker.data.dao.Converters

@Entity(tableName = "bill_splits")
@TypeConverters(Converters::class)
data class BillSplit(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val transactionName: String,
    val totalAmount: Double,
    val participants: List<String> = emptyList(),
    val amounts: List<Double> = emptyList(),
    val isSettled: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
) {
    companion object {
        fun fromLegacy(
            id: Long = 0,
            transactionName: String,
            totalAmount: Double,
            participantsJson: String,
            amountsJson: String,
            isSettled: Boolean = false,
            createdAt: Long = System.currentTimeMillis()
        ): BillSplit {
            val participants = participantsJson.split(",").filter { it.isNotBlank() }
            val amounts = amountsJson.split(",").filter { it.isNotBlank() }.map { it.toDoubleOrNull() ?: 0.0 }
            return BillSplit(id, transactionName, totalAmount, participants, amounts, isSettled, createdAt)
        }
    }
}
