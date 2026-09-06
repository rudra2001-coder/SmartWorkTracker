package com.rudra.smartworktracker.ui.screens.analytics

import android.app.Application
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rudra.smartworktracker.model.*
import com.rudra.smartworktracker.ui.components.LoadingShimmer
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(onNavigateBack: () -> Unit) {
    val application = LocalContext.current.applicationContext as Application
    val viewModel: AnalyticsViewModel = viewModel(factory = AnalyticsViewModelFactory(application))
    val analyticsData by viewModel.analyticsData.collectAsState()
    val selectedPeriod by viewModel.selectedPeriod.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    val backgroundGradient = Brush.verticalGradient(
        colors = listOf(
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
            MaterialTheme.colorScheme.surface
        )
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Analytics") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (isLoading) {
            LoadingShimmer(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            )
        } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundGradient)
                .padding(padding),
            contentPadding = PaddingValues(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item {
                AnimatedSummaryGrid(analyticsData)
            }

            item {
                EnhancedBalanceDonutCard(
                    score = analyticsData.workLifeBalanceScore,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            item {
                AnalyticsSectionHeader(
                    title = "Financial Health",
                    icon = Icons.Default.AccountBalanceWallet,
                    subtitle = "Income vs Expenses Analysis"
                )
                EnhancedFinancialChart(
                    incomes = analyticsData.incomes,
                    expenses = analyticsData.expenses,
                    savings = analyticsData.totalSavings,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            item {
                MonthlyIncomeExpenseChart(
                    monthlyData = analyticsData.monthlyFinancialData,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    EnhancedProductivityCard(
                        score = analyticsData.productivityScore,
                        trend = analyticsData.productivityTrend,
                        modifier = Modifier.weight(1f)
                    )
                    EnhancedHabitCard(
                        habits = analyticsData.habits,
                        completionRate = analyticsData.habitCompletionRate,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                AnalyticsSectionHeader(
                    title = "Wellness Metrics",
                    icon = Icons.Default.HealthAndSafety,
                    subtitle = "Daily health tracking"
                )
                EnhancedHealthMetricsGrid(
                    data = analyticsData,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            item {
                AnalyticsSectionHeader(
                    title = "Focus Analysis",
                    icon = Icons.Default.Timer,
                    subtitle = "Deep work vs Pomodoro sessions"
                )
                EnhancedFocusChart(
                    focusSessions = analyticsData.focusSessions,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            item {
                AnalyticsSectionHeader(
                    title = "Recent Achievements",
                    icon = Icons.Default.EmojiEvents,
                    subtitle = "Celebrate your wins"
                )
                AchievementsCarousel(
                    achievements = analyticsData.recentAchievements,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            item {
                WeeklyPerformanceChart(
                    weeklyData = analyticsData.weeklyPerformance,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }
        }
    }
}

@Composable
fun PeriodSelector(
    selectedPeriod: AnalyticsPeriod,
    onPeriodSelected: (AnalyticsPeriod) -> Unit
) {
    Row(
        modifier = Modifier
            .padding(horizontal = 8.dp)
            .background(
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                RoundedCornerShape(24.dp)
            )
            .padding(4.dp)
    ) {
        AnalyticsPeriod.values().forEach { period ->
            FilterChip(
                selected = selectedPeriod == period,
                onClick = { onPeriodSelected(period) },
                label = { Text(period.displayName, style = MaterialTheme.typography.labelSmall) },
                modifier = Modifier.padding(horizontal = 4.dp),
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    }
}

@Composable
fun AnalyticsSectionHeader(title: String, icon: ImageVector, subtitle: String? = null) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                null,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        if (subtitle != null) {
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 32.dp)
            )
        }
    }
}

@Composable
fun EnhancedProductivityCard(score: Int, trend: Float, modifier: Modifier = Modifier) {
    val animatedScore by animateFloatAsState(
        targetValue = score.toFloat(),
        animationSpec = tween(1000, easing = FastOutSlowInEasing),
        label = "animatedScore"
    )

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(2.dp)
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
                Text(
                    "Productivity",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium
                )
                if (trend != 0f) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            if (trend > 0) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                            null,
                            modifier = Modifier.size(14.dp),
                            tint = if (trend > 0) Color(0xFF4CAF50) else Color(0xFFF44336)
                        )
                        Text(
                            "${if (trend > 0) "+" else ""}${trend.toInt()}%",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (trend > 0) Color(0xFF4CAF50) else Color(0xFFF44336)
                        )
                    }
                }
            }

            Text(
                "$animatedScore",
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(score / 100f)
                        .clip(RoundedCornerShape(4.dp))
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primary,
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                                )
                            )
                        )
                )
            }

            Text(
                when {
                    score >= 80 -> "Excellent! You're crushing it!"
                    score >= 60 -> "Good work! Keep pushing!"
                    score >= 40 -> "Decent effort. Room for improvement."
                    else -> "Let's focus on building better habits."
                },
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

@Composable
fun EnhancedHabitCard(habits: List<Habit>, completionRate: Float, modifier: Modifier = Modifier) {
    val animatedRate by animateFloatAsState(
        targetValue = completionRate,
        animationSpec = tween(1000, easing = FastOutSlowInEasing),
        label = "animatedRate"
    )

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "Habits",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    "${(completionRate * 100).toInt()}%",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.tertiary
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            habits.take(3).forEach { habit ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        if (habit.streak > 0) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                        null,
                        modifier = Modifier.size(16.dp),
                        tint = if (habit.streak > 0) Color(0xFF4CAF50) else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        habit.name,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        "${habit.streak} days",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (habits.size > 3) {
                Text(
                    "+${habits.size - 3} more habits",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(animatedRate)
                        .clip(RoundedCornerShape(4.dp))
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.tertiary,
                                    MaterialTheme.colorScheme.tertiary.copy(alpha = 0.7f)
                                )
                            )
                        )
                )
            }
        }
    }
}

