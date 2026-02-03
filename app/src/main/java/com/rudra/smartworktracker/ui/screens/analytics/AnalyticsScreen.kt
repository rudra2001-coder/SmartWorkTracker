package com.rudra.smartworktracker.ui.screens.analytics

import android.app.Application
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
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rudra.smartworktracker.data.entity.Income
import com.rudra.smartworktracker.model.*
import kotlinx.coroutines.delay
import java.util.Locale

@Composable
fun AnalyticsScreen(onNavigateBack: () -> Unit) {
    val application = LocalContext.current.applicationContext as Application
    val viewModel: AnalyticsViewModel = viewModel(factory = AnalyticsViewModelFactory(application))
    val analyticsData by viewModel.analyticsData.collectAsState()

    val backgroundGradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF1A237E).copy(alpha = 0.05f),
            MaterialTheme.colorScheme.surface
        )
    )

    Scaffold(
        topBar = {
            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
                Text(
                    "Performance Insights",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    "Your overall activity and balance",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundGradient)
                .padding(padding),
            contentPadding = PaddingValues(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // 1. Core Summary Grid
            item {
                SummaryGrid(analyticsData)
            }

            // 2. Main Balance Score (Work-Life)
            item {
                BalanceDonutCard(
                    score = analyticsData.workLifeBalanceScore,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
            }

            // 3. Financial Radar Section
            item {
                AnalyticsSectionHeader("Financial Health", Icons.Default.AccountBalanceWallet)
                FinancialSummaryChart(
                    incomes = analyticsData.incomes,
                    expenses = analyticsData.expenses,
                    savings = analyticsData.totalSavings,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
            }

            // 4. Productivity & Habits Row
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    ProductivityMiniCard(
                        score = analyticsData.productivityScore,
                        modifier = Modifier.weight(1f)
                    )
                    HabitMiniCard(
                        habits = analyticsData.habits,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // 5. Daily Health Insights
            item {
                AnalyticsSectionHeader("Health Metrics", Icons.Default.HealthAndSafety)
                HealthMetricsGrid(analyticsData, modifier = Modifier.padding(horizontal = 20.dp))
            }

            // 6. Focus Quality
            item {
                FocusQualityChart(
                    focusSessions = analyticsData.focusSessions,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
            }
        }
    }
}

@Composable
fun SummaryGrid(data: AnalyticsData) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        QuickStatBox(
            label = "Work",
            value = "${String.format(Locale.getDefault(), "%.1f", data.workHoursToday)}h",
            color = Color(0xFF4A90E2),
            icon = Icons.Default.Timer,
            modifier = Modifier.weight(1f)
        )
        QuickStatBox(
            label = "Calories",
            value = "${data.totalCaloriesToday.toInt()}",
            color = Color(0xFFE91E63),
            icon = Icons.Default.LocalFireDepartment,
            modifier = Modifier.weight(1f)
        )
        QuickStatBox(
            label = "Badges",
            value = "${data.achievementsCount}",
            color = Color(0xFFFFC107),
            icon = Icons.Default.EmojiEvents,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun QuickStatBox(label: String, value: String, color: Color, icon: ImageVector, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.08f)),
        border = BorderStroke(1.dp, color.copy(alpha = 0.1f))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, null, modifier = Modifier.size(20.dp), tint = color)
            Text(value, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, color = color)
            Text(label, style = MaterialTheme.typography.labelSmall, color = color.copy(alpha = 0.7f))
        }
    }
}

