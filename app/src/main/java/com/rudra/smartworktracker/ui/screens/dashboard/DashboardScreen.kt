package com.rudra.smartworktracker.ui.screens.dashboard

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animateColor
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.automirrored.outlined.TrendingDown
import androidx.compose.material.icons.automirrored.outlined.TrendingUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.BeachAccess
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Work
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.AttachMoney
import androidx.compose.material.icons.outlined.MoneyOff
import androidx.compose.material.icons.outlined.Remove
import androidx.compose.material.icons.outlined.Savings
import androidx.compose.material.icons.outlined.ShowChart
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rudra.smartworktracker.data.AppDatabase
import com.rudra.smartworktracker.data.entity.Income
import com.rudra.smartworktracker.model.Expense
import com.rudra.smartworktracker.model.ExpenseCategory
import com.rudra.smartworktracker.model.ExpenseCategory.*
import com.rudra.smartworktracker.model.WorkLog
import com.rudra.smartworktracker.model.WorkType
import com.rudra.smartworktracker.ui.DashboardUiState
import com.rudra.smartworktracker.ui.FinancialSummary
import com.rudra.smartworktracker.ui.MonthlyStats
import com.rudra.smartworktracker.ui.WorkLogUi
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.TextStyle
import java.util.Calendar
import java.util.Locale
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

// ─────────────────────────────────────────────────────────────────────────────
// Design Tokens
// ─────────────────────────────────────────────────────────────────────────────

private val CardShape = RoundedCornerShape(20.dp)
private val PillShape = RoundedCornerShape(50.dp)
private val ChipShape = RoundedCornerShape(12.dp)

private val EmeraldGreen = Color(0xFF00C896)
private val CoralRed = Color(0xFFFF5757)
private val SapphireBlue = Color(0xFF3B82F6)
private val GoldenAmber = Color(0xFFF59E0B)
private val VioletPurple = Color(0xFF8B5CF6)
private val SlateGray = Color(0xFF64748B)

private val GreenSurface = Color(0xFFE6FBF4)
private val RedSurface = Color(0xFFFFEDED)
private val BlueSurface = Color(0xFFEFF6FF)
private val AmberSurface = Color(0xFFFFFBEB)
private val PurpleSurface = Color(0xFFF5F3FF)