@Composable
fun EnhancedHealthMetricsGrid(data: AnalyticsData, modifier: Modifier = Modifier) {
    val metrics = listOf(
        HealthMetricItem(
            title = "Hydration",
            value = data.totalWaterToday.toInt(),
            target = 2000,
            unit = "ml",
            icon = Icons.Default.LocalDrink,
            color = Color(0xFF03A9F4)
        ),
        HealthMetricItem(
            title = "Sleep",
            value = data.sleepHours.toFloat(),
            target = 8f,
            unit = "h",
            icon = Icons.Default.Bedtime,
            color = Color(0xFF673AB7)
        ),
        HealthMetricItem(
            title = "Calories",
            value = data.totalCaloriesToday.toInt(),
            target = 2000,
            unit = "cal",
            icon = Icons.Default.LocalFireDepartment,
            color = Color(0xFFE91E63)
        ),
        HealthMetricItem(
            title = "Steps",
            value = data.stepsToday,
            target = 10000,
            unit = "steps",
            icon = Icons.Default.DirectionsWalk,
            color = Color(0xFF4CAF50)
        )
    )

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        metrics.chunked(2).forEach { rowMetrics ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                rowMetrics.forEach { metric ->
                    EnhancedHealthCard(
                        metric = metric,
                        modifier = Modifier.weight(1f)
                    )
                }
                if (rowMetrics.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

data class HealthMetricItem(
    val title: String,
    val value: Number,
    val target: Number,
    val unit: String,
    val icon: ImageVector,
    val color: Color
)

@Composable
fun EnhancedHealthCard(metric: HealthMetricItem, modifier: Modifier = Modifier) {
    val progress = metric.value.toFloat() / metric.target.toFloat()
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(1000, easing = FastOutSlowInEasing),
        label = "animatedProgress"
    )

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = metric.color.copy(alpha = 0.08f)
        ),
        border = BorderStroke(1.dp, metric.color.copy(alpha = 0.2f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(56.dp)
            ) {
                CircularProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier.fillMaxSize(),
                    color = metric.color,
                    strokeWidth = 4.dp,
                    trackColor = metric.color.copy(alpha = 0.2f)
                )
                Icon(
                    metric.icon,
                    null,
                    modifier = Modifier.size(24.dp),
                    tint = metric.color
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                "${metric.value}${metric.unit}",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium,
                color = metric.color
            )

            Text(
                metric.title,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                "Goal: ${metric.target}${metric.unit}",
                style = MaterialTheme.typography.labelSmall,
                color = metric.color.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
fun EnhancedFocusChart(focusSessions: List<FocusSession>, modifier: Modifier = Modifier) {
    val deepWorkMinutes = focusSessions.filter { it.type == FocusType.DEEP_WORK }.sumOf { it.duration } / 60
    val pomodoroMinutes = focusSessions.filter { it.type == FocusType.POMODORO }.sumOf { it.duration } / 60
    val totalMinutes = deepWorkMinutes + pomodoroMinutes

    var animatedDeep by remember { mutableStateOf(0L) }
    var animatedPomodoro by remember { mutableStateOf(0L) }

    LaunchedEffect(deepWorkMinutes, pomodoroMinutes) {
        animatedDeep = 0
        animatedPomodoro = 0
        kotlinx.coroutines.delay(200)
        animatedDeep = deepWorkMinutes
        animatedPomodoro = pomodoroMinutes
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                "Focus Time Distribution",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                FocusTypeCard(
                    label = "Deep Work",
                    minutes = animatedDeep,
                    color = Color(0xFF3F51B5),
                    icon = Icons.Default.Psychology
                )
                FocusTypeCard(
                    label = "Pomodoro",
                    minutes = animatedPomodoro,
                    color = Color(0xFFFF5722),
                    icon = Icons.Default.Timer
                )
            }

            if (totalMinutes > 0) {
                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "Total Focus Time",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        "${totalMinutes} minutes",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val deepPercentage = (deepWorkMinutes.toFloat() / totalMinutes).coerceIn(0f, 1f)
                    Box(
                        modifier = Modifier
                            .weight(deepPercentage)
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFF3F51B5))
                    )
                    Box(
                        modifier = Modifier
                            .weight(1f - deepPercentage)
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFFFF5722))
                    )
                }
            }
        }
    }
}

