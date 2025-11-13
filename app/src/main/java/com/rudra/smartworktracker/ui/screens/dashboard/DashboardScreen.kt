package com.rudra.smartworktracker.ui.screens.dashboard

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rudra.smartworktracker.data.AppDatabase
import com.rudra.smartworktracker.model.WorkType
import com.rudra.smartworktracker.ui.FinancialSummary
import com.rudra.smartworktracker.ui.MonthlyStats
import com.rudra.smartworktracker.ui.WorkLogUi
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onNavigateToAddEntry: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val context = LocalContext.current
    val viewModel: DashboardViewModel = viewModel(
        factory = DashboardViewModel.factory(AppDatabase.getDatabase(context))
    )
    val uiState by viewModel.uiState.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToAddEntry,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, "Add Entry")
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(16.dp)
        ) {
            item {
                Header(userName = uiState.userName, onSettingsClick = onNavigateToSettings)
            }
            item {
                FinancialSummaryCard(summary = uiState.financialSummary)
            }
            // Today's Status Card
            item {
                TodayStatusCard(
                    workType = uiState.todayWorkType,
                    onWorkTypeSelected = { workType ->
                        coroutineScope.launch {
                            viewModel.updateTodayWorkType(workType)
                        }
                    }
                )
            }

            // Quick Stats
            item {
                MonthlyStatsCard(stats = uiState.monthlyStats)
            }

            // Recent Activity
            item {
                RecentActivityCard(activities = uiState.recentActivities)
            }
        }
    }
}

@Composable
fun Header(userName: String?, onSettingsClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Hello, ${userName ?: "there"}!",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Here's your dashboard for today.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        IconButton(onClick = onSettingsClick) {
            Icon(Icons.Default.Settings, contentDescription = "Settings")
        }
    }
}

