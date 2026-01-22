package com.rudra.smartworktracker.ui.screens.team

import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.ContactsContract
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.platform.*
import androidx.compose.ui.text.font.*
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.*
import androidx.compose.ui.unit.*
import androidx.compose.ui.window.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rudra.smartworktracker.data.SharedPreferenceManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.*
import java.time.format.*
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeamScreen() {
    val context = LocalContext.current
    val sharedPreferenceManager = remember { SharedPreferenceManager(context) }
    val teamViewModel: TeamViewModel = viewModel(
        factory = TeamViewModelFactory(sharedPreferenceManager)
    )

    val teams by teamViewModel.teams.collectAsState()
    val dutyCalendar by teamViewModel.dutyCalendar.collectAsState()
    val pendingSwaps by teamViewModel.pendingSwaps.collectAsState()
    
    var searchQuery by remember { mutableStateOf("") }
    var selectedTab by remember { mutableIntStateOf(0) }
    var showAddTeamDialog by remember { mutableStateOf(false) }
    var showAddTeammateDialog by remember { mutableStateOf(false) }
    var showDutyCalendar by remember { mutableStateOf(false) }
    var showContactPicker by remember { mutableStateOf(false) }
    var selectedTeam by remember { mutableStateOf<Team?>(null) }
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }

    val tabs = listOf("Teams", "Calendar", "Overview", "Swaps")
    
    val backgroundGradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF667EEA).copy(alpha = 0.1f),
            Color(0xFF764BA2).copy(alpha = 0.05f),
            MaterialTheme.colorScheme.surface
        )
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Team Manager", fontWeight = FontWeight.Bold, fontSize = 24.sp)
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(8.dp),
                    titleContentColor = MaterialTheme.colorScheme.primary
                ),
                actions = {
                    IconButton(onClick = { showContactPicker = true }) {
                        Icon(Icons.Default.Contacts, contentDescription = "Import Contacts", tint = MaterialTheme.colorScheme.primary)
                    }
                    IconButton(onClick = { 
                        selectedTeam = teams.firstOrNull()
                        showDutyCalendar = true 
                    }) {
                        BadgedBox(
                            badge = { if (pendingSwaps.isNotEmpty()) Badge { Text(pendingSwaps.size.toString()) } }
                        ) {
                            Icon(Icons.Default.CalendarToday, contentDescription = "Duty Calendar", tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            AnimatedVisibility(
                visible = selectedTab == 0,
                enter = fadeIn() + scaleIn(),
                exit = fadeOut() + scaleOut()
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp), horizontalAlignment = Alignment.End) {
                    FloatingActionButton(
                        onClick = { showAddTeammateDialog = true },
                        containerColor = MaterialTheme.colorScheme.secondary,
                        shape = CircleShape,
                        modifier = Modifier.size(56.dp)
                    ) {
                        Icon(Icons.Default.PersonAdd, contentDescription = "Add Teammate")
                    }
                    
                    FloatingActionButton(
                        onClick = { showAddTeamDialog = true },
                        containerColor = MaterialTheme.colorScheme.primary,
                        shape = CircleShape,
                        modifier = Modifier.size(64.dp)
                    ) {
                        Icon(Icons.Default.GroupAdd, contentDescription = "Add Team", modifier = Modifier.size(28.dp))
                    }
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().background(backgroundGradient).padding(paddingValues)) {
            Column(modifier = Modifier.fillMaxSize()) {
                SearchBar(query = searchQuery, onQueryChange = { searchQuery = it }, modifier = Modifier.fillMaxWidth().padding(16.dp))

                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color.Transparent,
                    contentColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = { Text(text = title, style = MaterialTheme.typography.labelLarge.copy(fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Medium)) },
                            icon = {
                                val icon = when (title) {
                                    "Teams" -> Icons.Default.Group
                                    "Calendar" -> Icons.Default.CalendarToday
                                    "Overview" -> Icons.Default.Dashboard
                                    "Swaps" -> Icons.Default.SwapHoriz
                                    else -> Icons.Default.Group
                                }
                                Icon(icon, contentDescription = title, modifier = Modifier.size(20.dp))
                            }
                        )
                    }
                }

                when (selectedTab) {
                    0 -> TeamsTab(
                        teams = teams,
                        searchQuery = searchQuery,
                        onTeamSelected = { team -> selectedTeam = team },
                        onAddTeammate = { team -> selectedTeam = team; showAddTeammateDialog = true },
                        onManageDuty = { team -> selectedTeam = team; showDutyCalendar = true },
                        onCallTeammate = { phoneNumber ->
                            val intent = Intent(Intent.ACTION_DIAL).apply { data = Uri.parse("tel:$phoneNumber") }
                            context.startActivity(intent)
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                    
                    1 -> CalendarTab(
                        dutyCalendar = dutyCalendar,
                        selectedDate = selectedDate,
                        onDateSelected = { date -> selectedDate = date },
                        onDutyClick = { /* Show details */ },
                        onSwapRequest = { duty -> teamViewModel.initiateDutySwap(duty) },
                        modifier = Modifier.fillMaxSize()
                    )
                    
                    2 -> TeamOverviewTab(
                        teams = teams,
                        searchQuery = searchQuery,
                        modifier = Modifier.fillMaxSize()
                    )

                    3 -> SwapsTab(
                        pendingSwaps = pendingSwaps,
                        onApproveSwap = { swap -> teamViewModel.approveDutySwap(swap) },
                        onRejectSwap = { swap -> teamViewModel.rejectDutySwap(swap) },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }

        if (showAddTeamDialog) {
            AddTeamDialog(onDismiss = { showAddTeamDialog = false }, onConfirm = { team -> teamViewModel.addTeam(team); showAddTeamDialog = false })
        }

        if (showAddTeammateDialog) {
            AddTeammateDialog(
                teams = teams,
                selectedTeam = selectedTeam,
                onDismiss = { showAddTeammateDialog = false; selectedTeam = null },
                onAddFromContacts = { showContactPicker = true },
                onConfirm = { teamName, teammate -> teamViewModel.addTeammate(teamName, teammate); showAddTeammateDialog = false; selectedTeam = null }
            )
        }

        if (showDutyCalendar && selectedTeam != null) {
            DutyCalendarDialog(
                team = selectedTeam!!,
                onDismiss = { showDutyCalendar = false; selectedTeam = null },
                onAssignDuty = { teammate, date, shift -> teamViewModel.assignDuty(selectedTeam!!.name, teammate.id, date, shift) },
                onRemoveDuty = { duty -> teamViewModel.removeDuty(duty) },
                onToggleHoliday = { teammate, date -> teamViewModel.toggleHoliday(selectedTeam!!.name, teammate.id, date) },
                onSetWeeklySchedule = { teammate, days, start, end -> teamViewModel.setWeeklySchedule(selectedTeam!!.name, teammate.id, days, start, end) },
                getTeammateStats = { teammateId -> teamViewModel.getTeammateDutyStats(teammateId) }
            )
        }

        if (showContactPicker) {
            ContactPickerDialog(
                onDismiss = { showContactPicker = false },
                onContactSelected = { contact -> showContactPicker = false }
            )
        }
    }
}

@Composable
fun TeamOverviewTab(teams: List<Team>, searchQuery: String, modifier: Modifier = Modifier) {
    val allTeammates = teams.flatMap { it.teammates }.filter { it.name.contains(searchQuery, true) }
    
    LazyColumn(modifier = modifier, contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item {
            Text("Team Duty Overview", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Text("Weekly regular working days and times for all members", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        }
        
        if (allTeammates.isEmpty()) {
            item { EmptyState(icon = Icons.Default.People, title = "No members found", description = "Add members to see their schedule") }
        } else {
            items(allTeammates) { teammate ->
                TeammateOverviewCard(teammate)
            }
        }
    }
}

@Composable
fun TeammateOverviewCard(teammate: Teammate) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer), contentAlignment = Alignment.Center) {
                    Text(teammate.name.take(1).uppercase(), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(teammate.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(teammate.role, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                }
            }
            
            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color.LightGray.copy(alpha = 0.5f))
            
            Text("Weekly Schedule", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            
            val days = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                days.forEachIndexed { index, day ->
                    val isWorking = teammate.dutySchedule.regularDutyDays.contains(index + 1)
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isWorking) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(day.take(1), color = if (isWorking) Color.White else Color.Gray, style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
            
            if (teammate.dutySchedule.regularDutyDays.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Schedule, null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = "${teammate.dutySchedule.dutyStartTime.format(DateTimeFormatter.ofPattern("hh:mm a"))} - ${teammate.dutySchedule.dutyEndTime.format(DateTimeFormatter.ofPattern("hh:mm a"))}",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium
                    )
                }
            } else {
                Text("No regular schedule set", style = MaterialTheme.typography.bodySmall, color = Color.Red.copy(alpha = 0.7f), modifier = Modifier.padding(top = 4.dp))
            }
        }
    }
}

@Composable
fun SearchBar(query: String, onQueryChange: (String) -> Unit, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(4.dp)),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Search, contentDescription = "Search", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(12.dp))
            TextField(
                value = query,
                onValueChange = onQueryChange,
                placeholder = { Text("Search teams, members, duties...") },
                singleLine = true,
                modifier = Modifier.weight(1f),
                colors = TextFieldDefaults.colors(focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent, disabledContainerColor = Color.Transparent, focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent),
                trailingIcon = { if (query.isNotEmpty()) IconButton(onClick = { onQueryChange("") }) { Icon(Icons.Default.Clear, contentDescription = "Clear") } }
            )
        }
    }
}

