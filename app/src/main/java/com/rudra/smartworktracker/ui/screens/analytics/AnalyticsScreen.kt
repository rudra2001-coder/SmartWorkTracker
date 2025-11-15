package com.rudra.smartworktracker.ui.screens.analytics

import android.app.Application
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rudra.smartworktracker.data.entity.Income
import com.rudra.smartworktracker.model.Expense
import com.rudra.smartworktracker.model.FocusSession
import com.rudra.smartworktracker.model.FocusType
import com.rudra.smartworktracker.model.Habit
import kotlinx.coroutines.delay

@Composable
fun AnalyticsScreen(onNavigateBack: () -> Unit) {
    val application = LocalContext.current.applicationContext as Application
    val viewModel: AnalyticsViewModel = viewModel(factory = AnalyticsViewModelFactory(application))
    val analyticsData by viewModel.analyticsData.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primaryContainer,
                        MaterialTheme.colorScheme.surface
                    )
                )
            )
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item {
            Text(
                "Analytics Dashboard",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        }

        item {
            FinancialSummaryChart(
                incomes = analyticsData.incomes,
                expenses = analyticsData.expenses
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    ProductivityScoreCard(score = analyticsData.productivityScore)
                }
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    HabitConsistencyCard(habits = analyticsData.habits)
                }
            }
        }

        item {
            FocusQualityChart(focusSessions = analyticsData.focusSessions)
        }
    }
}

@Composable
fun FinancialSummaryChart(
    incomes: List<Income>,
    expenses: List<Expense>
) {
    val totalIncome = incomes.sumOf { it.amount }
    val totalExpense = expenses.sumOf { it.amount }
    val savings = totalIncome - totalExpense
    var animatedIncome by remember { mutableFloatStateOf(0f) }
    var animatedExpense by remember { mutableFloatStateOf(0f) }
    var animatedSavings by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(totalIncome, totalExpense, savings) {
        animatedIncome = 0f
        animatedExpense = 0f
        animatedSavings = 0f

        delay(300)
        animatedIncome = totalIncome.toFloat()
        delay(200)
        animatedExpense = totalExpense.toFloat()
        delay(200)
        animatedSavings = savings.toFloat()
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 16.dp,
                shape = RoundedCornerShape(24.dp),
                clip = true
            ),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.AttachMoney,
                    contentDescription = "Financial",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.padding(8.dp))
                Text(
                    "Financial Summary",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.fillMaxWidth()
            ) {
                FinancialMetricCard(
                    title = "Income",
                    value = animatedIncome,
                    color = MaterialTheme.colorScheme.primary,
                    icon = Icons.AutoMirrored.Filled.TrendingUp
                )

                FinancialMetricCard(
                    title = "Expense",
                    value = animatedExpense,
                    color = MaterialTheme.colorScheme.error,
                    icon = Icons.Default.BarChart
                )

                FinancialMetricCard(
                    title = "Savings",
                    value = animatedSavings,
                    color = if (savings >= 0) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.error,
                    icon = Icons.Default.CheckCircle
                )
            }
        }
    }
}

@Composable
fun FinancialMetricCard(
    title: String,
    value: Float,
    color: Color,
    icon: ImageVector
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                icon,
                contentDescription = title,
                tint = color,
                modifier = Modifier.size(28.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            title,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            "₹${"%.0f".format(value)}",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

@Composable
fun ProductivityScoreCard(score: Int) {
    val animatedProgress = remember { Animatable(0f) }

    LaunchedEffect(score) {
        animatedProgress.animateTo(
            targetValue = score / 100f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        )
    }
    val surfaceVariantColor = MaterialTheme.colorScheme.surfaceVariant
    val primaryColor = MaterialTheme.colorScheme.primary
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 12.dp,
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
                "Productivity Score",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Circular Progress Indicator
            Box(
                modifier = Modifier.size(120.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.size(120.dp)) {
                    drawCircle(
                        color = surfaceVariantColor,
                        style = Stroke(width = 12.dp.toPx(), cap = StrokeCap.Round)
                    )

                    val sweepAngle = animatedProgress.value * 360
                    drawArc(
                        color = primaryColor,
                        startAngle = -90f,
                        sweepAngle = sweepAngle,
                        useCenter = false,
                        style = Stroke(width = 12.dp.toPx(), cap = StrokeCap.Round)
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "${(animatedProgress.value * 100).toInt()}",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        "points",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            LinearProgressIndicator(
                progress = { animatedProgress.value },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        }
    }
}

@Composable
fun FocusQualityChart(focusSessions: List<FocusSession>) {
    val totalDeepWork = focusSessions.filter { it.type == FocusType.DEEP_WORK }.sumOf { it.duration } / 60
    val totalPomodoro = focusSessions.filter { it.type == FocusType.POMODORO }.sumOf { it.duration } / 60
    val totalFocus = (totalDeepWork + totalPomodoro).toInt()

    var animatedDeepWork by remember { mutableFloatStateOf(0f) }
    var animatedPomodoro by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(totalDeepWork, totalPomodoro) {
        animatedDeepWork = 0f
        animatedPomodoro = 0f

        delay(400)
        animatedDeepWork = totalDeepWork.toFloat()
        delay(300)
        animatedPomodoro = totalPomodoro.toFloat()
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 12.dp,
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
                "Focus Quality",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                "Total Focus Time: ${totalFocus}min",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                FocusTypeCard(
                    type = "Deep Work",
                    minutes = animatedDeepWork.toInt(),
                    color = MaterialTheme.colorScheme.primary,
                    totalMinutes = totalFocus
                )

                FocusTypeCard(
                    type = "Pomodoro",
                    minutes = animatedPomodoro.toInt(),
                    color = MaterialTheme.colorScheme.secondary,
                    totalMinutes = totalFocus
                )
            }
        }
    }
}

@Composable
fun FocusTypeCard(type: String, minutes: Int, color: Color, totalMinutes: Int) {
    val percentage = if (totalMinutes > 0) (minutes.toFloat() / totalMinutes) * 100 else 0f

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "${minutes}m",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            type,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            "${percentage.toInt()}%",
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

@Composable
fun HabitConsistencyCard(habits: List<Habit>) {
    val totalHabits = habits.size
    val completedHabits = habits.count { it.streak > 0 }
    val consistency = if (totalHabits > 0) (completedHabits.toFloat() / totalHabits.toFloat()) * 100 else 0f
    val animatedProgress = remember { Animatable(0f) }

    LaunchedEffect(consistency) {
        animatedProgress.animateTo(
            targetValue = consistency / 100f,
            animationSpec = tween(durationMillis = 1000, easing = EaseOutCubic)
        )
    }
    val surfaceVariantColor = MaterialTheme.colorScheme.surfaceVariant
    val tertiaryColor = MaterialTheme.colorScheme.tertiary
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 12.dp,
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
                "Habit Consistency",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Donut Chart
            Box(
                modifier = Modifier.size(100.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.size(100.dp)) {
                    drawCircle(
                        color = surfaceVariantColor,
                        style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
                    )

                    val sweepAngle = animatedProgress.value * 360
                    drawArc(
                        color = tertiaryColor,
                        startAngle = -90f,
                        sweepAngle = sweepAngle,
                        useCenter = false,
                        style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "${(animatedProgress.value * 100).toInt()}%",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                "$completedHabits/$totalHabits habits",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = { animatedProgress.value },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = MaterialTheme.colorScheme.tertiary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        }
    }
}
