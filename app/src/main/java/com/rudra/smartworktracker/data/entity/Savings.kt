package com.rudra.smartworktracker.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "savings")
data class Savings(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val amount: Double,
    val timestamp: Long
)
