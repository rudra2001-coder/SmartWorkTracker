package com.rudra.smartworktracker.ui.screens.team

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rudra.smartworktracker.data.SharedPreferenceManager
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime
import java.time.DayOfWeek

class TeamViewModel(private val sharedPreferenceManager: SharedPreferenceManager) : ViewModel() {

    private val _teams = MutableStateFlow(loadAndCleanTeams())
    val teams: StateFlow<List<Team>> = _teams.asStateFlow()

    private val _dutyCalendar = MutableStateFlow<Map<LocalDate, List<AssignedDuty>>>(emptyMap())
    val dutyCalendar: StateFlow<Map<LocalDate, List<AssignedDuty>>> = _dutyCalendar.asStateFlow()

    private val _pendingSwaps = MutableStateFlow<List<DutySwap>>(emptyList())
    val pendingSwaps: StateFlow<List<DutySwap>> = _pendingSwaps.asStateFlow()

    private val _uiEvent = MutableSharedFlow<String>()
    val uiEvent = _uiEvent.asSharedFlow()

    init {
        updateDutyCalendar()
        
        viewModelScope.launch {
            _teams.collect { 
                updateDutyCalendar() 
            }
        }
    }

    private fun updateTeams(newTeams: List<Team>) {
        _teams.value = newTeams
        sharedPreferenceManager.saveTeams(newTeams)
        updateDutyCalendar()
    }

    private fun updateDutyCalendar() {
        val calendarMap = mutableMapOf<LocalDate, MutableList<AssignedDuty>>()
        
        _teams.value.forEach { team ->
            team.teammates.forEach { teammate ->
                // Add explicit assigned duties
                teammate.dutySchedule.assignedDuties.forEach { duty ->
                    val list = calendarMap.getOrPut(duty.date) { mutableListOf() }
                    list.add(duty.copy()) 
                }
                
                // Add regular schedule duties for the next 30 days if not overridden
                val today = LocalDate.now()
                for (i in 0..30) {
                    val date = today.plusDays(i.toLong())
                    val dayValue = date.dayOfWeek.value 
                    
                    if (teammate.dutySchedule.regularDutyDays.contains(dayValue)) {
                        val hasExplicit = teammate.dutySchedule.assignedDuties.any { it.date == date }
                        val isHoliday = teammate.dutySchedule.offDays.contains(date)
                        
                        if (!hasExplicit && !isHoliday) {
                            val list = calendarMap.getOrPut(date) { mutableListOf() }
                            list.add(AssignedDuty(
                                date = date,
                                startTime = teammate.dutySchedule.dutyStartTime,
                                endTime = teammate.dutySchedule.dutyEndTime,
                                dutyType = "Regular (Auto)",
                                notes = "Weekly Schedule"
                            ))
                        }
                    }
                }
            }
        }
        
        _dutyCalendar.value = calendarMap
    }

    fun addTeam(team: Team) {
        val currentTeams = _teams.value.toMutableList()
        currentTeams.add(team)
        updateTeams(currentTeams)
    }

    fun updateTeammate(teamName: String, teammateId: String, update: (Teammate) -> Teammate) {
        val currentTeams = _teams.value.toMutableList()
        val teamIndex = currentTeams.indexOfFirst { it.name == teamName }
        if (teamIndex != -1) {
            val team = currentTeams[teamIndex]
            val updatedTeammates = team.teammates.map { teammate ->
                if (teammate.id == teammateId) update(teammate) else teammate
            }
            val updatedTeam = team.copy(teammates = updatedTeammates)
            currentTeams[teamIndex] = updatedTeam
            updateTeams(currentTeams)
        }
    }

    fun setWeeklySchedule(teamName: String, teammateId: String, days: List<Int>, start: LocalTime, end: LocalTime) {
        updateTeammate(teamName, teammateId) { teammate ->
            teammate.copy(
                dutySchedule = teammate.dutySchedule.copy(
                    regularDutyDays = days,
                    dutyStartTime = start,
                    dutyEndTime = end
                )
            )
        }
    }

    fun assignDuty(teamName: String, teammateId: String, date: LocalDate, shift: DutyShift) {
        updateTeammate(teamName, teammateId) { teammate ->
            val newDuty = AssignedDuty(
                date = date,
                startTime = shift.startTime,
                endTime = shift.endTime,
                dutyType = shift.type,
                notes = shift.notes
            )
            val filteredDuties = teammate.dutySchedule.assignedDuties.filterNot { it.date == date }
            teammate.copy(dutySchedule = teammate.dutySchedule.copy(assignedDuties = filteredDuties + newDuty))
        }
    }

