package com.rudra.smartworktracker.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "income")
data class Income(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val amount: Double
)