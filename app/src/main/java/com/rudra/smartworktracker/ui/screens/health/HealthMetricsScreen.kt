package com.rudra.smartworktracker.ui.screens.health

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.*
import androidx.compose.ui.text.input.*
import androidx.compose.ui.text.style.*
import androidx.compose.ui.unit.*
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rudra.smartworktracker.model.HealthMetricType
import kotlinx.coroutines.delay
import java.time.*
import java.time.format.*
import java.util.*
import kotlin.math.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HealthMetricsScreen(viewModel: HealthMetricsViewModel = viewModel()) {
    val healthData by viewModel.healthData.collectAsState()
    val healthAnalytics by viewModel.healthAnalytics.collectAsState()
    val goals by viewModel.goals.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val dailyWorkRoutine by viewModel.dailyWorkRoutine.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) }
    var showBreakReminder by remember { mutableStateOf(false) }
    var showHydrationPopup by remember { mutableStateOf(false) }

    // Premium background with gradient
    val backgroundGradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF667EEA).copy(alpha = 0.1f),
            Color(0xFF764BA2).copy(alpha = 0.05f),
            MaterialTheme.colorScheme.surface
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundGradient)
    ) {
        // Main content with tabs
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(4.dp)
        ) {
            // Enhanced Header with Work Hours
            WorkHealthHeader(
                dailyStreak = healthAnalytics.dailyStreak,
                workHoursToday = dailyWorkRoutine.currentWorkHours,
                nextBreakIn = dailyWorkRoutine.nextBreakInMinutes,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )

            // Tab Row
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.Transparent,
                contentColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier.fillMaxWidth()
            ) {
                listOf("Dashboard", "Daily Log", "Nutrition", "Progress").forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Medium
                                )
                            )
                        }
                    )
                }
            }

            when (selectedTab) {
                0 -> DashboardTab(
                    healthData = healthData,
                    healthAnalytics = healthAnalytics,
                    goals = goals,
                    dailyRoutine = dailyWorkRoutine,
                    onMetricClick = { type ->
                        viewModel.showMetricInput(type)
                    },
                    onAddBreak = {
                        viewModel.logBreakTime()
                        showBreakReminder = false
                    },
                    onAddWater = {
                        viewModel.logWaterIntake(250.0) // Standard glass size
                        showHydrationPopup = false
                    },
                    modifier = Modifier.fillMaxSize()
                )

                1 -> DailyLogTab(
                    recentEntries = healthData.recentEntries,
                    workSessionStats = healthData.workSessionStats,
                    onLogCustom = { type ->
                        viewModel.showMetricInput(type)
                    },
                    modifier = Modifier.fillMaxSize()
                )

                2 -> NutritionTab(
                    nutritionData = healthData.nutritionData,
                    dailyGoals = healthAnalytics.nutritionGoals,
                    onAddMeal = {
                        viewModel.logMeal()
                    },
                    modifier = Modifier.fillMaxSize()
                )

                3 -> ProgressTab(
                    weightProgress = healthData.weightProgress,
                    productivityTrend = healthData.productivityTrend,
                    sleepPattern = healthData.sleepPattern,
                    workHoursTrend = healthData.workHoursTrend,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        // Break Reminder Popup
        if (showBreakReminder && dailyWorkRoutine.shouldTakeBreak) {
            BreakReminderPopup(
                breakType = dailyWorkRoutine.nextBreakType,
                onTakeBreak = {
                    viewModel.logBreakTime()
                    showBreakReminder = false
                },
                onSnooze = {
                    showBreakReminder = false
                    // Snooze for 10 minutes
                },
                onDismiss = { showBreakReminder = false }
            )
        }

        // Quick Water Log Popup
        if (showHydrationPopup) {
            WaterLogPopup(
                onLogWater = { amount ->
                    viewModel.logWaterIntake(amount)
                    showHydrationPopup = false
                },
                onDismiss = { showHydrationPopup = false }
            )
        }

        // Input Dialog
        uiState.selectedMetric?.let { metricType ->
            if (uiState.showInputDialog) {
                HealthInputDialog(
                    metricType = metricType,
                    onSave = { value ->
                        viewModel.saveHealthMetric(metricType, value)
                        viewModel.showMetricInput(metricType)
                    },
                    onDismiss = { viewModel.showMetricInput(metricType) }
                )
            }
        }
    }
}

