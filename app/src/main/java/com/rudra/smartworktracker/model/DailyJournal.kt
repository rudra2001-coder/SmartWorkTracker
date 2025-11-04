package com.rudra.smartworktracker.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(tableName = "daily_journals")
data class DailyJournal(
    @PrimaryKey val date: LocalDate,
    val morningIntention: String = "",
    val eveningReflection: String = "",
    val gratitude: String = ""
)
