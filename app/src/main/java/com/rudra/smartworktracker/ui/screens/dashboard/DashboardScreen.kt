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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddRoad
import androidx.compose.material.icons.filled.BeachAccess
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
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
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

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
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                "Financial Summary",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))
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

                // Summary Items
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    FinancialSummaryItem(
                        label = "Income",
                        amount = summary.totalIncome,
                        color = MaterialTheme.colorScheme.primary
                    )
                    FinancialSummaryItem(
                        label = "Expense",
                        amount = summary.totalExpense,
                        color = MaterialTheme.colorScheme.error
                    )
                    FinancialSummaryItem(
                        label = "Savings",
                        amount = summary.netSavings,
                        color = Color(0xFF004D40)


                    )
                    FinancialSummaryItem(
                        label = "Meal_Amount",
                        amount = summary.totalMealCost,
                        color = MaterialTheme.colorScheme.tertiary
                    )
//                    FinancialSummaryItem(
//                        label = "Savings Percentage",
//                        amount = summary.savingsPercentage,
//                        color = MaterialTheme.colorScheme.tertiary
//                    )

                    selectedCategory?.let {
                        FinancialSummaryItem(
                            label = it.name,
                            amount = expensesByCategory[it] ?: 0.0,
                            color = it.color
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FinancialPieChart(
    expensesByCategory: Map<ExpenseCategory, Double>,
    onSliceSelected: (ExpenseCategory) -> Unit
) {
    val totalExpenses = expensesByCategory.values.sum()
    if (totalExpenses == 0.0) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(text = "No expense data")
        }
        return
    }
    val proportions = expensesByCategory.map { (it.value / totalExpenses).toFloat() }
    val categories = expensesByCategory.keys.toList()
    val colors = categories.map { it.color }

    // Animation for each slice
    val animatedProportions = proportions.map { proportion ->
        val animatedValue = remember { Animatable(0f) }
        LaunchedEffect(proportion) {
            animatedValue.animateTo(
                targetValue = proportion,
                animationSpec = tween(durationMillis = 1000)
            )
        }
        animatedValue.value
    }

    Box(
        modifier = Modifier
            .size(150.dp)
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    val canvasSize = size
                    val centerX = canvasSize.width / 2f
                    val centerY = canvasSize.height / 2f
                    val dx = offset.x - centerX
                    val dy = offset.y - centerY
                    var touchAngle = Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())).toFloat()
                    if (touchAngle < 0) {
                        touchAngle += 360f
                    }
                    touchAngle = (touchAngle + 90) % 360 // Adjust for start angle

                    var currentAngle = 0f
                    animatedProportions.forEachIndexed { index, proportion ->
                        val sweepAngle = proportion * 360f
                        if (touchAngle in currentAngle..(currentAngle + sweepAngle)) {
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
            val strokeWidth = 20.dp.toPx()
            var startAngle = -90f

            animatedProportions.forEachIndexed { index, proportion ->
                val sweepAngle = proportion * 360f
                drawArc(
                    color = colors[index],
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Butt),
                    size = Size(size.width, size.height)
                )
                startAngle += sweepAngle
            }
        }

        // Center text
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Total",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "%.2f".format(totalExpenses),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

val ExpenseCategory.color: Color
    @Composable
    get() = when (this) {
        ExpenseCategory.MEAL -> MaterialTheme.colorScheme.secondary
        ExpenseCategory.TRANSPORT -> Color(0xFFD63DB8)
        ExpenseCategory.SHOPPING -> Color(0xFF6200EE)
        ExpenseCategory.BILLS -> Color(0xFF03DAC5)
        ExpenseCategory.ENTERTAINMENT -> Color(0xFF00C853)
        ExpenseCategory.OTHER -> Color.Gray
    }


@Composable
fun FinancialSummaryItem(
    label: String,
    amount: Double,
    color: Color,
    modifier: Modifier = Modifier
) {
    var currentValue by remember { mutableDoubleStateOf(0.0) }

    LaunchedEffect(amount) {
        val animation = Animatable(0f)
        animation.animateTo(amount.toFloat(), animationSpec = tween(durationMillis = 1000)) {
            currentValue = this.value.toDouble()
        }
    }

    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        // Colored dot
        Box(
            modifier = Modifier
                .size(12.dp)
                .background(color, shape = CircleShape)
        )
        Spacer(modifier = Modifier.width(8.dp))

        // Text
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "%.2f BDT".format(currentValue),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = color
            )
        }
    }
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