@Composable
fun FocusTypeCard(label: String, minutes: Long, color: Color, icon: ImageVector) {
    val animatedMinutes by animateFloatAsState(
        targetValue = minutes.toFloat(),
        animationSpec = tween(800, easing = FastOutSlowInEasing),
        label = "animatedMinutes"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = color, modifier = Modifier.size(24.dp))
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "${animatedMinutes}m",
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleMedium,
            color = color
        )
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun AchievementsCarousel(achievements: List<Achievement>, modifier: Modifier = Modifier) {
    if (achievements.isEmpty()) {
        Card(
            modifier = modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    Icons.Default.EmojiEvents,
                    null,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "No achievements yet",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    "Complete tasks to earn badges!",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    } else {
        LazyRow(
            modifier = modifier,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(horizontal = 4.dp)
        ) {
            items(achievements.take(5)) { achievement ->
                AchievementCard(achievement)
            }
        }
    }
}

@Composable
fun AchievementCard(achievement: Achievement) {
    Card(
        modifier = Modifier.width(140.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.EmojiEvents,
                null,
                modifier = Modifier.size(32.dp),
                tint = Color(0xFFFFC107)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                achievement.name,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                achievement.unlockedTimestamp?.let {
                    Instant.ofEpochMilli(it)
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate()
                        .format(DateTimeFormatter.ofPattern("MMM d"))
                } ?: "Locked",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
