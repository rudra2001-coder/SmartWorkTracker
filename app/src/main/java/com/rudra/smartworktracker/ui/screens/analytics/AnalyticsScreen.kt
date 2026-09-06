package com.rudra.smartworktracker.ui.screens.analytics

import android.app.Application
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.*
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

// ─────────────────────────────────────────────────────────────
//  Design Tokens (Dashboard-aligned)
// ─────────────────────────────────────────────────────────────
private val CardShape = RoundedCornerShape(20.dp)
private val PillShape = RoundedCornerShape(50.dp)
private val ChipShape = RoundedCornerShape(12.dp)

private val EmeraldGreen  = Color(0xFF00C896)
private val CoralRed      = Color(0xFFFF5757)
private val SapphireBlue  = Color(0xFF3B82F6)
private val GoldenAmber   = Color(0xFFF59E0B)
private val VioletPurple  = Color(0xFF8B5CF6)
private val SlateGray     = Color(0xFF64748B)

private val GreenSurface  = Color(0xFFE6FBF4)
private val RedSurface    = Color(0xFFFFEDED)
private val BlueSurface   = Color(0xFFEFF6FF)
private val AmberSurface  = Color(0xFFFFFBEB)
private val PurpleSurface = Color(0xFFF5F3FF)

// ─────────────────────────────────────────────────────────────
//  Root Screen
// ─────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(onNavigateBack: () -> Unit = {}) {
    val application = LocalContext.current.applicationContext as Application
    val viewModel: AnalyticsViewModel = viewModel(
        factory = AnalyticsViewModelFactory(application)
    )
    val data by viewModel.analyticsData.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Ambient glow blobs in background
        AmbientBackground()

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 40.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {

            // ── Header ──────────────────────────────────────────────
            item {
                DashboardHeader(data = data, onBack = onNavigateBack)
            }

            // ── Top KPI Strip ────────────────────────────────────────
            item {
                Spacer(modifier = Modifier.height(8.dp))
                KpiStrip(data = data)
            }

            // ── Work-Life Balance Ring ───────────────────────────────
            item {
                Spacer(modifier = Modifier.height(20.dp))
                SectionLabel("Balance Score")
                Spacer(modifier = Modifier.height(8.dp))
                BalanceRingCard(
                    score = data.workLifeBalanceScore,
                    productivityScore = data.productivityScore,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            // ── Financial Overview ───────────────────────────────────
            item {
                Spacer(modifier = Modifier.height(20.dp))
                SectionLabel("Finances")
                Spacer(modifier = Modifier.height(8.dp))
                FinancialOverviewCard(
                    incomes = data.incomes,
                    expenses = data.expenses,
                    savings = data.totalSavings,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            // ── Monthly Chart ────────────────────────────────────────
            item {
                Spacer(modifier = Modifier.height(12.dp))
                MonthlyBarChartCard(
                    monthlyData = data.monthlyFinancialData,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            // ── Productivity + Habits ────────────────────────────────
            item {
                Spacer(modifier = Modifier.height(20.dp))
                SectionLabel("Performance")
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ProductivityRingCard(
                        score = data.productivityScore,
                        trend = data.productivityTrend,
                        modifier = Modifier.weight(1f)
                    )
                    HabitStreakCard(
                        habits = data.habits,
                        completionRate = data.habitCompletionRate,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // ── Wellness Grid ─────────────────────────────────────────
            item {
                Spacer(modifier = Modifier.height(20.dp))
                SectionLabel("Wellness")
                Spacer(modifier = Modifier.height(8.dp))
                WellnessGrid(data = data, modifier = Modifier.padding(horizontal = 16.dp))
            }

            // ── Focus Timeline ────────────────────────────────────────
            item {
                Spacer(modifier = Modifier.height(20.dp))
                SectionLabel("Focus")
                Spacer(modifier = Modifier.height(8.dp))
                FocusTimelineCard(
                    sessions = data.focusSessions,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            // ── Weekly Performance ────────────────────────────────────
            item {
                Spacer(modifier = Modifier.height(20.dp))
                SectionLabel("Weekly View")
                Spacer(modifier = Modifier.height(8.dp))
                WeeklyPulseChart(
                    weeklyData = data.weeklyPerformance,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            // ── Achievements ──────────────────────────────────────────
            item {
                Spacer(modifier = Modifier.height(20.dp))
                SectionLabel("Achievements")
                Spacer(modifier = Modifier.height(8.dp))
                AchievementsRow(
                    achievements = data.recentAchievements
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
//  Ambient Background
// ─────────────────────────────────────────────────────────────
@Composable
private fun AmbientBackground() {
    val primaryColor = MaterialTheme.colorScheme.primary
    val tertiaryColor = MaterialTheme.colorScheme.tertiary
    Canvas(modifier = Modifier.fillMaxSize()) {
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(primaryColor.copy(alpha = 0.08f), Color.Transparent),
                center = Offset(size.width * 0.8f, size.height * 0.1f),
                radius = 320.dp.toPx()
            ),
            center = Offset(size.width * 0.8f, size.height * 0.1f),
            radius = 320.dp.toPx()
        )
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(tertiaryColor.copy(alpha = 0.06f), Color.Transparent),
                center = Offset(size.width * 0.1f, size.height * 0.35f),
                radius = 240.dp.toPx()
            ),
            center = Offset(size.width * 0.1f, size.height * 0.35f),
            radius = 240.dp.toPx()
        )
    }
}

// ─────────────────────────────────────────────────────────────
//  Section Label
// ─────────────────────────────────────────────────────────────
@Composable
private fun SectionLabel(title: String) {
    Text(
        text = title.uppercase(),
        style = MaterialTheme.typography.labelSmall.copy(
            letterSpacing = 2.sp,
            fontWeight = FontWeight.SemiBold
        ),
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f),
        modifier = Modifier.padding(horizontal = 20.dp)
    )
}

// ─────────────────────────────────────────────────────────────
//  Dashboard Header
// ─────────────────────────────────────────────────────────────
@Composable
private fun DashboardHeader(data: AnalyticsData, onBack: () -> Unit) {
    val today = remember {
        LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, d MMM"))
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(top = 56.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .shadow(4.dp, CircleShape, clip = false)
                .background(MaterialTheme.colorScheme.surface, CircleShape)
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Analytics",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.5).sp
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = today,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
            )
        }

        val overallScore = remember(data.workLifeBalanceScore, data.productivityScore) {
            ((data.workLifeBalanceScore + data.productivityScore) / 2).coerceIn(0, 100)
        }
        Surface(
            shape = PillShape,
            color = GoldenAmber.copy(alpha = 0.12f)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Icon(
                    Icons.Default.Bolt,
                    contentDescription = null,
                    tint = GoldenAmber,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = "$overallScore",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = GoldenAmber
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
//  KPI Strip
// ─────────────────────────────────────────────────────────────
@Composable
private fun KpiStrip(data: AnalyticsData) {
    val items = remember(data) {
        listOf(
            KpiItem("Focus", "${data.focusScore}%", Icons.Default.Psychology, VioletPurple, PurpleSurface, data.focusTrend),
            KpiItem("Hours", "${String.format(Locale.getDefault(), "%.1f", data.workHoursToday)}h", Icons.Default.Timer, SapphireBlue, BlueSurface, data.workHoursTrend),
            KpiItem("Habits", "${(data.habitCompletionRate * 100).toInt()}%", Icons.Default.CheckCircle, EmeraldGreen, GreenSurface, 0f),
            KpiItem("Calories", "${data.totalCaloriesToday.toInt()}", Icons.Default.LocalFireDepartment, GoldenAmber, AmberSurface, data.caloriesTrend)
        )
    }

    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(items.size) { idx ->
            KpiChip(item = items[idx], index = idx)
        }
    }
}

data class KpiItem(
    val label: String,
    val value: String,
    val icon: ImageVector,
    val color: Color,
    val surfaceColor: Color,
    val trend: Float
)

@Composable
private fun KpiChip(item: KpiItem, index: Int) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(index * 80L)
        visible = true
    }
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(400)) + slideInVertically(tween(400)) { it / 2 }
    ) {
        Card(
            modifier = Modifier
                .width(90.dp)
                .shadow(6.dp, CardShape, clip = false),
            shape = CardShape,
            colors = CardDefaults.cardColors(containerColor = item.surfaceColor),
            elevation = CardDefaults.cardElevation(0.dp)
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                horizontalAlignment = Alignment.Start
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .background(item.color.copy(alpha = 0.15f), RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(item.icon, null, tint = item.color, modifier = Modifier.size(18.dp))
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = item.value,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                    color = item.color
                )
                Text(
                    text = item.label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (item.trend != 0f) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            if (item.trend > 0) Icons.AutoMirrored.Filled.TrendingUp else Icons.AutoMirrored.Filled.TrendingDown,
                            null,
                            tint = if (item.trend > 0) EmeraldGreen else CoralRed,
                            modifier = Modifier.size(10.dp)
                        )
                        Text(
                            text = "${if (item.trend > 0) "+" else ""}${item.trend.toInt()}%",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                            color = if (item.trend > 0) EmeraldGreen else CoralRed
                        )
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
//  Balance Ring Card
// ─────────────────────────────────────────────────────────────
@Composable
private fun BalanceRingCard(
    score: Int,
    productivityScore: Int,
    modifier: Modifier = Modifier
) {
    val animScore = remember { Animatable(0f) }
    val animProd  = remember { Animatable(0f) }

    LaunchedEffect(score, productivityScore) {
        animScore.animateTo(score / 100f, tween(1400, easing = FastOutSlowInEasing))
    }
    LaunchedEffect(productivityScore) {
        delay(200)
        animProd.animateTo(productivityScore / 100f, tween(1200, easing = FastOutSlowInEasing))
    }

    val scoreColor = when {
        score >= 75 -> EmeraldGreen
        score >= 45 -> GoldenAmber
        else        -> CoralRed
    }

    val trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f)
    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(8.dp, CardShape, clip = false),
        shape = CardShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(
                            brush = Brush.linearGradient(listOf(SapphireBlue, VioletPurple)),
                            shape = RoundedCornerShape(10.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Psychology, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                }
                Text("Balance Score", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(110.dp)) {
                Canvas(modifier = Modifier.size(110.dp)) {
                    val stroke = 9.dp.toPx()
                    val innerStroke = 6.dp.toPx()
                    val radius = (size.minDimension - stroke) / 2
                    val innerRadius = radius - stroke - 6.dp.toPx()

                    // Outer track
                    drawArc(
                        color = trackColor,
                        startAngle = -90f, sweepAngle = 360f,
                        useCenter = false,
                        style = Stroke(stroke, cap = StrokeCap.Round),
                        topLeft = Offset(stroke / 2, stroke / 2),
                        size = Size(size.width - stroke, size.height - stroke)
                    )
                    // Outer progress — balance
                    drawArc(
                        color = scoreColor,
                        startAngle = -90f,
                        sweepAngle = animScore.value * 360f,
                        useCenter = false,
                        style = Stroke(stroke, cap = StrokeCap.Round),
                        topLeft = Offset(stroke / 2, stroke / 2),
                        size = Size(size.width - stroke, size.height - stroke)
                    )
                    // Inner track
                    val innerOff = stroke + innerStroke / 2 + 6.dp.toPx()
                    drawArc(
                        color = trackColor,
                        startAngle = -90f, sweepAngle = 360f,
                        useCenter = false,
                        style = Stroke(innerStroke, cap = StrokeCap.Round),
                        topLeft = Offset(innerOff, innerOff),
                        size = Size(size.width - innerOff * 2, size.height - innerOff * 2)
                    )
                    // Inner progress — productivity
                    drawArc(
                        color = SapphireBlue,
                        startAngle = -90f,
                        sweepAngle = animProd.value * 360f,
                        useCenter = false,
                        style = Stroke(innerStroke, cap = StrokeCap.Round),
                        topLeft = Offset(innerOff, innerOff),
                        size = Size(size.width - innerOff * 2, size.height - innerOff * 2)
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${(animScore.value * 100).toInt()}",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.ExtraBold
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 28.sp
                    )
                    Text(
                        text = "/ 100",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                    )
                }
            }

            Spacer(modifier = Modifier.width(20.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Work-Life Balance",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    when {
                        score >= 75 -> "Excellent balance — keep it up!"
                        score >= 45 -> "Good, with room to improve."
                        else        -> "Take time to recharge."
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f),
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Legend rows
                RingLegendRow(label = "Balance", value = score, color = scoreColor)
                Spacer(modifier = Modifier.height(6.dp))
                RingLegendRow(label = "Productivity", value = productivityScore, color = SapphireBlue)
            }
                }
            }
    }
}

@Composable
private fun RingLegendRow(label: String, value: Int, color: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(color, CircleShape)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
            modifier = Modifier.weight(1f)
        )
        Text(
            "$value%",
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = color
        )
    }
}

// ─────────────────────────────────────────────────────────────
//  Financial Overview Card
// ─────────────────────────────────────────────────────────────
@Composable
private fun FinancialOverviewCard(
    incomes: List<Income>,
    expenses: List<Expense>,
    savings: Double,
    modifier: Modifier = Modifier
) {
    val totalIncome  = incomes.sumOf { it.amount }
    val totalExpense = expenses.sumOf { it.amount }
    val savingsRate  = if (totalIncome > 0) ((totalIncome - totalExpense) / totalIncome * 100) else 0.0

    val animIncome  by animateFloatAsState(totalIncome.toFloat(),  tween(1100), label = "inc")
    val animExpense by animateFloatAsState(totalExpense.toFloat(), tween(1100), label = "exp")
    val animSavings by animateFloatAsState(savings.toFloat(),      tween(1100), label = "sav")

    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(8.dp, CardShape, clip = false),
        shape = CardShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(
                            brush = Brush.linearGradient(listOf(EmeraldGreen, SapphireBlue)),
                            shape = RoundedCornerShape(10.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.AccountBalance, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                }
                Text("Finances", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                FinanceColumn("Income",   animIncome.toDouble(),  EmeraldGreen)
                FinanceDivider()
                FinanceColumn("Expenses", animExpense.toDouble(), CoralRed)
                FinanceDivider()
                FinanceColumn("Savings",  animSavings.toDouble(), SapphireBlue)
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "Savings rate",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    "${String.format(Locale.getDefault(), "%.1f", savingsRate)}%",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                    color = if (savingsRate >= 20) EmeraldGreen else GoldenAmber
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            SlimProgressBar(
                progress = (savingsRate / 100f).toFloat().coerceIn(0f, 1f),
                color = if (savingsRate >= 20) EmeraldGreen else GoldenAmber
            )

            if (expenses.isNotEmpty()) {
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    "Top categories",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(10.dp))
                ExpenseCategoryBars(expenses)
            }
        }
    }
}

@Composable
private fun FinanceColumn(label: String, value: Double, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "৳${formatShort(value)}",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = color
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f)
        )
    }
}

@Composable
private fun FinanceDivider() {
    Box(
        modifier = Modifier
            .width(1.dp)
            .height(36.dp)
            .background(MaterialTheme.colorScheme.outlineVariant)
    )
}

@Composable
private fun SlimProgressBar(progress: Float, color: Color, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(4.dp)
            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f), RoundedCornerShape(2.dp))
            .clip(RoundedCornerShape(2.dp))
    ) {
        val animP by animateFloatAsState(progress, tween(800), label = "bar")
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(animP)
                .background(
                    Brush.horizontalGradient(listOf(color, color.copy(alpha = 0.6f))),
                    RoundedCornerShape(2.dp)
                )
        )
    }
}

