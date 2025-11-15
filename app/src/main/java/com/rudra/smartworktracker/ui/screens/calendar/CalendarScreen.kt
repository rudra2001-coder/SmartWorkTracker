package com.rudra.smartworktracker.ui.screens.calendar

import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rudra.smartworktracker.data.AppDatabase
import com.rudra.smartworktracker.model.WorkType
import com.rudra.smartworktracker.ui.WorkLogUi
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class, ExperimentalFoundationApi::class)
@Composable
fun CalendarScreen(
    onNavigateBack: () -> Unit,
    onNavigateToEditEntry: (Long) -> Unit
) {
    val context = LocalContext.current
    val viewModel: CalendarViewModel = viewModel(factory = CalendarViewModel.factory(AppDatabase.getDatabase(context)))
    val uiState by viewModel.uiState.collectAsState()
    var currentMonth by remember { mutableStateOf(YearMonth.now()) }

    // Enhanced color scheme usage
    val surfaceColor = MaterialTheme.colorScheme.surface
    val primaryColor = MaterialTheme.colorScheme.primary
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Work Calendar",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            "Back",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    AnimatedContent(
                        targetState = uiState.isMultiSelectMode,
                        transitionSpec = {
                            fadeIn() with fadeOut()
                        },
                        label = "multi select icon"
                    ) { isMultiSelect ->
                        IconButton(
                            onClick = { viewModel.toggleMultiSelectMode() },
                            modifier = Modifier.animateContentSize()
                        ) {
                            Icon(
                                if (isMultiSelect) Icons.Default.Done else Icons.Default.SelectAll,
                                contentDescription = if (isMultiSelect) "Done Selection" else "Select Multiple",
                                tint = if (isMultiSelect) primaryColor else onSurfaceColor
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = surfaceColor,
                    scrolledContainerColor = surfaceColor
                )
            )
        },
        floatingActionButton = {
            AnimatedVisibility(
                visible = uiState.isMultiSelectMode && uiState.multiSelectedDates.isNotEmpty(),
                enter = scaleIn() + fadeIn(),
                exit = scaleOut() + fadeOut()
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Selection count chip
                    Card(
                        colors = CardDefaults.cardColors(containerColor = primaryColor),
                        shape = CircleShape
                    ) {
                        Text(
                            text = uiState.multiSelectedDates.size.toString(),
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Mark as Office Day FAB
                    ExtendedFloatingActionButton(
                        onClick = { viewModel.markSelectedDates(WorkType.OFFICE) },
                        icon = { Icon(Icons.Default.Edit, "Mark as Office Day") },
                        text = { Text("Mark Office") },
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            // Enhanced Month Navigation Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .shadow(
                        elevation = 8.dp,
                        shape = RoundedCornerShape(20.dp),
                        clip = true
                    ),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column {
                    MonthNavigation(currentMonth) { newMonth -> currentMonth = newMonth }

                    // Quick month navigation
                    if (!uiState.isMultiSelectMode) {
                        QuickMonthNavigation(
                            currentMonth = currentMonth,
                            onMonthChange = { currentMonth = it },
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }
                }
            }

            // Enhanced Calendar Content
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .shadow(
                        elevation = 12.dp,
                        shape = RoundedCornerShape(24.dp),
                        clip = true
                    ),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Enhanced header with selection mode indicator
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Select dates to mark work type",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.weight(1f)
                        )

                        AnimatedVisibility(visible = uiState.isMultiSelectMode) {
                            Text(
                                "${uiState.multiSelectedDates.size} selected",
                                style = MaterialTheme.typography.labelMedium,
                                color = primaryColor,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    DaysOfWeekHeader()
                    CalendarGrid(
                        currentMonth = currentMonth,
                        workLogs = uiState.workLogs,
                        selectedDate = uiState.selectedDate,
                        multiSelectedDates = uiState.multiSelectedDates,
                        isMultiSelectMode = uiState.isMultiSelectMode,
                        onDateSelected = { viewModel.onDateSelected(it) }
                    )
                }
            }

            // Enhanced Work Log Details with smooth animation
            AnimatedVisibility(
                visible = uiState.selectedWorkLog != null && !uiState.isMultiSelectMode,
                enter = slideInVertically(animationSpec = tween(400)) { it } + fadeIn(animationSpec = tween(400)),
                exit = slideOutVertically(animationSpec = tween(400)) { it } + fadeOut(animationSpec = tween(400))
            ) {
                uiState.selectedWorkLog?.let { workLog ->
                    WorkLogDetails(
                        workLog = workLog,
                        onEdit = onNavigateToEditEntry,
                        onDelete = viewModel::deleteWorkLog,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun QuickMonthNavigation(
    currentMonth: YearMonth,
    onMonthChange: (YearMonth) -> Unit,
    modifier: Modifier = Modifier
) {
    val currentYear = YearMonth.now().year
    val months = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")

    LazyRow(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        items(months) { month ->
            val monthIndex = months.indexOf(month) + 1
            val isSelected = currentMonth.monthValue == monthIndex

            TextButton(
                onClick = {
                    onMonthChange(YearMonth.of(currentYear, monthIndex))
                }
            ) {
                Text(month)
            }
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun MonthNavigation(currentMonth: YearMonth, onMonthChange: (YearMonth) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = { onMonthChange(currentMonth.minusMonths(1)) },
            modifier = Modifier.size(40.dp),
            colors = IconButtonDefaults.iconButtonColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        ) {
            Icon(
                Icons.Default.ChevronLeft,
                contentDescription = "Previous Month",
                modifier = Modifier.size(20.dp)
            )
        }

        AnimatedContent(
            targetState = currentMonth,
            transitionSpec = {
                if (targetState.isAfter(initialState)) {
                    slideInHorizontally(animationSpec = tween(300)) { width -> width } +
                            fadeIn(animationSpec = tween(300)) togetherWith
                            slideOutHorizontally(animationSpec = tween(300)) { width -> -width } +
                            fadeOut(animationSpec = tween(300))
                } else {
                    slideInHorizontally(animationSpec = tween(300)) { width -> -width } +
                            fadeIn(animationSpec = tween(300)) togetherWith
                            slideOutHorizontally(animationSpec = tween(300)) { width -> width } +
                            fadeOut(animationSpec = tween(300))
                }.using(SizeTransform(clip = false))
            },
            label = "month name"
        ) { month ->
            Text(
                text = month.format(DateTimeFormatter.ofPattern("MMMM yyyy")),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
        }

        IconButton(
            onClick = { onMonthChange(currentMonth.plusMonths(1)) },
            modifier = Modifier.size(40.dp),
            colors = IconButtonDefaults.iconButtonColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        ) {
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = "Next Month",
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun DaysOfWeekHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        val days = DayOfWeek.values()
        days.forEach { day ->
            Text(
                text = day.getDisplayName(TextStyle.SHORT, Locale.getDefault()),
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun CalendarGrid(
    currentMonth: YearMonth,
    workLogs: List<WorkLogUi>,
    selectedDate: LocalDate,
    multiSelectedDates: List<LocalDate>,
    isMultiSelectMode: Boolean,
    onDateSelected: (LocalDate) -> Unit
) {
    val daysInMonth = currentMonth.lengthOfMonth()
    val firstDayOfMonth = currentMonth.atDay(1).dayOfWeek.value - 1
    val today = LocalDate.now()

    LazyVerticalGrid(
        columns = GridCells.Fixed(7),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        items(daysInMonth + firstDayOfMonth) { dayIndex ->
            if (dayIndex >= firstDayOfMonth) {
                val date = currentMonth.atDay(dayIndex - firstDayOfMonth + 1)
                val workLogForDay = workLogs.find {
                    it.date.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate() == date
                }
                val isSelected = if (isMultiSelectMode) multiSelectedDates.contains(date) else date == selectedDate

                CalendarDay(
                    date = date,
                    workType = workLogForDay?.workType,
                    isSelected = isSelected,
                    isToday = date == today,
                    onDateSelected = { onDateSelected(date) }
                )
            } else {
                // Empty space for days before the first day of month
                Spacer(modifier = Modifier.aspectRatio(1f))
            }
        }
    }
}

@Composable
fun CalendarDay(
    date: LocalDate,
    workType: WorkType?,
    isSelected: Boolean,
    isToday: Boolean,
    onDateSelected: () -> Unit
) {
    val backgroundColor by animateColorAsState(
        targetValue = when {
            isSelected -> MaterialTheme.colorScheme.primary
            isToday -> MaterialTheme.colorScheme.primaryContainer
            else -> Color.Transparent
        },
        animationSpec = tween(300),
        label = "background color"
    )

    val textColor by animateColorAsState(
        targetValue = when {
            isSelected -> MaterialTheme.colorScheme.onPrimary
            isToday -> MaterialTheme.colorScheme.onPrimaryContainer
            else -> MaterialTheme.colorScheme.onSurface
        },
        animationSpec = tween(300),
        label = "text color"
    )

    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .padding(2.dp)
            .clip(CircleShape)
            .background(backgroundColor)
            .then(
                if (isToday && !isSelected)
                    Modifier.border(
                        2.dp,
                        MaterialTheme.colorScheme.primary,
                        CircleShape
                    )
                else Modifier
            )
            .clickable { onDateSelected() },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = date.dayOfMonth.toString(),
                color = textColor,
                fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Medium,
                fontSize = 14.sp
            )

            workType?.let {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(
                            color = when (it) {
                                WorkType.OFFICE -> MaterialTheme.colorScheme.secondary
                                WorkType.HOME_OFFICE -> MaterialTheme.colorScheme.tertiary
                                WorkType.OFF_DAY -> MaterialTheme.colorScheme.error
                                WorkType.EXTRA_WORK -> MaterialTheme.colorScheme.primary
                            }
                        )
                )
            }
        }
    }
}

@Composable
fun WorkLogDetails(
    workLog: WorkLogUi,
    onEdit: (Long) -> Unit,
    onDelete: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(20.dp),
                clip = true
            ),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // Header with actions
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Text(
                        "Work Entry Details",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        workLog.formattedDate,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Row {
                    FilledTonalIconButton(
                        onClick = { onEdit(workLog.id) },
                        colors = IconButtonDefaults.filledTonalIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    ) {
                        Icon(Icons.Default.Edit, "Edit Entry")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    FilledTonalIconButton(
                        onClick = { onDelete(workLog.id) },
                        colors = IconButtonDefaults.filledTonalIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.onErrorContainer
                        )
                    ) {
                        Icon(Icons.Default.Delete, "Delete Entry")
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Work type with colored chip
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Work Type:",
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f),
                    color = MaterialTheme.colorScheme.onSurface
                )
                WorkTypeChip(workLog.workType)
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Time information
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    InfoRow("Start Time", workLog.startTime ?: "Not set")
                    Spacer(modifier = Modifier.height(8.dp))
                    InfoRow("End Time", workLog.endTime ?: "Not set")
                    Spacer(modifier = Modifier.height(8.dp))
                    InfoRow("Duration", workLog.duration, isHighlighted = true)
                }
            }
        }
    }
}

@Composable
fun WorkTypeChip(workType: WorkType) {
    val (backgroundColor, textColor) = when (workType) {
        WorkType.OFFICE -> MaterialTheme.colorScheme.secondary to MaterialTheme.colorScheme.onSecondary
        WorkType.HOME_OFFICE -> MaterialTheme.colorScheme.tertiary to MaterialTheme.colorScheme.onTertiary
        WorkType.OFF_DAY -> MaterialTheme.colorScheme.error to MaterialTheme.colorScheme.onError
        WorkType.EXTRA_WORK -> MaterialTheme.colorScheme.primary to MaterialTheme.colorScheme.onPrimary
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(backgroundColor)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = workType.name.replace("_", " "),
            color = textColor,
            fontWeight = FontWeight.Medium,
            fontSize = 12.sp
        )
    }
}

@Composable
fun InfoRow(label: String, value: String, isHighlighted: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            label,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            value,
            fontWeight = if (isHighlighted) FontWeight.Bold else FontWeight.Normal,
            color = if (isHighlighted) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.End
        )
    }
}
