package com.rudra.smartworktracker.ui.screens.dashboard

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.TrendingDown
import androidx.compose.material.icons.automirrored.outlined.TrendingUp
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rudra.smartworktracker.ui.components.AnimatedDoubleCounter
import com.rudra.smartworktracker.ui.FinancialSummary

@Composable
fun NetWorthHeroCard(financialSummary: FinancialSummary) {
    val netWorth = financialSummary.allTimeIncome - financialSummary.allTimeExpense
    val isPositive = netWorth >= 0

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(12.dp, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = "Net Worth",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))

            AnimatedDoubleCounter(
                targetValue = netWorth,
                prefix = "\u09F3",
                color = if (isPositive) Color(0xFF4CAF50) else MaterialTheme.colorScheme.error,
                fontSize = 32.sp,
                durationMillis = 1000
            )

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                NetWorthSide(
                    label = "Income",
                    value = financialSummary.allTimeIncome,
                    color = Color(0xFF4CAF50),
                    isIncome = true
                )
                NetWorthSide(
                    label = "Expense",
                    value = financialSummary.allTimeExpense,
                    color = MaterialTheme.colorScheme.error,
                    isIncome = false
                )
            }
        }
    }
}

@Composable
private fun NetWorthSide(label: String, value: Double, color: Color, isIncome: Boolean) {
    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                if (isIncome) Icons.AutoMirrored.Outlined.TrendingUp else Icons.AutoMirrored.Outlined.TrendingDown,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.height(2.dp))
        AnimatedDoubleCounter(
            targetValue = value,
            prefix = "\u09F3",
            color = color,
            fontSize = 18.sp,
            durationMillis = 800
        )
    }
}
