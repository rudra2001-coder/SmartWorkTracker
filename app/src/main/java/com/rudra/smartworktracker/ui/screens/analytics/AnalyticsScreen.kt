package com.rudra.smartworktracker.ui.screens.analytics

import android.app.Application
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rudra.smartworktracker.data.entity.Income
import com.rudra.smartworktracker.model.*
import kotlinx.coroutines.delay
import java.text.NumberFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.math.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(onNavigateBack: () -> Unit) {
    val application = LocalContext.current.applicationContext as Application
    val viewModel: AnalyticsViewModel = viewModel(factory = AnalyticsViewModelFactory(application))
    val analyticsData by viewModel.analyticsData.collectAsState()
    val selectedPeriod by viewModel.selectedPeriod.collectAsState()

    val backgroundGradient = Brush.verticalGradient(
        colors = listOf(
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
            MaterialTheme.colorScheme.surface
        )
    )

    Scaffold(

    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundGradient)
                .padding(padding),
            contentPadding = PaddingValues(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // 1. Animated Summary Grid
            item {
                AnimatedSummaryGrid(analyticsData)
            }

            // 2. Main Balance Score (Work-Life) with enhanced animation
            item {
                EnhancedBalanceDonutCard(
                    score = analyticsData.workLifeBalanceScore,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            // 3. Financial Health Section with Trend
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

            // 3b. Monthly Financial Bar Chart (6 months)
            item {
                MonthlyIncomeExpenseChart(
                    monthlyData = analyticsData.monthlyFinancialData,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            // 4. Productivity & Habits Enhanced Cards
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

            // 5. Health Metrics with Progress Rings
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

            // 6. Focus Quality with Timeline
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

            // 7. Achievements Showcase
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

            // 8. Weekly Performance Summary
            item {
                WeeklyPerformanceChart(
                    weeklyData = analyticsData.weeklyPerformance,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
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

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun AnimatedSummaryGrid(data: AnalyticsData) {
    val items = listOf(
        SummaryItem("Work Hours", "${String.format(Locale.getDefault(), "%.1f", data.workHoursToday)}h", Icons.Default.Timer, Color(0xFF4A90E2), data.workHoursTrend),
        SummaryItem("Calories", "${data.totalCaloriesToday.toInt()}", Icons.Default.LocalFireDepartment, Color(0xFFE91E63), data.caloriesTrend),
        SummaryItem("Achievements", "${data.achievementsCount}", Icons.Default.EmojiEvents, Color(0xFFFFC107), data.achievementsTrend),
        SummaryItem("Focus Score", "${data.focusScore}", Icons.Default.Psychology, Color(0xFF9C27B0), data.focusTrend)
    )

    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(items) { item ->
            AnimatedSummaryCard(item)
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun AnimatedSummaryCard(item: SummaryItem) {
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(100)
        isVisible = true
    }

    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(animationSpec = tween(500)) +
                slideInHorizontally(initialOffsetX = { it / 2 })
    ) {
        Card(
            modifier = Modifier.width(120.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = item.color.copy(alpha = 0.08f)
            ),
            border = BorderStroke(1.dp, item.color.copy(alpha = 0.2f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    item.icon,
                    null,
                    modifier = Modifier.size(28.dp),
                    tint = item.color
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    item.value,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleLarge,
                    color = item.color
                )
                Text(
                    item.label,
                    style = MaterialTheme.typography.labelSmall,
                    color = item.color.copy(alpha = 0.7f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (item.trend != 0f) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Icon(
                            if (item.trend > 0) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                            null,
                            modifier = Modifier.size(12.dp),
                            tint = if (item.trend > 0) Color(0xFF4CAF50) else Color(0xFFF44336)
                        )
                        Text(
                            "${if (item.trend > 0) "+" else ""}${item.trend.toInt()}%",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (item.trend > 0) Color(0xFF4CAF50) else Color(0xFFF44336)
                        )
                    }
                }
            }
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
fun EnhancedBalanceDonutCard(score: Int, modifier: Modifier = Modifier) {
    val animatedScore = remember { Animatable(0f) }
    val scoreColor = when {
        score >= 80 -> Color(0xFF4CAF50)
        score >= 50 -> Color(0xFFFFC107)
        else -> Color(0xFFF44336)
    }
    val surfaceVariantColor = MaterialTheme.colorScheme.surfaceVariant

    LaunchedEffect(score) {
        animatedScore.animateTo(
            score / 100f,
            animationSpec = tween(1500, easing = FastOutSlowInEasing)
        )
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(120.dp)
            ) {
                Canvas(modifier = Modifier.size(120.dp)) {
                    drawCircle(
                        color = surfaceVariantColor,
                        radius = size.minDimension / 2,
                        style = Stroke(12.dp.toPx())
                    )
                    drawArc(
                        color = scoreColor,
                        startAngle = -90f,
                        sweepAngle = animatedScore.value * 360f,
                        useCenter = false,
                        style = Stroke(12.dp.toPx(), cap = StrokeCap.Round)
                    )
                    // Inner circle with gradient
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                scoreColor.copy(alpha = 0.2f),
                                Color.Transparent
                            )
                        ),
                        radius = size.minDimension / 3
                    )
                }
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "${(animatedScore.value * 100).toInt()}%",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.headlineSmall,
                        color = scoreColor
                    )
                    Text(
                        "Balance",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(modifier = Modifier.width(24.dp))
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    "Work-Life Balance",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    when {
                        score > 80 -> "🎉 Excellent! You're maintaining a healthy balance."
                        score > 50 -> "👍 Good balance, but there's room for improvement."
                        else -> "⚠️ Warning: Work overload detected. Take time to recharge."
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))
                LinearProgressIndicator(
                    progress = { score / 100f },
                    modifier = Modifier.fillMaxWidth(),
                    color = scoreColor,
                    trackColor = scoreColor.copy(alpha = 0.2f)
                )
            }
        }
    }
}

@Composable
fun EnhancedFinancialChart(
    incomes: List<Income>,
    expenses: List<Expense>,
    savings: Double,
    modifier: Modifier = Modifier
) {
    val totalIncome = incomes.sumOf { it.amount }
    val totalExpense = expenses.sumOf { it.amount }
    val netSavings = totalIncome - totalExpense
    val savingsRate = if (totalIncome > 0) (netSavings / totalIncome) * 100 else 0.0

    var animatedIncome by remember { mutableStateOf(0.0) }
    var animatedExpense by remember { mutableStateOf(0.0) }

    LaunchedEffect(totalIncome, totalExpense) {
        animatedIncome = 0.0
        animatedExpense = 0.0
        delay(100)
        animatedIncome = totalIncome
        animatedExpense = totalExpense
    }

    Card(
        modifier = modifier,
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
            // Financial Summary Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                EnhancedFinanceItem(
                    label = "Income",
                    value = animatedIncome,
                    color = Color(0xFF4CAF50),
                    icon = Icons.AutoMirrored.Filled.TrendingUp,
                    prefix = "৳"
                )
                EnhancedFinanceItem(
                    label = "Expenses",
                    value = animatedExpense,
                    color = Color(0xFFF44336),
                    icon = Icons.Default.BarChart,
                    prefix = "৳"
                )
                EnhancedFinanceItem(
                    label = "Savings",
                    value = savings,
                    color = Color(0xFF2196F3),
                    icon = Icons.Default.Savings,
                    prefix = "৳"
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Savings Rate Indicator
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Savings Rate",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    "${String.format(Locale.getDefault(), "%.1f", savingsRate)}%",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (savingsRate > 20) Color(0xFF4CAF50) else Color(0xFFFFC107)
                )
            }
            LinearProgressIndicator(
                progress = { (savingsRate / 100f).toFloat() },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                color = Color(0xFF4CAF50),
                trackColor = Color(0xFF4CAF50).copy(alpha = 0.2f)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Expense Distribution
            if (expenses.isNotEmpty()) {
                Text(
                    "Expense Distribution",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                ExpenseDistributionChart(expenses)
            }
        }
    }
}

@Composable
fun ExpenseDistributionChart(expenses: List<Expense>) {
    val groupedExpenses = expenses.groupBy { it.category }
        .mapValues { it.value.sumOf { expense -> expense.amount } }
        .toList()
        .sortedByDescending { it.second }
        .take(5)

    val total = groupedExpenses.sumOf { it.second }

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        groupedExpenses.forEach { (category, amount) ->
            val percentage = (amount / total * 100).toFloat()
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    category.name,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.width(80.dp)
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(percentage / 100f)
                            .clip(RoundedCornerShape(4.dp))
                            .background(MaterialTheme.colorScheme.primary)
                    )
                }
                Text(
                    "${String.format(Locale.getDefault(), "%.1f", percentage)}%",
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.width(50.dp),
                    textAlign = TextAlign.End
                )
            }
        }
    }
}

