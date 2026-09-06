package com.rudra.smartworktracker.ui.screens.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AttachMoney
import androidx.compose.material.icons.outlined.MoneyOff
import androidx.compose.material.icons.outlined.Savings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rudra.smartworktracker.ui.FinancialSummary
import com.rudra.smartworktracker.ui.components.AnimatedDoubleCounter

@Composable
fun TodaySnapshot(financialSummary: FinancialSummary) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        TodayMetricCard(
            title = "Income",
            value = financialSummary.dailyIncome,
            icon = Icons.Outlined.AttachMoney,
            color = Color(0xFF4CAF50),
            modifier = Modifier.weight(1f)
        )
        TodayMetricCard(
            title = "Expense",
            value = financialSummary.dailyExpense,
            icon = Icons.Outlined.MoneyOff,
            color = MaterialTheme.colorScheme.error,
            modifier = Modifier.weight(1f)
        )
        TodayMetricCard(
            title = "Savings",
            value = financialSummary.dailySavings,
            icon = Icons.Outlined.Savings,
            color = if (financialSummary.dailySavings >= 0) Color(0xFF4CAF50) else MaterialTheme.colorScheme.error,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun TodayMetricCard(
    title: String,
    value: Double,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.08f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            AnimatedDoubleCounter(
                targetValue = value,
                prefix = "\u09F3",
                color = color,
                fontSize = 16.sp,
                durationMillis = 600
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
