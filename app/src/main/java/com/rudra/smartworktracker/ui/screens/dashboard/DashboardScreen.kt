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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.TrendingDown
import androidx.compose.material.icons.automirrored.outlined.TrendingUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddRoad
import androidx.compose.material.icons.filled.BeachAccess
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Work
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.PieChart
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material.icons.outlined.Savings
import androidx.compose.material.icons.outlined.TrendingDown
import androidx.compose.material.icons.outlined.TrendingUp
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rudra.smartworktracker.data.AppDatabase
import com.rudra.smartworktracker.model.ExpenseCategory
import com.rudra.smartworktracker.model.WorkType
import com.rudra.smartworktracker.ui.FinancialSummary
import com.rudra.smartworktracker.ui.MonthlyStats
import com.rudra.smartworktracker.ui.WorkLogUi
import com.rudra.smartworktracker.ui.hasFinancialData
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.tensorflow.lite.support.label.Category
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.togetherWith
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onNavigateToAddEntry: () -> Unit,
    onNavigateToAllFunsion: () -> Unit
) {
    val context = LocalContext.current
    val viewModel: DashboardViewModel = viewModel(
        factory = DashboardViewModel.factory(AppDatabase.getDatabase(context), context)
    )
    val uiState by viewModel.uiState.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Dashboard") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToAddEntry,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, "Add Entry")
            }
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
                    summary = uiState.financialSummary,
                    expensesByCategory = uiState.expensesByCategory
                )
            }
            item {
                AllFunsionCard(onNavigateToAllFunsion = onNavigateToAllFunsion)
            }
            // Today's Status Card
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

            // Quick Stats
            item {
                MonthlyStatsCard(stats = uiState.monthlyStats)
            }

            // Recent Activity
            item {
                RecentActivityCard(activities = uiState.recentActivities)
            }
        }
    }
}

