package com.rudra.smartworktracker.ui.screens.dashboard

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.TrendingDown
import androidx.compose.material.icons.automirrored.outlined.TrendingUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BeachAccess
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Work
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.AttachMoney
import androidx.compose.material.icons.outlined.MoneyOff
import androidx.compose.material.icons.outlined.Savings
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rudra.smartworktracker.data.AppDatabase
import com.rudra.smartworktracker.model.ExpenseCategory
import com.rudra.smartworktracker.model.WorkType
import com.rudra.smartworktracker.ui.FinancialSummary
import com.rudra.smartworktracker.ui.MonthlyStats
import com.rudra.smartworktracker.ui.WorkLogUi
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.TextStyle
import java.util.Locale
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

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

    // Derived state for better performance
    val hasRecentActivities by remember(uiState.recentActivities) {
        derivedStateOf { uiState.recentActivities.isNotEmpty() }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Dashboard",
                        style = typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colorScheme.surface,
                    titleContentColor = colorScheme.onSurface
                )
            )
        },
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
            contentPadding = PaddingValues(16.dp)
        ) {
            item {
                Header(userName = uiState.userName)
            }

            item {
                FinancialSummaryCard(
                    summary = uiState.financialSummary
                )
            }

            item {
                PerformanceRow(
                    summary = uiState.financialSummary,
                    stats = uiState.monthlyStats
                )
            }

            item {
                CategorySummaryCard(expensesByCategory = uiState.expensesByCategory)
            }

            if (hasRecentActivities) {
                item {
                    WeeklyActivityTimeline(activities = uiState.recentActivities)
                }
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
        }
    }
}

/**
 * Optimized header with better performance and accessibility
 */
@Composable
fun Header(userName: String?) {
    val displayName = remember(userName) { userName ?: "Rudra" }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Circular Avatar with better accessibility
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(
                    color = colorScheme.primary.copy(alpha = 0.1f),
                    shape = CircleShape
                )
                .border(
                    width = 1.dp,
                    color = colorScheme.primary.copy(alpha = 0.2f),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = displayName.first().uppercase(),
                style = typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = colorScheme.primary
            )
        }

        // Greeting and Subtitle
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Hello, $displayName 👋",
                style = typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = colorScheme.onSurface
            )
            Text(
                text = "Today is a good day to stay productive.",
                style = typography.bodyMedium,
                color = colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Optimized financial summary card with better state management
 */
@Composable
fun FinancialSummaryCard(summary: FinancialSummary) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .border(
                width = 1.dp,
                color = colorScheme.onSurface.copy(alpha = 0.2f),
                shape = RoundedCornerShape(24.dp)
            ),
        elevation = CardDefaults.cardElevation(0.dp),
        colors = CardDefaults.cardColors(
            containerColor = colorScheme.surface.copy(alpha = 0.3f)
        ),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Financial Overview",
                style = typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            val cardMinHeight = 120.dp

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                FinancialMetricCard(
                    title = "Total Income",
                    amount = summary.totalIncome,
                    delta = 8.2f,
                    trendData = listOf(0.5f, 0.6f, 0.4f, 0.7f, 0.8f, 0.6f, 0.9f),
                    color = colorScheme.primary,
                    icon = Icons.AutoMirrored.Outlined.TrendingUp,
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = cardMinHeight)
                )

                FinancialMetricCard(
                    title = "Total Expenses",
                    amount = summary.totalExpense,
                    delta = -5.6f,
                    trendData = listOf(0.9f, 0.8f, 0.7f, 0.6f, 0.5f, 0.4f, 0.3f),
                    color = colorScheme.error,
                    icon = Icons.AutoMirrored.Outlined.TrendingDown,
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = cardMinHeight)
                )

                FinancialMetricCard(
                    title = "Net Savings",
                    amount = summary.netSavings,
                    delta = 15.3f,
                    trendData = listOf(0.3f, 0.4f, 0.5f, 0.6f, 0.8f, 0.7f, 0.9f),
                    color = Color(0xFF0F9D58),
                    icon = Icons.Outlined.Savings,
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = cardMinHeight)
                )
            }
        }
    }
}

/**
 * Performance row with better state derivation
 */
