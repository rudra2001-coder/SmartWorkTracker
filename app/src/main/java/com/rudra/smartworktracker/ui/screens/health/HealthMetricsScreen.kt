package com.rudra.smartworktracker.ui.screens.health

import android.widget.Toast
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.MonitorWeight
import androidx.compose.material.icons.filled.Nightlight
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rudra.smartworktracker.model.HealthMetricType
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HealthMetricsScreen(viewModel: HealthMetricsViewModel = viewModel()) {
    val healthData by viewModel.healthData.collectAsState()
    val context = LocalContext.current

    var selectedMetric by remember { mutableStateOf(HealthMetricType.WEIGHT) }
    var inputValue by remember { mutableStateOf("") }
    var showInputDialog by remember { mutableStateOf(false) }

    LaunchedEffect(selectedMetric) {
        // Reset input when metric changes
        inputValue = ""
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                        MaterialTheme.colorScheme.surface
                    )
                )
            )
    ) {
        // Header
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .shadow(
                    elevation = 8.dp,
                    shape = RoundedCornerShape(20.dp),
                    clip = true
                ),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Health Dashboard",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    "Track your wellness journey",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Health Overview Cards
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    HealthMetricCard(
                        title = "Weight",
                        currentValue = healthData.currentWeight,
                        unit = "kg",
                        targetValue = healthData.weightGoal,
                        icon = Icons.Default.MonitorWeight,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            selectedMetric = HealthMetricType.WEIGHT
                            showInputDialog = true
                        }
                    )

                    HealthMetricCard(
                        title = "BMI",
                        currentValue = healthData.currentBMI,
                        unit = "",
                        targetValue = 22.5, // Healthy BMI range
                        icon = Icons.Default.Calculate,
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            // BMI is calculated automatically
                        }
                    )
                }
            }

            // Progress Visualization
            item {
                HealthProgressChart(
                    weightProgress = healthData.weightProgress,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Quick Actions
            item {
                Text(
                    "Quick Log",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    HealthQuickAction(
                        title = "Weight",
                        icon = Icons.Default.MonitorWeight,
                        color = MaterialTheme.colorScheme.primary,
                        onClick = {
                            selectedMetric = HealthMetricType.WEIGHT
                            showInputDialog = true
                        }
                    )

                    HealthQuickAction(
                        title = "Height",
                        icon = Icons.Default.Straighten,
                        color = MaterialTheme.colorScheme.tertiary,
                        onClick = {
                            selectedMetric = HealthMetricType.HEIGHT
                            showInputDialog = true
                        }
                    )

                    HealthQuickAction(
                        title = "Water",
                        icon = Icons.Default.LocalDrink,
                        color = MaterialTheme.colorScheme.secondary,
                        onClick = {
                            selectedMetric = HealthMetricType.WATER
                            showInputDialog = true
                        }
                    )

                    HealthQuickAction(
                        title = "Sleep",
                        icon = Icons.Default.Nightlight,
                        color = MaterialTheme.colorScheme.primary,
                        onClick = {
                            selectedMetric = HealthMetricType.SLEEP
                            showInputDialog = true
                        }
                    )
                }
            }

            // Recent Entries
            item {
                if (healthData.recentEntries.isNotEmpty()) {
                    RecentHealthEntries(
                        entries = healthData.recentEntries,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }

    // Input Dialog
    if (showInputDialog) {
        HealthInputDialog(
            metricType = selectedMetric,
            value = inputValue,
            onValueChange = { inputValue = it },
            onConfirm = {
                if (inputValue.isNotBlank()) {
                    try {
                        viewModel.saveHealthMetric(selectedMetric, inputValue.toDouble())
                        Toast.makeText(
                            context,
                            "${selectedMetric.displayName} saved successfully!",
                            Toast.LENGTH_SHORT
                        ).show()
                        showInputDialog = false
                        inputValue = ""
                    } catch (_: NumberFormatException) {
                        Toast.makeText(context, "Please enter a valid number", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(context, "Please enter a value", Toast.LENGTH_SHORT).show()
                }
            },
            onDismiss = {
                showInputDialog = false
                inputValue = ""
            }
        )
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
                elevation = 8.dp,
                shape = RoundedCornerShape(20.dp),
                clip = true
            )
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header with icon
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    icon,
                    contentDescription = title,
                    tint = color,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Circular Progress
            Box(
                modifier = Modifier.size(100.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.size(100.dp)) {
                    drawCircle(
                        color = color.copy(alpha = 0.1f),
                        style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
                    )

                    val sweepAngle = animatedProgress.value * 360
                    drawArc(
                        color = color,
                        startAngle = -90f,
                        sweepAngle = sweepAngle,
                        useCenter = false,
                        style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        currentValue?.let { String.format(Locale.getDefault(), "%.1f", it) } ?: "--",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = color
                    )
                    Text(
                        unit,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Target
            targetValue?.let {
                Text(
                    String.format(Locale.getDefault(), "Goal: %.1f%s", it, unit),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun HealthProgressChart(
    weightProgress: List<Pair<LocalDate, Double>>,
    modifier: Modifier = Modifier
) {
    val onSurfaceVariant20 = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f)
    val primaryColor = MaterialTheme.colorScheme.primary
    val tertiaryColor = MaterialTheme.colorScheme.tertiary
    val errorColor = MaterialTheme.colorScheme.error
    Card(
        modifier = modifier
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(20.dp),
                clip = true
            ),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                "Weight Progress",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (weightProgress.size >= 2) {
                // Simple line chart visualization
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
                        .clip(RoundedCornerShape(12.dp))
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val maxWeight = weightProgress.maxOf { it.second }
                        val minWeight = weightProgress.minOf { it.second }
                        val weightRange = maxWeight - minWeight

                        val xStep = size.width / (weightProgress.size - 1)
                        val yScale = if (weightRange > 0) size.height * 0.8f / weightRange.toFloat() else 1f


                        // Draw grid lines
                        for (i in 0..4) {
                            val y = size.height * 0.1f + (i * size.height * 0.2f)
                            drawLine(
                                color = onSurfaceVariant20,
                                start = Offset(0f, y),
                                end = Offset(size.width, y),
                                strokeWidth = 1.dp.toPx()
                            )
                        }

                        // Draw line
                        for (i in 0 until weightProgress.size - 1) {
                            val x1 = i * xStep
                            val y1 = size.height - ((weightProgress[i].second - minWeight).toFloat() * yScale) - size.height * 0.1f
                            val x2 = (i + 1) * xStep
                            val y2 = size.height - ((weightProgress[i + 1].second - minWeight).toFloat() * yScale) - size.height * 0.1f

                            drawLine(
                                color = primaryColor,
                                start = Offset(x1, y1),
                                end = Offset(x2, y2),
                                strokeWidth = 3.dp.toPx(),
                                cap = StrokeCap.Round
                            )
                        }

                        // Draw points
                        weightProgress.forEachIndexed { index, (_, weight) ->
                            val x = index * xStep
                            val y = size.height - ((weight - minWeight).toFloat() * yScale) - size.height * 0.1f

                            drawCircle(
                                color = primaryColor,
                                radius = 4.dp.toPx(),
                                center = Offset(x, y)
                            )
                        }
                    }
                }

                // Progress summary
                Spacer(modifier = Modifier.height(12.dp))
                val firstWeight = weightProgress.first().second
                val lastWeight = weightProgress.last().second
                val difference = lastWeight - firstWeight
                val trendIcon = if (difference < 0) Icons.AutoMirrored.Filled.TrendingDown else Icons.AutoMirrored.Filled.TrendingUp
                val trendColor = if (difference < 0) tertiaryColor else errorColor

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Overall change: ${String.format(Locale.getDefault(), "%.1f", kotlin.math.abs(difference))}kg",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Icon(
                        trendIcon,
                        contentDescription = "Trend",
                        tint = trendColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
            } else {
                Text(
                    "Log more data to see progress",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp)
                )
            }
        }
    }
}

@Composable
fun RowScope.HealthQuickAction(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .weight(1f)
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(16.dp),
                clip = true
            )
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                icon,
                contentDescription = title,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                title,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Medium,
                color = color,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun RecentHealthEntries(
    entries: List<HealthMetricEntry>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(20.dp),
                clip = true
            ),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                "Recent Entries",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(12.dp))

            entries.take(5).forEach { entry ->
                HealthEntryRow(entry = entry)
            }
        }
    }
}

@Composable
fun HealthEntryRow(entry: HealthMetricEntry) {
    val date = Instant.ofEpochMilli(entry.timestamp).atZone(ZoneId.systemDefault()).toLocalDateTime()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            entry.type.icon,
            contentDescription = entry.type.displayName,
            tint = entry.type.color,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                entry.type.displayName,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                date.format(DateTimeFormatter.ofPattern("MMM d, HH:mm")),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            String.format(Locale.getDefault(), "%.1f%s", entry.value, entry.type.unit),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = entry.type.color
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HealthInputDialog(
    metricType: HealthMetricType,
    value: String,
    onValueChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        title = {
            Text("Log ${metricType.displayName}")
        },
        text = {
            Column {
                Text(
                    "Enter your ${metricType.displayName.lowercase()}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))
                TextField(
                    value = value,
                    onValueChange = onValueChange,
                    label = { Text("${metricType.displayName} (${metricType.unit})") },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        }
    )
}

// Extension properties for HealthMetricType
val HealthMetricType.displayName: String
    get() = when (this) {
        HealthMetricType.WEIGHT -> "Weight"
        HealthMetricType.HEIGHT -> "Height"
        HealthMetricType.WATER -> "Water"
        HealthMetricType.SLEEP -> "Sleep"
    }

val HealthMetricType.unit: String
    get() = when (this) {
        HealthMetricType.WEIGHT -> "kg"
        HealthMetricType.HEIGHT -> "cm"
        HealthMetricType.WATER -> "ml"
        HealthMetricType.SLEEP -> "hrs"
    }

val HealthMetricType.icon: androidx.compose.ui.graphics.vector.ImageVector
    get() = when (this) {
        HealthMetricType.WEIGHT -> Icons.Default.MonitorWeight
        HealthMetricType.HEIGHT -> Icons.Default.Straighten
        HealthMetricType.WATER -> Icons.Default.LocalDrink
        HealthMetricType.SLEEP -> Icons.Default.Nightlight
    }

val HealthMetricType.color: Color
    @Composable
    get() = when (this) {
        HealthMetricType.WEIGHT -> MaterialTheme.colorScheme.primary
        HealthMetricType.HEIGHT -> MaterialTheme.colorScheme.tertiary
        HealthMetricType.WATER -> MaterialTheme.colorScheme.secondary
        HealthMetricType.SLEEP -> MaterialTheme.colorScheme.primary
    }

@Preview(showBackground = true)
@Composable
fun HealthMetricsScreenPreview() {
    MaterialTheme {
        HealthMetricsScreen()
    }
}