@Composable
fun TeamsTab(teams: List<Team>, searchQuery: String, onTeamSelected: (Team) -> Unit, onAddTeammate: (Team) -> Unit, onManageDuty: (Team) -> Unit, onCallTeammate: (String) -> Unit, modifier: Modifier = Modifier) {
    val filteredTeams = if (searchQuery.isEmpty()) teams else teams.filter { it.name.contains(searchQuery, true) || it.teammates.any { t -> t.name.contains(searchQuery, true) } }
    LazyColumn(modifier = modifier, verticalArrangement = Arrangement.spacedBy(16.dp), contentPadding = PaddingValues(16.dp)) {
        if (filteredTeams.isEmpty()) {
            item { EmptyState(icon = Icons.Default.Group, title = "No teams found", description = "Try a different search or create a team") }
        } else {
            items(filteredTeams, key = { it.id }) { team ->
                TeamCard(team = team, onTeamSelected = { onTeamSelected(team) }, onAddTeammate = { onAddTeammate(team) }, onManageDuty = { onManageDuty(team) }, onCallTeammate = onCallTeammate)
            }
        }
    }
}

@Composable
fun TeamCard(team: Team, onTeamSelected: () -> Unit, onAddTeammate: () -> Unit, onManageDuty: () -> Unit, onCallTeammate: (String) -> Unit, modifier: Modifier = Modifier) {
    var expanded by remember { mutableStateOf(false) }
    Card(
        modifier = modifier.clickable { onTeamSelected() }.animateContentSize(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(4.dp)),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(20.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Box(modifier = Modifier.size(16.dp).clip(CircleShape).background(Color(team.teamColor)))
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(text = team.name, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), maxLines = 1)
                        if (team.description.isNotEmpty()) Text(text = team.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                IconButton(onClick = { expanded = !expanded }) { Icon(if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore, contentDescription = null) }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                TeamStatItem(icon = Icons.Default.People, value = "${team.teammates.size}", label = "Members", color = Color(0xFF4CAF50))
                TeamStatItem(icon = Icons.Default.Schedule, value = "${team.dutyCycleDays}", label = "Cycle", color = Color(0xFF2196F3))
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                ActionButton(text = "Add Member", icon = Icons.Default.PersonAdd, onClick = onAddTeammate, color = MaterialTheme.colorScheme.secondary, modifier = Modifier.weight(1f))
                ActionButton(text = "Manage Duty", icon = Icons.Default.CalendarToday, onClick = onManageDuty, color = MaterialTheme.colorScheme.primary, modifier = Modifier.weight(1f))
            }
            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.fillMaxWidth().padding(top = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                    Text("Team Members", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    team.teammates.forEach { teammate ->
                        TeammateRow(teammate = teammate, onCall = { teammate.phoneNumbers.firstOrNull()?.let(onCallTeammate) })
                    }
                }
            }
        }
    }
}

