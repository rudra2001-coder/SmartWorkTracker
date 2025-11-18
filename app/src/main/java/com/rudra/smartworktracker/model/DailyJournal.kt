package com.rudra.smartworktracker.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(
    tableName = "daily_journals",
    indices = [Index(value = ["date"], unique = true)]
)
data class DailyJournal(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val date: LocalDate,
    val morningIntention: String = "",
    val eveningReflection: String = "",
    val gratitude: String = ""
)
