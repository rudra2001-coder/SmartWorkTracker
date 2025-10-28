package com.rudra.smartworktracker.ui.screens.analytics

import android.graphics.Paint
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.rudra.smartworktracker.data.entity.WorkType
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import kotlin.math.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun AnalyticsScreen(
    onNavigateBack: () -> Unit,
    viewModel: AnalyticsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // Remove HorizontalPager for now to fix compilation
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Work Analytics",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            // Simple Month Header without pager
            SimpleMonthHeader()

            Spacer(modifier = Modifier.height(8.dp))

            // Analytics Content
            AnimatedAnalyticsContent(
                analyticsData = uiState.analyticsData,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun SimpleMonthHeader() {
    val currentMonth = YearMonth.now()
    val formatter = remember { DateTimeFormatter.ofPattern("MMMM yyyy") }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "${currentMonth.format(formatter)} Analytics",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun AnimatedAnalyticsContent(
    analyticsData: AnalyticsData,
    modifier: Modifier = Modifier
) {
    var isContentVisible by remember { mutableStateOf(false) }

    LaunchedEffect(analyticsData) {
        isContentVisible = true
    }

    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(16.dp)
    ) {
        // Summary Cards
        item {
            AnimatedVisibility(
                visible = isContentVisible,
                enter = slideInVertically() + fadeIn()
            ) {
                SummaryCardsRow(analyticsData = analyticsData)
            }
        }

        // Work Distribution Pie Chart
        item {
            AnimatedVisibility(
                visible = isContentVisible,
                enter = slideInVertically(animationSpec = tween(300, delayMillis = 100)) + fadeIn()
            ) {
                WorkDistributionChart(analyticsData = analyticsData)
            }
        }

        // Weekly Trend Chart
        item {
            AnimatedVisibility(
                visible = isContentVisible,
                enter = slideInVertically(animationSpec = tween(300, delayMillis = 200)) + fadeIn()
            ) {
                WeeklyTrendChart(analyticsData = analyticsData)
            }
        }

        // Work Type Breakdown
        item {
            AnimatedVisibility(
                visible = isContentVisible,
                enter = slideInVertically(animationSpec = tween(300, delayMillis = 300)) + fadeIn()
            ) {
                WorkTypeBreakdown(analyticsData = analyticsData)
            }
        }

        // Productivity Insights
        item {
            AnimatedVisibility(
                visible = isContentVisible,
                enter = slideInVertically(animationSpec = tween(300, delayMillis = 400)) + fadeIn()
            ) {
                ProductivityInsights(analyticsData = analyticsData)
            }
        }
    }
}

@Composable
fun SummaryCardsRow(analyticsData: AnalyticsData) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Total Work Days
        AnimatedSummaryCard(
            title = "Work Days",
            value = analyticsData.totalWorkDays,
            targetValue = analyticsData.totalWorkDays,
            icon = Icons.Default.Work,
            color = MaterialTheme.colorScheme.primary,
            delay = 0
        )

        // Office Days
        AnimatedSummaryCard(
            title = "Office",
            value = analyticsData.officeDays,
            targetValue = analyticsData.officeDays,
            icon = Icons.Default.Business,
            color = MaterialTheme.colorScheme.secondary,
            delay = 100
        )

        // Home Office Days
        AnimatedSummaryCard(
            title = "Home",
            value = analyticsData.homeOfficeDays,
            targetValue = analyticsData.homeOfficeDays,
            icon = Icons.Default.Home,
            color = MaterialTheme.colorScheme.tertiary,
            delay = 200
        )
    }
}