@Composable
fun PerformanceRow(summary: FinancialSummary, stats: MonthlyStats) {
    Column {
        Text(
            text = "Performance",
            style = typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(start = 16.dp, bottom = 16.dp)
        )

        val cardMinHeight = 120.dp

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            FinancialMetricCard(
                title = "Total Loan",
                amount = summary.totalLoan,
                delta = 0f,
                trendData = List(7) { 0.5f },
                color = colorScheme.tertiary,
                icon = Icons.Outlined.AccountBalance,
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = cardMinHeight)
            )

            FinancialMetricCard(
                title = "Office Days",
                amount = stats.officeDays.toDouble(),
                delta = 2.5f,
                trendData = listOf(0.5f, 0.6f, 0.4f, 0.7f, 0.8f, 0.6f, 0.9f),
                color = colorScheme.primary,
                icon = Icons.Filled.Work,
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = cardMinHeight)
            )

            FinancialMetricCard(
                title = "Off Days",
                amount = stats.offDays.toDouble(),
                delta = -1.0f,
                trendData = listOf(0.5f, 0.4f, 0.6f, 0.3f, 0.2f, 0.1f, 0.0f),
                color = colorScheme.secondary,
                icon = Icons.Filled.BeachAccess,
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = cardMinHeight)
            )
        }
    }
}

/**
 * Optimized financial metric card with better number formatting and performance
 */
@Composable
fun FinancialMetricCard(
    title: String,
    amount: Double,
    delta: Float,
    trendData: List<Float>,
    color: Color,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    val minHeight = 120.dp
    var currentValue by remember { mutableDoubleStateOf(0.0) }

    // Optimized animation
    LaunchedEffect(amount) {
        currentValue = 0.0 // Reset for re-animation
        val animation = Animatable(0f)
        animation.animateTo(
            targetValue = amount.toFloat(),
            animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing)
        ) {
            currentValue = this.value.toDouble()
        }
    }

    // Memoized formatted values
    val formattedValue = remember(currentValue, title) {
        if (title.contains("Days", ignoreCase = true)) {
            currentValue.toInt().toString()
        } else {
            "৳${String.format("%.2f", currentValue)}"
        }
    }

    val formattedDelta = remember(delta) {
        "${if (delta > 0) "+" else ""}${String.format("%.1f", delta)}%"
    }

    Card(
        modifier = modifier
            .heightIn(min = minHeight)
            .clip(RoundedCornerShape(18.dp)),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f),
            contentColor = color
        ),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(14.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top row: Icon + Title
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = color,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = title,
                    style = typography.bodyMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = colorScheme.onSurfaceVariant
                )
            }

            // Main KPI Number
            Text(
                text = formattedValue,
                style = typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = color
            )

            // Bottom row: Sparkline + Delta Indicator
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Sparkline Trend Chart
                SparklineChart(
                    data = trendData,
                    color = color,
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp)
                )

                // Delta Indicator
                DeltaIndicator(
                    delta = delta,
                    formattedDelta = formattedDelta
                )
            }
        }
    }
}

/**
 * Extracted Delta Indicator for better reusability
 */
@Composable
private fun DeltaIndicator(delta: Float, formattedDelta: String) {
    val trendIcon = if (delta >= 0) {
        Icons.AutoMirrored.Outlined.TrendingUp
    } else {
        Icons.AutoMirrored.Outlined.TrendingDown
    }

    val trendColor = if (delta >= 0) {
        Color(0xFF0F9D58)
    } else {
        colorScheme.error
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = trendIcon,
            contentDescription = if (delta >= 0) "Trending up" else "Trending down",
            tint = trendColor,
            modifier = Modifier.size(16.dp)
        )
        Text(
            text = formattedDelta,
            style = typography.bodySmall,
            fontWeight = FontWeight.Medium,
            color = trendColor
        )
    }
}

/**
 * Optimized sparkline chart with better performance
 */
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

        // Move to first point
        path.moveTo(
            0f,
            size.height - ((data[0] - yMin) / yRange) * size.height
        )

        // Draw lines to subsequent points
        data.forEachIndexed { index, value ->
            if (index > 0) {
                val x = index * xStep
                val y = size.height - ((value - yMin) / yRange) * size.height
                path.lineTo(x, y)
            }
        }

        drawPath(
            path = path,
            color = color,
            style = Stroke(width = 2.dp.toPx())
        )
    }
}

