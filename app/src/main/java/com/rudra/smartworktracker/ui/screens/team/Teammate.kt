package com.rudra.smartworktracker.ui.screens.team

data class Teammate(
    val name: String,
    val phoneNumbers: List<String> = emptyList() // Changed from single number to list
)