@Composable
fun EnhancedFinanceItem(
    label: String,
    value: Double,
    color: Color,
    icon: ImageVector,
    prefix: String = ""
) {
    val animatedValue by animateFloatAsState(
        targetValue = value.toFloat(),
        animationSpec = tween(1000, easing = FastOutSlowInEasing),
        label = "animatedValue"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            icon,
            null,
            modifier = Modifier.size(24.dp),
            tint = color
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            "$prefix${String.format(Locale.getDefault(), "%.0f", animatedValue)}",
            fontWeight = FontWeight.Bold,
            color = color,
            style = MaterialTheme.typography.titleMedium
        )
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

            // Circular progress indicator
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

            // Active habits list
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
        delay(200)
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
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                achievement.unlockedTimestamp?.let {
                    java.time.Instant.ofEpochMilli(it)
                        .atZone(java.time.ZoneId.systemDefault())
                        .toLocalDate()
                        .format(DateTimeFormatter.ofPattern("MMM d"))
                } ?: "Locked",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun WeeklyPerformanceChart(weeklyData: List<WeeklyPerformance>, modifier: Modifier = Modifier) {
    if (weeklyData.isEmpty()) return

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
                "Weekly Performance",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Bar chart
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom
            ) {
                weeklyData.take(7).forEach { day ->
                    val height = (day.productivityScore / 100f) * 120.dp
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .width(32.dp)
                                .height(height)
                                .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                                .background(
                                    brush = Brush.verticalGradient(
                                        colors = listOf(
                                            MaterialTheme.colorScheme.primary,
                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                                        )
                                    )
                                )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            day.day.substring(0, 3),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            "${day.productivityScore}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}



data class SummaryItem(
    val label: String,
    val value: String,
    val icon: ImageVector,
    val color: Color,
    val trend: Float = 0f
)

enum class AnalyticsPeriod(val displayName: String) {
    WEEK("Week"),
    MONTH("Month"),
    QUARTER("Quarter"),
    YEAR("Year")
}
@Composable
fun MonthlyIncomeExpenseChart(
    monthlyData: List<MonthlyFinancialData>,
    modifier: Modifier = Modifier,
    onMonthClick: ((MonthlyFinancialData) -> Unit)? = null
) {
    if (monthlyData.isEmpty()) {
        EmptyStateMessage(modifier = modifier)
        return
    }

    val maxValue = remember(monthlyData) {
        monthlyData.maxOfOrNull { maxOf(it.income, it.expense) }
            ?.coerceAtLeast(1.0)
            ?: 1.0
    }

    val currencyFormat = remember {
        NumberFormat.getCurrencyInstance(Locale.getDefault()).apply {
            maximumFractionDigits = 0
        }
    }

    val barHeightAnimation = remember(maxValue) {
        Animatable(0f)
    }

    LaunchedEffect(maxValue) {
        barHeightAnimation.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = 800,
                easing = FastOutSlowInEasing
            )
        )
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp,
            pressedElevation = 8.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            ChartHeader(
                totalIncome = monthlyData.sumOf { it.income },
                totalExpense = monthlyData.sumOf { it.expense },
                currencyFormat = currencyFormat
            )

            Spacer(modifier = Modifier.height(20.dp))

            ChartBars(
                monthlyData = monthlyData,
                maxValue = maxValue,
                animationProgress = barHeightAnimation.value,
                onMonthClick = onMonthClick
            )

            Spacer(modifier = Modifier.height(16.dp))

            ChartValues(
                monthlyData = monthlyData,
                currencyFormat = currencyFormat
            )

            if (monthlyData.size > 1) {
                Divider(
                    modifier = Modifier.padding(top = 16.dp, bottom = 8.dp),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                )

                ChartSummary(
                    monthlyData = monthlyData,
                    currencyFormat = currencyFormat
                )
            }
        }
    }
}

