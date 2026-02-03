package com.rudra.smartworktracker.ui.screens.health

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseInOutCubic
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Coffee
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.Egg
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Grass
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.MonitorWeight
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.RemoveRedEye
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Work
import androidx.compose.material.icons.filled.WorkHistory
import androidx.compose.material.icons.outlined.Computer
import androidx.compose.material.icons.outlined.Straighten
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rudra.smartworktracker.model.HealthMetricType
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.abs

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

    // Premium background with subtle mesh gradient effect
    val backgroundGradient = Brush.verticalGradient(
        colors = listOf(
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f),
            MaterialTheme.colorScheme.surface,
            MaterialTheme.colorScheme.surface
        )
    )

    Scaffold(
        containerColor = Color.Transparent,
        modifier = Modifier.background(backgroundGradient)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Enhanced Header with premium feel
            WorkHealthHeader(
                dailyStreak = healthAnalytics.dailyStreak,
                workHoursToday = dailyWorkRoutine.currentWorkHours,
                nextBreakIn = dailyWorkRoutine.nextBreakInMinutes,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            )

            // Modern Tab Row with better styling
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.Transparent,
                contentColor = MaterialTheme.colorScheme.primary,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = MaterialTheme.colorScheme.primary,
                        height = 3.dp
                    )
                },
                divider = {}
            ) {
                val tabs = listOf("Dashboard", "Log", "Nutrition", "Progress")
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 14.sp
                                )
                            )
                        }
                    )
                }
            }

            AnimatedContent(
                targetState = selectedTab,
                transitionSpec = {
                    if (targetState > initialState) {
                        slideInHorizontally { it } + fadeIn() togetherWith slideOutHorizontally { -it } + fadeOut()
                    } else {
                        slideInHorizontally { -it } + fadeIn() togetherWith slideOutHorizontally { it } + fadeOut()
                    }.using(SizeTransform(clip = false))
                },
                label = "TabTransition"
            ) { targetTab ->
                when (targetTab) {
                    0 -> DashboardTab(
                        healthData = healthData,
                        healthAnalytics = healthAnalytics,
                        goals = goals,
                        dailyRoutine = dailyWorkRoutine,
                        onMetricClick = { viewModel.showMetricInput(it) },
                        onAddBreak = { viewModel.logBreakTime(); showBreakReminder = false },
                        onAddWater = { viewModel.logWaterIntake(250.0); showHydrationPopup = false },
                        onStretch = { viewModel.saveHealthMetric(HealthMetricType.EXERCISE, 5.0, "Quick Stretch") },
                        onEyeExercise = { viewModel.saveHealthMetric(HealthMetricType.BREAKS, 1.0, "Eye Care") },
                        onLogWork = { viewModel.showMetricInput(HealthMetricType.SCREEN_TIME) },
                        onLogSleep = { viewModel.showMetricInput(HealthMetricType.SLEEP) },
                        modifier = Modifier.fillMaxSize()
                    )
                    1 -> DailyLogTab(
                        recentEntries = healthData.recentEntries,
                        workSessionStats = healthData.workSessionStats,
                        onLogCustom = { viewModel.showMetricInput(it) },
                        modifier = Modifier.fillMaxSize()
                    )
                    2 -> NutritionTab(
                        nutritionData = healthData.nutritionData,
                        dailyGoals = healthAnalytics.nutritionGoals,
                        onAddMeal = { viewModel.showMealInput() },
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
        }

        // Overlay Dialogs
        if (showBreakReminder && dailyWorkRoutine.shouldTakeBreak) {
            BreakReminderPopup(
                breakType = dailyWorkRoutine.nextBreakType,
                onTakeBreak = { viewModel.logBreakTime(); showBreakReminder = false },
                onSnooze = { showBreakReminder = false },
                onDismiss = { showBreakReminder = false }
            )
        }

        if (showHydrationPopup) {
            WaterLogPopup(
                onLogWater = { viewModel.logWaterIntake(it); showHydrationPopup = false },
                onDismiss = { showHydrationPopup = false }
            )
        }

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

        if (uiState.showMealDialog) {
            MealInputDialog(
                onSave = { c, p, cr, f ->
                    viewModel.logMeal(c, p, cr, f)
                    viewModel.showMealInput()
                },
                onDismiss = { viewModel.showMealInput() }
            )
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
        modifier = modifier.shadow(8.dp, RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Work Wellness",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    "${LocalTime.now().format(DateTimeFormatter.ofPattern("hh:mm a"))} • Today: ${workHoursToday}h",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                // Break timer with animated ring
                Box(contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(
                        progress = { nextBreakIn / 50f },
                        modifier = Modifier.size(48.dp),
                        color = Color(0xFFFF9800),
                        strokeWidth = 4.dp,
                        trackColor = Color(0xFFFF9800).copy(alpha = 0.1f)
                    )
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "$nextBreakIn",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFFFF9800)
                        )
                        Text("min", style = MaterialTheme.typography.labelSmall, fontSize = 8.sp, color = Color(0xFFFF9800))
                    }
                }

                // Streak badge with glassmorphism feel
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(Color(0xFFFFD700), Color(0xFFFFA500))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("$dailyStreak", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold), color = Color.White)
                        Text("DAYS", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), fontSize = 8.sp, color = Color.White)
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
    onStretch: () -> Unit,
    onEyeExercise: () -> Unit,
    onLogWork: () -> Unit,
    onLogSleep: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(20.dp),
        contentPadding = PaddingValues(16.dp)
    ) {
        // Quick Stats Row
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
                    target = "≥75%",
                    icon = Icons.Outlined.Straighten,
                    color = Color(0xFF7B61FF),
                    trend = dailyRoutine.postureTrend,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Productivity Gauge Section
        item {
            ProductivitySection(
                productivityScore = dailyRoutine.productivityScore,
                focusTime = dailyRoutine.focusHours,
                completedTasks = dailyRoutine.completedTasks,
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Hydration & Nutrition Row
        item {
            Row(
                modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Max),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                HydrationCard(
                    currentWater = dailyRoutine.waterConsumed,
                    targetWater = goals[HealthMetricType.WATER] ?: 3000.0,
                    onAddWater = onAddWater,
                    modifier = Modifier.weight(1f).fillMaxHeight()
                )

                NutritionCard(
                    caloriesConsumed = healthData.nutritionData.calories,
                    caloriesTarget = healthAnalytics.nutritionGoals.caloriesTarget,
                    proteinIntake = healthData.nutritionData.protein,
                    proteinTarget = healthAnalytics.nutritionGoals.proteinTarget,
                    modifier = Modifier.weight(1f).fillMaxHeight()
                )
            }
        }

        // Quick Actions Grid
        item {
            QuickWorkActions(
                onLogBreak = onAddBreak,
                onStretch = onStretch,
                onEyeExercise = onEyeExercise,
                onHydrate = onAddWater,
                onLogWork = onLogWork,
                onLogSleep = onLogSleep,
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Eye Health Detail
        item {
            EyeHealthSection(
                eyeStrainLevel = dailyRoutine.eyeStrainLevel,
                lastEyeBreak = dailyRoutine.lastEyeBreakMinutes,
                recommendedEyeExercises = listOf("20-20-20 Rule", "Eye Rolling", "Blinking"),
                onDoExercise = onEyeExercise,
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Goals Progress
        item {
            DailyGoalsProgress(
                completedGoals = dailyRoutine.completedHealthGoals,
                totalGoals = dailyRoutine.totalHealthGoals,
                goals = listOf(
                    "Drink 2.5L Water",
                    "Take 5 Active Breaks",
                    "30 min Physical Activity",
                    "7-8h Quality Sleep",
                    "Calorie Limit Met"
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
    icon: ImageVector,
    color: Color,
    trend: Float,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.05f)),
        border = BorderStroke(1.dp, color.copy(alpha = 0.1f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(color.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, null, tint = color, modifier = Modifier.size(18.dp))
                }
                if (abs(trend) > 0) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            if (trend > 0) Icons.AutoMirrored.Filled.TrendingUp else Icons.AutoMirrored.Filled.TrendingDown,
                            null,
                            tint = if (trend > 0) Color(0xFF4CAF50) else Color(0xFFF44336),
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            "${abs(trend)}%",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (trend > 0) Color(0xFF4CAF50) else Color(0xFFF44336)
                        )
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
            Text(value, style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold))
            Text(title, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("Goal: $target", style = MaterialTheme.typography.labelSmall, color = color, fontWeight = FontWeight.Bold)
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
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp))
    ) {
        Row(modifier = Modifier.padding(24.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(90.dp)) {
                CircularProgressIndicator(
                    progress = { productivityScore / 100f },
                    modifier = Modifier.size(90.dp),
                    strokeWidth = 10.dp,
                    color = getProductivityColor(productivityScore),
                    trackColor = getProductivityColor(productivityScore).copy(alpha = 0.1f),
                    strokeCap = StrokeCap.Round
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("$productivityScore%", fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.titleLarge)
                    Text("Score", style = MaterialTheme.typography.labelSmall, fontSize = 10.sp)
                }
            }
            Spacer(Modifier.width(24.dp))
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Daily Productivity", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Timer, null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(8.dp))
                    Text("${String.format("%.1f", focusTime)}h Focused Time", style = MaterialTheme.typography.bodySmall)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CheckCircle, null, modifier = Modifier.size(16.dp), tint = Color(0xFF4CAF50))
                    Spacer(Modifier.width(8.dp))
                    Text("$completedTasks Tasks Completed", style = MaterialTheme.typography.bodySmall)
                }
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
    val progress = (currentWater / targetWater).toFloat().coerceIn(0f, 1f)
    Card(
        modifier = modifier.clickable(onClick = onAddWater),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2196F3).copy(alpha = 0.05f)),
        border = BorderStroke(1.dp, Color(0xFF2196F3).copy(alpha = 0.15f))
    ) {
        Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.LocalDrink, null, tint = Color(0xFF2196F3), modifier = Modifier.size(24.dp))
                Surface(color = Color(0xFF2196F3).copy(alpha = 0.1f), shape = CircleShape) {
                    Text("${(progress * 100).toInt()}%", modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall, color = Color(0xFF2196F3), fontWeight = FontWeight.Bold)
                }
            }
            Spacer(Modifier.weight(1f))
            Text("${currentWater.toInt()}", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold, color = Color(0xFF2196F3))
            Text("/ ${targetWater.toInt()} ml", style = MaterialTheme.typography.labelLarge, color = Color(0xFF2196F3).copy(alpha = 0.7f))
            Spacer(Modifier.weight(1f))
            Text("Tap to add 250ml", style = MaterialTheme.typography.labelSmall, color = Color(0xFF2196F3).copy(alpha = 0.5f), fontStyle = FontStyle.Italic)
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
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF4CAF50).copy(alpha = 0.05f)),
        border = BorderStroke(1.dp, Color(0xFF4CAF50).copy(alpha = 0.15f))
    ) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Restaurant, null, tint = Color(0xFF4CAF50), modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text("Nutrition", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = Color(0xFF4CAF50))
            }
            
            NutritionProgressMiniRow("Calories", caloriesConsumed, caloriesTarget, Color(0xFF4CAF50), "kcal")
            NutritionProgressMiniRow("Protein", proteinIntake, proteinTarget, Color(0xFFFF9800), "g")
        }
    }
}

