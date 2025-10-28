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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.rudra.smartworktracker.data.entity.WorkType
import com.rudra.smartworktracker.ui.MonthlyStats
import com.rudra.smartworktracker.ui.WorkLogUi
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onNavigateToCalendar: () -> Unit,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Smart Work Tracker",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToCalendar,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Schedule, "Calendar")
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
    var currentValue by remember { mutableStateOf(0) }

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

        Text(
            text = workLog.duration,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun WorkTypeAnimation(workType: WorkType?) {
    val infiniteTransition = rememberInfiniteTransition(label = "work_animation")

    // Pulsing animation
    val pulse by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "pulse"
    )

    // Rotation animation for extra work
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ), label = "rotation"
    )

    Box(
        modifier = Modifier
            .size(120.dp)
            .graphicsLayer {
                scaleX = pulse
                scaleY = pulse
                rotationZ = if (workType == WorkType.EXTRA_WORK) rotation else 0f
            },
        contentAlignment = Alignment.Center
    ) {
        when (workType) {
            WorkType.OFFICE -> OfficeAnimation()
            WorkType.HOME_OFFICE -> HomeOfficeAnimation()
            WorkType.OFF_DAY -> OffDayAnimation()
            WorkType.EXTRA_WORK -> ExtraWorkAnimation()
            null -> IdleAnimation()
        }
    }
}

@Composable
fun OfficeAnimation() {
    Icon(
        imageVector = Icons.Filled.Work,
        contentDescription = "Office",
        modifier = Modifier.size(64.dp),
        tint = MaterialTheme.colorScheme.primary
    )
}

@Composable
fun HomeOfficeAnimation() {
    val infiniteTransition = rememberInfiniteTransition(label = "home_animation")
    val wifiBars by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 1500
                0.3f at 0
                1f at 500
                0.3f at 1000
                1f at 1500
            },
            repeatMode = RepeatMode.Restart
        ), label = "wifi"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = Icons.Filled.Home,
            contentDescription = "Home Office",
            modifier = Modifier.size(48.dp),
            tint = MaterialTheme.colorScheme.secondary
        )

        // Animated WiFi bars
        Row(
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            listOf(0.3f, 0.6f, 1.0f).forEach { targetHeight ->
                AnimatedWifiBar(
                    progress = wifiBars,
                    targetHeight = targetHeight,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }
    }
}

@Composable
fun AnimatedWifiBar(progress: Float, targetHeight: Float, color: Color) {
    val animatedHeight by animateDpAsState(
        targetValue = (16.dp * targetHeight) * progress,
        label = "wifi_bar"
    )

    Box(
        modifier = Modifier
            .width(6.dp)
            .height(animatedHeight)
            .background(color, RoundedCornerShape(2.dp))
    )
}

@Composable
fun OffDayAnimation() {
    val infiniteTransition = rememberInfiniteTransition(label = "offday_animation")
    val sunRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ), label = "sun_rotation"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(80.dp)
    ) {
        // Sun with rays
        Canvas(modifier = Modifier.size(60.dp)) {
            // Sun center
            drawCircle(
                color = Color(0xFFFFD700),
                radius = 20f
            )

            // Sun rays
            for (i in 0 until 8) {
                val angle = i * 45f + sunRotation
                val rad = Math.toRadians(angle.toDouble()).toFloat()
                val startX = center.x + 25f * cos(rad)
                val startY = center.y + 25f * sin(rad)
                val endX = center.x + 35f * cos(rad)
                val endY = center.y + 35f * sin(rad)

                drawLine(
                    color = Color(0xFFFFA500),
                    start = Offset(startX, startY),
                    end = Offset(endX, endY),
                    strokeWidth = 3f
                )
            }
        }

        Icon(
            imageVector = Icons.Filled.BeachAccess,
            contentDescription = "Off Day",
            modifier = Modifier.size(32.dp),
            tint = Color.White
        )
    }
}

@Composable
fun ExtraWorkAnimation() {
    val infiniteTransition = rememberInfiniteTransition(label = "extra_work_animation")
    val boltScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "bolt_scale"
    )

    val sparkleAlpha by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ), label = "sparkle_alpha"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(80.dp)
    ) {
        // Lightning bolt with animation
        Icon(
            imageVector = Icons.Filled.Bolt,
            contentDescription = "Extra Work",
            modifier = Modifier
                .size(48.dp)
                .graphicsLayer {
                    scaleX = boltScale
                    scaleY = boltScale
                },
            tint = MaterialTheme.colorScheme.error
        )

        // Sparkle effects
        Canvas(modifier = Modifier.matchParentSize()) {
            for (i in 0 until 6) {
                val angle = i * 60f
                val rad = Math.toRadians(angle.toDouble()).toFloat()
                val distance = 25f
                val x = center.x + distance * cos(rad)
                val y = center.y + distance * sin(rad)

                drawCircle(
                    color = Color.Yellow.copy(alpha = sparkleAlpha),
                    radius = 4f,
                    center = Offset(x, y)
                )
            }
        }
    }
}

@Composable
fun IdleAnimation() {
    val infiniteTransition = rememberInfiniteTransition(label = "idle_animation")
    val questionMarkScale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "question_scale"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(80.dp)
    ) {
        Text(
            text = "?",
            modifier = Modifier
                .graphicsLayer {
                    scaleX = questionMarkScale
                    scaleY = questionMarkScale
                },
            style = MaterialTheme.typography.displayLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Bold
        )
    }
}