@Composable
fun TeammateRow(teammate: Teammate, onCall: () -> Unit) {
    var isExpanded by remember { mutableStateOf(false) }
    Card(
        modifier = Modifier.clickable { isExpanded = !isExpanded },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.animateContentSize()) {
            Row(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)), contentAlignment = Alignment.Center) {
                    Text(teammate.name.take(1).uppercase(), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = teammate.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                    Text(text = teammate.role, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                IconButton(onClick = onCall, enabled = teammate.phoneNumbers.isNotEmpty()) {
                    Icon(Icons.Default.Call, contentDescription = "Call", tint = if (teammate.phoneNumbers.isNotEmpty()) MaterialTheme.colorScheme.primary else Color.Gray)
                }
            }
            if (isExpanded) {
                Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp)) {
                    if (teammate.phoneNumbers.size > 1) {
                        Text("Other Numbers:", style = MaterialTheme.typography.labelSmall)
                        teammate.phoneNumbers.drop(1).forEach { phone ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(phone, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                                IconButton(onClick = { /* Call this one */ }, modifier = Modifier.size(24.dp)) {
                                    Icon(Icons.Default.Call, null, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                    if (teammate.emergencyContact != null) {
                        Text("Emergency: ${teammate.emergencyContact}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.error)
                    }
                    if (teammate.notes.isNotEmpty()) {
                        Text("Notes: ${teammate.notes}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    }
                }
            }
        }
    }
}

@Composable
fun CalendarTab(dutyCalendar: Map<LocalDate, List<AssignedDuty>>, selectedDate: LocalDate, onDateSelected: (LocalDate) -> Unit, onDutyClick: (AssignedDuty) -> Unit, onSwapRequest: (AssignedDuty) -> Unit, modifier: Modifier = Modifier) {
    val weekDates = remember(selectedDate) {
        val startOfWeek = selectedDate.with(DayOfWeek.MONDAY)
        (0..6).map { startOfWeek.plusDays(it.toLong()) }
    }
    Column(modifier = modifier) {
        WeekSelector(weekDates = weekDates, selectedDate = selectedDate, onDateSelected = onDateSelected, modifier = Modifier.fillMaxWidth().padding(16.dp))
        val dutiesForDate = dutyCalendar[selectedDate] ?: emptyList()
        LazyColumn(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(12.dp), contentPadding = PaddingValues(16.dp)) {
            if (dutiesForDate.isEmpty()) {
                item { EmptyState(icon = Icons.Default.EventAvailable, title = "No duties scheduled", description = "Assign duties for this date") }
            } else {
                items(dutiesForDate) { duty -> DutyCard(duty = duty, onClick = { onDutyClick(duty) }, onSwapRequest = { onSwapRequest(duty) }) }
            }
        }
    }
}

@Composable
fun WeekSelector(weekDates: List<LocalDate>, selectedDate: LocalDate, onDateSelected: (LocalDate) -> Unit, modifier: Modifier = Modifier) {
    Card(modifier = modifier, shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(4.dp))) {
        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
            weekDates.forEach { date ->
                val isSelected = date == selectedDate
                Box(
                    modifier = Modifier.size(56.dp).clip(RoundedCornerShape(16.dp)).clickable { onDateSelected(date) }
                        .background(if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else Color.Transparent)
                        .border(if (isSelected) 2.dp else 1.dp, if (isSelected) MaterialTheme.colorScheme.primary else Color.LightGray, RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = date.dayOfWeek.getDisplayName(java.time.format.TextStyle.SHORT, Locale.getDefault()), style = MaterialTheme.typography.labelSmall)
                        Text(text = date.dayOfMonth.toString(), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun DutyCard(duty: AssignedDuty, onClick: () -> Unit, onSwapRequest: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = if (duty.isSwapped) Color(0xFFFFF3E0) else MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = duty.dutyType, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Text(text = "${duty.startTime.format(DateTimeFormatter.ofPattern("hh:mm a"))} - ${duty.endTime.format(DateTimeFormatter.ofPattern("hh:mm a"))}", style = MaterialTheme.typography.bodyMedium)
                if (duty.notes.isNotEmpty()) Text(text = duty.notes, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
            if (!duty.isSwapped) IconButton(onClick = onSwapRequest) { Icon(Icons.Default.SwapHoriz, contentDescription = "Swap", tint = MaterialTheme.colorScheme.secondary) }
        }
    }
}

@Composable
fun SwapsTab(pendingSwaps: List<DutySwap>, onApproveSwap: (DutySwap) -> Unit, onRejectSwap: (DutySwap) -> Unit, modifier: Modifier = Modifier) {
    LazyColumn(modifier = modifier, verticalArrangement = Arrangement.spacedBy(12.dp), contentPadding = PaddingValues(16.dp)) {
        if (pendingSwaps.isEmpty()) {
            item { EmptyState(icon = Icons.Default.SwapHoriz, title = "No pending swaps", description = "All requests processed") }
        } else {
            items(pendingSwaps) { swap -> SwapRequestCard(swap = swap, onApprove = { onApproveSwap(swap) }, onReject = { onRejectSwap(swap) }) }
        }
    }
}

@Composable
fun SwapRequestCard(swap: DutySwap, onApprove: () -> Unit, onReject: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0))) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "Swap Request", fontWeight = FontWeight.Bold)
            Text(text = "${swap.requestDate} ↔ ${swap.swapDate}")
            if (swap.status == SwapStatus.PENDING) {
                Row(modifier = Modifier.fillMaxWidth().padding(top = 12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(onClick = onReject, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = Color.Red)) { Text("Reject") }
                    Button(onClick = onApprove, modifier = Modifier.weight(1f)) { Text("Approve") }
                }
            }
        }
    }
}

@Composable
fun TeamStatItem(icon: androidx.compose.ui.graphics.vector.ImageVector, value: String, label: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = color)
        }
        Text(label, style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
fun ActionButton(text: String, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit, color: Color, modifier: Modifier = Modifier) {
    Button(onClick = onClick, modifier = modifier.height(48.dp), colors = ButtonDefaults.buttonColors(containerColor = color), shape = RoundedCornerShape(12.dp)) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(text, fontSize = 12.sp)
    }
}

@Composable
fun EmptyState(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, description: String) {
    Column(modifier = Modifier.fillMaxWidth().padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(48.dp), tint = Color.Gray)
        Text(title, fontWeight = FontWeight.Bold)
        Text(description, textAlign = TextAlign.Center, color = Color.Gray)
    }
}