/**
 * Optimized category summary card with better empty state handling
 */
@Composable
fun CategorySummaryCard(expensesByCategory: Map<ExpenseCategory, Double>) {
    val topExpenses by remember(expensesByCategory) {
        derivedStateOf {
            expensesByCategory.entries
                .sortedByDescending { it.value }
                .take(3)
        }
    }

    val totalExpenses by remember(expensesByCategory) {
        derivedStateOf {
            expensesByCategory.values.sum().coerceAtLeast(1.0)
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "This Month's Summary",
                style = typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            if (topExpenses.isEmpty()) {
                EmptyState(
                    message = "No expense data for this month yet.",
                    modifier = Modifier.height(100.dp)
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    topExpenses.forEach { (category, amount) ->
                        ExpenseBar(
                            category = category.name,
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

/**
 * Reusable empty state component
 */
@Composable
fun EmptyState(message: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            style = typography.bodyMedium,
            color = colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Optimized expense bar with better animation control
 */
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
            Text(
                text = category,
                style = typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "৳${String.format("%.0f", amount)}",
                style = typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .background(colorScheme.surfaceVariant, RoundedCornerShape(4.dp))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(animatedProportion.value)
                    .height(8.dp)
                    .background(color, RoundedCornerShape(4.dp))
            )
        }
    }
}

/**
 * Optimized weekly activity timeline with better date handling
 */
@Composable
fun WeeklyActivityTimeline(activities: List<WorkLogUi>) {
    val today = LocalDate.now()
    val weekDays by remember(today) {
        derivedStateOf {
            (0..6).map { today.minusDays(it.toLong()) }.reversed()
        }
    }

    val activityMap by remember(activities, weekDays) {
        derivedStateOf {
            activities
                .filter {
                    val activityDate = it.date.toInstant()
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate()
                    activityDate in weekDays.first()..weekDays.last()
                }
                .associateBy {
                    it.date.toInstant()
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate()
                }
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Weekly Activity",
                style = typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            if (weekDays.all { activityMap[it] == null }) {
                EmptyState(
                    message = "No activity recorded this week.",
                    modifier = Modifier.height(100.dp)
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    weekDays.forEach { date ->
                        DayActivityRow(
                            date = date,
                            workLog = activityMap[date]
                        )
                    }
                }
            }
        }
    }
}


/**
 * Optimized day activity row with better work type handling
 */
@Composable
fun DayActivityRow(date: LocalDate, workLog: WorkLogUi?) {

    val OfficeColor = Color(0xFF2196F3)
    val HomeOfficeColor = Color(0xFFFF9800)
    val OffDayColor = Color(0xFF9C27B0)
    val ExtraWorkColor = Color(0xFFE91E63)
    val NoEntryColor = Color(0xFF9E9E9E)

    val workType = workLog?.workType

    val (color, displayName) = remember(workType) {
        when (workType) {
            WorkType.OFFICE -> OfficeColor to "Office"
            WorkType.HOME_OFFICE -> HomeOfficeColor to "Home Office"
            WorkType.OFF_DAY -> OffDayColor to "Off Day"
            WorkType.EXTRA_WORK -> ExtraWorkColor to "Extra Work"
            null -> NoEntryColor to "No Entry"
        }
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault()),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.width(40.dp)
        )

        Box(
            modifier = Modifier
                .size(12.dp)
                .background(color, CircleShape)
        )

        Text(
            text = displayName,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )
    }
}

/**
 * Optimized quick action menu with better state management
 */
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
        label = "rotation"
    )

    Column(
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        AnimatedVisibility(
            visible = isExpanded,
            enter = fadeIn() + slideInVertically(initialOffsetY = { it }),
            exit = fadeOut() + slideOutVertically(targetOffsetY = { it })
        ) {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                QuickActionItem(
                    icon = Icons.Outlined.AttachMoney,
                    text = "New Income",
                    onClick = onNavigateToIncome
                )
                QuickActionItem(
                    icon = Icons.Outlined.MoneyOff,
                    text = "New Expense",
                    onClick = onNavigateToExpense
                )
                QuickActionItem(
                    icon = Icons.Outlined.AccountBalance,
                    text = "New Loan",
                    onClick = onNavigateToLoan
                )
            }
        }

        FloatingActionButton(
            onClick = { isExpanded = !isExpanded },
            containerColor = colorScheme.primary,
            contentColor = colorScheme.onPrimary
        ) {
            Icon(
                Icons.Default.Add,
                contentDescription = "Add Entry",
                modifier = Modifier.graphicsLayer(rotationZ = rotation)
            )
        }
    }
}

