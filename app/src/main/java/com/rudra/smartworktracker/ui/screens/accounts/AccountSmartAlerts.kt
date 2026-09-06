package com.rudra.smartworktracker.ui.screens.accounts

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.rudra.smartworktracker.engine.SmartAlert

@Composable
fun SmartAlertsSection(alerts: List<SmartAlert>) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        alerts.take(2).forEach { alert ->
            AlertItem(alert = alert)
        }
    }
}

@Composable
fun AlertItem(alert: SmartAlert) {
    val (icon, color, message) = when (alert) {
        is SmartAlert.LowBalance -> Triple(
            Icons.Default.Warning,
            Color(0xFFFF9800),
            alert.message
        )
        is SmartAlert.ApproachingLimit -> Triple(
            Icons.Default.TrendingUp,
            Color(0xFFFFC107),
            alert.message
        )
        is SmartAlert.HighSpending -> Triple(
            Icons.Default.TrendingDown,
            Color(0xFFF44336),
            alert.message
        )
        is SmartAlert.TransferHabit -> Triple(
            Icons.Default.SwapHoriz,
            Color(0xFF2196F3),
            alert.message
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(icon, contentDescription = null, tint = color)
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = color
            )
        }
    }
}