    fun toggleHoliday(teamName: String, teammateId: String, date: LocalDate) {
        updateTeammate(teamName, teammateId) { teammate ->
            val currentOffDays = teammate.dutySchedule.offDays.toMutableList()
            if (currentOffDays.contains(date)) {
                currentOffDays.remove(date)
            } else {
                currentOffDays.add(date)
            }
            val updatedDuties = teammate.dutySchedule.assignedDuties.filterNot { it.date == date }
            teammate.copy(dutySchedule = teammate.dutySchedule.copy(offDays = currentOffDays, assignedDuties = updatedDuties))
        }
    }

    fun removeDuty(duty: AssignedDuty) {
        val currentTeams = _teams.value.toMutableList()
        currentTeams.forEachIndexed { teamIndex, team ->
            val updatedTeammates = team.teammates.map { teammate ->
                if (teammate.dutySchedule.assignedDuties.contains(duty)) {
                    teammate.copy(dutySchedule = teammate.dutySchedule.copy(assignedDuties = teammate.dutySchedule.assignedDuties - duty))
                } else teammate
            }
            currentTeams[teamIndex] = team.copy(teammates = updatedTeammates)
        }
        updateTeams(currentTeams)
    }

    fun getTeammateDutyStats(teammateId: String): DutyStats {
        val teammate = _teams.value.flatMap { it.teammates }.find { it.id == teammateId } ?: return DutyStats(0, 0, 0, 0.0)
        
        val manualDuties = teammate.dutySchedule.assignedDuties
        val totalDutiesCount = manualDuties.size
        
        var totalOvertimeHours = 0.0
        manualDuties.forEach { duty ->
            val hours = calculateDuration(duty.startTime, duty.endTime)
            if (hours > 8.0) totalOvertimeHours += (hours - 8.0)
        }
        
        return DutyStats(
            totalDuties = totalDutiesCount,
            upcomingDuties = manualDuties.count { it.date.isAfter(LocalDate.now().minusDays(1)) },
            completedSwaps = 0,
            overtimeHours = totalOvertimeHours
        )
    }

    private fun calculateDuration(start: LocalTime, end: LocalTime): Double {
        val duration = if (end.isAfter(start)) {
            Duration.between(start, end)
        } else {
            Duration.ofHours(24).minus(Duration.between(end, start))
        }
        return duration.toMinutes() / 60.0
    }

    private fun loadAndCleanTeams(): List<Team> {
        return sharedPreferenceManager.getTeams() ?: emptyList()
    }

    fun addTeammate(teamName: String, teammate: Teammate) {
        val currentTeams = _teams.value.toMutableList()
        val teamIndex = currentTeams.indexOfFirst { it.name == teamName }
        if (teamIndex != -1) {
            val team = currentTeams[teamIndex]
            currentTeams[teamIndex] = team.copy(teammates = team.teammates + teammate)
            updateTeams(currentTeams)
        }
    }

    fun initiateDutySwap(requesterId: String, responderId: String, dutyDate: LocalDate) {
        val swap = DutySwap(
            requesterId = requesterId,
            responderId = responderId,
            requestDate = dutyDate,
            swapDate = LocalDate.now().plusDays(1),
            reason = "Automatic Swap Request"
        )
        val currentSwaps = _pendingSwaps.value.toMutableList()
        currentSwaps.add(swap)
        _pendingSwaps.value = currentSwaps
        
        viewModelScope.launch {
            _uiEvent.emit("Swap Request Sent Successfully")
        }
    }

    fun approveDutySwap(swap: DutySwap) {
        val currentSwaps = _pendingSwaps.value.toMutableList()
        val index = currentSwaps.indexOfFirst { it.id == swap.id }
        if (index != -1) {
            currentSwaps[index] = swap.copy(status = SwapStatus.APPROVED)
            _pendingSwaps.value = currentSwaps
            
            // Actually perform the swap in duties if needed
            // This would involve finding the duties on those dates and swapping owners
            
            viewModelScope.launch {
                _uiEvent.emit("Swap Request Approved")
            }
        }
    }

    fun rejectDutySwap(swap: DutySwap) {
        val currentSwaps = _pendingSwaps.value.toMutableList()
        val index = currentSwaps.indexOfFirst { it.id == swap.id }
        if (index != -1) {
            currentSwaps[index] = swap.copy(status = SwapStatus.REJECTED)
            _pendingSwaps.value = currentSwaps
            
            viewModelScope.launch {
                _uiEvent.emit("Swap Request Rejected")
            }
        }
    }

    fun autoScheduleDuties(id: String, now: LocalDate, days: Int) {}
}

data class DutyShift(val startTime: LocalTime, val endTime: LocalTime, val type: String = "Regular", val notes: String = "")
data class DutyStats(val totalDuties: Int, val upcomingDuties: Int, val completedSwaps: Int, val overtimeHours: Double = 0.0)
