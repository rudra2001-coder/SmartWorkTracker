package com.rudra.smartworktracker.ui.screens.team

import java.time.LocalDate

data class Team(
    val id: String = System.currentTimeMillis().toString(),
    val name: String,
    val description: String = "",
    val teammates: List<Teammate> = emptyList(),
    val teamColor: Int = 0xFF6C63FF.toInt(), // Default purple
    val createdDate: Long = System.currentTimeMillis(),
    val dutyCycleDays: Int = 7, // Default weekly cycle
    val nextDutyAssignDate: LocalDate = LocalDate.now()
)
