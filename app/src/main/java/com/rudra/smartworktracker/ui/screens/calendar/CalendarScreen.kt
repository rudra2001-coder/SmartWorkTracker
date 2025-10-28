package com.rudra.smartworktracker.ui.screens.calendar

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.rudra.smartworktracker.data.entity.WorkType
import com.rudra.smartworktracker.ui.WorkLogUi
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId
import java.time.YearMonth
import java.time.format.DateTimeFormatter


@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class,
    ExperimentalAnimationApi::class
)
@Composable
fun CalendarScreen(
    onNavigateBack: () -> Unit,
    viewModel: CalendarViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val pagerState = rememberPagerState(initialPage = Int.MAX_VALUE / 2, pageCount = { Int.MAX_VALUE })
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Work Calendar",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        floatingActionButton = {
            // Quick action FAB with animation
            var expanded by remember { mutableStateOf(false) }

            Box {
                // Secondary FABs
                AnimatedVisibility(
                    visible = expanded,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        horizontalAlignment = Alignment.End
                    ) {
                        listOf(
                            WorkType.OFFICE to Icons.Default.Work,
                            WorkType.HOME_OFFICE to Icons.Default.Home,
                            WorkType.OFF_DAY to Icons.Default.BeachAccess,
                            WorkType.EXTRA_WORK to Icons.Default.Bolt
                        ).forEach { (workType, icon) ->
                            SmallFloatingActionButton(
                                onClick = {
                                    viewModel.markSelectedDate(workType)
                                    expanded = false
                                },
                                containerColor = when (workType) {
                                    WorkType.OFFICE -> MaterialTheme.colorScheme.primary
                                    WorkType.HOME_OFFICE -> MaterialTheme.colorScheme.secondary
                                    WorkType.OFF_DAY -> MaterialTheme.colorScheme.tertiary
                                    WorkType.EXTRA_WORK -> MaterialTheme.colorScheme.error
                                }
                            ) {
                                Icon(icon, workType.name)
                            }
                        }
                    }
                }

                // Main FAB
                FloatingActionButton(
                    onClick = { expanded = !expanded },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    AnimatedContent(
                        targetState = expanded,
                        transitionSpec = {
                            (scaleIn() + fadeIn()).togetherWith(scaleOut() + fadeOut())
                        }, label = "fab_icon"
                    ) { isExpanded ->
                        Icon(
                            if (isExpanded) Icons.Default.Close else Icons.Default.Add,
                            if (isExpanded) "Close" else "Add Work Type"
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            // Month Navigation with smooth animations
            MonthNavigationHeader(
                pagerState = pagerState,
                onMonthChange = { page ->
                    coroutineScope.launch {
                        pagerState.animateScrollToPage(page)
                    }
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Infinite month pager
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f)
            ) { page ->
                val currentMonth = remember(page) {
                    YearMonth.now().plusMonths(page.toLong() - (Int.MAX_VALUE / 2))
                }

                AnimatedMonthCalendar(
                    month = currentMonth,
                    workLogs = uiState.workLogs,
                    selectedDate = uiState.selectedDate,
                    onDateSelected = viewModel::selectDate,
                    modifier = Modifier.fillMaxSize()
                )
            }

            // Selected Date Details with animation
            AnimatedVisibility(
                visible = uiState.selectedDate != null,
                enter = slideInVertically() + fadeIn(),
                exit = slideOutVertically() + fadeOut()
            ) {
                uiState.selectedDate?.let { selectedDate ->
                    SelectedDateDetails(
                        selectedDate = selectedDate,
                        workLog = uiState.workLogs.find { it.date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate() == selectedDate },
                        onWorkTypeChange = { workType ->
                            viewModel.markDateWithWorkType(selectedDate, workType)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalAnimationApi::class)
@Composable
fun MonthNavigationHeader(
    pagerState: androidx.compose.foundation.pager.PagerState,
    onMonthChange: (Int) -> Unit
) {
    val currentMonth = remember(pagerState.currentPage) {
        YearMonth.now().plusMonths(pagerState.currentPage.toLong() - (Int.MAX_VALUE / 2))
    }

    val formatter = remember { DateTimeFormatter.ofPattern("MMMM yyyy") }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Previous Month Button
        IconButton(
            onClick = { onMonthChange(pagerState.currentPage - 1) },
            modifier = Modifier
                .size(48.dp)
                .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
        ) {
            Icon(Icons.Default.ChevronLeft, "Previous Month")
        }

        // Month Title with animation
        AnimatedContent(
            targetState = currentMonth,
            transitionSpec = {
                if (targetState > initialState) {
                    (slideInHorizontally { width -> width } + fadeIn()).togetherWith(
                        slideOutHorizontally { width -> -width } + fadeOut())
                } else {
                    (slideInHorizontally { width -> -width } + fadeIn()).togetherWith(
                        slideOutHorizontally { width -> width } + fadeOut())
                }.using(
                    SizeTransform(clip = false)
                )
            }, label = "month_title"
        ) { month ->
            Text(
                text = month.format(formatter),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        // Next Month Button
        IconButton(
            onClick = { onMonthChange(pagerState.currentPage + 1) },
            modifier = Modifier
                .size(48.dp)
                .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
        ) {
            Icon(Icons.Default.ChevronRight, "Next Month")
        }
    }
}

@Composable
fun AnimatedMonthCalendar(
    month: YearMonth,
    workLogs: List<WorkLogUi>,
    selectedDate: LocalDate?,
    onDateSelected: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    val daysInMonth = month.lengthOfMonth()
    val firstDayOfMonth = month.atDay(1).dayOfWeek.value % 7 // Adjust for Sunday start

    // Pinch to zoom animation
    var scale by remember { mutableFloatStateOf(1f) }

    Column(
        modifier = modifier
            .pointerInput(Unit) {
                detectTransformGestures { _, _, zoom, _ ->
                    scale = (scale * zoom).coerceIn(0.8f, 1.5f)
                }
            }
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .padding(16.dp)
    ) {
        // Week day headers
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat").forEach { day ->
                Text(
                    text = day,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Calendar grid with staggered animation
        LazyColumn {
            items((0 until 42).chunked(7).size) { weekIndex ->
                AnimatedVisibility(
                    visible = true,
                    enter = slideInVertically(
                        initialOffsetY = { it * 2 },
                        animationSpec = tween(300, delayMillis = weekIndex * 50)
                    ) + fadeIn()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        for (dayOffset in 0 until 7) {
                            val dayIndex = weekIndex * 7 + dayOffset
                            val dayNumber = dayIndex - firstDayOfMonth + 1

                            if (dayNumber in 1..daysInMonth) {
                                val date = month.atDay(dayNumber)
                                val workLog = workLogs.find { it.date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate() == date }

                                AnimatedCalendarDay(
                                    dayNumber = dayNumber,
                                    date = date,
                                    workLog = workLog,
                                    isSelected = date == selectedDate,
                                    onClick = { onDateSelected(date) },
                                    modifier = Modifier.weight(1f)
                                )
                            } else {
                                // Empty day
                                Spacer(modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AnimatedCalendarDay(
    dayNumber: Int,
    date: LocalDate,
    workLog: WorkLogUi?,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isToday = date == LocalDate.now()

    // Selection animation
    val selectionScale by animateFloatAsState(
        targetValue = if (isSelected) 1.2f else 1f,
        animationSpec = spring(dampingRatio = 0.6f),
        label = "selection_scale"
    )

    // Background color based on work type
    val backgroundColor by animateColorAsState(
        targetValue = when (workLog?.workType) {
            WorkType.OFFICE -> MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
            WorkType.HOME_OFFICE -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f)
            WorkType.OFF_DAY -> MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f)
            WorkType.EXTRA_WORK -> MaterialTheme.colorScheme.error.copy(alpha = 0.2f)
            null -> Color.Transparent
        },
        label = "background_color"
    )

    // Border color for today
    val borderColor = if (isToday) MaterialTheme.colorScheme.primary else Color.Transparent

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .padding(4.dp)
            .border(
                width = 2.dp,
                color = borderColor,
                shape = CircleShape
            )
            .background(backgroundColor, CircleShape)
            .clip(CircleShape)
            .clickable { onClick() }
            .graphicsLayer {
                scaleX = selectionScale
                scaleY = selectionScale
            },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = dayNumber.toString(),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                else MaterialTheme.colorScheme.onSurface
            )

            // Work type indicator dot
            workLog?.workType?.let { workType ->
                Spacer(modifier = Modifier.height(2.dp))
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .background(
                            color = when (workType) {
                                WorkType.OFFICE -> MaterialTheme.colorScheme.primary
                                WorkType.HOME_OFFICE -> MaterialTheme.colorScheme.secondary
                                WorkType.OFF_DAY -> MaterialTheme.colorScheme.tertiary
                                WorkType.EXTRA_WORK -> MaterialTheme.colorScheme.error
                            },
                            shape = CircleShape
                        )
                )
            }
        }

        // Selection ring animation
        if (isSelected) {
            val infiniteTransition = rememberInfiniteTransition(label = "selection_ring")
            val ringAlpha by infiniteTransition.animateFloat(
                initialValue = 1f,
                targetValue = 0f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1000, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart
                ), label = "ring_alpha"
            )

            Box(
                modifier = Modifier
                    .matchParentSize()
                    .border(
                        width = 2.dp,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = ringAlpha),
                        shape = CircleShape
                    )
            )
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun SelectedDateDetails(
    selectedDate: LocalDate,
    workLog: WorkLogUi?,
    onWorkTypeChange: (WorkType) -> Unit,
    modifier: Modifier = Modifier
) {
    val formatter = remember { DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy") }

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = selectedDate.format(formatter),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Current work type with animation
            AnimatedContent(
                targetState = workLog?.workType,
                transitionSpec = {
                    (scaleIn() + fadeIn()).togetherWith(scaleOut() + fadeOut())
                }, label = "work_type_display"
            ) { currentWorkType ->
                Text(
                    text = currentWorkType?.let {
                        when (it) {
                            WorkType.OFFICE -> "🏢 Office Day"
                            WorkType.HOME_OFFICE -> "🏠 Home Office"
                            WorkType.OFF_DAY -> "🌴 Off Day"
                            WorkType.EXTRA_WORK -> "⚡ Extra Work"
                        }
                    } ?: "📋 No work type set",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Work type selection buttons
            Text(
                text = "Mark as:",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                listOf(
                    WorkType.OFFICE to Icons.Default.Work,
                    WorkType.HOME_OFFICE to Icons.Default.Home,
                    WorkType.OFF_DAY to Icons.Default.BeachAccess,
                    WorkType.EXTRA_WORK to Icons.Default.Bolt
                ).forEach { (workType, icon) ->
                    WorkTypeSelectionButton(
                        workType = workType,
                        icon = icon,
                        isSelected = workLog?.workType == workType,
                        onClick = { onWorkTypeChange(workType) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Time selection if needed
            if (workLog?.workType in listOf(WorkType.OFFICE, WorkType.HOME_OFFICE, WorkType.EXTRA_WORK)) {
                Spacer(modifier = Modifier.height(16.dp))
                // Add time picker components here
            }
        }
    }
}

@Composable
fun RowScope.WorkTypeSelectionButton(
    workType: WorkType,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val buttonColor by animateColorAsState(
        targetValue = if (isSelected) {
            when (workType) {
                WorkType.OFFICE -> MaterialTheme.colorScheme.primary
                WorkType.HOME_OFFICE -> MaterialTheme.colorScheme.secondary
                WorkType.OFF_DAY -> MaterialTheme.colorScheme.tertiary
                WorkType.EXTRA_WORK -> MaterialTheme.colorScheme.error
            }
        } else {
            MaterialTheme.colorScheme.surface
        },
        label = "button_color"
    )

    val contentColor by animateColorAsState(
        targetValue = if (isSelected) {
            MaterialTheme.colorScheme.onPrimary
        } else {
            MaterialTheme.colorScheme.onSurface
        },
        label = "content_color"
    )

    OutlinedButton(
        onClick = onClick,
        modifier = modifier,
        colors = ButtonDefaults.buttonColors(
            containerColor = buttonColor,
            contentColor = contentColor
        ),
        border = if (!isSelected) {
            ButtonDefaults.outlinedButtonBorder
        } else {
            null
        }
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp))
            Text(
                text = when (workType) {
                    WorkType.OFFICE -> "Office"
                    WorkType.HOME_OFFICE -> "Home"
                    WorkType.OFF_DAY -> "Off"
                    WorkType.EXTRA_WORK -> "Extra"
                },
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}