@Composable
fun FinancialSummaryCard(summary: FinancialSummary) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "Financial Summary",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                FinancialSummaryItem(
                    label = "Income",
                    amount = summary.totalIncome,
                    icon = Icons.Default.TrendingUp,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
                FinancialSummaryItem(
                    label = "Expense",
                    amount = summary.totalExpense,
                    icon = Icons.Default.TrendingDown,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.weight(1f)
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                FinancialSummaryItem(
                    label = "Savings",
                    amount = summary.netSavings,
                    icon = Icons.Default.AccountBalanceWallet,
                    color = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.weight(1f)
                )
                FinancialSummaryItem(
                    label = "Meal Cost",
                    amount = summary.totalMealCost,
                    icon = Icons.Outlined.Restaurant,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun FinancialSummaryItem(
    label: String,
    amount: Double,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    var currentValue by remember { mutableStateOf(0.0) }

    LaunchedEffect(amount) {
        val animation = Animatable(0f)
        animation.animateTo(amount.toFloat(), animationSpec = tween(durationMillis = 1000)) {
            currentValue = this.value.toDouble()
        }
    }

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = color,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "%.2f BDT".format(currentValue),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = color
            )
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun TodayStatusCard(
    workType: WorkType?,
    onWorkTypeSelected: (WorkType) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val cardElevation by animateDpAsState(
        targetValue = if (expanded) 8.dp else 4.dp,
        label = "card_elevation"
    )

    // Background color animation
    val backgroundColor by animateColorAsState(
        targetValue = when (workType) {
            WorkType.OFFICE -> MaterialTheme.colorScheme.primaryContainer
            WorkType.HOME_OFFICE -> MaterialTheme.colorScheme.secondaryContainer
            WorkType.OFF_DAY -> MaterialTheme.colorScheme.tertiaryContainer
            WorkType.EXTRA_WORK -> MaterialTheme.colorScheme.errorContainer
            null -> MaterialTheme.colorScheme.surfaceVariant
        },
        label = "background_color"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(cardElevation, RoundedCornerShape(24.dp)),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        onClick = { expanded = !expanded }
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Custom Compose Animation
            WorkTypeAnimation(workType = workType)

            Spacer(modifier = Modifier.height(16.dp))

            // Animated text transition
            AnimatedContent(
                targetState = workType,
                transitionSpec = {
                    (slideInVertically { height -> height } + fadeIn()).togetherWith(
                        slideOutVertically { height -> -height } + fadeOut())
                }, label = "text_animation"
            ) { targetWorkType ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = targetWorkType?.let {
                            when (it) {
                                WorkType.OFFICE -> "Office Day 🏢"
                                WorkType.HOME_OFFICE -> "Home Office 🏠"
                                WorkType.OFF_DAY -> "Off Day 🌴"
                                WorkType.EXTRA_WORK -> "Extra Work ⚡"
                            }
                        } ?: "Mark Your Day",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = targetWorkType?.let {
                            when (it) {
                                WorkType.OFFICE -> "You're working from office today"
                                WorkType.HOME_OFFICE -> "Working comfortably from home"
                                WorkType.OFF_DAY -> "Enjoy your day off!"
                                WorkType.EXTRA_WORK -> "Going above and beyond!"
                            }
                        } ?: "Tap to select today's work type",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Animated expansion of work type buttons
            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column {
                    Spacer(modifier = Modifier.height(24.dp))

                    // Work Type Selection Buttons with staggered animation
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        listOf(
                            WorkType.OFFICE to Icons.Filled.Work,
                            WorkType.HOME_OFFICE to Icons.Filled.Home,
                            WorkType.OFF_DAY to Icons.Filled.BeachAccess,
                            WorkType.EXTRA_WORK to Icons.Filled.Bolt
                        ).forEachIndexed { index, (type, icon) ->
                            AnimatedWorkTypeButton(
                                workType = type,
                                icon = icon,
                                selected = workType == type,
                                onClick = { onWorkTypeSelected(type) },
                                delay = index * 100
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MealCountCard(mealCount: Int, onAddMeal: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "Today's Meals",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "$mealCount meals recorded",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Button(onClick = onAddMeal) {
                Icon(Icons.Outlined.Restaurant, contentDescription = "Add Meal")
            }
        }
    }
}


@Composable
fun RowScope.AnimatedWorkTypeButton(
    workType: WorkType,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
    delay: Int = 0
) {
    val buttonScale by animateFloatAsState(
        targetValue = if (selected) 1.1f else 1f,
        animationSpec = spring(dampingRatio = 0.6f),
        label = "button_scale"
    )

    // Staggered entrance animation
    val enterTransition = remember {
        scaleIn(
            animationSpec = tween(300, delay),
            initialScale = 0.8f
        ) + fadeIn(animationSpec = tween(300, delay))
    }

    AnimatedVisibility(
        visible = true,
        enter = enterTransition
    ) {
        OutlinedButton(
            onClick = onClick,
            modifier = Modifier
                .weight(1f)
                .graphicsLayer {
                    scaleX = buttonScale
                    scaleY = buttonScale
                },
            colors = ButtonDefaults.buttonColors(
                containerColor = if (selected) {
                    when (workType) {
                        WorkType.OFFICE -> MaterialTheme.colorScheme.primary
                        WorkType.HOME_OFFICE -> MaterialTheme.colorScheme.secondary
                        WorkType.OFF_DAY -> MaterialTheme.colorScheme.tertiary
                        WorkType.EXTRA_WORK -> MaterialTheme.colorScheme.error
                    }
                } else {
                    MaterialTheme.colorScheme.surface
                },
                contentColor = if (selected) {
                    MaterialTheme.colorScheme.onPrimary
                } else {
                    MaterialTheme.colorScheme.onSurface
                }
            ),
            border = if (!selected) {
                ButtonDefaults.outlinedButtonBorder
            } else {
                null
            }
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.height(4.dp))
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
}


@Composable
fun MonthlyStatsCard(stats: MonthlyStats) {
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        isVisible = true
    }

    Card(
        modifier = Modifier
            .fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "Monthly Summary",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                AnimatedStatItem(
                    value = stats.officeDays,
                    label = "Office Days",
                    color = MaterialTheme.colorScheme.primary,
                    visible = isVisible,
                    delay = 0
                )
                AnimatedStatItem(
                    value = stats.homeOfficeDays,
                    label = "Home Days",
                    color = MaterialTheme.colorScheme.secondary,
                    visible = isVisible,
                    delay = 100
                )
                AnimatedStatItem(
                    value = stats.offDays,
                    label = "Off Days",
                    color = MaterialTheme.colorScheme.tertiary,
                    visible = isVisible,
                    delay = 200
                )
                AnimatedStatItem(
                    value = stats.extraHours.toInt(),
                    label = "Extra Hours",
                    color = MaterialTheme.colorScheme.error,
                    visible = isVisible,
                    delay = 300
                )
            }
        }
    }
}

@Composable
fun AnimatedStatItem(value: Int, label: String, color: Color, visible: Boolean, delay: Int) {
    var currentValue by remember { mutableIntStateOf(0) }

    LaunchedEffect(visible, value) {
        if (visible) {
            // Animate number counting
            for (i in 0..value) {
                currentValue = i
                delay(20L)
            }
        }
    }

    val animationSpec = remember {
        tween<Float>(durationMillis = 600, delayMillis = delay, easing = FastOutSlowInEasing)
    }

    val animatedProgress by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = animationSpec,
        label = "stat_item"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.graphicsLayer {
            alpha = animatedProgress
            scaleX = 0.5f + animatedProgress * 0.5f
            scaleY = 0.5f + animatedProgress * 0.5f
        }
    ) {
        Text(
            text = currentValue.toString(),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}


@Composable
fun RecentActivityCard(activities: List<WorkLogUi>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "Recent Activity",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(12.dp))

            activities.forEach { activity ->
                RecentActivityItem(workLog = activity)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun RecentActivityItem(workLog: WorkLogUi) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = when (workLog.workType) {
                    WorkType.OFFICE -> MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                    WorkType.HOME_OFFICE -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f)
                    WorkType.OFF_DAY -> MaterialTheme.colorScheme.tertiary.copy(alpha = 0.1f)
                    WorkType.EXTRA_WORK -> MaterialTheme.colorScheme.error.copy(alpha = 0.1f)
                },
                shape = RoundedCornerShape(12.dp)
            )
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                text = workLog.formattedDate,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = workLog.workType.name,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun WorkTypeAnimation(workType: WorkType?) {
    val infiniteTransition = rememberInfiniteTransition(label = "infinite_transition")
    val angle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "angle"
    )

    // Color transition
    val primaryColor by animateColorAsState(
        targetValue = when (workType) {
            WorkType.OFFICE -> MaterialTheme.colorScheme.primary
            WorkType.HOME_OFFICE -> MaterialTheme.colorScheme.secondary
            WorkType.OFF_DAY -> MaterialTheme.colorScheme.tertiary
            WorkType.EXTRA_WORK -> MaterialTheme.colorScheme.error
            null -> MaterialTheme.colorScheme.primary
        },
        label = "primary_color"
    )
    val secondaryColor by animateColorAsState(
        targetValue = when (workType) {
            WorkType.OFFICE -> MaterialTheme.colorScheme.secondary
            WorkType.HOME_OFFICE -> MaterialTheme.colorScheme.tertiary
            WorkType.OFF_DAY -> MaterialTheme.colorScheme.primary
            WorkType.EXTRA_WORK -> MaterialTheme.colorScheme.secondary
            null -> MaterialTheme.colorScheme.secondary
        },
        label = "secondary_color"
    )

    Canvas(modifier = Modifier.size(120.dp)) {
        val radius = size.minDimension / 2f
        val particleCount = 20

        (0 until particleCount).forEach { i ->
            val progress = (i.toFloat() / particleCount + angle / 360f) % 1f
            val currentAngle = progress * 360f
            val currentRadius = radius * (1 - progress * 0.5f)

            val x = center.x + currentRadius * cos(Math.toRadians(currentAngle.toDouble())).toFloat()
            val y = center.y + currentRadius * sin(Math.toRadians(currentAngle.toDouble())).toFloat()

            drawCircle(
                color = if (i % 2 == 0) primaryColor else secondaryColor,
                radius = (1 - progress) * 6f, // Particle size decreases over time
                center = Offset(x, y),
                alpha = 1 - progress // Fade out
            )
        }
    }
}