// ─────────────────────────────────────────────────────────────────────────────
// Dashboard Screen
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onNavigateToAddEntry: () -> Unit,
    onNavigateToIncome: () -> Unit,
    onNavigateToExpense: () -> Unit,
    onNavigateToLoan: () -> Unit
) {
    val context = LocalContext.current
    val viewModel: DashboardViewModel = viewModel(
        factory = DashboardViewModel.factory(AppDatabase.getDatabase(context), context)
    )
    val uiState by viewModel.uiState.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    val hasRecentActivities by remember(uiState.recentActivities) {
        derivedStateOf { uiState.recentActivities.isNotEmpty() }
    }

    Scaffold(
        topBar = {},
        containerColor = colorScheme.background,
        floatingActionButton = {
            QuickActionMenu(
                onNavigateToAddEntry = onNavigateToAddEntry,
                onNavigateToIncome = onNavigateToIncome,
                onNavigateToExpense = onNavigateToExpense,
                onNavigateToLoan = onNavigateToLoan
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 20.dp)
        ) {
            item { Header(userName = uiState.userName) }
            item { NetBalanceHeroCard(financialSummary = uiState.financialSummary) }
            item { TripleMetricRow(financialSummary = uiState.financialSummary) }
            item { DailySnapshotCard(financialSummary = uiState.financialSummary) }
            item { SavingsRatioCard(financialSummary = uiState.financialSummary) }
            item { OvertimeSummaryCard(financialSummary = uiState.financialSummary) }
            item { MonthlyStatsCard(stats = uiState.monthlyStats) }
            item { WorkAttendanceRingCard(stats = uiState.monthlyStats) }
            item { CategorySummaryCard(expensesByCategory = uiState.expensesByCategory) }
            if (hasRecentActivities) {
                item { WeeklyActivityTimeline(activities = uiState.recentActivities) }
            }
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
            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Header
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun Header(userName: String?) {
    val displayName = remember(userName) { userName ?: "Rudra" }
    val greeting = remember {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        when {
            hour < 12 -> "Good Morning"
            hour < 17 -> "Good Afternoon"
            else -> "Good Evening"
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Gradient avatar
        Box(
            modifier = Modifier
                .size(52.dp)
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(SapphireBlue, VioletPurple)
                    ),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = displayName.first().uppercase(),
                style = typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "$greeting, $displayName 👋",
                style = typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = colorScheme.onSurface
            )
            Text(
                text = "Here's your financial overview",
                style = typography.bodySmall,
                color = colorScheme.onSurfaceVariant
            )
        }

        // Live indicator badge
        Surface(
            shape = PillShape,
            color = EmeraldGreen.copy(alpha = 0.12f)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(7.dp)
                        .background(EmeraldGreen, CircleShape)
                )
                Text(
                    text = "Live",
                    style = typography.labelSmall,
                    color = EmeraldGreen,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Net Balance Hero Card (replaces old FinancialSummaryChart net section)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun NetBalanceHeroCard(financialSummary: FinancialSummary) {
    val isPositive = financialSummary.netSavings >= 0
    val accentColor = if (isPositive) EmeraldGreen else CoralRed
    val bgColor = if (isPositive) GreenSurface else RedSurface

    var animatedBalance by remember { mutableDoubleStateOf(0.0) }
    val animatedValue by animateFloatAsState(
        targetValue = animatedBalance.toFloat(),
        animationSpec = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
        label = "balance_anim"
    )

    LaunchedEffect(financialSummary.netSavings) {
        delay(200)
        animatedBalance = financialSummary.netSavings
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 12.dp, shape = CardShape, clip = false),
        shape = CardShape,
        colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Label row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Outlined.Savings,
                    contentDescription = null,
                    tint = SlateGray,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    "All-Time Net Balance",
                    style = typography.bodyMedium,
                    color = colorScheme.onSurfaceVariant
                )
            }

            Spacer(Modifier.height(12.dp))

            // Big number
            Text(
                text = "৳${"%,.0f".format(animatedValue)}",
                style = typography.displaySmall,
                fontWeight = FontWeight.ExtraBold,
                color = accentColor
            )

            Spacer(Modifier.height(12.dp))

            // Status pill
            Surface(
                shape = PillShape,
                color = bgColor
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        if (isPositive) Icons.AutoMirrored.Outlined.TrendingUp
                        else Icons.AutoMirrored.Outlined.TrendingDown,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = if (isPositive) "Healthy surplus" else "Deficit — review expenses",
                        style = typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = accentColor
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            // All-time income vs expense mini bar
            AllTimeIncomeExpenseBar(
                income = financialSummary.allTimeIncome,
                expense = financialSummary.allTimeExpense
            )
        }
    }
}

@Composable
private fun AllTimeIncomeExpenseBar(income: Double, expense: Double) {
    val total = (income + expense).coerceAtLeast(1.0)
    val incomeRatio = (income / total).toFloat().coerceIn(0f, 1f)

    val animatedRatio = remember { Animatable(0f) }
    LaunchedEffect(incomeRatio) {
        animatedRatio.animateTo(incomeRatio, tween(1000, easing = FastOutSlowInEasing))
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Box(Modifier.size(10.dp).background(EmeraldGreen, CircleShape))
                Text("Income ৳${"%,.0f".format(income)}", style = typography.labelSmall, color = colorScheme.onSurfaceVariant)
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Box(Modifier.size(10.dp).background(CoralRed, CircleShape))
                Text("Expense ৳${"%,.0f".format(expense)}", style = typography.labelSmall, color = colorScheme.onSurfaceVariant)
            }
        }

        Spacer(Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .clip(RoundedCornerShape(5.dp))
                .background(CoralRed.copy(alpha = 0.25f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(animatedRatio.value)
                    .height(10.dp)
                    .background(
                        brush = Brush.horizontalGradient(
                            listOf(EmeraldGreen, SapphireBlue)
                        ),
                        shape = RoundedCornerShape(5.dp)
                    )
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Triple Metric Row  — Monthly Income / Expense / Savings
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun TripleMetricRow(financialSummary: FinancialSummary) {
    var animatedIncome by remember { mutableFloatStateOf(0f) }
    var animatedExpense by remember { mutableFloatStateOf(0f) }
    var animatedSavings by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(financialSummary) {
        delay(300)
        animatedIncome = financialSummary.totalIncome.toFloat()
        delay(150)
        animatedExpense = financialSummary.totalExpense.toFloat()
        delay(150)
        animatedSavings = financialSummary.monthlyNetSavings.toFloat()
    }

    val incomeAnim by animateFloatAsState(animatedIncome, tween(900, easing = FastOutSlowInEasing), label = "i")
    val expenseAnim by animateFloatAsState(animatedExpense, tween(900, easing = FastOutSlowInEasing), label = "e")
    val savingsAnim by animateFloatAsState(animatedSavings, tween(900, easing = FastOutSlowInEasing), label = "s")

    Text(
        "This Month",
        style = typography.labelLarge,
        fontWeight = FontWeight.Bold,
        color = colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(bottom = 4.dp, start = 2.dp)
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        GlassMetricCard(
            label = "Income",
            value = "৳${"%,.0f".format(incomeAnim)}",
            icon = Icons.AutoMirrored.Filled.TrendingUp,
            accentColor = EmeraldGreen,
            bgColor = GreenSurface,
            modifier = Modifier.weight(1f)
        )
        GlassMetricCard(
            label = "Expense",
            value = "৳${"%,.0f".format(expenseAnim)}",
            icon = Icons.AutoMirrored.Outlined.TrendingDown,
            accentColor = CoralRed,
            bgColor = RedSurface,
            modifier = Modifier.weight(1f)
        )
        GlassMetricCard(
            label = "Saved",
            value = "৳${"%,.0f".format(savingsAnim)}",
            icon = Icons.Default.CheckCircle,
            accentColor = if (financialSummary.monthlyNetSavings >= 0) EmeraldGreen else CoralRed,
            bgColor = if (financialSummary.monthlyNetSavings >= 0) GreenSurface else RedSurface,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun GlassMetricCard(
    label: String,
    value: String,
    icon: ImageVector,
    accentColor: Color,
    bgColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.shadow(6.dp, CardShape, clip = false),
        shape = CardShape,
        colors = CardDefaults.cardColors(containerColor = bgColor),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .background(accentColor.copy(alpha = 0.15f), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(18.dp))
            }
            Text(
                value,
                style = typography.titleSmall,
                fontWeight = FontWeight.ExtraBold,
                color = accentColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                label,
                style = typography.labelSmall,
                color = colorScheme.onSurfaceVariant
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Daily Snapshot Card
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun DailySnapshotCard(financialSummary: FinancialSummary) {
    var animatedIncome by remember { mutableFloatStateOf(0f) }
    var animatedExpense by remember { mutableFloatStateOf(0f) }
    var animatedSavings by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(financialSummary) {
        delay(400)
        animatedIncome = financialSummary.dailyIncome.toFloat()
        animatedExpense = financialSummary.dailyExpense.toFloat()
        animatedSavings = financialSummary.dailySavings.toFloat()
    }

    val incomeAnim by animateFloatAsState(animatedIncome, tween(800), label = "di")
    val expenseAnim by animateFloatAsState(animatedExpense, tween(800), label = "de")
    val savingsAnim by animateFloatAsState(animatedSavings, tween(800), label = "ds")

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp, CardShape, clip = false),
        shape = CardShape,
        colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(
                            brush = Brush.linearGradient(listOf(GoldenAmber, CoralRed)),
                            shape = RoundedCornerShape(10.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.BarChart, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                }
                Text("Today's Snapshot", style = typography.titleMedium, fontWeight = FontWeight.Bold)
            }

            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                DailyMetricItem("Earned", "৳${"%,.0f".format(incomeAnim)}", EmeraldGreen)
                VerticalDividerLine()
                DailyMetricItem("Spent", "৳${"%,.0f".format(expenseAnim)}", CoralRed)
                VerticalDividerLine()
                DailyMetricItem(
                    "Saved",
                    "৳${"%,.0f".format(savingsAnim)}",
                    if (financialSummary.dailySavings >= 0) EmeraldGreen else CoralRed
                )
            }
        }
    }
}

@Composable
private fun DailyMetricItem(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = typography.titleMedium, fontWeight = FontWeight.ExtraBold, color = color)
        Spacer(Modifier.height(2.dp))
        Text(label, style = typography.labelSmall, color = colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun VerticalDividerLine() {
    Box(
        modifier = Modifier
            .width(1.dp)
            .height(40.dp)
            .background(colorScheme.outlineVariant)
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// Savings Ratio Card (NEW)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun SavingsRatioCard(financialSummary: FinancialSummary) {
    val savingsRate = if (financialSummary.totalIncome > 0)
        (financialSummary.monthlyNetSavings / financialSummary.totalIncome).coerceIn(0.0, 1.0)
    else 0.0

    val animatedRate = remember { Animatable(0f) }
    LaunchedEffect(savingsRate) {
        delay(500)
        animatedRate.animateTo(savingsRate.toFloat(), tween(1200, easing = FastOutSlowInEasing))
    }

    val rateColor = when {
        savingsRate >= 0.3 -> EmeraldGreen
        savingsRate >= 0.1 -> GoldenAmber
        else -> CoralRed
    }

    val rateLabel = when {
        savingsRate >= 0.3 -> "Excellent saver 🏆"
        savingsRate >= 0.2 -> "Good saving habit 👍"
        savingsRate >= 0.1 -> "Room to improve 📈"
        else -> "Needs attention ⚠️"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp, CardShape, clip = false),
        shape = CardShape,
        colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Circular gauge
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(80.dp)) {
                Canvas(modifier = Modifier.size(80.dp)) {
                    val strokeWidth = 9.dp.toPx()
                    val radius = (size.minDimension - strokeWidth) / 2f
                    val center = Offset(size.width / 2f, size.height / 2f)
                    // Background arc
                    drawCircle(
                        color = rateColor.copy(alpha = 0.12f),
                        radius = radius,
                        style = Stroke(strokeWidth)
                    )
                    // Filled arc
                    drawArc(
                        color = rateColor,
                        startAngle = -90f,
                        sweepAngle = 360f * animatedRate.value,
                        useCenter = false,
                        style = Stroke(strokeWidth, cap = StrokeCap.Round),
                        topLeft = Offset(center.x - radius, center.y - radius),
                        size = Size(radius * 2, radius * 2)
                    )
                }
                Text(
                    "${(animatedRate.value * 100).toInt()}%",
                    style = typography.titleSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = rateColor
                )
            }

            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Savings Rate", style = typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(rateLabel, style = typography.bodySmall, color = colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(4.dp))
                Surface(shape = ChipShape, color = rateColor.copy(alpha = 0.1f)) {
                    Text(
                        "Target: 30%+",
                        style = typography.labelSmall,
                        color = rateColor,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Overtime Summary Card (upgraded)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun OvertimeSummaryCard(financialSummary: FinancialSummary) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp, CardShape, clip = false),
        shape = CardShape,
        colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
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
                            brush = Brush.linearGradient(listOf(VioletPurple, SapphireBlue)),
                            shape = RoundedCornerShape(10.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Bolt, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                }
                Text("Overtime", style = typography.titleMedium, fontWeight = FontWeight.Bold)
            }

            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OvertimeStatBlock(
                    label = "Hours Logged",
                    value = "${"%.1f".format(financialSummary.overtimeHours)} hrs",
                    accentColor = VioletPurple,
                    bgColor = PurpleSurface,
                    modifier = Modifier.weight(1f)
                )
                OvertimeStatBlock(
                    label = "Earnings",
                    value = "৳${"%,.0f".format(financialSummary.overtimeEarnings)}",
                    accentColor = SapphireBlue,
                    bgColor = BlueSurface,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun OvertimeStatBlock(
    label: String,
    value: String,
    accentColor: Color,
    bgColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = bgColor,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(label, style = typography.labelSmall, color = colorScheme.onSurfaceVariant)
            Text(value, style = typography.titleMedium, fontWeight = FontWeight.ExtraBold, color = accentColor)
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Monthly Stats Card (upgraded)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun MonthlyStatsCard(stats: MonthlyStats) {
    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { isVisible = true }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp, CardShape, clip = false),
        shape = CardShape,
        colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
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
                    Icon(Icons.Default.BarChart, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                }
                Text("Monthly Summary", style = typography.titleMedium, fontWeight = FontWeight.Bold)
            }

            Spacer(Modifier.height(18.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                AnimatedStatItem(
                    value = stats.officeDays,
                    label = "Office",
                    color = SapphireBlue,
                    visible = isVisible,
                    delay = 0
                )
                AnimatedStatItem(
                    value = stats.homeOfficeDays,
                    label = "Home",
                    color = EmeraldGreen,
                    visible = isVisible,
                    delay = 100
                )
                AnimatedStatItem(
                    value = stats.offDays,
                    label = "Off",
                    color = VioletPurple,
                    visible = isVisible,
                    delay = 200
                )
                AnimatedStatItem(
                    value = stats.extraHours.toInt(),
                    label = "Extra hrs",
                    color = GoldenAmber,
                    visible = isVisible,
                    delay = 300
                )
            }

            Spacer(Modifier.height(16.dp))
            HorizontalDivider(color = colorScheme.outlineVariant, thickness = 0.5.dp)
            Spacer(Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Total Work Days",
                    style = typography.bodyMedium,
                    color = colorScheme.onSurfaceVariant
                )
                Surface(shape = PillShape, color = SapphireBlue.copy(alpha = 0.1f)) {
                    Text(
                        "${stats.totalWorkDays} days",
                        style = typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = SapphireBlue,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 5.dp)
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Work Attendance Ring Card (NEW visual)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun WorkAttendanceRingCard(stats: MonthlyStats) {
    val totalDays = (stats.officeDays + stats.homeOfficeDays + stats.offDays + stats.extraHours.toInt()).coerceAtLeast(1)
    val segments = listOf(
        Triple("Office", stats.officeDays.toFloat(), SapphireBlue),
        Triple("Home", stats.homeOfficeDays.toFloat(), EmeraldGreen),
        Triple("Off", stats.offDays.toFloat(), VioletPurple),
        Triple("Extra", stats.extraHours.toInt().toFloat(), GoldenAmber)
    ).filter { it.second > 0 }

    val animatedSweeps = segments.map { (_, value, _) ->
        val anim = remember { Animatable(0f) }
        LaunchedEffect(value) {
            delay(600)
            anim.animateTo((value / totalDays) * 360f, tween(1000, easing = FastOutSlowInEasing))
        }
        anim
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp, CardShape, clip = false),
        shape = CardShape,
        colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.spacedBy(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Donut Chart
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(110.dp)) {
                Canvas(modifier = Modifier.size(110.dp)) {
                    val strokeWidth = 18.dp.toPx()
                    val radius = (size.minDimension - strokeWidth) / 2f
                    val topLeft = Offset(center.x - radius, center.y - radius)
                    val arcSize = Size(radius * 2, radius * 2)

                    // Background ring
                    drawCircle(
                        color = Color.LightGray.copy(alpha = 0.15f),
                        radius = radius,
                        style = Stroke(strokeWidth)
                    )

                    var startAngle = -90f
                    segments.forEachIndexed { index, (_, _, color) ->
                        val sweep = animatedSweeps.getOrNull(index)?.value ?: 0f
                        if (sweep > 0f) {
                            drawArc(
                                color = color,
                                startAngle = startAngle,
                                sweepAngle = sweep - 2f, // small gap
                                useCenter = false,
                                style = Stroke(strokeWidth, cap = StrokeCap.Round),
                                topLeft = topLeft,
                                size = arcSize
                            )
                        }
                        startAngle += sweep
                    }
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "${stats.totalWorkDays}",
                        style = typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = colorScheme.onSurface
                    )
                    Text("days", style = typography.labelSmall, color = colorScheme.onSurfaceVariant)
                }
            }

            // Legend
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                segments.forEach { (name, value, color) ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(Modifier.size(10.dp).background(color, CircleShape))
                        Text(name, style = typography.bodySmall, modifier = Modifier.weight(1f), color = colorScheme.onSurfaceVariant)
                        Text(
                            "${value.toInt()}",
                            style = typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = color
                        )
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Animated Stat Item (kept, refined)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun AnimatedStatItem(
    value: Int,
    label: String,
    color: Color,
    visible: Boolean,
    delay: Int = 0
) {
    var animatedValue by remember { mutableIntStateOf(0) }

    LaunchedEffect(visible, value) {
        if (visible) {
            delay(delay.toLong())
            for (i in 0..value) {
                animatedValue = i
                delay(18L)
            }
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(54.dp)
                .background(color.copy(alpha = 0.1f), RoundedCornerShape(14.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (visible) animatedValue.toString() else "0",
                style = typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = color
            )
        }
        Spacer(Modifier.height(6.dp))
        Text(
            text = label,
            style = typography.labelSmall,
            color = colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Category Summary Card (upgraded)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun CategorySummaryCard(expensesByCategory: Map<ExpenseCategory, Double>) {
    val topExpenses by remember(expensesByCategory) {
        derivedStateOf {
            expensesByCategory.entries
                .sortedByDescending { it.value }
                .take(6)
        }
    }

    val totalExpenses by remember(expensesByCategory) {
        derivedStateOf { expensesByCategory.values.sum().coerceAtLeast(1.0) }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp, CardShape, clip = false),
        shape = CardShape,
        colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
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
                            brush = Brush.linearGradient(listOf(CoralRed, GoldenAmber)),
                            shape = RoundedCornerShape(10.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.PieChart, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                }
                Text("Expense Breakdown", style = typography.titleMedium, fontWeight = FontWeight.Bold)
            }

            Spacer(Modifier.height(16.dp))

            if (topExpenses.isEmpty()) {
                EmptyState(
                    message = "No expense data for this month yet.",
                    modifier = Modifier.height(100.dp)
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    topExpenses.forEach { (category, amount) ->
                        ExpenseBar(
                            category = category.name.replace("_", " ").lowercase()
                                .replaceFirstChar { it.uppercase() },
                            amount = amount,
                            total = totalExpenses,
                            color = category.color
                        )
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Expense Bar (upgraded)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun ExpenseBar(category: String, amount: Double, total: Double, color: Color) {
    val proportion = (amount / total).toFloat().coerceIn(0f, 1f)
    val animatedProportion = remember { Animatable(0f) }

    LaunchedEffect(proportion) {
        animatedProportion.animateTo(
            proportion,
            animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing)
        )
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                Box(Modifier.size(8.dp).background(color, CircleShape))
                Text(
                    text = category,
                    style = typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Text(
                text = "৳${"%,.0f".format(amount)}",
                style = typography.bodySmall,
                fontWeight = FontWeight.SemiBold,
                color = color
            )
        }
        Spacer(Modifier.height(5.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(7.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(color.copy(alpha = 0.12f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(animatedProportion.value)
                    .height(7.dp)
                    .background(
                        brush = Brush.horizontalGradient(listOf(color.copy(alpha = 0.7f), color)),
                        shape = RoundedCornerShape(4.dp)
                    )
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Empty State
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun EmptyState(message: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Text(text = message, style = typography.bodyMedium, color = colorScheme.onSurfaceVariant)
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Weekly Activity Timeline (upgraded)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun WeeklyActivityTimeline(activities: List<WorkLogUi>) {
    val today = LocalDate.now()
    val weekDays by remember(today) {
        derivedStateOf { (0..6).map { today.minusDays(it.toLong()) }.reversed() }
    }

    val activityMap by remember(activities, weekDays) {
        derivedStateOf {
            activities
                .filter {
                    val activityDate = it.date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
                    activityDate in weekDays.first()..weekDays.last()
                }
                .associateBy { it.date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate() }
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp, CardShape, clip = false),
        shape = CardShape,
        colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
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
                    Icon(Icons.Outlined.ShowChart, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                }
                Text("This Week", style = typography.titleMedium, fontWeight = FontWeight.Bold)
            }

            Spacer(Modifier.height(16.dp))

            // Compact pill row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                weekDays.forEach { date ->
                    val log = activityMap[date]
                    val isToday = date == today
                    DayPill(date = date, workLog = log, isToday = isToday)
                }
            }

            Spacer(Modifier.height(16.dp))
            HorizontalDivider(color = colorScheme.outlineVariant, thickness = 0.5.dp)
            Spacer(Modifier.height(14.dp))

            // Detail list
            if (weekDays.all { activityMap[it] == null }) {
                EmptyState(
                    message = "No activity recorded this week.",
                    modifier = Modifier.height(60.dp)
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    weekDays.filter { activityMap[it] != null }.forEach { date ->
                        DayActivityRow(date = date, workLog = activityMap[date])
                    }
                }
            }
        }
    }
}

@Composable
private fun DayPill(date: LocalDate, workLog: WorkLogUi?, isToday: Boolean) {
    val (color, _) = remember(workLog?.workType) {
        workTypeStyle(workLog?.workType)
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            date.dayOfWeek.getDisplayName(TextStyle.NARROW, Locale.getDefault()),
            style = typography.labelSmall,
            color = if (isToday) colorScheme.primary else colorScheme.onSurfaceVariant,
            fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal
        )
        Box(
            modifier = Modifier
                .size(34.dp)
                .background(
                    color = if (workLog != null) color.copy(alpha = 0.15f)
                    else colorScheme.surfaceVariant,
                    shape = CircleShape
                )
                .then(
                    if (isToday) Modifier.border(2.dp, colorScheme.primary, CircleShape)
                    else Modifier
                ),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .background(if (workLog != null) color else SlateGray.copy(alpha = 0.4f), CircleShape)
            )
        }
    }
}

@Composable
fun DayActivityRow(date: LocalDate, workLog: WorkLogUi?) {
    val (color, displayName) = remember(workLog?.workType) {
        workTypeStyle(workLog?.workType)
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault()),
            style = typography.bodySmall,
            fontWeight = FontWeight.Bold,
            color = colorScheme.onSurfaceVariant,
            modifier = Modifier.width(32.dp)
        )

        Surface(shape = ChipShape, color = color.copy(alpha = 0.1f)) {
            Text(
                displayName,
                style = typography.labelSmall,
                fontWeight = FontWeight.Medium,
                color = color,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
            )
        }

        workLog?.duration?.let { dur ->
            Spacer(Modifier.weight(1f))
            Text(dur, style = typography.labelSmall, color = colorScheme.onSurfaceVariant)
        }
    }
}

private fun workTypeStyle(workType: WorkType?): Pair<Color, String> = when (workType) {
    WorkType.OFFICE -> Color(0xFF3B82F6) to "Office"
    WorkType.HOME_OFFICE -> Color(0xFF10B981) to "Home Office"
    WorkType.OFF_DAY -> Color(0xFF8B5CF6) to "Off Day"
    WorkType.EXTRA_WORK -> Color(0xFFF59E0B) to "Extra Work"
    WorkType.OVERTIME -> Color(0xFFEF4444) to "Overtime"
    null -> Color(0xFF94A3B8) to "No Entry"
}

// ─────────────────────────────────────────────────────────────────────────────
// Today Status Card (upgraded)
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun TodayStatusCard(
    workType: WorkType?,
    onWorkTypeSelected: (WorkType) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val (accentColor, _) = remember(workType) { workTypeStyle(workType) }

    val (title, subtitle) = remember(workType) {
        when (workType) {
            WorkType.OFFICE -> "Office Day 🏢" to "You're working from office today"
            WorkType.HOME_OFFICE -> "Home Office 🏠" to "Working comfortably from home"
            WorkType.OFF_DAY -> "Off Day 🌴" to "Enjoy your rest!"
            WorkType.EXTRA_WORK -> "Extra Work ⚡" to "Going above and beyond!"
            WorkType.OVERTIME -> "Overtime 🚀" to "Crushing it!"
            null -> "Log Attendance" to "What are you working on today?"
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp, CardShape, clip = false),
        shape = CardShape,
        colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
        onClick = { expanded = !expanded }
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Status Icon with Gradient
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            brush = Brush.linearGradient(
                                listOf(accentColor.copy(alpha = 0.7f), accentColor)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = when (workType) {
                            WorkType.OFFICE -> Icons.Default.Work
                            WorkType.HOME_OFFICE -> Icons.Default.Home
                            WorkType.OFF_DAY -> Icons.Default.BeachAccess
                            WorkType.EXTRA_WORK, WorkType.OVERTIME -> Icons.Default.Bolt
                            null -> Icons.Default.Add
                        },
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.onSurface
                    )
                    Text(
                        text = subtitle,
                        style = typography.bodySmall,
                        color = colorScheme.onSurfaceVariant
                    )
                }

                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(24.dp)
                )
            }

            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column {
                    Spacer(Modifier.height(20.dp))
                    HorizontalDivider(color = colorScheme.outlineVariant, thickness = 0.5.dp)
                    Spacer(Modifier.height(20.dp))
                    WorkTypeSelectionButtons(
                        workType = workType,
                        onWorkTypeSelected = {
                            onWorkTypeSelected(it)
                            expanded = false
                        }
                    )
                }
            }

            if (!expanded) {
                Spacer(Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = PillShape,
                        color = accentColor.copy(alpha = 0.1f)
                    ) {
                        Text(
                            text = if (workType == null) "Log Status" else "Change",
                            style = typography.labelSmall,
                            color = accentColor,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun WorkTypeSelectionButtons(
    workType: WorkType?,
    onWorkTypeSelected: (WorkType) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        listOf(
            Triple(WorkType.OFFICE, Icons.Filled.Work, "Office"),
            Triple(WorkType.HOME_OFFICE, Icons.Filled.Home, "Home"),
            Triple(WorkType.OFF_DAY, Icons.Filled.BeachAccess, "Off"),
            Triple(WorkType.EXTRA_WORK, Icons.Filled.Bolt, "Extra")
        ).forEachIndexed { index, (type, icon, label) ->
            AnimatedWorkTypeButton(
                workType = type,
                icon = icon,
                label = label,
                selected = workType == type,
                onClick = { onWorkTypeSelected(type) },
                delay = index * 80
            )
        }
    }
}

@Composable
fun RowScope.AnimatedWorkTypeButton(
    workType: WorkType,
    icon: ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    delay: Int = 0
) {
    val (color, _) = workTypeStyle(workType)
    val buttonScale by animateFloatAsState(
        targetValue = if (selected) 1.05f else 1f,
        animationSpec = spring(dampingRatio = 0.6f),
        label = "btn_scale"
    )

    val bgColor = if (selected) color else colorScheme.surface
    val contentColor = if (selected) Color.White else colorScheme.onSurface

    Surface(
        modifier = Modifier
            .weight(1f)
            .graphicsLayer { scaleX = buttonScale; scaleY = buttonScale }
            .shadow(if (selected) 6.dp else 2.dp, ChipShape)
            .clickable(onClick = onClick),
        shape = ChipShape,
        color = bgColor
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(vertical = 12.dp)
        ) {
            Icon(icon, contentDescription = null, tint = contentColor, modifier = Modifier.size(20.dp))
            Spacer(Modifier.height(4.dp))
            Text(label, style = typography.labelSmall, color = contentColor, fontWeight = FontWeight.Medium)
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Delta Indicator (kept, refined)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun DeltaIndicator(
    delta: Float,
    formattedDelta: String,
    modifier: Modifier = Modifier
) {
    val isPositive = delta > 0f
    val isNeutral = delta == 0f

    val trendIcon = when {
        isPositive -> Icons.AutoMirrored.Outlined.TrendingUp
        !isPositive && !isNeutral -> Icons.AutoMirrored.Outlined.TrendingDown
        else -> Icons.Outlined.Remove
    }

    val trendColor = when {
        isPositive -> EmeraldGreen
        isNeutral -> colorScheme.onSurfaceVariant
        else -> CoralRed
    }

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(trendIcon, contentDescription = null, tint = trendColor, modifier = Modifier.size(16.dp))
        Text(
            text = formattedDelta,
            style = typography.bodySmall.copy(fontWeight = FontWeight.Medium),
            color = trendColor
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Sparkline Chart (kept)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun SparklineChart(
    data: List<Float>,
    color: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        if (data.size < 2) return@Canvas
        val path = Path()
        val xStep = size.width / (data.size - 1)
        val yMax = data.maxOrNull() ?: 1f
        val yMin = data.minOrNull() ?: 0f
        val yRange = if (yMax > yMin) yMax - yMin else 1f
        path.moveTo(0f, size.height - ((data[0] - yMin) / yRange) * size.height)
        data.forEachIndexed { index, value ->
            if (index > 0) {
                val x = index * xStep
                val y = size.height - ((value - yMin) / yRange) * size.height
                path.lineTo(x, y)
            }
        }
        drawPath(path = path, color = color, style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round))
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Quick Action Menu (upgraded)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun QuickActionMenu(
    onNavigateToAddEntry: () -> Unit,
    onNavigateToIncome: () -> Unit,
    onNavigateToExpense: () -> Unit,
    onNavigateToLoan: () -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }
    val rotation by animateFloatAsState(
        targetValue = if (isExpanded) 45f else 0f,
        label = "fab_rotation"
    )

    Column(
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        AnimatedVisibility(
            visible = isExpanded,
            enter = fadeIn(tween(200)) + slideInVertically(tween(200)) { it },
            exit = fadeOut(tween(150)) + slideOutVertically(tween(150)) { it }
        ) {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                QuickActionItem(
                    icon = Icons.Outlined.AttachMoney,
                    text = "Income",
                    color = EmeraldGreen,
                    onClick = { isExpanded = false; onNavigateToIncome() }
                )
                QuickActionItem(
                    icon = Icons.Outlined.MoneyOff,
                    text = "Expense",
                    color = CoralRed,
                    onClick = { isExpanded = false; onNavigateToExpense() }
                )
                QuickActionItem(
                    icon = Icons.Outlined.AccountBalance,
                    text = "Loan",
                    color = VioletPurple,
                    onClick = { isExpanded = false; onNavigateToLoan() }
                )
            }
        }

        FloatingActionButton(
            onClick = { isExpanded = !isExpanded },
            containerColor = SapphireBlue,
            contentColor = Color.White,
            shape = CircleShape
        ) {
            Icon(
                Icons.Default.Add,
                contentDescription = "Add Entry",
                modifier = Modifier.graphicsLayer(rotationZ = rotation)
            )
        }
    }
}

@Composable
fun QuickActionItem(
    icon: ImageVector,
    text: String,
    color: Color,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.clickable(onClick = onClick, role = Role.Button)
    ) {
        Surface(
            shape = PillShape,
            color = colorScheme.surface,
            shadowElevation = 4.dp
        ) {
            Text(
                text = text,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                style = typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = colorScheme.onSurface
            )
        }
        SmallFloatingActionButton(
            onClick = onClick,
            containerColor = color,
            contentColor = Color.White,
            shape = CircleShape
        ) {
            Icon(icon, contentDescription = text, modifier = Modifier.size(18.dp))
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// FinancialSummaryChart — kept for backward compat (delegates to new cards)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun FinancialSummaryChart(financialSummary: FinancialSummary) {
    NetBalanceHeroCard(financialSummary = financialSummary)
}

// ─────────────────────────────────────────────────────────────────────────────
// FinancialMetricCard & OvertimeMetricCard — kept for backward compat
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun FinancialMetricCard(
    title: String,
    value: Float,
    color: Color,
    icon: ImageVector
) {
    GlassMetricCard(
        label = title,
        value = "৳${"%,.0f".format(value)}",
        icon = icon,
        accentColor = color,
        bgColor = color.copy(alpha = 0.08f)
    )
}

@Composable
fun OvertimeMetricCard(
    title: String,
    value: Float,
    color: Color,
    icon: ImageVector
) {
    OvertimeStatBlock(
        label = title,
        value = "${"%.1f".format(value)}",
        accentColor = color,
        bgColor = color.copy(alpha = 0.08f)
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// ExpenseCategory color extension
// ─────────────────────────────────────────────────────────────────────────────

val ExpenseCategory.color: Color
    @Composable
    get() = when (this) {
        MEAL -> Color(0xFFE91E63)
        TRANSPORT -> Color(0xFF8B5CF6)
        SHOPPING -> Color(0xFF3B82F6)
        BILLS -> Color(0xFF06B6D4)
        ENTERTAINMENT -> Color(0xFF10B981)
        HEALTHCARE -> Color(0xFFEF4444)
        EDUCATION -> Color(0xFF6366F1)
        PERSONAL_CARE -> Color(0xFFF43F5E)
        GIFTS -> Color(0xFFF59E0B)
        TRAVEL -> Color(0xFF0EA5E9)
        SUBSCRIPTIONS -> Color(0xFF7C3AED)
        OTHER -> Color(0xFF64748B)
    }