package com.rudra.smartworktracker.ui.screens.overtime

import android.app.DatePickerDialog
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rudra.smartworktracker.model.WorkLog
import java.text.SimpleDateFormat
import java.util.*

private val CardShape = RoundedCornerShape(20.dp)
private val PillShape = RoundedCornerShape(50.dp)
private val ChipShape = RoundedCornerShape(12.dp)

private val EmeraldGreen = Color(0xFF00C896)
private val CoralRed = Color(0xFFFF5757)
private val SapphireBlue = Color(0xFF3B82F6)
private val GoldenAmber = Color(0xFFF59E0B)
private val VioletPurple = Color(0xFF8B5CF6)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OvertimeScreen(viewModel: OvertimeViewModel = viewModel()) {
    var startTime by remember { mutableStateOf("") }
    var endTime by remember { mutableStateOf("") }
    var overtimeRate by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf<Date?>(Date()) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showAddSheet by remember { mutableStateOf(false) }

    var showStartTimePicker by remember { mutableStateOf(false) }
    var showEndTimePicker by remember { mutableStateOf(false) }

    val startTimePickerState = rememberTimePickerState(initialHour = 9, initialMinute = 0)
    val endTimePickerState = rememberTimePickerState(initialHour = 18, initialMinute = 0)

    val sheetState = rememberModalBottomSheetState()
    val context = LocalContext.current

    val monthlyLogs by viewModel.monthlyOvertimeLogs.collectAsState()
    val yearlyLogs by viewModel.yearlyOvertimeLogs.collectAsState()
    val monthlySummary by viewModel.monthlySummary.collectAsState(initial = OvertimeSummary())
    val yearlySummary by viewModel.yearlySummary.collectAsState(initial = OvertimeSummary())

    var showDeleteDialog by remember { mutableStateOf(false) }
    var logToDelete by remember { mutableStateOf<WorkLog?>(null) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {},
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddSheet = true },
                containerColor = CoralRed,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Overtime")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 20.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier.size(52.dp).background(
                            brush = Brush.linearGradient(listOf(CoralRed, GoldenAmber)),
                            shape = RoundedCornerShape(14.dp)
                        ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Bolt, contentDescription = null, tint = Color.White, modifier = Modifier.size(28.dp))
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Overtime Tracking", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        Text("Track your extra work hours", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            item {
                OvertimeSummaryView(monthlySummary, yearlySummary, viewModel.getMonthName(), viewModel.getYearString())
            }

            item {
                SectionHeader(text = "Overtime History")
            }

            item {
                OvertimeLogsList(monthlyLogs, yearlyLogs, viewModel) { log ->
                    logToDelete = log
                    showDeleteDialog = true
                }
            }

            item { Spacer(Modifier.height(80.dp)) }
        }
    }

    if (showAddSheet) {
        ModalBottomSheet(
            onDismissRequest = { showAddSheet = false },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            AddOvertimeContent(
                startTime = startTime,
                endTime = endTime,
                overtimeRate = overtimeRate,
                selectedDate = selectedDate,
                onStartTimeChange = { startTime = it },
                onEndTimeChange = { endTime = it },
                onOvertimeRateChange = { overtimeRate = it },
                onShowDatePicker = { showDatePicker = true },
                onShowStartTimePicker = { showStartTimePicker = true },
                onShowEndTimePicker = { showEndTimePicker = true },
                onSave = {
                    if (viewModel.saveOvertime(selectedDate!!, startTime, endTime, overtimeRate.toDoubleOrNull() ?: 0.0)) {
                        Toast.makeText(context, "✓ Overtime Saved!", Toast.LENGTH_SHORT).show()
                        startTime = ""
                        endTime = ""
                        overtimeRate = ""
                        showAddSheet = false
                    } else {
                        Toast.makeText(context, "Invalid time format. Please use HH:mm", Toast.LENGTH_SHORT).show()
                    }
                }
            )
        }
    }

    if (showDatePicker) {
        val calendar = Calendar.getInstance()
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                calendar.set(year, month, dayOfMonth)
                selectedDate = calendar.time
                showDatePicker = false
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    if (showStartTimePicker) {
        TimePickerDialog(
            onDismissRequest = { showStartTimePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    startTime = String.format("%02d:%02d", startTimePickerState.hour, startTimePickerState.minute)
                    showStartTimePicker = false
                }) { Text("OK", color = CoralRed) }
            },
            dismissButton = {
                TextButton(onClick = { showStartTimePicker = false }) { Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant) }
            }
        ) {
            TimePicker(state = startTimePickerState)
        }
    }

    if (showEndTimePicker) {
        TimePickerDialog(
            onDismissRequest = { showEndTimePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    endTime = String.format("%02d:%02d", endTimePickerState.hour, endTimePickerState.minute)
                    showEndTimePicker = false
                }) { Text("OK", color = CoralRed) }
            },
            dismissButton = {
                TextButton(onClick = { showEndTimePicker = false }) { Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant) }
            }
        ) {
            TimePicker(state = endTimePickerState)
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Overtime Log", color = MaterialTheme.colorScheme.onSurface) },
            text = { Text("Are you sure you want to delete this log?", color = MaterialTheme.colorScheme.onSurfaceVariant) },
            confirmButton = {
                TextButton(
                    onClick = {
                        logToDelete?.let { log ->
                            viewModel.deleteOvertime(log)
                            Toast.makeText(context, "Log Deleted", Toast.LENGTH_SHORT).show()
                        }
                        showDeleteDialog = false
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = CoralRed)
                ) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant) }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = CardShape
        )
    }
}