@Composable
fun Header(userName: String?) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Hello, ${userName ?: "there"}!",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Here's your dashboard for today.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun FinancialSummaryCard(
    summary: FinancialSummary,
    expensesByCategory: Map<ExpenseCategory, Double>
) {
    var selectedCategory by remember { mutableStateOf<ExpenseCategory?>(null) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        elevation = CardDefaults.cardElevation(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        ),
        shape = MaterialTheme.shapes.large
    ) {
        Column(
            modifier = Modifier.padding(24.dp)
        ) {
            // Header with title and date
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Financial Overview",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    "Current Month",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 1st Row: Total Income and Total Expenses
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                FinancialMetricCard(
                    title = "Total   Income",
                    amount = summary.totalIncome,
                    color = MaterialTheme.colorScheme.primary,
                    icon = Icons.AutoMirrored.Outlined.TrendingUp,
                    modifier = Modifier.weight(1f)
                )

                FinancialMetricCard(
                    title = "Total Expenses",
                    amount = summary.totalExpense,
                    color = MaterialTheme.colorScheme.error,
                    icon = Icons.AutoMirrored.Outlined.TrendingDown,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 2nd Row: Net Savings and Meal Expenses
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                FinancialMetricCard(
                    title = "Net   Savings",
                    amount = summary.netSavings,
                    color = Color(0xFF0F9D58),
                    icon = Icons.Outlined.Savings,
                    modifier = Modifier.weight(1f)
                )

                FinancialMetricCard(
                    title = "Meal Expenses",
                    amount = summary.totalMealCost,
                    color = MaterialTheme.colorScheme.tertiary,
                    icon = Icons.Outlined.Restaurant,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Savings Rate Progress Bar
            SavingsProgressBar(
                savingsPercentage = summary.savingsPercentage,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Pie Chart Section
            Column {
                Text(
                    "Expense Distribution",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Pie Chart
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        FinancialPieChart(
                            expensesByCategory = expensesByCategory,
                            onSliceSelected = { selectedCategory = it }
                        )
                    }

                    Spacer(modifier = Modifier.width(24.dp))

                    // Category Legend and Details
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Category Legend
                        expensesByCategory.forEach { (category, amount) ->
                            CategoryLegendItem(
                                category = category,
                                amount = amount,
                                totalExpenses = expensesByCategory.values.sum(),
                                isSelected = selectedCategory == category,
                                onClick = { selectedCategory = if (selectedCategory == category) null else category }
                            )
                        }

                        // Selected Category Details
                        AnimatedContent(
                            targetState = selectedCategory,
                            transitionSpec = {
                                fadeIn() + expandVertically() togetherWith fadeOut() + shrinkVertically()
                            },
                            label = "categoryDetails"
                        ) { category ->
                            if (category != null) {
                                val categoryAmount = expensesByCategory[category] ?: 0.0
                                val totalExpenses = expensesByCategory.values.sum()
                                val percentage = if (totalExpenses > 0) (categoryAmount / totalExpenses) * 100 else 0.0

                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(
                                            color = category.color.copy(alpha = 0.08f),
                                            shape = MaterialTheme.shapes.medium
                                        )
                                        .padding(12.dp)
                                ) {
                                    Text(
                                        text = "${category.name} Details",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = category.color
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "৳${"%.2f".format(categoryAmount)} • ${"%.1f%%".format(percentage)}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            } else {
                                Spacer(modifier = Modifier.height(0.dp))
                            }
                        }
                    }
                }
            }

            // You can add your graph component here after the pie chart
            Spacer(modifier = Modifier.height(24.dp))

            // Placeholder for graph - replace with your actual graph component
            Text(
                "Expense Trend Graph",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            // Add your graph composable here
            // ExpenseGraph(data = yourGraphData)
        }
    }
}

@Composable
fun FinancialMetricCard(
    title: String,
    amount: Double,
    color: Color,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    var currentValue by remember { mutableDoubleStateOf(0.0) }

    LaunchedEffect(amount) {
        val animation = Animatable(0f)
        animation.animateTo(
            amount.toFloat(),
            animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing)
        ) {
            currentValue = this.value.toDouble()
        }
    }

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.08f),
            contentColor = color
        ),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "৳${"%.2f".format(currentValue)}",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

@Composable
fun CategoryLegendItem(
    category: ExpenseCategory,
    amount: Double,
    totalExpenses: Double,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val percentage = if (totalExpenses > 0) (amount / totalExpenses) * 100 else 0.0

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) category.color.copy(alpha = 0.1f)
            else MaterialTheme.colorScheme.surfaceContainerLow
        ),
        elevation = if (isSelected) CardDefaults.cardElevation(4.dp)
        else CardDefaults.cardElevation(1.dp),
        border = if (isSelected) BorderStroke(1.dp, category.color.copy(alpha = 0.3f))
        else null
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .background(category.color, shape = CircleShape)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = category.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "৳${"%.2f".format(amount)}",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${"%.1f".format(percentage)}%",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// Keep your existing FinancialPieChart, SavingsProgressBar, and other composables the same
@Composable
fun FinancialPieChart(
    expensesByCategory: Map<ExpenseCategory, Double>,
    onSliceSelected: (ExpenseCategory) -> Unit
) {
    val totalExpenses = expensesByCategory.values.sum()
    if (totalExpenses == 0.0) {
        Box(
            modifier = Modifier
                .size(200.dp)
                .background(
                    color = MaterialTheme.colorScheme.surfaceContainerLowest,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Outlined.PieChart,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "No Data",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        return
    }

    val proportions = expensesByCategory.map { (it.value / totalExpenses).toFloat() }
    val categories = expensesByCategory.keys.toList()
    val colors = categories.map { it.color }
    var selectedIndex by remember { mutableStateOf<Int?>(null) }

    // Animation for each slice
    val animatedProportions = proportions.map { proportion ->
        val animatedValue = remember { Animatable(0f) }
        LaunchedEffect(proportion) {
            animatedValue.animateTo(
                targetValue = proportion,
                animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing)
            )
        }
        animatedValue.value
    }

    Box(
        modifier = Modifier
            .size(200.dp)
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    val canvasSize = size
                    val centerX = canvasSize.width / 2f
                    val centerY = canvasSize.height / 2f
                    val dx = offset.x - centerX
                    val dy = offset.y - centerY
                    var touchAngle = Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())).toFloat()
                    if (touchAngle < 0) touchAngle += 360f
                    touchAngle = (touchAngle + 90) % 360

                    var currentAngle = 0f
                    animatedProportions.forEachIndexed { index, proportion ->
                        val sweepAngle = proportion * 360f
                        if (touchAngle in currentAngle..(currentAngle + sweepAngle)) {
                            selectedIndex = index
                            onSliceSelected(categories[index])
                            return@detectTapGestures
                        }
                        currentAngle += sweepAngle
                    }
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 24.dp.toPx()
            var startAngle = -90f

            animatedProportions.forEachIndexed { index, proportion ->
                val sweepAngle = proportion * 360f
                val isSelected = selectedIndex == index
                val arcColor = if (isSelected) colors[index].copy(alpha = 0.8f) else colors[index]

                drawArc(
                    color = arcColor,
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    style = Stroke(
                        width = if (isSelected) strokeWidth * 1.2f else strokeWidth,
                        cap = StrokeCap.Round
                    ),
                    size = Size(size.width, size.height)
                )
                startAngle += sweepAngle
            }
        }

        // Enhanced center content
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Total Expenses",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "%.0f".format(totalExpenses),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "BDT",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun SavingsProgressBar(
    savingsPercentage: Double,
    modifier: Modifier = Modifier
) {
    val animatedProgress by animateFloatAsState(
        targetValue = (savingsPercentage / 100).coerceIn(0.0, 1.0).toFloat(),
        animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
        label = "savings progress"
    )

    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                "Savings Rate",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                "%.1f%%".format(savingsPercentage),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = if (savingsPercentage >= 20) Color(0xFF0F9D58)
                else MaterialTheme.colorScheme.error
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        LinearProgressIndicator(
            progress = { animatedProgress },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp),
            color = if (savingsPercentage >= 20) Color(0xFF0F9D58)
            else MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceContainerHighest
        )
    }
}

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
@Composable
fun AllFunsionCard(onNavigateToAllFunsion: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onNavigateToAllFunsion() },
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.AddRoad, contentDescription = "All Funsion")
            Spacer(modifier = Modifier.width(16.dp))
            Text("All Funsion", style = MaterialTheme.typography.titleLarge)
        }
    }
}


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

    // Background color animation
    val backgroundColor by animateColorAsState(
        targetValue = when (workType) {
            WorkType.OFFICE -> MaterialTheme.colorScheme.primaryContainer
            WorkType.HOME_OFFICE -> MaterialTheme.colorScheme.secondaryContainer
            WorkType.OFF_DAY -> MaterialTheme.colorScheme.tertiaryContainer
            WorkType.EXTRA_WORK -> MaterialTheme.colorScheme.errorContainer
            null -> MaterialTheme.colorScheme.surfaceVariant
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
            // Custom Compose Animation
            WorkTypeAnimation(workType = workType)

            Spacer(modifier = Modifier.height(16.dp))

            // Animated text transition
            AnimatedContent(
                targetState = workType,
                transitionSpec = {
                    (slideInVertically { height -> height } + fadeIn()).togetherWith(
                        slideOutVertically { height -> -height } + fadeOut())
                }, label = "text_animation"
            ) { targetWorkType ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = targetWorkType?.let {
                            when (it) {
                                WorkType.OFFICE -> "Office Day 🏢"
                                WorkType.HOME_OFFICE -> "Home Office 🏠"
                                WorkType.OFF_DAY -> "Off Day 🌴"
                                WorkType.EXTRA_WORK -> "Extra Work ⚡"
                            }
                        } ?: "Mark Your Day",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = targetWorkType?.let {
                            when (it) {
                                WorkType.OFFICE -> "You're working from office today"
                                WorkType.HOME_OFFICE -> "Working comfortably from home"
                                WorkType.OFF_DAY -> "Enjoy your day off!"
                                WorkType.EXTRA_WORK -> "Going above and beyond!"
                            }
                        } ?: "Tap to select today's work type",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Animated expansion of work type buttons
            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column {
                    Spacer(modifier = Modifier.height(24.dp))

                    // Work Type Selection Buttons with staggered animation
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
            }
        }
    }
}

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

    // Staggered entrance animation
    val enterTransition = remember {
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
                        WorkType.OFFICE -> MaterialTheme.colorScheme.primary
                        WorkType.HOME_OFFICE -> MaterialTheme.colorScheme.secondary
                        WorkType.OFF_DAY -> MaterialTheme.colorScheme.tertiary
                        WorkType.EXTRA_WORK -> MaterialTheme.colorScheme.error
                    }
                } else {
                    MaterialTheme.colorScheme.surface
                },
                contentColor = if (selected) {
                    MaterialTheme.colorScheme.onPrimary
                } else {
                    MaterialTheme.colorScheme.onSurface
                }
            ),
            border = if (!selected) {
                ButtonDefaults.outlinedButtonBorder(true)
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
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}


