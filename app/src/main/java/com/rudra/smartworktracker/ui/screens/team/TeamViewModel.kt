package com.rudra.smartworktracker.ui.screens.team

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rudra.smartworktracker.data.SharedPreferenceManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TeamViewModel(private val sharedPreferenceManager: SharedPreferenceManager) : ViewModel() {

    // Use StateFlow for better Compose integration
    private val _teams = MutableStateFlow(sharedPreferenceManager.getTeams())
    val teams: StateFlow<List<Team>> = _teams.asStateFlow()

    // For backward compatibility with existing code
    @Deprecated("Use teams StateFlow instead", replaceWith = ReplaceWith("teams"))
    var teamsLegacy = mutableStateOf(sharedPreferenceManager.getTeams())
        private set

    init {
        // Sync both systems for backward compatibility
        viewModelScope.launch {
            _teams.collect { teamList ->
                teamsLegacy.value = teamList
            }
        }
    }

    fun addTeam(team: Team) {
        val currentTeams = _teams.value.toMutableList()
        currentTeams.add(team)
        _teams.value = currentTeams
        sharedPreferenceManager.saveTeams(currentTeams)
    }

    fun addTeammate(teamName: String, teammate: Teammate) {
        val currentTeams = _teams.value.toMutableList()
        val team = currentTeams.find { it.name == teamName }
        team?.let {
            val updatedTeammates = it.teammates.toMutableList()
            updatedTeammates.add(teammate)
            val updatedTeam = it.copy(teammates = updatedTeammates)
            val teamIndex = currentTeams.indexOf(it)
            currentTeams[teamIndex] = updatedTeam
            _teams.value = currentTeams
            sharedPreferenceManager.saveTeams(currentTeams)
        }
    }

    // New function to remove a team
    fun removeTeam(teamName: String) {
        val currentTeams = _teams.value.toMutableList()
        currentTeams.removeIf { it.name == teamName }
        _teams.value = currentTeams
        sharedPreferenceManager.saveTeams(currentTeams)
    }

    // New function to remove a teammate
    fun removeTeammate(teamName: String, teammateName: String) {
        val currentTeams = _teams.value.toMutableList()
        val team = currentTeams.find { it.name == teamName }
        team?.let {
            val updatedTeammates = it.teammates.toMutableList()
                .filterNot { teammate -> teammate.name == teammateName }
            val updatedTeam = it.copy(teammates = updatedTeammates)
            val teamIndex = currentTeams.indexOf(it)
            currentTeams[teamIndex] = updatedTeam
            _teams.value = currentTeams
            sharedPreferenceManager.saveTeams(currentTeams)
        }
    }

    // Search functionality could also be handled here if needed
    // Alternatively, you can filter in the UI as shown in the TeamScreen
}