@Composable
fun RowScope.AnimatedSummaryCard(
    title: String,
    value: Int,
    targetValue: Int,
    icon: ImageVector,
    color: Color,
    delay: Int
) {
    var animatedValue by remember { mutableStateOf(0) }
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        isVisible = true
        // Animate number counting
        for (i in 0..targetValue) {
            animatedValue = i
            delay(30L)
        }
    }

    val animationSpec = remember {
        tween<Float>(durationMillis = 600, delayMillis = delay, easing = FastOutSlowInEasing)
    }

    val animatedProgress by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = animationSpec,
        label = "summary_card"
    )

    Card(
        modifier = Modifier
            .weight(1f)
            .graphicsLayer {
                alpha = animatedProgress
                scaleX = 0.8f + animatedProgress * 0.2f
                scaleY = 0.8f + animatedProgress * 0.2f
            },
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = color,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = if (isVisible) animatedValue.toString() else "0",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )

            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun WorkDistributionChart(analyticsData: AnalyticsData) {
    val chartEntries = remember(analyticsData) {
        listOf(
            analyticsData.officeDays.toFloat(),
            analyticsData.homeOfficeDays.toFloat(),
            analyticsData.offDays.toFloat(),
            analyticsData.extraWorkDays.toFloat()
        )
    }

    val colors = listOf(
        MaterialTheme.colorScheme.primary,
        MaterialTheme.colorScheme.secondary,
        MaterialTheme.colorScheme.tertiary,
        MaterialTheme.colorScheme.error
    )

    val labels = listOf("Office", "Home", "Off", "Extra")

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "Work Distribution",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Custom pie chart implementation
            AnimatedPieChart(
                entries = chartEntries,
                colors = colors,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Legend
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                labels.forEachIndexed { index, label ->
                    ChartLegendItem(
                        color = colors[index],
                        label = label,
                        value = chartEntries[index].toInt(),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
fun AnimatedPieChart(
    entries: List<Float>,
    colors: List<Color>,
    modifier: Modifier = Modifier
) {
    val total = entries.sum()
    val animationProgress = remember { Animatable(0f) }

    LaunchedEffect(entries) {
        animationProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(1000, easing = FastOutSlowInEasing)
        )
    }

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            var startAngle = -90f

            entries.forEachIndexed { index, value ->
                if (value > 0) {
                    val sweepAngle = (value / total) * 360f * animationProgress.value

                    drawArc(
                        color = colors[index],
                        startAngle = startAngle,
                        sweepAngle = sweepAngle,
                        useCenter = true
                    )

                    startAngle += sweepAngle
                }
            }
        }

        // Center text
        Text(
            text = "${(total * animationProgress.value).toInt()} Days",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun ChartLegendItem(
    color: Color,
    label: String,
    value: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .background(color, CircleShape)
        )

        Spacer(modifier = Modifier.width(4.dp))

        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "$value",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun WeeklyTrendChart(analyticsData: AnalyticsData) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "Weekly Trend",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Custom line chart
            CustomLineChart(
                data = analyticsData.weeklyTrend,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            )
        }
    }
}

@Composable
fun CustomLineChart(
    data: List<Float>,
    modifier: Modifier = Modifier
) {
    val animationProgress = remember { Animatable(0f) }

    LaunchedEffect(data) {
        animationProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(1000, easing = FastOutSlowInEasing)
        )
    }

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            if (data.isEmpty()) return@Canvas

            val padding = 40f
            val chartWidth = size.width - 2 * padding
            val chartHeight = size.height - 2 * padding

            // Draw grid lines
            drawGridLines(padding, chartWidth, chartHeight)

            // Draw line chart
           // drawLineChart(data, padding, chartWidth, chartHeight, animationProgress.value)

            // Draw data points
           // drawDataPoints(data, padding, chartWidth, chartHeight, animationProgress.value)
        }
    }
}

// REMOVE @Composable from these private Canvas functions - they are not composables!
private fun DrawScope.drawGridLines(
    padding: Float,
    chartWidth: Float,
    chartHeight: Float
) {
    val gridColor = Color.Gray.copy(alpha = 0.3f)

    // Horizontal grid lines
    for (i in 0..4) {
        val y = padding + (chartHeight / 4) * i
        drawLine(
            color = gridColor,
            start = Offset(padding, y),
            end = Offset(size.width - padding, y),
            strokeWidth = 1f
        )
    }

    // Vertical grid lines
    for (i in 0..4) {
        val x = padding + (chartWidth / 4) * i
        drawLine(
            color = gridColor,
            start = Offset(x, padding),
            end = Offset(x, size.height - padding),
            strokeWidth = 1f
        )
    }
}

@Composable
private fun DrawScope.drawLineChart(
    data: List<Float>,
    padding: Float,
    chartWidth: Float,
    chartHeight: Float,
    progress: Float
) {
    if (data.isEmpty()) return

    val maxValue = data.maxOrNull() ?: 1f
    val minValue = data.minOrNull() ?: 0f
    val valueRange = maxValue - minValue

    val path = Path()
    val points = mutableListOf<Offset>()

    data.forEachIndexed { index, value ->
        val x = padding + (chartWidth / (data.size - 1)) * index * progress
        val y = size.height - padding - ((value - minValue) / valueRange) * chartHeight * progress

        val point = Offset(x, y)
        points.add(point)

        if (index == 0) {
            path.moveTo(point.x, point.y)
        } else {
            path.lineTo(point.x, point.y)
        }
    }

    // Draw the line
    drawPath(
        path = path,
        color = MaterialTheme.colorScheme.primary,
        style = Stroke(width = 3f, cap = StrokeCap.Round, join = StrokeJoin.Round)
    )

    // Fill under the line
    path.lineTo(padding + chartWidth * progress, size.height - padding)
    path.lineTo(padding, size.height - padding)
    path.close()

    drawPath(
        path = path,
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
        style = Fill
    )
}

