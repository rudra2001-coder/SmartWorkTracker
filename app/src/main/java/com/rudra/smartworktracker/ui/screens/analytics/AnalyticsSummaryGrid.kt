package com.rudra.smartworktracker.ui.screens.analytics

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun AnimatedSummaryGrid(data: AnalyticsData) {
    val items = listOf(
        SummaryItem("Work Hours", "${String.format(java.util.Locale.getDefault(), "%.1f", data.workHoursToday)}h", Icons.Default.Timer, Color(0xFF4A90E2), data.workHoursTrend),
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