@Composable
private fun SectionHeader(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(start = 2.dp)
    )
}

@Composable
private fun OvertimeSummaryView(monthlySummary: OvertimeSummary, yearlySummary: OvertimeSummary, monthName: String, year: String) {
    Card(
        modifier = Modifier.fillMaxWidth().shadow(8.dp, CardShape, clip = false),
        shape = CardShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(20.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            SummaryColumn(title = "This Month ($monthName)", summary = monthlySummary, color = VioletPurple, modifier = Modifier.weight(1f))
            Box(Modifier.width(1.dp).height(60.dp).background(MaterialTheme.colorScheme.outlineVariant))
            SummaryColumn(title = "This Year ($year)", summary = yearlySummary, color = SapphireBlue, modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun SummaryColumn(title: String, summary: OvertimeSummary, color: Color, modifier: Modifier) {
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(title, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(8.dp))
        Text("${String.format("%.2f", summary.totalHours)}h", fontWeight = FontWeight.ExtraBold, fontSize = 24.sp, color = color)
        Text("৳${String.format("%.2f", summary.totalEarnings)}", color = EmeraldGreen, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun AddOvertimeContent(
    startTime: String, endTime: String, overtimeRate: String, selectedDate: Date?,
    onStartTimeChange: (String) -> Unit, onEndTimeChange: (String) -> Unit, onOvertimeRateChange: (String) -> Unit,
    onShowDatePicker: () -> Unit, onShowStartTimePicker: () -> Unit, onShowEndTimePicker: () -> Unit, onSave: () -> Unit
) {
    val textFieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = CoralRed,
        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
        focusedLabelColor = CoralRed,
        unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
        cursorColor = CoralRed
    )

    Column(
        modifier = Modifier.fillMaxWidth().padding(24.dp).navigationBarsPadding(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(
                modifier = Modifier.size(36.dp).background(
                    brush = Brush.linearGradient(listOf(CoralRed, GoldenAmber)),
                    shape = RoundedCornerShape(10.dp)
                ),
                contentAlignment = Alignment.Center
            ) { Icon(Icons.Default.Bolt, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp)) }
            Text("Add New Overtime", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        }

        OutlinedTextField(
            value = selectedDate?.let { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(it) } ?: "Select a date",
            onValueChange = {},
            label = { Text("Date") },
            leadingIcon = { Icon(Icons.Default.CalendarToday, "Date", tint = CoralRed) },
            modifier = Modifier.fillMaxWidth().clickable(onClick = onShowDatePicker),
            shape = ChipShape, colors = textFieldColors, readOnly = true, enabled = false
        )

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = startTime, onValueChange = onStartTimeChange, label = { Text("Start Time") },
                leadingIcon = { Icon(Icons.Default.Schedule, "Start Time", tint = CoralRed) },
                modifier = Modifier.weight(1f).clickable { onShowStartTimePicker() },
                shape = ChipShape, colors = textFieldColors, placeholder = { Text("09:00") }, readOnly = true, enabled = false
            )
            OutlinedTextField(
                value = endTime, onValueChange = onEndTimeChange, label = { Text("End Time") },
                leadingIcon = { Icon(Icons.Default.Schedule, "End Time", tint = CoralRed) },
                modifier = Modifier.weight(1f).clickable { onShowEndTimePicker() },
                shape = ChipShape, colors = textFieldColors, placeholder = { Text("18:00") }, readOnly = true, enabled = false
            )
        }

        OutlinedTextField(
            value = overtimeRate, onValueChange = onOvertimeRateChange, label = { Text("Overtime Rate (per hour)") },
            leadingIcon = { Icon(Icons.Default.AttachMoney, "Rate", tint = CoralRed) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(), shape = ChipShape, colors = textFieldColors, singleLine = true
        )

        Button(
            onClick = onSave,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = ChipShape,
            colors = ButtonDefaults.buttonColors(containerColor = CoralRed)
        ) {
            Icon(Icons.Default.Save, "Save", tint = Color.White)
            Spacer(Modifier.width(8.dp))
            Text("Save Overtime", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun OvertimeLogsList(monthlyLogs: List<WorkLog>, yearlyLogs: List<WorkLog>, viewModel: OvertimeViewModel, onDeleteClick: (WorkLog) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().shadow(6.dp, CardShape, clip = false),
        shape = CardShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            TabRow(
                selectedTabIndex = viewModel.selectedTab,
                containerColor = Color.Transparent,
                contentColor = CoralRed,
                divider = {}
            ) {
                Tab(selected = viewModel.selectedTab == 0, onClick = { viewModel.selectedTab = 0 }, text = { Text("Monthly", fontWeight = if (viewModel.selectedTab == 0) FontWeight.Bold else FontWeight.Normal) })
                Tab(selected = viewModel.selectedTab == 1, onClick = { viewModel.selectedTab = 1 }, text = { Text("Yearly", fontWeight = if (viewModel.selectedTab == 1) FontWeight.Bold else FontWeight.Normal) })
            }
            Spacer(Modifier.height(8.dp))
            LazyColumn(modifier = Modifier.height(300.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                val logs = if (viewModel.selectedTab == 0) monthlyLogs else yearlyLogs
                items(logs) { log ->
                    OvertimeLogItem(log, viewModel, onDelete = { onDeleteClick(log) })
                }
                if (logs.isEmpty()) {
                    item {
                        Box(Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                            Text("No overtime entries yet", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun OvertimeLogItem(log: WorkLog, viewModel: OvertimeViewModel, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = ChipShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(40.dp).background(CoralRed.copy(alpha = 0.1f), ChipShape),
                contentAlignment = Alignment.Center
            ) { Icon(Icons.Default.Bolt, null, tint = CoralRed, modifier = Modifier.size(20.dp)) }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(SimpleDateFormat("dd MMM, yyyy", Locale.getDefault()).format(log.date), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                Text("${log.startTime} - ${log.endTime}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                val duration = viewModel.calculateDuration(log.startTime, log.endTime)
                val earnings = duration * (log.overtimeRate ?: 0.0)
                Text("Hours: ${String.format("%.2f", duration)}h  •  Earned: ৳${String.format("%.2f", earnings)}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, "Delete Log", tint = CoralRed.copy(alpha = 0.6f))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerDialog(
    onDismissRequest: () -> Unit,
    confirmButton: @Composable () -> Unit,
    dismissButton: @Composable (() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            tonalElevation = 6.dp,
            modifier = Modifier
                .width(IntrinsicSize.Min)
                .height(IntrinsicSize.Min)
                .background(shape = MaterialTheme.shapes.extraLarge, color = MaterialTheme.colorScheme.surface),
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp),
                    text = "Select Time",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                content()
                Row(
                    modifier = Modifier.height(40.dp).fillMaxWidth()
                ) {
                    Spacer(modifier = Modifier.weight(1f))
                    dismissButton?.invoke()
                    confirmButton()
                }
            }
        }
    }
}