/**
 * Optimized quick action item with better accessibility
 */
@Composable
fun QuickActionItem(icon: ImageVector, text: String, onClick: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .clickable(
                onClick = onClick,
                role = Role.Button
            )
    ) {
        Card(
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Text(
                text = text,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                style = typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
        }
        SmallFloatingActionButton(
            onClick = onClick,
            containerColor = colorScheme.secondaryContainer,
            contentColor = colorScheme.onSecondaryContainer
        ) {
            Icon(
                icon,
                contentDescription = text,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

/**
 * Optimized today status card with better animation performance
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun TodayStatusCard(
    workType: WorkType?,
    onWorkTypeSelected: (WorkType) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val cardElevation by animateDpAsState(
        targetValue = if (expanded) 8.dp else 4.dp,
        label = "card_elevation"
    )

    val backgroundColor by animateColorAsState(
        targetValue = when (workType) {
            WorkType.OFFICE -> colorScheme.primaryContainer
            WorkType.HOME_OFFICE -> colorScheme.secondaryContainer
            WorkType.OFF_DAY -> colorScheme.tertiaryContainer
            WorkType.EXTRA_WORK -> colorScheme.errorContainer
            null -> colorScheme.surfaceVariant
        },
        label = "background_color"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(cardElevation, RoundedCornerShape(24.dp)),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        onClick = { expanded = !expanded }
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            WorkTypeAnimation(workType = workType)

            Spacer(modifier = Modifier.height(16.dp))

            AnimatedContent(
                targetState = workType,
                transitionSpec = {
                    (slideInVertically { height -> height } + fadeIn()).togetherWith(
                        slideOutVertically { height -> -height } + fadeOut())
                },
                label = "text_animation"
            ) { targetWorkType ->
                WorkTypeContent(workType = targetWorkType)
            }

            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column {
                    Spacer(modifier = Modifier.height(24.dp))
                    WorkTypeSelectionButtons(
                        workType = workType,
                        onWorkTypeSelected = onWorkTypeSelected
                    )
                }
            }
        }
    }
}

/**
 * Extracted work type content for better readability
 */
@Composable
private fun WorkTypeContent(workType: WorkType?) {
    val (title, subtitle) = remember(workType) {
        when (workType) {
            WorkType.OFFICE -> "Office Day 🏢" to "You're working from office today"
            WorkType.HOME_OFFICE -> "Home Office 🏠" to "Working comfortably from home"
            WorkType.OFF_DAY -> "Off Day 🌴" to "Enjoy your day off!"
            WorkType.EXTRA_WORK -> "Extra Work ⚡" to "Going above and beyond!"
            null -> "Mark Your Day" to "Tap to select today's work type"
        }
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = title,
            style = typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = subtitle,
            style = typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Extracted work type selection buttons for better organization
 */
@Composable
private fun WorkTypeSelectionButtons(
    workType: WorkType?,
    onWorkTypeSelected: (WorkType) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        listOf(
            WorkType.OFFICE to Icons.Filled.Work,
            WorkType.HOME_OFFICE to Icons.Filled.Home,
            WorkType.OFF_DAY to Icons.Filled.BeachAccess,
            WorkType.EXTRA_WORK to Icons.Filled.Bolt
        ).forEachIndexed { index, (type, icon) ->
            AnimatedWorkTypeButton(
                workType = type,
                icon = icon,
                selected = workType == type,
                onClick = { onWorkTypeSelected(type) },
                delay = index * 100
            )
        }
    }
}

/**
 * Optimized work type button with better animation
 */
@Composable
fun RowScope.AnimatedWorkTypeButton(
    workType: WorkType,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
    delay: Int = 0
) {
    val buttonScale by animateFloatAsState(
        targetValue = if (selected) 1.1f else 1f,
        animationSpec = spring(dampingRatio = 0.6f),
        label = "button_scale"
    )

    val enterTransition = remember(delay) {
        scaleIn(
            animationSpec = tween(300, delay),
            initialScale = 0.8f
        ) + fadeIn(animationSpec = tween(300, delay))
    }

    AnimatedVisibility(
        visible = true,
        enter = enterTransition
    ) {
        OutlinedButton(
            onClick = onClick,
            modifier = Modifier
                .weight(1f)
                .graphicsLayer {
                    scaleX = buttonScale
                    scaleY = buttonScale
                },
            colors = ButtonDefaults.buttonColors(
                containerColor = if (selected) {
                    when (workType) {
                        WorkType.OFFICE -> colorScheme.primary
                        WorkType.HOME_OFFICE -> colorScheme.secondary
                        WorkType.OFF_DAY -> colorScheme.tertiary
                        WorkType.EXTRA_WORK -> colorScheme.error
                    }
                } else {
                    colorScheme.surface
                },
                contentColor = if (selected) {
                    colorScheme.onPrimary
                } else {
                    colorScheme.onSurface
                }
            ),
            border = if (!selected) {
                ButtonDefaults.outlinedButtonBorder
            } else {
                null
            }
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = when (workType) {
                        WorkType.OFFICE -> "Office"
                        WorkType.HOME_OFFICE -> "Home"
                        WorkType.OFF_DAY -> "Off"
                        WorkType.EXTRA_WORK -> "Extra"
                    },
                    style = typography.labelSmall
                )
            }
        }
    }
}

/**
 * Optimized work type animation with better performance
 */
@Composable
fun WorkTypeAnimation(workType: WorkType?) {
    val infiniteTransition = rememberInfiniteTransition(label = "infinite_transition")
    val angle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "angle"
    )

    val primaryColor by animateColorAsState(
        targetValue = when (workType) {
            WorkType.OFFICE -> colorScheme.primary
            WorkType.HOME_OFFICE -> colorScheme.secondary
            WorkType.OFF_DAY -> colorScheme.tertiary
            WorkType.EXTRA_WORK -> colorScheme.error
            null -> colorScheme.primary
        },
        label = "primary_color"
    )

    val secondaryColor by animateColorAsState(
        targetValue = when (workType) {
            WorkType.OFFICE -> colorScheme.secondary
            WorkType.HOME_OFFICE -> colorScheme.tertiary
            WorkType.OFF_DAY -> colorScheme.primary
            WorkType.EXTRA_WORK -> colorScheme.secondary
            null -> colorScheme.secondary
        },
        label = "secondary_color"
    )

    Canvas(modifier = Modifier.size(120.dp)) {
        val radius = size.minDimension / 2f
        val particleCount = 20

        (0 until particleCount).forEach { i ->
            val progress = (i.toFloat() / particleCount + angle / 360f) % 1f
            val currentAngle = progress * 360f
            val currentRadius = radius * (1 - progress * 0.5f)

            // Use radians for better performance
            val radians = (currentAngle * PI / 180).toFloat()
            val x = center.x + currentRadius * cos(radians)
            val y = center.y + currentRadius * sin(radians)

            drawCircle(
                color = if (i % 2 == 0) primaryColor else secondaryColor,
                radius = (1 - progress) * 6f,
                center = Offset(x, y),
                alpha = 1 - progress
            )
        }
    }
}

/**
 * Extension property for ExpenseCategory colors
 */
val ExpenseCategory.color: Color
    @Composable
    get() = when (this) {
        ExpenseCategory.MEAL -> Color(0xFFE91E63)
        ExpenseCategory.TRANSPORT -> Color(0xFF9C27B0)
        ExpenseCategory.SHOPPING -> Color(0xFF2196F3)
        ExpenseCategory.BILLS -> Color(0xFF00BCD4)
        ExpenseCategory.ENTERTAINMENT -> Color(0xFF4CAF50)
        ExpenseCategory.OTHER -> Color(0xFF607D8B)
    }