@Composable
fun WorkHealthHeader(
    dailyStreak: Int,
    workHoursToday: Int,
    nextBreakIn: Int,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .shadow(
                elevation = 16.dp,
                shape = RoundedCornerShape(24.dp),
                spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                clip = true
            ),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(8.dp)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    "Work Wellness",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    "${LocalTime.now().format(DateTimeFormatter.ofPattern("hh:mm a"))} • Today: ${workHoursToday}h",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Break Timer
                if (nextBreakIn > 0) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFF9800).copy(alpha = 0.1f))
                            .border(
                                width = 2.dp,
                                color = Color(0xFFFF9800),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                "${nextBreakIn}",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = Color(0xFFFF9800)
                            )
                            Text(
                                "min",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFFFF9800)
                            )
                        }
                    }
                }

                // Streak Badge
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    Color(0xFFFFD700), // Gold
                                    Color(0xFFFFA500)  // Orange
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "$dailyStreak",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = Color.White
                        )
                        Text(
                            "Days",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DashboardTab(
    healthData: HealthData,
    healthAnalytics: HealthAnalytics,
    goals: Map<HealthMetricType, Double>,
    dailyRoutine: DailyWorkRoutine,
    onMetricClick: (HealthMetricType) -> Unit,
    onAddBreak: () -> Unit,
    onAddWater: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
    ) {
        // Real-time Health Stats
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                HealthStatCard(
                    title = "Screen Time",
                    value = "${String.format(Locale.getDefault(), "%.1f", dailyRoutine.screenTimeHours)}h",
                    target = "≤8h",
                    icon = Icons.Outlined.Computer,
                    color = Color(0xFF4A90E2),
                    trend = dailyRoutine.screenTimeTrend,
                    modifier = Modifier.weight(1f)
                )

                HealthStatCard(
                    title = "Posture",
                    value = "${dailyRoutine.goodPostureTime}%",
                    target = "≥70%",
                    icon = Icons.Outlined.Straighten,
                    color = Color(0xFF7B61FF),
                    trend = dailyRoutine.postureTrend,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Productivity Section
        item {
            ProductivitySection(
                productivityScore = healthAnalytics.productivityScore,
                focusTime = dailyRoutine.focusHours,
                completedTasks = dailyRoutine.completedTasks,
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Hydration & Nutrition
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                HydrationCard(
                    currentWater = healthData.currentValues[HealthMetricType.WATER] ?: 0.0,
                    targetWater = goals[HealthMetricType.WATER] ?: 2500.0,
                    onAddWater = onAddWater,
                    modifier = Modifier.weight(1f)
                )

                NutritionCard(
                    caloriesConsumed = healthData.nutritionData.calories,
                    caloriesTarget = healthAnalytics.nutritionGoals.caloriesTarget,
                    proteinIntake = healthData.nutritionData.protein,
                    proteinTarget = healthAnalytics.nutritionGoals.proteinTarget,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Eye & Posture Health
        item {
            EyeHealthSection(
                eyeStrainLevel = healthAnalytics.eyeStrainLevel,
                lastEyeBreak = dailyRoutine.lastEyeBreakMinutes,
                recommendedEyeExercises = listOf("20-20-20 Rule", "Palming", "Eye Rolling"),
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Quick Actions
        item {
            QuickWorkActions(
                onLogBreak = onAddBreak,
                onStretch = { /* Start stretching routine */ },
                onEyeExercise = { /* Start eye exercises */ },
                onHydrate = onAddWater,
                onLogMeal = { /* Log meal */ },
                onLogWork = { /* Log work session */ },
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Daily Goals Progress
        item {
            DailyGoalsProgress(
                completedGoals = dailyRoutine.completedHealthGoals,
                totalGoals = dailyRoutine.totalHealthGoals,
                goals = listOf(
                    "Drink 8 glasses of water",
                    "Take 5 breaks",
                    "30 min exercise",
                    "7-8 hours sleep",
                    "Balanced meals"
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun HealthStatCard(
    title: String,
    value: String,
    target: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    trend: Float,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(20.dp),
                spotColor = color.copy(alpha = 0.2f),
                clip = true
            ),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(4.dp)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        icon,
                        contentDescription = title,
                        tint = color,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        title,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = color
                    )
                }

                // Trend indicator
                if (abs(trend) > 0.1f) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            if (trend < 0) Icons.AutoMirrored.Filled.TrendingDown
                            else Icons.AutoMirrored.Filled.TrendingUp,
                            contentDescription = "Trend",
                            tint = if (trend < 0) Color(0xFF4CAF50) else Color(0xFFF44336),
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            "${String.format(Locale.getDefault(), "%.1f", abs(trend))}%",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (trend < 0) Color(0xFF4CAF50) else Color(0xFFF44336)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                value,
                style = MaterialTheme.typography.displaySmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 28.sp
                ),
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                "Target: $target",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun ProductivitySection(
    productivityScore: Int,
    focusTime: Double,
    completedTasks: Int,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .shadow(
                elevation = 12.dp,
                shape = RoundedCornerShape(24.dp),
                clip = true
            ),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(4.dp)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Productivity",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(getProductivityColor(productivityScore).copy(alpha = 0.1f))
                        .border(
                            width = 2.dp,
                            color = getProductivityColor(productivityScore),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "$productivityScore%",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = getProductivityColor(productivityScore)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                MetricItem(
                    label = "Focus Time",
                    value = String.format(Locale.getDefault(), "%.1fh", focusTime),
                    icon = Icons.Outlined.Timer,
                    color = Color(0xFF4A90E2),
                    modifier = Modifier.weight(1f)
                )

                MetricItem(
                    label = "Tasks Done",
                    value = "$completedTasks",
                    icon = Icons.Outlined.CheckCircle,
                    color = Color(0xFF4CAF50),
                    modifier = Modifier.weight(1f)
                )

                MetricItem(
                    label = "Break Time",
                    value = "45m",
                    icon = Icons.Outlined.Coffee,
                    color = Color(0xFFFF9800),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun HydrationCard(
    currentWater: Double,
    targetWater: Double,
    onAddWater: () -> Unit,
    modifier: Modifier = Modifier
) {
    val progress = if (targetWater > 0) (currentWater / targetWater).toFloat().coerceIn(0f, 1f) else 0f
    val animatedProgress = remember { Animatable(0f) }

    LaunchedEffect(progress) {
        animatedProgress.animateTo(
            targetValue = progress,
            animationSpec = tween(durationMillis = 800)
        )
    }

    Card(
        modifier = modifier
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(20.dp),
                clip = true
            )
            .clickable(onClick = onAddWater),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF2196F3).copy(alpha = 0.08f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Outlined.LocalDrink,
                        contentDescription = "Hydration",
                        tint = Color(0xFF2196F3),
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        "Hydration",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = Color(0xFF2196F3)
                    )
                }

                Text(
                    "${(progress * 100).toInt()}%",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = Color(0xFF2196F3)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Water bottle visualization
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF2196F3).copy(alpha = 0.1f))
            ) {
                // Water level
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(animatedProgress.value)
                        .background(Color(0xFF2196F3).copy(alpha = 0.3f))
                        .align(Alignment.BottomStart)
                )

                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "${String.format(Locale.getDefault(), "%.0f", currentWater)}/${String.format(Locale.getDefault(), "%.0f", targetWater)}",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = Color(0xFF2196F3)
                    )
                    Text(
                        "ml",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF2196F3).copy(alpha = 0.7f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                "Click to add 250ml",
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFF2196F3).copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun NutritionCard(
    caloriesConsumed: Double,
    caloriesTarget: Double,
    proteinIntake: Double,
    proteinTarget: Double,
    modifier: Modifier = Modifier
) {
    val caloriesProgress = if (caloriesTarget > 0) (caloriesConsumed / caloriesTarget).toFloat().coerceIn(0f, 1f) else 0f
    val proteinProgress = if (proteinTarget > 0) (proteinIntake / proteinTarget).toFloat().coerceIn(0f, 1f) else 0f

    Card(
        modifier = modifier
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(20.dp),
                clip = true
            ),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF4CAF50).copy(alpha = 0.08f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Outlined.Restaurant,
                        contentDescription = "Nutrition",
                        tint = Color(0xFF4CAF50),
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        "Nutrition",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = Color(0xFF4CAF50)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Calories Progress
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "Calories",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        "${String.format(Locale.getDefault(), "%.0f", caloriesConsumed)}",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = Color(0xFF4CAF50)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { caloriesProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = Color(0xFF4CAF50),
                    trackColor = Color(0xFF4CAF50).copy(alpha = 0.2f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Protein Progress
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "Protein",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        "${String.format(Locale.getDefault(), "%.1f", proteinIntake)}g",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = Color(0xFFFF9800)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { proteinProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = Color(0xFFFF9800),
                    trackColor = Color(0xFFFF9800).copy(alpha = 0.2f)
                )
            }
        }
    }
}

@Composable
fun EyeHealthSection(
    eyeStrainLevel: Int,
    lastEyeBreak: Int,
    recommendedEyeExercises: List<String>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .shadow(
                elevation = 12.dp,
                shape = RoundedCornerShape(24.dp),
                clip = true
            ),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(4.dp)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Eye Health",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(getEyeStrainColor(eyeStrainLevel).copy(alpha = 0.1f))
                        .border(
                            width = 2.dp,
                            color = getEyeStrainColor(eyeStrainLevel),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Lvl $eyeStrainLevel",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = getEyeStrainColor(eyeStrainLevel)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "Last Eye Break",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        if (lastEyeBreak >= 60) "${lastEyeBreak / 60}h ago"
                        else "$lastEyeBreak min ago",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = if (lastEyeBreak > 30) Color(0xFFF44336) else Color(0xFF4CAF50)
                    )
                }

                Button(
                    onClick = { /* Start eye exercise */ },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF2196F3)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Do Exercise")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                "Recommended Exercises:",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            recommendedEyeExercises.forEach { exercise ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Outlined.RemoveRedEye,
                        contentDescription = null,
                        tint = Color(0xFF2196F3),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        exercise,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
fun QuickWorkActions(
    onLogBreak: () -> Unit,
    onStretch: () -> Unit,
    onEyeExercise: () -> Unit,
    onHydrate: () -> Unit,
    onLogMeal: () -> Unit,
    onLogWork: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(24.dp),
                clip = true
            ),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(4.dp)
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "Quick Actions",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 4.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                QuickActionButton(
                    title = "Take Break",
                    icon = Icons.Outlined.Coffee,
                    color = Color(0xFFFF9800),
                    onClick = onLogBreak,
                    modifier = Modifier.weight(1f)
                )
                QuickActionButton(
                    title = "Stretch",
                    icon = Icons.Outlined.FitnessCenter,
                    color = Color(0xFF4CAF50),
                    onClick = onStretch,
                    modifier = Modifier.weight(1f)
                )
                QuickActionButton(
                    title = "Eye Care",
                    icon = Icons.Outlined.Visibility,
                    color = Color(0xFF2196F3),
                    onClick = onEyeExercise,
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                QuickActionButton(
                    title = "Drink Water",
                    icon = Icons.Outlined.LocalDrink,
                    color = Color(0xFF03A9F4),
                    onClick = onHydrate,
                    modifier = Modifier.weight(1f)
                )
                QuickActionButton(
                    title = "Log Meal",
                    icon = Icons.Outlined.Restaurant,
                    color = Color(0xFF9C27B0),
                    onClick = onLogMeal,
                    modifier = Modifier.weight(1f)
                )
                QuickActionButton(
                    title = "Log Work",
                    icon = Icons.Outlined.Work,
                    color = Color(0xFF795548),
                    onClick = onLogWork,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun QuickActionButton(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .height(80.dp)
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(16.dp),
                spotColor = color.copy(alpha = 0.3f),
                clip = true
            )
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.08f)
        )
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                icon,
                contentDescription = title,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                title,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Medium
                ),
                color = color,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun DailyGoalsProgress(
    completedGoals: Int,
    totalGoals: Int,
    goals: List<String>,
    modifier: Modifier = Modifier
) {
    val progress = if (totalGoals > 0) completedGoals.toFloat() / totalGoals else 0f

    Card(
        modifier = modifier
            .shadow(
                elevation = 12.dp,
                shape = RoundedCornerShape(24.dp),
                clip = true
            ),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(4.dp)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Daily Health Goals",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    "$completedGoals/$totalGoals",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = Color(0xFF4CAF50)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp)
                    .clip(RoundedCornerShape(6.dp)),
                color = Color(0xFF4CAF50),
                trackColor = Color(0xFF4CAF50).copy(alpha = 0.2f)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Column {
                goals.forEachIndexed { index, goal ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            if (index < completedGoals) Icons.Filled.CheckCircle else Icons.Outlined.RadioButtonUnchecked,
                            contentDescription = null,
                            tint = if (index < completedGoals) Color(0xFF4CAF50) else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            goal,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DailyLogTab(
    recentEntries: List<HealthMetricEntry>,
    workSessionStats: WorkSessionStats,
    onLogCustom: (HealthMetricType) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("Today's Work Sessions", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        MetricItem("Screen Time", "${String.format(Locale.getDefault(), "%.1f", workSessionStats.totalScreenTime / 60)}h", Icons.Default.Computer, Color(0xFF2196F3))
                        MetricItem("Focus", "${String.format(Locale.getDefault(), "%.1f", workSessionStats.totalFocusTime / 60)}h", Icons.Default.Timer, Color(0xFF4CAF50))
                        MetricItem("Breaks", "${workSessionStats.totalBreaks}", Icons.Default.Coffee, Color(0xFFFF9800))
                    }
                }
            }
        }

        items(recentEntries) { entry ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier.size(40.dp).clip(CircleShape).background(Color.Gray.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.History, contentDescription = null, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(entry.type.displayName, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                        Text(Instant.ofEpochMilli(entry.timestamp).atZone(ZoneId.systemDefault()).toLocalDateTime().format(DateTimeFormatter.ofPattern("hh:mm a")), style = MaterialTheme.typography.bodySmall)
                    }
                    Text("${entry.value} ${entry.type.unit}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun NutritionTab(
    nutritionData: NutritionData,
    dailyGoals: NutritionGoals,
    onAddMeal: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp)) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("Daily Nutrition Progress", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))
                
                NutritionProgressRow("Calories", nutritionData.calories, dailyGoals.caloriesTarget, "kcal", Color(0xFF4CAF50))
                NutritionProgressRow("Protein", nutritionData.protein, dailyGoals.proteinTarget, "g", Color(0xFF2196F3))
                NutritionProgressRow("Fiber", nutritionData.fiber, dailyGoals.fiberTarget, "g", Color(0xFFFF9800))
            }
        }
        
        Button(onClick = onAddMeal, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Log Meal")
        }
    }
}

@Composable
fun NutritionProgressRow(label: String, current: Double, target: Double, unit: String, color: Color) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, style = MaterialTheme.typography.bodyMedium)
            Text("${current.toInt()}/${target.toInt()} $unit", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { if (target > 0) (current / target).toFloat().coerceIn(0f, 1f) else 0f },
            modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape),
            color = color,
            trackColor = color.copy(alpha = 0.2f)
        )
    }
}

@Composable
fun ProgressTab(
    weightProgress: List<Pair<LocalDate, Double>>,
    productivityTrend: List<Pair<LocalDate, Double>>,
    sleepPattern: List<Pair<LocalDate, Double>>,
    workHoursTrend: List<Pair<LocalDate, Double>>,
    modifier: Modifier = Modifier
) {
    LazyColumn(modifier = modifier, contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item { ProgressChartCard("Weight Progress (kg)", weightProgress) }
        item { ProgressChartCard("Work Hours (h)", workHoursTrend) }
        item { ProgressChartCard("Productivity (focus min)", productivityTrend) }
        item { ProgressChartCard("Sleep Pattern (h)", sleepPattern) }
    }
}

@Composable
fun ProgressChartCard(title: String, data: List<Pair<LocalDate, Double>>) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp)) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
            if (data.size >= 2) {
                AnimatedLineChart(dataPoints = data, modifier = Modifier.fillMaxWidth().height(150.dp))
            } else {
                Box(modifier = Modifier.fillMaxWidth().height(150.dp), contentAlignment = Alignment.Center) {
                    Text("Insufficient data for chart", color = Color.Gray)
                }
            }
        }
    }
}