@Composable
fun MonthlyStatsCard(stats: MonthlyStats) {
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        isVisible = true
    }

    Card(
        modifier = Modifier
            .fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "Monthly Summary",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                AnimatedStatItem(
                    value = stats.officeDays,
                    label = "Office Days",
                    color = MaterialTheme.colorScheme.primary,
                    visible = isVisible,
                    delay = 0
                )
                AnimatedStatItem(
                    value = stats.homeOfficeDays,
                    label = "Home Days",
                    color = MaterialTheme.colorScheme.secondary,
                    visible = isVisible,
                    delay = 100
                )
                AnimatedStatItem(
                    value = stats.offDays,
                    label = "Off Days",
                    color = MaterialTheme.colorScheme.tertiary,
                    visible = isVisible,
                    delay = 200
                )
                AnimatedStatItem(
                    value = stats.extraHours.toInt(),
                    label = "Extra Hours",
                    color = MaterialTheme.colorScheme.error,
                    visible = isVisible,
                    delay = 300
                )
            }
        }
    }
}

@Composable
fun AnimatedStatItem(value: Int, label: String, color: Color, visible: Boolean, delay: Int) {
    var currentValue by remember { mutableIntStateOf(0) }

    LaunchedEffect(visible, value) {
        if (visible) {
            // Animate number counting
            val animation = Animatable(0f)
            animation.animateTo(value.toFloat(), animationSpec = tween(durationMillis = 1000, delayMillis = delay)) {
                currentValue = this.value.toInt()
            }
        }
    }

    val animationSpec = remember {
        tween<Float>(durationMillis = 600, delayMillis = delay, easing = FastOutSlowInEasing)
    }

    val animatedProgress by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = animationSpec,
        label = "stat_item"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.graphicsLayer {
            alpha = animatedProgress
            scaleX = 0.5f + animatedProgress * 0.5f
            scaleY = 0.5f + animatedProgress * 0.5f
        }
    ) {
        Text(
            text = currentValue.toString(),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}


@Composable
fun RecentActivityCard(activities: List<WorkLogUi>) {
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "Recent Activity",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(12.dp))

            activities.forEach { activity ->
                RecentActivityItem(workLog = activity)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun RecentActivityItem(workLog: WorkLogUi) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = when (workLog.workType) {
                    WorkType.OFFICE -> MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                    WorkType.HOME_OFFICE -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f)
                    WorkType.OFF_DAY -> MaterialTheme.colorScheme.tertiary.copy(alpha = 0.1f)
                    WorkType.EXTRA_WORK -> MaterialTheme.colorScheme.error.copy(alpha = 0.1f)
                },
                shape = RoundedCornerShape(12.dp)
            )
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                text = workLog.formattedDate,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = workLog.workType.name,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun WorkTypeAnimation(workType: WorkType?) {
    val infiniteTransition = rememberInfiniteTransition(label = "infinite_transition")
    val angle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "angle"
    )

    // Color transition
    val primaryColor by animateColorAsState(
        targetValue = when (workType) {
            WorkType.OFFICE -> MaterialTheme.colorScheme.primary
            WorkType.HOME_OFFICE -> MaterialTheme.colorScheme.secondary
            WorkType.OFF_DAY -> MaterialTheme.colorScheme.tertiary
            WorkType.EXTRA_WORK -> MaterialTheme.colorScheme.error
            null -> MaterialTheme.colorScheme.primary
        },
        label = "primary_color"
    )
    val secondaryColor by animateColorAsState(
        targetValue = when (workType) {
            WorkType.OFFICE -> MaterialTheme.colorScheme.secondary
            WorkType.HOME_OFFICE -> MaterialTheme.colorScheme.tertiary
            WorkType.OFF_DAY -> MaterialTheme.colorScheme.primary
            WorkType.EXTRA_WORK -> MaterialTheme.colorScheme.secondary
            null -> MaterialTheme.colorScheme.secondary
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

            val x = center.x + currentRadius * cos(Math.toRadians(currentAngle.toDouble())).toFloat()
            val y = center.y + currentRadius * sin(Math.toRadians(currentAngle.toDouble())).toFloat()

            drawCircle(
                color = if (i % 2 == 0) primaryColor else secondaryColor,
                radius = (1 - progress) * 6f, // Particle size decreases over time
                center = Offset(x, y),
                alpha = 1 - progress // Fade out
            )
        }
    }
}
