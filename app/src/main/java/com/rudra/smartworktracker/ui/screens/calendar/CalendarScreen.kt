package com.rudra.smartworktracker.ui.screens.calendar

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.animation.expandVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rudra.smartworktracker.data.AppDatabase
import com.rudra.smartworktracker.model.WorkType
import com.rudra.smartworktracker.ui.WorkLogUi
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId
import java.time.YearMonth
import java.time.format.DateTimeFormatter

@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalFoundationApi::class,
    ExperimentalAnimationApi::class
)
@Composable
fun CalendarScreen(
    onNavigateBack: () -> Unit,
    onNavigateToEditEntry: (Long) -> Unit
) {
    val context = LocalContext.current
    val viewModel: CalendarViewModel = viewModel(
        factory = CalendarViewModel.factory(AppDatabase.getDatabase(context))
    )
    val uiState by viewModel.uiState.collectAsState()

    val pagerState = rememberPagerState(
        initialPage = Int.MAX_VALUE / 2,
        pageCount = { Int.MAX_VALUE }
    )
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
            var expanded by remember { mutableStateOf(false) }

            Box {
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
            MonthNavigationHeader(
                pagerState = pagerState,
                onMonthChange = { page ->
                    coroutineScope.launch {
                        pagerState.animateScrollToPage(page)
                    }
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

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

            AnimatedVisibility(
                visible = uiState.selectedDate != null,
                enter = slideInVertically(initialOffsetY = { it / 2 }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it / 2 }) + fadeOut()
            ) {
                uiState.selectedDate?.let { selectedDate ->
                    SelectedDateDetails(
                        selectedDate = selectedDate,
                        workLog = uiState.workLogs.find {
                            it.date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate() == selectedDate
                        },
                        onDelete = { viewModel.deleteWorkLog(selectedDate) },
                        onEdit = { workLogId -> onNavigateToEditEntry(workLogId) },
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
        IconButton(
            onClick = { onMonthChange(pagerState.currentPage - 1) },
            modifier = Modifier
                .size(48.dp)
                .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
        ) {
            Icon(Icons.Default.ChevronLeft, "Previous Month")
        }

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
    val firstDayOfMonth = month.atDay(1).dayOfWeek.value % 7

    var scale by remember { mutableFloatStateOf(1f) }

    LazyVerticalGrid(
        columns = GridCells.Fixed(7),
        modifier = modifier.pointerInput(Unit) {
            detectTransformGestures { _, _, zoom, _ ->
                scale = (scale * zoom).coerceIn(0.8f, 1.2f)
            }
        }
    ) {
        items(daysInMonth + firstDayOfMonth) { day ->
            if (day >= firstDayOfMonth) {
                val date = month.atDay(day - firstDayOfMonth + 1)
                val workLog = workLogs.find {
                    it.date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate() == date
                }
                CalendarDay(
                    date = date,
                    workType = workLog?.workType,
                    isSelected = date == selectedDate,
                    onDateSelected = onDateSelected,
                    modifier = Modifier.graphicsLayer(scaleX = scale, scaleY = scale)
                )
            }
        }
    }
}

@Composable
fun CalendarDay(
    date: LocalDate,
    workType: WorkType?,
    isSelected: Boolean,
    onDateSelected: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
        label = "day_background"
    )

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .padding(2.dp)
            .clip(CircleShape)
            .background(backgroundColor)
            .clickable { onDateSelected(date) },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = date.dayOfMonth.toString(),
            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
        )
        workType?.let {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 4.dp)
                    .size(6.dp)
                    .background(
                        color = when (it) {
                            WorkType.OFFICE -> MaterialTheme.colorScheme.secondary
                            WorkType.HOME_OFFICE -> MaterialTheme.colorScheme.tertiary
                            WorkType.OFF_DAY -> MaterialTheme.colorScheme.error
                            WorkType.EXTRA_WORK -> MaterialTheme.colorScheme.primary
                        },
                        shape = CircleShape
                    )
            )
        }
    }
}

@Composable
fun SelectedDateDetails(
    selectedDate: LocalDate,
    workLog: WorkLogUi?,
    onDelete: () -> Unit,
    onEdit: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Details for ${selectedDate.format(DateTimeFormatter.ofPattern("MMMM dd, yyyy"))}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                if (workLog != null) {
                    Text("Work Type: ${workLog.workType.name}")
                    Text("Duration: ${workLog.duration}")
                } else {
                    Text("No work log for this date.")
                }
            }
            if (workLog != null) {
                Row {
                    IconButton(onClick = { onEdit(workLog.id) }) {
                        Icon(Icons.Default.Edit, "Edit")
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, "Delete")
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CalendarScreenPreview() {
    MaterialTheme {
        CalendarScreen(onNavigateBack = {}, onNavigateToEditEntry = {})
    }
}