@Composable
private fun DrawScope.drawDataPoints(
    data: List<Float>,
    padding: Float,
    chartWidth: Float,
    chartHeight: Float,
    progress: Float
) {
    if (data.isEmpty()) return

    val maxValue = data.maxOrNull() ?: 1f
    val minValue = data.minOrNull() ?: 0f
    val valueRange = maxValue - minValue

    data.forEachIndexed { index, value ->
        val x = padding + (chartWidth / (data.size - 1)) * index * progress
        val y = size.height - padding - ((value - minValue) / valueRange) * chartHeight * progress

        drawCircle(
            color = MaterialTheme.colorScheme.primary,
            center = Offset(x, y),
            radius = 4f
        )

        // Draw value label
        drawContext.canvas.nativeCanvas.apply {
            drawText(
                value.toInt().toString(),
                x,
                y - 15f,
                Paint().apply {
                    color = MaterialTheme.colorScheme.onSurface.toArgb()
                    textSize = 12.sp.toPx()
                    textAlign = Paint.Align.CENTER
                }
            )
        }
    }
}

@Composable
fun WorkTypeBreakdown(analyticsData: AnalyticsData) {
    val workTypes = listOf(
        WorkType.OFFICE to analyticsData.officeDays,
        WorkType.HOME_OFFICE to analyticsData.homeOfficeDays,
        WorkType.OFF_DAY to analyticsData.offDays,
        WorkType.EXTRA_WORK to analyticsData.extraWorkDays
    )

    val totalDays = workTypes.sumOf { it.second }

    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "Work Type Breakdown",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(16.dp))

            workTypes.forEachIndexed { index, (workType, days) ->
                AnimatedWorkTypeItem(
                    workType = workType,
                    days = days,
                    totalDays = totalDays,
                    delay = index * 150,
                    modifier = Modifier.fillMaxWidth()
                )

                if (index < workTypes.size - 1) {
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }
}

@Composable
fun AnimatedWorkTypeItem(
    workType: WorkType,
    days: Int,
    totalDays: Int,
    delay: Int,
    modifier: Modifier = Modifier
) {
    val animatedProgress = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        delay(delay.toLong())
        animatedProgress.animateTo(
            targetValue = if (totalDays > 0) days.toFloat() / totalDays else 0f,
            animationSpec = tween(800)
        )
    }

    val percentage = if (totalDays > 0) (days.toFloat() / totalDays) else 0f

    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = when (workType) {
                        WorkType.OFFICE -> Icons.Default.Work
                        WorkType.HOME_OFFICE -> Icons.Default.Home
                        WorkType.OFF_DAY -> Icons.Default.BeachAccess
                        WorkType.EXTRA_WORK -> Icons.Default.Bolt
                    },
                    contentDescription = null,
                    tint = when (workType) {
                        WorkType.OFFICE -> MaterialTheme.colorScheme.primary
                        WorkType.HOME_OFFICE -> MaterialTheme.colorScheme.secondary
                        WorkType.OFF_DAY -> MaterialTheme.colorScheme.tertiary
                        WorkType.EXTRA_WORK -> MaterialTheme.colorScheme.error
                    },
                    modifier = Modifier.size(20.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = when (workType) {
                        WorkType.OFFICE -> "Office Days"
                        WorkType.HOME_OFFICE -> "Home Office"
                        WorkType.OFF_DAY -> "Off Days"
                        WorkType.EXTRA_WORK -> "Extra Work"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Text(
                text = "$days days",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Animated progress bar
        LinearProgressIndicator(
            progress = animatedProgress.value,
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = when (workType) {
                WorkType.OFFICE -> MaterialTheme.colorScheme.primary
                WorkType.HOME_OFFICE -> MaterialTheme.colorScheme.secondary
                WorkType.OFF_DAY -> MaterialTheme.colorScheme.tertiary
                WorkType.EXTRA_WORK -> MaterialTheme.colorScheme.error
            },
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "%.1f%% of total".format(percentage * 100),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun ProductivityInsights(analyticsData: AnalyticsData) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "Productivity Insights",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Add insightful metrics and comparisons
            InsightItem(
                icon = Icons.AutoMirrored.Filled.TrendingUp,
                title = "Work Consistency",
                value = "${analyticsData.consistencyScore}%",
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(12.dp))

            InsightItem(
                icon = Icons.Default.Schedule,
                title = "Avg. Work Hours",
                value = "${analyticsData.averageHours}h",
                color = MaterialTheme.colorScheme.secondary
            )

            Spacer(modifier = Modifier.height(12.dp))

            InsightItem(
                icon = Icons.Default.Star,
                title = "Productivity Score",
                value = analyticsData.productivityScore.toString(),
                color = MaterialTheme.colorScheme.tertiary
            )
        }
    }
}

@Composable
fun InsightItem(
    icon: ImageVector,
    title: String,
    value: String,
    color: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = color,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}