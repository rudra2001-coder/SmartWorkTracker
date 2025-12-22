package com.rudra.smartworktracker.ui.screens.team

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.rudra.smartworktracker.data.SharedPreferenceManager

class TeamViewModel(private val sharedPreferenceManager: SharedPreferenceManager) : ViewModel() {

    var teams = mutableStateOf(sharedPreferenceManager.getTeams())
        private set

    fun addTeam(team: Team) {
        val currentTeams = teams.value.toMutableList()
        currentTeams.add(team)
        teams.value = currentTeams
        sharedPreferenceManager.saveTeams(currentTeams)
    }

    fun addTeammate(teamName: String, teammate: Teammate) {
        val currentTeams = teams.value.toMutableList()
        val team = currentTeams.find { it.name == teamName }
        team?.let {
            val updatedTeammates = it.teammates.toMutableList()
            updatedTeammates.add(teammate)
            val updatedTeam = it.copy(teammates = updatedTeammates)
            val teamIndex = currentTeams.indexOf(it)
            currentTeams[teamIndex] = updatedTeam
            teams.value = currentTeams
            sharedPreferenceManager.saveTeams(currentTeams)
        }
    }
}
