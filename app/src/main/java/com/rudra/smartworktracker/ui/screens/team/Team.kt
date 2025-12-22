package com.rudra.smartworktracker.ui.screens.team

data class Team(
    val name: String,
    val teammates: List<Teammate> = emptyList()
)
