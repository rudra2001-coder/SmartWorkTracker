package com.rudra.smartworktracker.ui.screens.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.rudra.smartworktracker.model.WorkType
import com.rudra.smartworktracker.ui.WorkLogUi
import com.rudra.smartworktracker.ui.components.SectionHeader
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun RecentActivityTimeline(activities: List<WorkLogUi>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            SectionHeader(title = "Recent Activity")

            if (activities.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "No recent activity",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                Spacer(modifier = Modifier.height(8.dp))
                activities.take(7).forEachIndexed { index, activity ->
                    ActivityRow(activity = activity)
                    if (index < activities.lastIndex) {
                        HorizontalDivider(
                            modifier = Modifier.padding(start = 19.dp, top = 2.dp, bottom = 2.dp),
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ActivityRow(activity: WorkLogUi) {
    val (color, label) = remember(activity.workType) {
        when (activity.workType) {
            WorkType.OFFICE -> Color(0xFF2196F3) to "Office"
            WorkType.HOME_OFFICE -> Color(0xFFFF9800) to "Home Office"
            WorkType.OFF_DAY -> Color(0xFF9C27B0) to "Off Day"
            WorkType.EXTRA_WORK -> Color(0xFFE91E63) to "Extra Work"
            WorkType.OVERTIME -> Color(0xFFE91E63) to "Overtime"
        }
    }

    val dateStr = remember(activity.date) {
        SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(activity.date)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .background(color, CircleShape)
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = dateStr,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Surface(
            shape = RoundedCornerShape(8.dp),
            color = color.copy(alpha = 0.1f)
        ) {
            Text(
                text = activity.duration,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Medium,
                color = color
            )
        }
    }
}
