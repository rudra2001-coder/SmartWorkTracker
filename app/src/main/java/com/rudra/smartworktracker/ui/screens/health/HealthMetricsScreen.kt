package com.rudra.smartworktracker.ui.screens.health

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.automirrored.outlined.ShowChart
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.Calculate
import androidx.compose.material.icons.outlined.Insights
import androidx.compose.material.icons.outlined.LocalDrink
import androidx.compose.material.icons.outlined.MonitorWeight
import androidx.compose.material.icons.outlined.Nightlight
import androidx.compose.material.icons.outlined.Straighten
import androidx.compose.material.icons.outlined.TrackChanges
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rudra.smartworktracker.model.HealthMetricType
import kotlinx.coroutines.delay
import java.time.Instant
import java.time.LocalDate
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

    var selectedMetric by remember { mutableStateOf(HealthMetricType.WEIGHT) }
    var showInputDialog by remember { mutableStateOf(false) }
    var showGoalDialog by remember { mutableStateOf(false) }

    // Confetti effect
    if (uiState.showConfetti) {
        ConfettiAnimation()
    }

    // Premium gradient background
    val backgroundGradient = Brush.verticalGradient(
        colors = listOf(
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f),
            MaterialTheme.colorScheme.surface,
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.05f)
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundGradient)
    ) {
        // Animated floating particles
        AnimatedParticles()

        // Main content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(4.dp)
        ) {
            // Header with gradient
            HealthHeader(
                dailyStreak = healthAnalytics.dailyStreak,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                // Health Overview Cards
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        HealthMetricCard(
                            title = "Weight",
                            currentValue = healthData.currentValues[HealthMetricType.WEIGHT],
                            unit = "kg",
                            targetValue = goals[HealthMetricType.WEIGHT],
                            icon = Icons.Outlined.MonitorWeight,
                            color = Color(0xFF6C63FF), // Premium purple
                            trend = healthAnalytics.weightTrend,
                            modifier = Modifier.weight(1f),
                            onClick = {
                                selectedMetric = HealthMetricType.WEIGHT
                                showInputDialog = true
                            }
                        )

                        HealthMetricCard(
                            title = "BMI",
                            currentValue = healthAnalytics.bmi,
                            unit = "",
                            targetValue = 22.5,
                            icon = Icons.Outlined.Calculate,
                            color = Color(0xFF4CC9F0), // Cyan
                            bmiCategory = healthAnalytics.bmiCategory,
                            modifier = Modifier.weight(1f),
                            onClick = {
                                // Show BMI info
                            }
                        )
                    }
                }

                // Progress Visualization Section
                item {
                    HealthProgressSection(
                        weightProgress = healthData.weightProgress,
                        waterConsistency = healthData.waterConsistency,
                        sleepConsistency = healthData.sleepConsistency,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Quick Actions with premium design
                item {
                    Text(
                        "Quick Log",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )

                    QuickActionsGrid(
                        onWeightClick = {
                            selectedMetric = HealthMetricType.WEIGHT
                            showInputDialog = true
                        },
                        onHeightClick = {
                            selectedMetric = HealthMetricType.HEIGHT
                            showInputDialog = true
                        },
                        onWaterClick = {
                            selectedMetric = HealthMetricType.WATER
                            showInputDialog = true
                        },
                        onSleepClick = {
                            selectedMetric = HealthMetricType.SLEEP
                            showInputDialog = true
                        },
                        onGoalClick = { showGoalDialog = true }
                    )
                }

                // Recent Entries with glassmorphism effect
                if (healthData.recentEntries.isNotEmpty()) {
                    item {
                        RecentEntriesCard(
                            entries = healthData.recentEntries,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                // Health Insights
                item {
                    HealthInsightsCard(
                        analytics = healthAnalytics,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // Error/Success Snackbars
        if (uiState.error != null) {
            LaunchedEffect(uiState.error) {
                delay(3000)
                viewModel.uiState.value.copy(error = null)
            }
        }

        if (uiState.saveSuccess) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 80.dp)
            ) {
                SuccessToast(message = "${selectedMetric.displayName} saved successfully!")
            }
        }
    }

    // Input Dialog
    if (showInputDialog) {
        HealthInputDialog(
            metricType = selectedMetric,
            onSave = { value ->
                viewModel.saveHealthMetric(selectedMetric, value)
                showInputDialog = false
            },
            onDismiss = { showInputDialog = false }
        )
    }

    // Goal Setting Dialog
    if (showGoalDialog) {
        GoalSettingDialog(
            currentGoals = goals,
            onGoalUpdate = { type, value ->
                viewModel.updateGoal(type, value)
                showGoalDialog = false
            },
            onDismiss = { showGoalDialog = false }
        )
    }
}

@Composable
fun HealthHeader(dailyStreak: Int, modifier: Modifier = Modifier) {
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
                    "Health Dashboard",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    "Track your wellness journey",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
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

@Composable
fun HealthMetricCard(
    title: String,
    currentValue: Double?,
    unit: String,
    targetValue: Double?,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    trend: Float = 0f,
    bmiCategory: BMICategory? = null,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val animatedProgress = remember { Animatable(0f) }
    val progress = if (currentValue != null && targetValue != null && targetValue != 0.0) {
        (currentValue / targetValue).toFloat().coerceIn(0f, 1f)
    } else 0f

    LaunchedEffect(progress) {
        animatedProgress.animateTo(
            targetValue = progress,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        )
    }

    Card(
        modifier = modifier
            .shadow(
                elevation = 12.dp,
                shape = RoundedCornerShape(24.dp),
                spotColor = color.copy(alpha = 0.2f),
                clip = true
            )
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(4.dp)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        icon,
                        contentDescription = title,
                        tint = color,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        title,
                        style = MaterialTheme.typography.titleMedium.copy(
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
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            "${String.format(Locale.getDefault(), "%.1f", abs(trend))}%",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (trend < 0) Color(0xFF4CAF50) else Color(0xFFF44336)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Animated Circular Progress
            Box(
                modifier = Modifier.size(120.dp),
                contentAlignment = Alignment.Center
            ) {
                // Background ring
                Canvas(modifier = Modifier.size(120.dp)) {
                    drawCircle(
                        color = color.copy(alpha = 0.1f),
                        style = Stroke(width = 12.dp.toPx(), cap = StrokeCap.Round)
                    )

                    // Progress ring
                    val sweepAngle = animatedProgress.value * 360
                    drawArc(
                        color = color,
                        startAngle = -90f,
                        sweepAngle = sweepAngle,
                        useCenter = false,
                        style = Stroke(width = 12.dp.toPx(), cap = StrokeCap.Round)
                    )
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        currentValue?.let {
                            String.format(Locale.getDefault(), "%.1f", it)
                        } ?: "--",
                        style = MaterialTheme.typography.displaySmall.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        unit,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // BMI Category or Target
            if (bmiCategory != null && bmiCategory != BMICategory.UNKNOWN) {
                Text(
                    bmiCategory.name.replace("_", " "),
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Medium
                    ),
                    color = getBMIColor(bmiCategory)
                )
            } else {
                targetValue?.let {
                    Text(
                        "Goal: ${String.format(Locale.getDefault(), "%.1f", it)}$unit",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun HealthProgressSection(
    weightProgress: List<Pair<LocalDate, Double>>,
    waterConsistency: Int,
    sleepConsistency: Int,
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
                .padding(24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Progress Overview",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    ConsistencyBadge(
                        type = "Water",
                        consistency = waterConsistency,
                        color = Color(0xFF2196F3)
                    )
                    ConsistencyBadge(
                        type = "Sleep",
                        consistency = sleepConsistency,
                        color = Color(0xFF9C27B0)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            if (weightProgress.size >= 2) {
                AnimatedLineChart(
                    dataPoints = weightProgress,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                )
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.AutoMirrored.Outlined.ShowChart,
                        contentDescription = "No Data",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Log more data to see progress",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
fun ConsistencyBadge(type: String, consistency: Int, color: Color) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.1f))
                .border(
                    width = 2.dp,
                    color = color.copy(alpha = 0.3f),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "$consistency%",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = color
            )
        }
        Text(
            type,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun QuickActionsGrid(
    onWeightClick: () -> Unit,
    onHeightClick: () -> Unit,
    onWaterClick: () -> Unit,
    onSleepClick: () -> Unit,
    onGoalClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                QuickActionButton(
                    title = "Weight",
                    icon = Icons.Outlined.MonitorWeight,
                    color = Color(0xFF6C63FF),
                    onClick = onWeightClick,
                    modifier = Modifier.weight(1f)
                )
                QuickActionButton(
                    title = "Height",
                    icon = Icons.Outlined.Straighten,
                    color = Color(0xFF4CC9F0),
                    onClick = onHeightClick,
                    modifier = Modifier.weight(1f)
                )
                QuickActionButton(
                    title = "Water",
                    icon = Icons.Outlined.LocalDrink,
                    color = Color(0xFF2196F3),
                    onClick = onWaterClick,
                    modifier = Modifier.weight(1f)
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                QuickActionButton(
                    title = "Sleep",
                    icon = Icons.Outlined.Nightlight,
                    color = Color(0xFF9C27B0),
                    onClick = onSleepClick,
                    modifier = Modifier.weight(1f)
                )
                QuickActionButton(
                    title = "Goals",
                    icon = Icons.Outlined.TrackChanges,
                    color = Color(0xFF4CAF50),
                    onClick = onGoalClick,
                    modifier = Modifier.weight(1f)
                )
                QuickActionButton(
                    title = "Insights",
                    icon = Icons.Outlined.Insights,
                    color = Color(0xFFFF9800),
                    onClick = { /* Show insights */ },
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
            .aspectRatio(1f)
            .shadow(
                elevation = 6.dp,
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
                modifier = Modifier.size(28.dp)
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
fun RecentEntriesCard(
    entries: List<HealthMetricEntry>,
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
                .padding(24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Recent Entries",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    "View All",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Medium
                    ),
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            entries.take(3).forEach { entry ->
                HealthEntryRow(entry = entry)
                if (entry != entries.take(3).last()) {
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 8.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)
                    )
                }
            }
        }
    }
}

@Composable
fun HealthEntryRow(entry: HealthMetricEntry) {
    val date = Instant.ofEpochMilli(entry.timestamp)
        .atZone(ZoneId.systemDefault())
        .toLocalDateTime()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(entry.type.color.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    entry.type.icon,
                    contentDescription = entry.type.displayName,
                    tint = entry.type.color,
                    modifier = Modifier.size(20.dp)
                )
            }

            Column {
                Text(
                    entry.type.displayName,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Medium
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    date.format(DateTimeFormatter.ofPattern("MMM d, HH:mm")),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Text(
            "${String.format(Locale.getDefault(), "%.1f", entry.value)}${entry.type.unit}",
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Bold
            ),
            color = entry.type.color
        )
    }
}

@Composable
fun HealthInsightsCard(
    analytics: HealthAnalytics,
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
                .padding(24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Health Insights",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Icon(
                    Icons.Outlined.Insights,
                    contentDescription = "Insights",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Add insights here based on analytics
            // Example: Show recommendations or health tips
            Text(
                "Keep up the good work! " +
                        if (analytics.dailyStreak >= 3) "You've maintained a ${analytics.dailyStreak}-day streak!"
                        else "Log daily to build your streak.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun AnimatedParticles() {
    // Implement animated floating particles in background
    // For now, we'll leave this as a placeholder
}

@Composable
fun ConfettiAnimation() {
    // Implement confetti animation for success events
    // For now, we'll leave this as a placeholder
}

@Composable
fun SuccessToast(message: String) {
    Card(
        modifier = Modifier
            .shadow(
                elevation = 16.dp,
                shape = RoundedCornerShape(24.dp),
                clip = true
            ),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF4CAF50).copy(alpha = 0.95f)
        )
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                Icons.Filled.CheckCircle,
                contentDescription = "Success",
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
            Text(
                message,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Medium
                ),
                color = Color.White
            )
        }
    }
}

@Composable
fun getBMIColor(category: BMICategory): Color {
    return when (category) {
        BMICategory.UNDERWEIGHT -> Color(0xFFFF9800)
        BMICategory.NORMAL -> Color(0xFF4CAF50)
        BMICategory.OVERWEIGHT -> Color(0xFFFF5722)
        BMICategory.OBESE -> Color(0xFFF44336)
        BMICategory.UNKNOWN -> MaterialTheme.colorScheme.onSurfaceVariant
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
fun GoalSettingDialog(
    currentGoals: Map<HealthMetricType, Double>,
    onGoalUpdate: (HealthMetricType, Double) -> Unit,
    onDismiss: () -> Unit
) {
    val healthMetricTypes = listOf(HealthMetricType.WEIGHT, HealthMetricType.WATER, HealthMetricType.SLEEP)
    var selectedMetric by remember { mutableStateOf(healthMetricTypes.first()) }
    var goalValue by remember { mutableStateOf(currentGoals[selectedMetric]?.toString() ?: "") }

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
        val path = Path()
        val xMin = dataPoints.first().first.toEpochDay().toFloat()
        val xMax = dataPoints.last().first.toEpochDay().toFloat()
        val yMin = dataPoints.minOf { it.second }.toFloat()
        val yMax = dataPoints.maxOf { it.second }.toFloat()

        dataPoints.forEachIndexed { index, pair ->
            val x = (pair.first.toEpochDay().toFloat() - xMin) / (xMax - xMin) * size.width
            val y = (1 - (pair.second.toFloat() - yMin) / (yMax - yMin)) * size.height
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


// Extension for HealthMetricType to include color
val HealthMetricType.color: Color
    @Composable
    get() = when (this) {
        HealthMetricType.WEIGHT -> Color(0xFF6C63FF)
        HealthMetricType.HEIGHT -> Color(0xFF4CC9F0)
        HealthMetricType.WATER -> Color(0xFF2196F3)
        HealthMetricType.SLEEP -> Color(0xFF9C27B0)
    }

// Extension for HealthMetricType to include icon
val HealthMetricType.icon: androidx.compose.ui.graphics.vector.ImageVector
    get() = when (this) {
        HealthMetricType.WEIGHT -> Icons.Outlined.MonitorWeight
        HealthMetricType.HEIGHT -> Icons.Outlined.Straighten
        HealthMetricType.WATER -> Icons.Outlined.LocalDrink
        HealthMetricType.SLEEP -> Icons.Outlined.Nightlight
    }

@Preview(showBackground = true)
@Composable
fun HealthMetricsScreenPreview() {
    MaterialTheme {
        HealthMetricsScreen()
    }
}