@Composable
fun AnalyticsSectionHeader(title: String, icon: ImageVector) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.width(8.dp))
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun BalanceDonutCard(score: Int, modifier: Modifier = Modifier) {
    val animatedScore = remember { Animatable(0f) }
    LaunchedEffect(score) { animatedScore.animateTo(score / 100f, tween(1000, easing = LinearOutSlowInEasing)) }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f))
    ) {
        Row(
            modifier = Modifier.padding(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(100.dp)) {
                Canvas(Modifier.size(100.dp)) {
                    drawCircle(Color.White.copy(alpha = 0.3f), style = Stroke(12.dp.toPx()))
                    drawArc(
                        color = Color(0xFF4CAF50),
                        startAngle = -90f,
                        sweepAngle = animatedScore.value * 360f,
                        useCenter = false,
                        style = Stroke(12.dp.toPx(), cap = StrokeCap.Round)
                    )
                }
                Text("${(animatedScore.value * 100).toInt()}%", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
            }
            Spacer(Modifier.width(24.dp))
            Column {
                Text("Work-Life Balance", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(
                    if (score > 80) "Excellent! Keep it up." else if (score > 50) "Good balance, small tweaks needed." else "Warning: Work overload detected.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun FinancialSummaryChart(incomes: List<Income>, expenses: List<Expense>, savings: Double, modifier: Modifier = Modifier) {
    val totalIncome = incomes.sumOf { it.amount }
    val totalExpense = expenses.sumOf { it.amount }
    
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(Modifier.padding(20.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                FinanceItem("Income", totalIncome, Color(0xFF4CAF50), Icons.AutoMirrored.Filled.TrendingUp)
                FinanceItem("Expenses", totalExpense, Color(0xFFF44336), Icons.Default.BarChart)
                FinanceItem("Savings", savings, Color(0xFF2196F3), Icons.Default.Savings)
            }
            Spacer(Modifier.height(20.dp))
            // Simple Bar
            val total = (totalIncome + totalExpense).toFloat()
            if (total > 0) {
                Row(Modifier.fillMaxWidth().height(12.dp).clip(CircleShape).background(Color.LightGray.copy(alpha = 0.2f))) {
                    Box(Modifier.fillMaxHeight().weight(totalIncome.toFloat() / total).background(Color(0xFF4CAF50)))
                    Box(Modifier.fillMaxHeight().weight(totalExpense.toFloat() / total).background(Color(0xFFF44336)))
                }
            }
        }
    }
}

@Composable
fun FinanceItem(label: String, value: Double, color: Color, icon: ImageVector) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, Modifier.size(14.dp), tint = color)
            Spacer(Modifier.width(4.dp))
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Text("৳${value.toInt()}", fontWeight = FontWeight.Bold, color = color, style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
fun HealthMetricsGrid(data: AnalyticsData, modifier: Modifier = Modifier) {
    Row(modifier, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        HealthCard("Hydration", "${data.totalWaterToday.toInt()}ml", Icons.Default.LocalDrink, Color(0xFF03A9F4), Modifier.weight(1f))
        HealthCard("Sleep", "${data.sleepHours}h", Icons.Default.Bedtime, Color(0xFF673AB7), Modifier.weight(1f))
    }
}

@Composable
fun HealthCard(title: String, value: String, icon: ImageVector, color: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.05f)),
        border = BorderStroke(1.dp, color.copy(alpha = 0.1f))
    ) {
        Column(Modifier.padding(16.dp)) {
            Icon(icon, null, tint = color, modifier = Modifier.size(24.dp))
            Spacer(Modifier.height(8.dp))
            Text(value, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            Text(title, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun FocusQualityChart(focusSessions: List<FocusSession>, modifier: Modifier = Modifier) {
    val totalMinutes = focusSessions.sumOf { it.duration } / 60
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
    ) {
        Column(Modifier.padding(20.dp)) {
            Text("Focus Distribution", fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(16.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                FocusIndicator("Deep Work", focusSessions.filter { it.type == FocusType.DEEP_WORK }.sumOf { it.duration } / 60, Color(0xFF3F51B5))
                FocusIndicator("Pomodoro", focusSessions.filter { it.type == FocusType.POMODORO }.sumOf { it.duration } / 60, Color(0xFFFF5722))
            }
        }
    }
}

@Composable
fun FocusIndicator(label: String, min: Long, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(Modifier.size(12.dp).clip(CircleShape).background(color))
        Text("${min}m", fontWeight = FontWeight.Bold)
        Text(label, style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
fun ProductivityMiniCard(score: Int, modifier: Modifier = Modifier) {
    Card(modifier, shape = RoundedCornerShape(20.dp)) {
        Column(Modifier.padding(16.dp)) {
            Text("Productivity", style = MaterialTheme.typography.labelMedium)
            Text("$score", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            LinearProgressIndicator(progress = { score / 100f }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp))
        }
    }
}

@Composable
fun HabitMiniCard(habits: List<Habit>, modifier: Modifier = Modifier) {
    val completion = if (habits.isNotEmpty()) (habits.count { it.streak > 0 }.toFloat() / habits.size) else 0f
    Card(modifier, shape = RoundedCornerShape(20.dp)) {
        Column(Modifier.padding(16.dp)) {
            Text("Habits", style = MaterialTheme.typography.labelMedium)
            Text("${(completion * 100).toInt()}%", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.tertiary)
            LinearProgressIndicator(progress = { completion }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp), color = MaterialTheme.colorScheme.tertiary)
        }
    }
}
