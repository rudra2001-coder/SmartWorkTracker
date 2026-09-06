package com.rudra.smartworktracker.ui.screens.dashboard

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rudra.smartworktracker.model.Expense
import com.rudra.smartworktracker.ui.components.SectionHeader
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.TextStyle as TextStyleEnum
import java.util.Locale

@Composable
fun WeeklySpendingTrend(expenses: List<Expense>) {
    val today = remember { LocalDate.now() }
    val last7Days = remember(today) { (0L..6L).map { today.minusDays(it) }.reversed() }

    val dailyTotals = remember(expenses, last7Days) {
        last7Days.map { date ->
            expenses.filter { expense ->
                val expDate = Instant.ofEpochMilli(expense.timestamp)
                    .atZone(ZoneId.systemDefault()).toLocalDate()
                expDate == date
            }.sumOf { it.amount }
        }
    }

    val maxVal = remember(dailyTotals) { (dailyTotals.maxOrNull() ?: 1.0).coerceAtLeast(1.0) }

    var animProgress by remember { mutableFloatStateOf(0f) }
    val animatedProgress by animateFloatAsState(
        targetValue = animProgress,
        animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
        label = "line"
    )
    LaunchedEffect(Unit) { animProgress = 1f }

    val lineColor = MaterialTheme.colorScheme.primary
    val gridColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)

    val textMeasurer = rememberTextMeasurer()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            SectionHeader(title = "Weekly Spending")

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .padding(top = 12.dp)
            ) {
                val chartHeight = size.height - 30f
                val chartWidth = size.width
                val stepX = chartWidth / (last7Days.size - 1).coerceAtLeast(1)

                // Grid lines
                for (i in 0..3) {
                    val y = chartHeight * (i / 3f)
                    drawLine(gridColor, Offset(0f, y), Offset(chartWidth, y), strokeWidth = 1f)
                }

                if (dailyTotals.isNotEmpty()) {
                    val points = dailyTotals.mapIndexed { i, v ->
                        Offset(
                            i * stepX,
                            chartHeight - ((v / maxVal) * chartHeight).toFloat() * animatedProgress
                        )
                    }

                    // Fill
                    val fillPath = Path().apply {
                        moveTo(points.first().x, chartHeight)
                        points.forEach { lineTo(it.x, it.y) }
                        lineTo(points.last().x, chartHeight)
                        close()
                    }
                    drawPath(fillPath, lineColor.copy(alpha = 0.1f))

                    // Line
                    val linePath = Path().apply {
                        moveTo(points.first().x, points.first().y)
                        for (i in 1 until points.size) {
                            lineTo(points[i].x, points[i].y)
                        }
                    }
                    drawPath(linePath, lineColor, style = Stroke(width = 2.5f, cap = StrokeCap.Round))

                    // Dots
                    points.forEach { p ->
                        drawCircle(lineColor, 4f, p)
                        drawCircle(Color.White, 2f, p)
                    }

                    // Day labels
                    val textStyle = TextStyle(fontSize = 10.sp, color = Color.Gray)
                    last7Days.forEachIndexed { i, date ->
                        val label = date.dayOfWeek.getDisplayName(TextStyleEnum.SHORT, Locale.getDefault())
                        val measured = textMeasurer.measure(label, textStyle)
                        drawText(
                            measured,
                            topLeft = Offset(
                                points[i].x - measured.size.width / 2f,
                                chartHeight + 6f
                            )
                        )
                    }
                }
            }
        }
    }
}
