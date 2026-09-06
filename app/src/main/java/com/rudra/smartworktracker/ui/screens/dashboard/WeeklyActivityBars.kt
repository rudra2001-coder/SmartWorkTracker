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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rudra.smartworktracker.model.WorkLog
import com.rudra.smartworktracker.ui.components.SectionHeader
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.TextStyle as TextStyleEnum
import java.util.Locale

@Composable
fun WeeklyActivityBars(workLogs: List<WorkLog>) {
    val today = remember { LocalDate.now() }
    val last7Days = remember(today) { (0L..6L).map { today.minusDays(it) }.reversed() }

    val dailyHours = remember(workLogs, last7Days) {
        last7Days.map { date ->
            workLogs.filter { log ->
                val logDate = Instant.ofEpochMilli(log.date.time)
                    .atZone(ZoneId.systemDefault()).toLocalDate()
                logDate == date
            }.sumOf { log ->
                val start = log.startTime?.let { parseMinutes(it) } ?: 0
                val end = log.endTime?.let { parseMinutes(it) } ?: 0
                ((end - start).coerceAtLeast(0)) / 60.0
            }
        }
    }

    val maxHours = remember(dailyHours) { (dailyHours.maxOrNull() ?: 1.0).coerceAtLeast(1.0) }

    var animProgress by remember { mutableFloatStateOf(0f) }
    val animatedProgress by animateFloatAsState(
        targetValue = animProgress,
        animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
        label = "bars"
    )
    LaunchedEffect(Unit) { animProgress = 1f }

    val barColor = MaterialTheme.colorScheme.primary
    val barBg = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
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
            SectionHeader(title = "Weekly Activity")

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .padding(top = 12.dp)
            ) {
                val barCount = last7Days.size
                val gap = 12f
                val barWidth = (size.width - gap * (barCount + 1)) / barCount
                val chartHeight = size.height - 30f

                last7Days.forEachIndexed { i, date ->
                    val x = gap + i * (barWidth + gap)
                    val barH = ((dailyHours[i] / maxHours) * chartHeight).toFloat() * animatedProgress
                    val y = chartHeight - barH

                    // Background bar
                    drawRoundRect(
                        barBg,
                        Offset(x, 0f),
                        Size(barWidth, chartHeight),
                        CornerRadius(6f, 6f)
                    )

                    // Filled bar
                    if (barH > 0f) {
                        drawRoundRect(
                            barColor,
                            Offset(x, y),
                            Size(barWidth, barH),
                            CornerRadius(6f, 6f)
                        )
                    }

                    // Day label
                    val label = date.dayOfWeek.getDisplayName(TextStyleEnum.SHORT, Locale.getDefault())
                    val measured = textMeasurer.measure(label, TextStyle(fontSize = 10.sp, color = Color.Gray))
                    drawText(
                        measured,
                        topLeft = Offset(x + barWidth / 2 - measured.size.width / 2, chartHeight + 6f)
                    )

                    // Hour label on bar
                    if (dailyHours[i] > 0) {
                        val hourText = "${"%.1f".format(dailyHours[i])}h"
                        val hourMeasured = textMeasurer.measure(hourText, TextStyle(fontSize = 9.sp, color = Color.White, fontWeight = FontWeight.Medium))
                        drawText(
                            hourMeasured,
                            topLeft = Offset(x + barWidth / 2 - hourMeasured.size.width / 2, y - 14f)
                        )
                    }
                }
            }
        }
    }
}

private fun parseMinutes(time: String): Int {
    return try {
        val parts = time.split(":")
        parts[0].toInt() * 60 + parts[1].toInt()
    } catch (_: Exception) {
        0
    }
}
