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
    private val _teams = MutableStateFlow(loadAndCleanTeams())
    val teams: StateFlow<List<Team>> = _teams.asStateFlow()

    // For backward compatibility with existing code
    @Deprecated("Use teams StateFlow instead", replaceWith = ReplaceWith("teams"))
    var teamsLegacy = mutableStateOf(loadAndCleanTeams())
        private set

    init {
        // Sync both systems for backward compatibility
        viewModelScope.launch {
            _teams.collect { teamList ->
                teamsLegacy.value = teamList
            }
        }
    }
    
    private fun loadAndCleanTeams(): List<Team> {
        val teamsFromPrefs = sharedPreferenceManager.getTeams() ?: return emptyList()
        return teamsFromPrefs.map { team ->
            val cleanTeammates = team.teammates?.map { teammate ->
                teammate.copy(phoneNumbers = teammate.phoneNumbers ?: emptyList())
            } ?: emptyList()
            team.copy(teammates = cleanTeammates)
        }
    }
    
    private fun updateTeams(newTeams: List<Team>) {
        _teams.value = newTeams
        sharedPreferenceManager.saveTeams(newTeams)
    }

    fun addTeam(team: Team) {
        val currentTeams = _teams.value.toMutableList()
        currentTeams.add(team)
        updateTeams(currentTeams)
    }

    fun addTeammate(teamName: String, teammate: Teammate) {
        val currentTeams = _teams.value.toMutableList()
        val teamIndex = currentTeams.indexOfFirst { it.name == teamName }
        if (teamIndex != -1) {
            val team = currentTeams[teamIndex]
            val updatedTeammates = (team.teammates ?: emptyList()) + teammate
            val updatedTeam = team.copy(teammates = updatedTeammates)
            currentTeams[teamIndex] = updatedTeam
            updateTeams(currentTeams)
        }
    }

    fun removeTeam(teamName: String) {
        val updatedTeams = _teams.value.filterNot { it.name == teamName }
        updateTeams(updatedTeams)
    }

    fun removeTeammate(teamName: String, teammateName: String) {
        val currentTeams = _teams.value.toMutableList()
        val teamIndex = currentTeams.indexOfFirst { it.name == teamName }
        if (teamIndex != -1) {
            val team = currentTeams[teamIndex]
            val updatedTeammates = (team.teammates ?: emptyList()).filterNot { it.name == teammateName }
            val updatedTeam = team.copy(teammates = updatedTeammates)
            currentTeams[teamIndex] = updatedTeam
            updateTeams(currentTeams)
        }
    }
}
