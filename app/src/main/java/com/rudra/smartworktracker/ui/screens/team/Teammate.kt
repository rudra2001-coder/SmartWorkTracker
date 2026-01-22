package com.rudra.smartworktracker.ui.screens.team

import android.net.Uri
import java.time.LocalDate
import java.time.LocalTime

data class Teammate(
    val id: String = System.currentTimeMillis().toString(),
    val name: String,
    val phoneNumbers: List<String> = emptyList(),
    val email: String? = null,
    val role: String = "Team Member",
    val photoUri: Uri? = null,
    val contactId: String? = null, 
    val dutySchedule: DutySchedule = DutySchedule(),
    val skills: List<String> = emptyList(),
    val notes: String = "",
    val isFavorite: Boolean = false,
    val availability: Availability = Availability(),
    val emergencyContact: String? = null
)

data class DutySchedule(
    val regularDutyDays: List<Int> = emptyList(), 
    val dutyStartTime: LocalTime = LocalTime.of(9, 0),
    val dutyEndTime: LocalTime = LocalTime.of(17, 0),
    val assignedDuties: List<AssignedDuty> = emptyList(),
    val offDays: List<LocalDate> = emptyList(),
    val preferredSwapDays: List<Int> = emptyList()
)

data class AssignedDuty(
    val date: LocalDate,
    val startTime: LocalTime,
    val endTime: LocalTime,
    val dutyType: String = "Regular",
    val notes: String = "",
    val isSwapped: Boolean = false,
    val swappedWith: String? = null,
    val confirmed: Boolean = true
)

data class Availability(
    val preferredShift: String = "Morning",
    val maxHoursPerDay: Int = 8,
    val maxDaysPerWeek: Int = 5,
    val unavailableDates: List<LocalDate> = emptyList()
)

data class DutySwap(
    val id: String = System.currentTimeMillis().toString(),
    val requesterId: String,
    val responderId: String,
    val requestDate: LocalDate,
    val swapDate: LocalDate,
    val reason: String = "",
    val status: SwapStatus = SwapStatus.PENDING,
    val createdAt: Long = System.currentTimeMillis()
)

enum class SwapStatus {
    PENDING, APPROVED, REJECTED, COMPLETED
}

data class Contact(
    val id: String,
    val name: String,
    val phoneNumbers: List<String>,
    val email: String? = null,
    val photoUri: Uri? = null
)
