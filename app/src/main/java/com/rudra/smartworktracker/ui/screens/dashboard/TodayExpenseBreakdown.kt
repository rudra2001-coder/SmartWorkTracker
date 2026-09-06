package com.rudra.smartworktracker.ui.screens.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Receipt
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.rudra.smartworktracker.model.Expense
import com.rudra.smartworktracker.ui.components.EmptyStateCard
import com.rudra.smartworktracker.ui.components.SectionHeader
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun TodayExpenseBreakdown(expenses: List<Expense>) {
    val today = remember { LocalDate.now() }
    val todayExpenses = remember(expenses) {
        expenses.filter { expense ->
            val expDate = Instant.ofEpochMilli(expense.timestamp)
                .atZone(ZoneId.systemDefault()).toLocalDate()
            expDate == today
        }.sortedByDescending { it.timestamp }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            SectionHeader(title = "Today's Expenses")

            if (todayExpenses.isEmpty()) {
                EmptyStateCard(
                    icon = Icons.Outlined.Receipt,
                    title = "No expenses today",
                    message = "Your expenses for today will appear here"
                )
            } else {
                Spacer(modifier = Modifier.height(8.dp))
                todayExpenses.forEach { expense ->
                    ExpenseRow(expense = expense)
                    if (expense != todayExpenses.last()) {
                        HorizontalDivider(
                            modifier = Modifier.padding(start = 40.dp),
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ExpenseRow(expense: Expense) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = expense.category.color.copy(alpha = 0.12f),
            modifier = Modifier.size(38.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = expense.category.displayName.first().toString(),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = expense.category.color
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = expense.merchant ?: expense.category.displayName,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (expense.notes != null) {
                Text(
                    text = expense.notes!!,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }
        }

        Text(
            text = "\u09F3${"%.0f".format(expense.amount)}",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
