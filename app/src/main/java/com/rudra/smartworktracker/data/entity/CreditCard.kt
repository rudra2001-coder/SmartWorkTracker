package com.rudra.smartworktracker.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "credit_cards")
data class CreditCard(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val cardName: String,
    val cardNumber: String, // Last 4 digits
    val cardLimit: Double,
    var currentBalance: Double = 0.0,
    val statementDate: Int, // Day of month
    val dueDate: Int // Day of month
)