@Composable
private fun ChartHeader(
    totalIncome: Double,
    totalExpense: Double,
    currencyFormat: NumberFormat
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Monthly Overview",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            LegendItem(
                color = IncomeGreen,
                label = "Income"
            )

            LegendItem(
                color = ExpenseRed,
                label = "Expense"
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "Total Income",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = currencyFormat.format(totalIncome),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = IncomeGreen
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "Total Expenses",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = currencyFormat.format(totalExpense),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = ExpenseRed
                )
            }
        }
    }
}

@Composable
private fun LegendItem(
    color: Color,
    label: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(start = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ChartBars(
    monthlyData: List<MonthlyFinancialData>,
    maxValue: Double,
    animationProgress: Float,
    onMonthClick: ((MonthlyFinancialData) -> Unit)?
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        monthlyData.forEach { monthData ->
            val incomeHeight = ((monthData.income / maxValue) * 120).dp.coerceAtLeast(4.dp)
            val expenseHeight = ((monthData.expense / maxValue) * 120).dp.coerceAtLeast(4.dp)

            val clickModifier = if (onMonthClick != null) {
                Modifier.clickable { onMonthClick(monthData) }
            } else Modifier

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .weight(1f)
                    .then(clickModifier)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                    verticalAlignment = Alignment.Bottom
                ) {
                    // Income Bar
                    Box(
                        modifier = Modifier
                            .width(14.dp)
                            .graphicsLayer {
                                scaleY = animationProgress
                                translationY = size.height * (1 - animationProgress)
                            }
                            .height(incomeHeight * animationProgress)
                            .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(IncomeGreen, IncomeGreen.copy(alpha = 0.7f))
                                )
                            )
                    )

                    // Expense Bar
                    Box(
                        modifier = Modifier
                            .width(14.dp)
                            .graphicsLayer {
                                scaleY = animationProgress
                                translationY = size.height * (1 - animationProgress)
                            }
                            .height(expenseHeight * animationProgress)
                            .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(ExpenseRed, ExpenseRed.copy(alpha = 0.7f))
                                )
                            )
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = monthData.month,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun ChartValues(
    monthlyData: List<MonthlyFinancialData>,
    currencyFormat: NumberFormat
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        monthlyData.forEach { monthData ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = currencyFormat.format(monthData.income),
                    style = MaterialTheme.typography.labelSmall,
                    color = IncomeGreen,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = currencyFormat.format(monthData.expense),
                    style = MaterialTheme.typography.labelSmall,
                    color = ExpenseRed,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun ChartSummary(
    monthlyData: List<MonthlyFinancialData>,
    currencyFormat: NumberFormat
) {
    val averageIncome = monthlyData.map { it.income }.average()
    val averageExpense = monthlyData.map { it.expense }.average()
    val netChange = monthlyData.sumOf { it.income - it.expense }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                text = "Monthly Average",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "${currencyFormat.format(averageIncome)} / ${currencyFormat.format(averageExpense)}",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium
            )
        }

        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "Net Change",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = currencyFormat.format(netChange),
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = if (netChange >= 0) IncomeGreen else ExpenseRed
            )
        }
    }
}

@Composable
private fun EmptyStateMessage(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No data available",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// Color constants
private val IncomeGreen = Color(0xFF4CAF50)
private val ExpenseRed = Color(0xFFF44336)