@Composable
fun BreakReminderPopup(
    breakType: String,
    onTakeBreak: () -> Unit,
    onSnooze: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(24.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    Icons.Filled.Timer,
                    contentDescription = "Break Time",
                    tint = Color(0xFFFF9800),
                    modifier = Modifier.size(48.dp)
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    "Time for a $breakType!",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    "Your eyes and body need a break from screen time.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = onSnooze,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Snooze")
                    }
                    
                    Button(
                        onClick = onTakeBreak,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF4CAF50)
                        )
                    ) {
                        Text("Take Break")
                    }
                }
            }
        }
    }
}

@Composable
fun WaterLogPopup(
    onLogWater: (Double) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(modifier = Modifier.fillMaxWidth().padding(16.dp), shape = RoundedCornerShape(24.dp)) {
            Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Log Water Intake", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(24.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                    WaterButton(250.0, "Glass", onLogWater)
                    WaterButton(500.0, "Bottle", onLogWater)
                }
                Spacer(modifier = Modifier.height(16.dp))
                TextButton(onClick = onDismiss) { Text("Cancel") }
            }
        }
    }
}

@Composable
fun WaterButton(amount: Double, label: String, onLogWater: (Double) -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { onLogWater(amount) }) {
        Icon(Icons.Default.LocalDrink, contentDescription = null, modifier = Modifier.size(48.dp), tint = Color(0xFF2196F3))
        Text("${amount.toInt()}ml", fontWeight = FontWeight.Bold)
        Text(label, style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
fun getProductivityColor(score: Int): Color {
    return when {
        score >= 80 -> Color(0xFF4CAF50)  // Green
        score >= 60 -> Color(0xFFFF9800)  // Orange
        else -> Color(0xFFF44336)         // Red
    }
}

@Composable
fun getEyeStrainColor(level: Int): Color {
    return when (level) {
        1 -> Color(0xFF4CAF50)  // Green - Low
        2 -> Color(0xFFFF9800)  // Orange - Medium
        3 -> Color(0xFFF44336)  // Red - High
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
}

@Composable
fun MetricItem(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            icon,
            contentDescription = label,
            tint = color,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            value,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold
            ),
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun HealthInputDialog(
    metricType: HealthMetricType,
    onSave: (Double) -> Unit,
    onDismiss: () -> Unit
) {
    var value by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Log ${metricType.displayName}",
                    style = MaterialTheme.typography.headlineSmall
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = value,
                    onValueChange = { value = it },
                    label = { Text("${metricType.displayName} (${metricType.unit})") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = {
                        value.toDoubleOrNull()?.let {
                            onSave(it)
                        }
                    }) {
                        Text("Save")
                    }
                }
            }
        }
    }
}