@Composable
private fun ExpenseCategoryBars(expenses: List<Expense>) {
    val grouped = remember(expenses) {
        expenses.groupBy { it.category }
            .mapValues { it.value.sumOf { e -> e.amount } }
            .toList()
            .sortedByDescending { it.second }
            .take(4)
    }
    val total = grouped.sumOf { it.second }
    val colors = listOf(SapphireBlue, VioletPurple, GoldenAmber, EmeraldGreen)

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        grouped.forEachIndexed { i, (cat, amount) ->
            val pct = if (total > 0) (amount / total).toFloat() else 0f
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .background(colors[i % colors.size], CircleShape)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    cat.name,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
                    modifier = Modifier.width(70.dp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                SlimProgressBar(
                    progress = pct,
                    color = colors[i % colors.size],
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 8.dp)
                )
                Text(
                    "${(pct * 100).toInt()}%",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                    modifier = Modifier.width(34.dp),
                    textAlign = TextAlign.End
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
//  Monthly Bar Chart Card
// ─────────────────────────────────────────────────────────────
@Composable
private fun MonthlyBarChartCard(
    monthlyData: List<MonthlyFinancialData>,
    modifier: Modifier = Modifier
) {
    if (monthlyData.isEmpty()) return

    val maxVal = remember(monthlyData) {
        monthlyData.maxOfOrNull { maxOf(it.income, it.expense) }?.coerceAtLeast(1.0) ?: 1.0
    }
    val animProgress = remember { Animatable(0f) }
    LaunchedEffect(maxVal) {
        animProgress.animateTo(1f, tween(900, easing = FastOutSlowInEasing))
    }

    // Tooltip state
    var selectedMonth by remember { mutableStateOf<Int?>(null) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(8.dp, CardShape, clip = false),
        shape = CardShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(
                            brush = Brush.linearGradient(listOf(SapphireBlue, EmeraldGreen)),
                            shape = RoundedCornerShape(10.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.BarChart, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                }
                Text("Monthly Overview", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.weight(1f))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    LegendDot(EmeraldGreen, "Income")
                    LegendDot(CoralRed, "Expense")
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Bar chart
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                monthlyData.forEachIndexed { idx, month ->
                    val incH = ((month.income / maxVal) * 100 * animProgress.value).dp
                        .coerceAtLeast(3.dp)
                    val expH = ((month.expense / maxVal) * 100 * animProgress.value).dp
                        .coerceAtLeast(3.dp)
                    val isSelected = selectedMonth == idx

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .weight(1f)
                            .pointerInput(idx) {
                                detectTapGestures {
                                    selectedMonth = if (selectedMonth == idx) null else idx
                                }
                            }
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(2.dp),
                            verticalAlignment = Alignment.Bottom
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(10.dp)
                                    .height(incH)
                                    .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                    .background(
                                        if (isSelected) EmeraldGreen
                                        else EmeraldGreen.copy(alpha = 0.55f)
                                    )
                            )
                            Box(
                                modifier = Modifier
                                    .width(10.dp)
                                    .height(expH)
                                    .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                    .background(
                                        if (isSelected) CoralRed
                                        else CoralRed.copy(alpha = 0.55f)
                                    )
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            month.month,
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                            color = if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), thickness = 0.5.dp)
            Spacer(modifier = Modifier.height(10.dp))

            // Income row — green values across all months
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                monthlyData.forEach { month ->
                    Text(
                        "৳${formatShort(month.income)}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 9.sp,
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = EmeraldGreen,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f),
                        maxLines = 1
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Expense row — red values across all months
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                monthlyData.forEach { month ->
                    Text(
                        "৳${formatShort(month.expense)}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 9.sp,
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = CoralRed,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f),
                        maxLines = 1
                    )
                }
            }

            // Tooltip for selected month
            selectedMonth?.let { idx ->
                if (idx < monthlyData.size) {
                    val m = monthlyData[idx]
                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 1.dp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(m.month, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurface)
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            Text(
                                "৳${formatShort(m.income)}",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = EmeraldGreen
                            )
                            Text(
                                "৳${formatShort(m.expense)}",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = CoralRed
                            )
                            val net = m.income - m.expense
                            Text(
                                "${if (net >= 0) "+" else ""}৳${formatShort(net)}",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = if (net >= 0) EmeraldGreen else GoldenAmber
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LegendDot(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .background(color, CircleShape)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
    }
}

// ─────────────────────────────────────────────────────────────
//  Productivity Ring Card
// ─────────────────────────────────────────────────────────────
@Composable
private fun ProductivityRingCard(
    score: Int,
    trend: Float,
    modifier: Modifier = Modifier
) {
    val anim = remember { Animatable(0f) }
    LaunchedEffect(score) {
        anim.animateTo(score / 100f, tween(1200, easing = FastOutSlowInEasing))
    }

    val trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f)
    Card(
        modifier = modifier.shadow(6.dp, CardShape, clip = false),
        shape = CardShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Productivity",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(12.dp))
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(80.dp)) {
                Canvas(Modifier.size(80.dp)) {
                    val s = 7.dp.toPx()
                    drawArc(
                        color = trackColor,
                        startAngle = -90f, sweepAngle = 360f, useCenter = false,
                        style = Stroke(s, cap = StrokeCap.Round),
                        topLeft = Offset(s / 2, s / 2),
                        size = Size(size.width - s, size.height - s)
                    )
                    drawArc(
                        brush = Brush.sweepGradient(listOf(SapphireBlue, VioletPurple, SapphireBlue)),
                        startAngle = -90f,
                        sweepAngle = anim.value * 360f,
                        useCenter = false,
                        style = Stroke(s, cap = StrokeCap.Round),
                        topLeft = Offset(s / 2, s / 2),
                        size = Size(size.width - s, size.height - s)
                    )
                }
                Text(
                    "${(anim.value * 100).toInt()}",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            if (trend != 0f) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        if (trend > 0) Icons.AutoMirrored.Filled.TrendingUp else Icons.AutoMirrored.Filled.TrendingDown,
                        null,
                        tint = if (trend > 0) EmeraldGreen else CoralRed,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        "${if (trend > 0) "+" else ""}${trend.toInt()}%",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (trend > 0) EmeraldGreen else CoralRed
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
//  Habit Streak Card
// ─────────────────────────────────────────────────────────────
@Composable
private fun HabitStreakCard(
    habits: List<Habit>,
    completionRate: Float,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.shadow(6.dp, CardShape, clip = false),
        shape = CardShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(0.dp)
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
                    "Habits",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    "${(completionRate * 100).toInt()}%",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = EmeraldGreen
                )
            }
            Spacer(modifier = Modifier.height(10.dp))

            habits.take(3).forEach { habit ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .background(
                                    if (habit.streak > 0) EmeraldGreen.copy(0.15f) else Color.Transparent,
                                CircleShape
                            )
                            .border(1.dp, if (habit.streak > 0) EmeraldGreen.copy(0.5f) else MaterialTheme.colorScheme.outlineVariant, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        if (habit.streak > 0) {
                            Icon(
                                Icons.Default.Check, null,
                                tint = EmeraldGreen,
                                modifier = Modifier.size(9.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        habit.name,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        "${habit.streak}d",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                        color = GoldenAmber
                    )
                }
            }

            if (habits.size > 3) {
                Text(
                    "+${habits.size - 3} more",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.25f),
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))
            SlimProgressBar(completionRate.coerceIn(0f, 1f), EmeraldGreen)
        }
    }
}

// ─────────────────────────────────────────────────────────────
//  Wellness Grid
// ─────────────────────────────────────────────────────────────
@Composable
private fun WellnessGrid(data: AnalyticsData, modifier: Modifier = Modifier) {
    val items = remember(data) {
        listOf(
            WellnessEntry("Hydration", data.totalWaterToday.toInt(), 2000, "ml",  Icons.Default.WaterDrop, SapphireBlue),
            WellnessEntry("Sleep",     data.sleepHours.toInt(),       8,    "h",   Icons.Default.Bedtime,   VioletPurple),
            WellnessEntry("Calories",  data.totalCaloriesToday.toInt(), 2000, "cal", Icons.Default.LocalFireDepartment, GoldenAmber),
            WellnessEntry("Steps",     data.stepsToday,               10000,"",   Icons.Default.DirectionsWalk, EmeraldGreen)
        )
    }

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        items.chunked(2).forEach { row ->
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                row.forEach { entry ->
                    WellnessCard(entry = entry, modifier = Modifier.weight(1f))
                }
                if (row.size == 1) Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}

data class WellnessEntry(
    val label: String, val value: Int, val target: Int,
    val unit: String, val icon: ImageVector, val color: Color
)

@Composable
private fun WellnessCard(entry: WellnessEntry, modifier: Modifier = Modifier) {
    val progress = (entry.value.toFloat() / entry.target).coerceIn(0f, 1f)
    val anim by animateFloatAsState(progress, tween(1000), label = "well")

    Card(
        modifier = modifier.shadow(6.dp, CardShape, clip = false),
        shape = CardShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(44.dp)) {
                Canvas(Modifier.size(44.dp)) {
                    val s = 4.dp.toPx()
                    drawArc(
                        color = entry.color.copy(0.1f),
                        startAngle = -90f, sweepAngle = 360f, useCenter = false,
                        style = Stroke(s, cap = StrokeCap.Round),
                        topLeft = Offset(s / 2, s / 2),
                        size = Size(size.width - s, size.height - s)
                    )
                    drawArc(
                        color = entry.color,
                        startAngle = -90f, sweepAngle = anim * 360f, useCenter = false,
                        style = Stroke(s, cap = StrokeCap.Round),
                        topLeft = Offset(s / 2, s / 2),
                        size = Size(size.width - s, size.height - s)
                    )
                }
                Icon(entry.icon, null, tint = entry.color, modifier = Modifier.size(18.dp))
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = "${entry.value}${entry.unit}",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = entry.label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f)
                )
                Text(
                    text = "/ ${entry.target}${entry.unit}",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                    color = entry.color.copy(alpha = 0.5f)
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
//  Focus Timeline Card
// ─────────────────────────────────────────────────────────────
@Composable
private fun FocusTimelineCard(
    sessions: List<FocusSession>,
    modifier: Modifier = Modifier
) {
    val deepMin     = sessions.filter { it.type == FocusType.DEEP_WORK  }.sumOf { it.duration } / 60
    val pomodoroMin = sessions.filter { it.type == FocusType.POMODORO   }.sumOf { it.duration } / 60
    val total       = deepMin + pomodoroMin

    val animDeep     by animateFloatAsState(deepMin.toFloat(),     tween(1000), label = "deep")
    val animPomodoro by animateFloatAsState(pomodoroMin.toFloat(), tween(1000), label = "pom")

    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(8.dp, CardShape, clip = false),
        shape = CardShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(
                            brush = Brush.linearGradient(listOf(SapphireBlue, VioletPurple)),
                            shape = RoundedCornerShape(10.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Psychology, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                }
                Text("Focus", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "$total min",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        "Total focus today",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    FocusTypeChip(
                        label = "Deep",
                        minutes = animDeep.toLong(),
                        color = SapphireBlue
                    )
                    FocusTypeChip(
                        label = "Pomodoro",
                        minutes = animPomodoro.toLong(),
                        color = GoldenAmber
                    )
                }
            }

            if (total > 0) {
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                ) {
                    val deepFrac = (deepMin.toFloat() / total).coerceIn(0f, 1f)
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .weight(deepFrac.coerceAtLeast(0.01f))
                            .background(SapphireBlue)
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .weight((1f - deepFrac).coerceAtLeast(0.01f))
                            .background(GoldenAmber)
                    )
                }
            }
        }
    }
}

@Composable
private fun FocusTypeChip(label: String, minutes: Long, color: Color) {
    Column(
        modifier = Modifier
            .background(color.copy(alpha = 0.1f), ChipShape)
            .border(1.dp, color.copy(alpha = 0.25f), ChipShape)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "${minutes}m",
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            color = color
        )
        Text(
            label,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
            color = color.copy(alpha = 0.6f)
        )
    }
}

// ─────────────────────────────────────────────────────────────
//  Weekly Pulse Chart
// ─────────────────────────────────────────────────────────────
@Composable
private fun WeeklyPulseChart(
    weeklyData: List<WeeklyPerformance>,
    modifier: Modifier = Modifier
) {
    if (weeklyData.isEmpty()) return

    val animProgress = remember { Animatable(0f) }
    LaunchedEffect(weeklyData) {
        animProgress.animateTo(1f, tween(1200, easing = FastOutSlowInEasing))
    }

    val density = LocalDensity.current

    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(8.dp, CardShape, clip = false),
        shape = CardShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(
                            brush = Brush.linearGradient(listOf(SapphireBlue, VioletPurple)),
                            shape = RoundedCornerShape(10.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.ShowChart, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                }
                Text("Weekly Pulse", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(16.dp))

            val avg = weeklyData.map { it.productivityScore }.average().toInt()

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                weeklyData.take(7).forEachIndexed { idx, day ->
                    val heightFraction = (day.productivityScore / 100f) * animProgress.value
                    val barH = (heightFraction * 100).dp.coerceAtLeast(4.dp)
                    val isToday = day.date == LocalDate.now()

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        if (isToday) {
                            Box(
                                modifier = Modifier
                                    .size(5.dp)
                                    .background(GoldenAmber, CircleShape)
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                        }
                        Box(
                            modifier = Modifier
                                .width(26.dp)
                                .height(barH)
                                .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                                .background(
                                    if (isToday) SapphireBlue
                                    else SapphireBlue.copy(alpha = 0.35f)
                                )
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            day.day.take(2),
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                            color = if (isToday) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(modifier = Modifier.height(10.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("7-day avg", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("$avg / 100", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = SapphireBlue)
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
//  Achievements Row
// ─────────────────────────────────────────────────────────────
@Composable
private fun AchievementsRow(achievements: List<Achievement>) {
    if (achievements.isEmpty()) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .shadow(4.dp, CardShape, clip = false),
            shape = CardShape,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(0.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Outlined.EmojiEvents,
                        null,
                        tint = MaterialTheme.colorScheme.onSurface.copy(0.15f),
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        "No achievements yet",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        return
    }

    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(achievements.size) { idx ->
            val a = achievements[idx]
            Card(
                modifier = Modifier
                    .width(100.dp)
                    .shadow(4.dp, CardShape, clip = false),
                shape = CardShape,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(GoldenAmber.copy(0.12f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.EmojiEvents,
                            null,
                            tint = GoldenAmber,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        a.name,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                        textAlign = TextAlign.Center,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        a.unlockedTimestamp?.let {
                            java.time.Instant.ofEpochMilli(it)
                                .atZone(java.time.ZoneId.systemDefault())
                                .toLocalDate()
                                .format(DateTimeFormatter.ofPattern("MMM d"))
                        } ?: "Locked",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.25f)
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
//  Helpers
// ─────────────────────────────────────────────────────────────
private fun formatShort(value: Double): String {
    return when {
        value >= 1_000_000 -> String.format(Locale.getDefault(), "%.1fM", value / 1_000_000)
        value >= 1_000     -> String.format(Locale.getDefault(), "%.1fK", value / 1_000)
        else               -> String.format(Locale.getDefault(), "%.0f", value)
    }
}

enum class AnalyticsPeriod(val displayName: String) {
    WEEK("Week"), MONTH("Month"), QUARTER("Quarter"), YEAR("Year")
}