@Composable
fun NutritionProgressMiniRow(label: String, current: Double, target: Double, color: Color, unit: String) {
    Column {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("${current.toInt()}/ ${target.toInt()} $unit", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { (current / target).toFloat().coerceIn(0f, 1f) },
            modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape),
            color = color,
            trackColor = color.copy(alpha = 0.1f)
        )
    }
}

@Composable
fun QuickWorkActions(
    onLogBreak: () -> Unit,
    onStretch: () -> Unit,
    onEyeExercise: () -> Unit,
    onHydrate: () -> Unit,
    onLogWork: () -> Unit,
    onLogSleep: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.shadow(4.dp, RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text("Quick Actions", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(16.dp))
            
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                    ActionTile("Break", Icons.Default.Coffee, Color(0xFFFF9800), onLogBreak, Modifier.weight(1f))
                    ActionTile("Stretch", Icons.Default.FitnessCenter, Color(0xFF4CAF50), onStretch, Modifier.weight(1f))
                    ActionTile("Eye Care", Icons.Default.Visibility, Color(0xFF2196F3), onEyeExercise, Modifier.weight(1f))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                    ActionTile("Water", Icons.Default.LocalDrink, Color(0xFF03A9F4), onHydrate, Modifier.weight(1f))
                    ActionTile("Work", Icons.Default.Work, Color(0xFF795548), onLogWork, Modifier.weight(1f))
                    ActionTile("Sleep", Icons.Default.Bedtime, Color(0xFF673AB7), onLogSleep, Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
fun ActionTile(label: String, icon: ImageVector, color: Color, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier
            .height(85.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = color.copy(alpha = 0.08f),
        border = BorderStroke(1.dp, color.copy(alpha = 0.15f))
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(icon, null, tint = color, modifier = Modifier.size(26.dp))
            Spacer(Modifier.height(8.dp))
            Text(label, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = color)
        }
    }
}

@Composable
fun EyeHealthSection(
    eyeStrainLevel: Int,
    lastEyeBreak: Int,
    recommendedEyeExercises: List<String>,
    onDoExercise: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.05f))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Eye Health Guard", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Surface(
                    color = if (eyeStrainLevel == 1) Color(0xFF4CAF50) else if (eyeStrainLevel == 2) Color(0xFFFF9800) else Color(0xFFF44336),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("STRAIN: LVL $eyeStrainLevel", modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp), style = MaterialTheme.typography.labelSmall, color = Color.White, fontWeight = FontWeight.ExtraBold)
                }
            }
            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.History, null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.width(8.dp))
                Text("Last break: $lastEyeBreak minutes ago", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = onDoExercise,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3))
            ) {
                Icon(Icons.Default.RemoveRedEye, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Start Relief Exercise")
            }
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
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp))
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
                Column {
                    Text("Daily Health Checklist", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("${(completedGoals.toFloat()/totalGoals * 100).toInt()}% Achievement", style = MaterialTheme.typography.labelSmall, color = Color(0xFF4CAF50), fontWeight = FontWeight.Bold)
                }
                Text("$completedGoals/$totalGoals", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold, color = Color(0xFF4CAF50))
            }
            Spacer(Modifier.height(16.dp))
            LinearProgressIndicator(
                progress = { completedGoals.toFloat() / totalGoals },
                modifier = Modifier.fillMaxWidth().height(10.dp).clip(CircleShape),
                color = Color(0xFF4CAF50),
                trackColor = Color(0xFF4CAF50).copy(alpha = 0.1f)
            )
            Spacer(Modifier.height(16.dp))
            goals.forEachIndexed { index, goal ->
                val isDone = index < completedGoals
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 6.dp)) {
                    Icon(
                        if (isDone) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                        null,
                        tint = if (isDone) Color(0xFF4CAF50) else MaterialTheme.colorScheme.outline,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        goal,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isDone) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        textDecoration = if (isDone) TextDecoration.LineThrough else null
                    )
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
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text("Session Insights", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold)
                    Spacer(modifier = Modifier.height(20.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        MetricItem("Screen", "${String.format("%.1f", workSessionStats.totalScreenTime / 60)}h", Icons.Default.Computer, Color(0xFF2196F3))
                        MetricItem("Focus", "${String.format("%.1f", workSessionStats.totalFocusTime / 60)}h", Icons.Default.Timer, Color(0xFF4CAF50))
                        MetricItem("Breaks", "${workSessionStats.totalBreaks}", Icons.Default.Coffee, Color(0xFFFF9800))
                    }
                }
            }
        }

        item {
            Text("Recent Activity", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp))
        }

        items(recentEntries) { entry ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier.size(44.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(getMetricIcon(entry.type), null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(entry.type.displayName, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                        Text(Instant.ofEpochMilli(entry.timestamp).atZone(ZoneId.systemDefault()).toLocalDateTime().format(DateTimeFormatter.ofPattern("MMM d, hh:mm a")), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Text("${entry.value}${entry.type.unit}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}

fun getMetricIcon(type: HealthMetricType): ImageVector = when(type) {
    HealthMetricType.WATER -> Icons.Default.LocalDrink
    HealthMetricType.SLEEP -> Icons.Default.Bedtime
    HealthMetricType.EXERCISE -> Icons.Default.FitnessCenter
    HealthMetricType.CALORIES -> Icons.Default.LocalFireDepartment
    HealthMetricType.SCREEN_TIME -> Icons.Default.Computer
    else -> Icons.Default.History
}

@Composable
fun NutritionTab(
    nutritionData: NutritionData,
    dailyGoals: NutritionGoals,
    onAddMeal: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
        Card(
            modifier = Modifier.fillMaxWidth().shadow(4.dp, RoundedCornerShape(28.dp)),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text("Nutrition Balance", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text("Today's consumption breakdown", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(24.dp))
                
                DetailedNutritionRow("Calories", nutritionData.calories, dailyGoals.caloriesTarget, "kcal", Color(0xFF4CAF50), Icons.Default.LocalFireDepartment)
                DetailedNutritionRow("Protein", nutritionData.protein, dailyGoals.proteinTarget, "g", Color(0xFF2196F3), Icons.Default.Egg)
                DetailedNutritionRow("Fiber", nutritionData.fiber, dailyGoals.fiberTarget, "g", Color(0xFFFF9800), Icons.Default.Grass)
            }
        }
        
        Button(
            onClick = onAddMeal,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(16.dp),
            elevation = ButtonDefaults.buttonElevation(4.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(12.dp))
            Text("Log New Meal Entry", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun DetailedNutritionRow(label: String, current: Double, target: Double, unit: String, color: Color, icon: ImageVector) {
    Column(modifier = Modifier.padding(vertical = 10.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, null, tint = color, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(8.dp))
                Text(label, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
            }
            Text("${current.toInt()} / ${target.toInt()} $unit", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
        }
        Spacer(modifier = Modifier.height(8.dp))
        LinearProgressIndicator(
            progress = { if (target > 0) (current / target).toFloat().coerceIn(0f, 1f) else 0f },
            modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape),
            color = color,
            trackColor = color.copy(alpha = 0.1f)
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
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        item { SectionTitle("Trends & Analysis") }
        item { TrendChartCard("Weight Journey (kg)", weightProgress, Color(0xFFE91E63), Icons.Default.MonitorWeight) }
        item { TrendChartCard("Work Hours (h)", workHoursTrend, Color(0xFF2196F3), Icons.Default.WorkHistory) }
        item { TrendChartCard("Focus Efficiency (min)", productivityTrend, Color(0xFF4CAF50), Icons.Default.Psychology) }
        item { TrendChartCard("Sleep Consistency (h)", sleepPattern, Color(0xFF673AB7), Icons.Default.Bedtime) }
    }
}

@Composable
fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.ExtraBold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}

@Composable
fun TrendChartCard(title: String, data: List<Pair<LocalDate, Double>>, color: Color, icon: ImageVector) {
    Card(
        modifier = Modifier.fillMaxWidth().shadow(2.dp, RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, null, tint = color, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(12.dp))
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(24.dp))
            if (data.size >= 2) {
                AnimatedLineChart(dataPoints = data, color = color, modifier = Modifier.fillMaxWidth().height(180.dp))
            } else {
                Box(modifier = Modifier.fillMaxWidth().height(180.dp), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Analytics, null, tint = Color.Gray.copy(alpha = 0.3f), modifier = Modifier.size(48.dp))
                        Text("Not enough data to plot trend", color = Color.Gray, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}

@Composable
fun getProductivityColor(score: Int): Color = when {
    score >= 80 -> Color(0xFF4CAF50)
    score >= 60 -> Color(0xFFFF9800)
    else -> Color(0xFFF44336)
}

@Composable
fun getEyeStrainColor(level: Int): Color = when (level) {
    1 -> Color(0xFF4CAF50)
    2 -> Color(0xFFFF9800)
    3 -> Color(0xFFF44336)
    else -> MaterialTheme.colorScheme.outline
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
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier.padding(16.dp).fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(getMetricIcon(metricType), null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(40.dp))
                Spacer(Modifier.height(16.dp))
                Text(
                    text = "Log ${metricType.displayName}",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(24.dp))
                OutlinedTextField(
                    value = value,
                    onValueChange = { value = it },
                    label = { Text("Enter ${metricType.displayName} (${metricType.unit})") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(32.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TextButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                        Text("Cancel")
                    }
                    Button(
                        onClick = { value.toDoubleOrNull()?.let { onSave(it) } },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Save")
                    }
                }
            }
        }
    }
}

@Composable
fun MealInputDialog(
    onSave: (Double, Double, Double, Double) -> Unit,
    onDismiss: () -> Unit
) {
    var calories by remember { mutableStateOf("") }
    var protein by remember { mutableStateOf("") }
    var carbs by remember { mutableStateOf("") }
    var fiber by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier.padding(16.dp).fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(Icons.Default.Restaurant, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(40.dp))
                Spacer(Modifier.height(16.dp))
                Text(
                    text = "Log Meal",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(24.dp))

                OutlinedTextField(
                    value = calories,
                    onValueChange = { calories = it },
                    label = { Text("Calories (kcal)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = protein,
                    onValueChange = { protein = it },
                    label = { Text("Protein (g)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = carbs,
                    onValueChange = { carbs = it },
                    label = { Text("Carbs (g)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = fiber,
                    onValueChange = { fiber = it },
                    label = { Text("Fiber (g)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(32.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TextButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                        Text("Cancel")
                    }
                    Button(
                        onClick = {
                            onSave(
                                calories.toDoubleOrNull() ?: 0.0,
                                protein.toDoubleOrNull() ?: 0.0,
                                carbs.toDoubleOrNull() ?: 0.0,
                                fiber.toDoubleOrNull() ?: 0.0
                            )
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
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
    color: Color,
    modifier: Modifier = Modifier
) {
    val animatable = remember { Animatable(0f) }

    LaunchedEffect(dataPoints) {
        animatable.animateTo(1f, animationSpec = tween(durationMillis = 1500, easing = EaseInOutCubic))
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
            color = color,
            style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round),
            alpha = animatable.value
        )
        
        // Gradient fill below the line
        val fillPath = path.apply {
            lineTo(size.width, size.height)
            lineTo(0f, size.height)
            close()
        }
        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(color.copy(alpha = 0.3f * animatable.value), Color.Transparent)
            )
        )
    }
}

@Composable
fun WaterLogPopup(
    onLogWater: (Double) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(modifier = Modifier.fillMaxWidth().padding(16.dp), shape = RoundedCornerShape(28.dp)) {
            Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.LocalDrink, null, tint = Color(0xFF2196F3), modifier = Modifier.size(48.dp))
                Spacer(Modifier.height(16.dp))
                Text("Stay Hydrated!", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Text("Select your serving size", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                Spacer(modifier = Modifier.height(32.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    WaterLogTile(250.0, "Glass", Icons.Default.LocalDrink, Modifier.weight(1f), onLogWater)
                    WaterLogTile(500.0, "Bottle", Icons.Default.LocalDrink, Modifier.weight(1f), onLogWater)
                }
                Spacer(modifier = Modifier.height(24.dp))
                TextButton(onClick = onDismiss) { Text("Dismiss", color = Color.Gray) }
            }
        }
    }
}

@Composable
fun WaterLogTile(amount: Double, label: String, icon: ImageVector, modifier: Modifier, onLogWater: (Double) -> Unit) {
    Surface(
        modifier = modifier.clickable { onLogWater(amount) },
        shape = RoundedCornerShape(20.dp),
        color = Color(0xFF2196F3).copy(alpha = 0.1f),
        border = BorderStroke(1.dp, Color(0xFF2196F3).copy(alpha = 0.2f))
    ) {
        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, null, tint = Color(0xFF2196F3), modifier = Modifier.size(24.dp))
            Spacer(Modifier.height(8.dp))
            Text("${amount.toInt()}ml", fontWeight = FontWeight.ExtraBold, color = Color(0xFF2196F3))
            Text(label, style = MaterialTheme.typography.labelSmall, color = Color(0xFF2196F3))
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
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier.size(64.dp).clip(CircleShape).background(Color(0xFFFF9800).copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.Timer, null, tint = Color(0xFFFF9800), modifier = Modifier.size(32.dp))
                }
                Spacer(modifier = Modifier.height(24.dp))
                Text("Time for a $breakType!", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold, textAlign = TextAlign.Center)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Refresh your mind and body to maintain peak focus.", style = MaterialTheme.typography.bodyMedium, color = Color.Gray, textAlign = TextAlign.Center)
                Spacer(modifier = Modifier.height(32.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(onClick = onSnooze, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp)) {
                        Text("Snooze")
                    }
                    Button(onClick = onTakeBreak, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))) {
                        Text("Take Break")
                    }
                }
            }
        }
    }
}

@Composable
fun MetricItem(
    label: String,
    value: String,
    icon: ImageVector,
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