@Composable
fun AddTeamDialog(onDismiss: () -> Unit, onConfirm: (Team) -> Unit) {
    var name by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    Dialog(onDismissRequest = onDismiss) {
        Card(shape = RoundedCornerShape(24.dp)) {
            Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("Add New Team", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Team Name") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = desc, onValueChange = { desc = it }, label = { Text("Description") }, modifier = Modifier.fillMaxWidth())
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Button(onClick = { onConfirm(Team(name = name, description = desc)) }, enabled = name.isNotBlank()) { Text("Add") }
                }
            }
        }
    }
}

@Composable
fun AddTeammateDialog(teams: List<Team>, selectedTeam: Team?, onDismiss: () -> Unit, onAddFromContacts: () -> Unit, onConfirm: (String, Teammate) -> Unit) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var role by remember { mutableStateOf("Team Member") }
    var selectedTeamName by remember { mutableStateOf(selectedTeam?.name ?: teams.firstOrNull()?.name ?: "") }
    Dialog(onDismissRequest = onDismiss) {
        Card(shape = RoundedCornerShape(24.dp)) {
            Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Add Team Member", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                if (selectedTeam == null) {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(teams) { team ->
                            FilterChip(selected = selectedTeamName == team.name, onClick = { selectedTeamName = team.name }, label = { Text(team.name) })
                        }
                    }
                }
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Phone") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone), modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = role, onValueChange = { role = it }, label = { Text("Role") }, modifier = Modifier.fillMaxWidth())
                Button(onClick = onAddFromContacts, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)) {
                    Icon(Icons.Default.Contacts, contentDescription = null); Spacer(Modifier.width(8.dp)); Text("Contacts")
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Button(onClick = { onConfirm(selectedTeamName, Teammate(name = name, phoneNumbers = listOf(phone), role = role)) }, enabled = name.isNotBlank() && selectedTeamName.isNotBlank()) { Text("Add") }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DutyCalendarDialog(
    team: Team,
    onDismiss: () -> Unit,
    onAssignDuty: (Teammate, LocalDate, DutyShift) -> Unit,
    onRemoveDuty: (AssignedDuty) -> Unit,
    onToggleHoliday: (Teammate, LocalDate) -> Unit,
    onSetWeeklySchedule: (Teammate, List<Int>, LocalTime, LocalTime) -> Unit,
    getTeammateStats: (String) -> DutyStats
) {
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var selectedTeammate by remember { mutableStateOf<Teammate?>(team.teammates.firstOrNull()) }
    var startTime by remember { mutableStateOf(LocalTime.of(9, 0)) }
    var endTime by remember { mutableStateOf(LocalTime.of(17, 0)) }
    var showTimePicker by remember { mutableStateOf(false) }
    var isPickingStartTime by remember { mutableStateOf(true) }
    var dutyNotes by remember { mutableStateOf("") }
    
    val stats = remember(selectedTeammate) { 
        selectedTeammate?.let { getTeammateStats(it.id) } 
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(modifier = Modifier.fillMaxWidth().fillMaxHeight(0.95f), shape = RoundedCornerShape(24.dp)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Team Operations", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Text("Select Member", style = MaterialTheme.typography.labelMedium)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    items(team.teammates) { teammate ->
                        FilterChip(
                            selected = selectedTeammate?.id == teammate.id,
                            onClick = { selectedTeammate = teammate },
                            label = { Text(teammate.name) }
                        )
                    }
                }
                
                selectedTeammate?.let { teammate ->
                    Spacer(Modifier.height(12.dp))
                    Text("Regular Weekly Schedule", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    val days = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        days.forEachIndexed { index, day ->
                            val dayNum = index + 1
                            val isSelected = teammate.dutySchedule.regularDutyDays.contains(dayNum)
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                                    .clickable {
                                        val currentDays = teammate.dutySchedule.regularDutyDays.toMutableList()
                                        if (isSelected) currentDays.remove(dayNum) else currentDays.add(dayNum)
                                        onSetWeeklySchedule(teammate, currentDays, teammate.dutySchedule.dutyStartTime, teammate.dutySchedule.dutyEndTime)
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(day.take(1), color = if (isSelected) Color.White else Color.Gray, style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(4.dp)) {
                        IconButton(onClick = { selectedDate = selectedDate.minusDays(1) }) { Icon(Icons.Default.ChevronLeft, null) }
                        Text(selectedDate.format(DateTimeFormatter.ofPattern("EEE, MMM d")), modifier = Modifier.weight(1f), textAlign = TextAlign.Center, fontWeight = FontWeight.Bold)
                        IconButton(onClick = { selectedDate = selectedDate.plusDays(1) }) { Icon(Icons.Default.ChevronRight, null) }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Start Time", style = MaterialTheme.typography.labelMedium)
                        Button(onClick = { isPickingStartTime = true; showTimePicker = true }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp)) {
                            Text(startTime.format(DateTimeFormatter.ofPattern("hh:mm a")), fontSize = 12.sp)
                        }
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text("End Time", style = MaterialTheme.typography.labelMedium)
                        Button(onClick = { isPickingStartTime = false; showTimePicker = true }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp)) {
                            Text(endTime.format(DateTimeFormatter.ofPattern("hh:mm a")), fontSize = 12.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                selectedTeammate?.let { teammate ->
                    val isHoliday = teammate.dutySchedule.offDays.contains(selectedDate)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = { onToggleHoliday(teammate, selectedDate) },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = if (isHoliday) Color(0xFF4CAF50) else Color(0xFFFF5252))
                        ) {
                            Icon(if (isHoliday) Icons.Default.WorkOutline else Icons.Default.BeachAccess, null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(4.dp))
                            Text(if (isHoliday) "Work" else "Holiday")
                        }
                        
                        Button(
                            onClick = { onAssignDuty(teammate, selectedDate, DutyShift(startTime, endTime, notes = dutyNotes)) },
                            modifier = Modifier.weight(1.2f),
                            enabled = !isHoliday
                        ) {
                            Icon(Icons.Default.AddCircleOutline, null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Save Duty")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                
                Text("Existing Duties", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                val dayDuties = team.teammates.flatMap { t -> 
                    t.dutySchedule.assignedDuties.filter { it.date == selectedDate }.map { it to t.name }
                }
                
                LazyColumn(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    items(dayDuties) { (duty, name) ->
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        ) {
                            Row(modifier = Modifier.fillMaxWidth().padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                                    Text("${duty.startTime.format(DateTimeFormatter.ofPattern("hh:mm a"))} - ${duty.endTime.format(DateTimeFormatter.ofPattern("hh:mm a"))}", style = MaterialTheme.typography.bodySmall)
                                }
                                IconButton(onClick = { onRemoveDuty(duty) }, modifier = Modifier.size(32.dp)) {
                                    Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                }

                Button(
                    onClick = onDismiss, 
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer, contentColor = MaterialTheme.colorScheme.onSecondaryContainer)
                ) {
                    Text("Done")
                }
            }
        }
    }

    if (showTimePicker) {
        val timeState = rememberTimePickerState(
            initialHour = if (isPickingStartTime) startTime.hour else endTime.hour,
            initialMinute = if (isPickingStartTime) startTime.minute else endTime.minute
        )
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    val newTime = LocalTime.of(timeState.hour, timeState.minute)
                    if (isPickingStartTime) startTime = newTime else endTime = newTime
                    showTimePicker = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showTimePicker = false }) { Text("Cancel") } },
            text = { TimePicker(state = timeState) }
        )
    }
}

fun loadContacts(context: Context): List<Contact> {
    val contacts = mutableListOf<Contact>()
    val contentResolver: ContentResolver = context.contentResolver
    val cursor = contentResolver.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, "${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME} ASC")
    cursor?.use {
        while (it.moveToNext()) {
            val id = it.getString(it.getColumnIndexOrThrow(ContactsContract.Contacts._ID))
            val name = it.getString(it.getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME)) ?: ""
            val phoneNumbers = mutableListOf<String>()
            contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, "${ContactsContract.CommonDataKinds.Phone.CONTACT_ID} = ?", arrayOf(id), null)?.use { pCursor ->
                while (pCursor.moveToNext()) { phoneNumbers.add(pCursor.getString(pCursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER))) }
            }
            if (name.isNotBlank() && phoneNumbers.isNotEmpty()) contacts.add(Contact(id, name, phoneNumbers))
        }
    }
    return contacts
}