@Composable
fun AnimatedLineChart(
    dataPoints: List<Pair<LocalDate, Double>>,
    modifier: Modifier = Modifier
) {
    val animatable = remember { Animatable(0f) }

    LaunchedEffect(dataPoints) {
        animatable.animateTo(1f, animationSpec = tween(durationMillis = 1000))
    }

    Canvas(modifier = modifier) {
        if (dataPoints.isEmpty()) return@Canvas
        val path = Path()
        val xMin = dataPoints.first().first.toEpochDay().toFloat()
        val xMax = dataPoints.last().first.toEpochDay().toFloat()
        val yMin = dataPoints.minOf { it.second }.toFloat()
        val yMax = dataPoints.maxOf { it.second }.toFloat()
        val xRange = if (xMax == xMin) 1f else xMax - xMin
        val yRange = if (yMax == yMin) 1f else yMax - yMin

        dataPoints.forEachIndexed { index, pair ->
            val x = (pair.first.toEpochDay().toFloat() - xMin) / xRange * size.width
            val y = (1 - (pair.second.toFloat() - yMin) / yRange) * size.height
            if (index == 0) {
                path.moveTo(x, y)
            } else {
                path.lineTo(x, y)
            }
        }

        drawPath(
            path = path,
            Color(0xFF2196F3),
            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
        )
    }
}

@Composable
fun GoalSettingDialog(
    currentGoals: Map<HealthMetricType, Double>,
    onGoalUpdate: (HealthMetricType, Double) -> Unit,
    onDismiss: () -> Unit
) {
    val healthMetricTypes = listOf(HealthMetricType.WEIGHT, HealthMetricType.WATER, HealthMetricType.SLEEP)

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = "Set Your Goals", style = MaterialTheme.typography.headlineSmall)
                Spacer(modifier = Modifier.height(16.dp))

                healthMetricTypes.forEach { metricType ->
                    var text by remember(metricType, currentGoals) {
                        mutableStateOf(currentGoals[metricType]?.toString() ?: "")
                    }
                    OutlinedTextField(
                        value = text,
                        onValueChange = {
                            text = it
                            it.toDoubleOrNull()?.let { value ->
                                onGoalUpdate(metricType, value)
                            }
                        },
                        label = { Text("Goal for ${metricType.displayName} (${metricType.unit})") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = onDismiss) {
                    Text("Done")
                }
            }
        }
    }
}
