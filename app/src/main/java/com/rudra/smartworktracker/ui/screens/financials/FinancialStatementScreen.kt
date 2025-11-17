package com.rudra.smartworktracker.ui.screens.financials

import android.app.Application
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.CompareArrows
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rudra.smartworktracker.data.entity.FinancialTransaction
import com.rudra.smartworktracker.data.entity.TransactionType
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinancialStatementScreen() {
    val context = LocalContext.current
    val viewModel: FinancialStatementViewModel = viewModel(factory = FinancialStatementViewModelFactory(context.applicationContext as Application))
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Financial Statement") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) {
            FilterChips(selectedFilter = uiState.filter, onFilterSelected = { newFilter -> viewModel.setFilter(newFilter) })
            when {
                uiState.isLoading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                uiState.errorMessage != null -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = uiState.errorMessage ?: "An unknown error occurred",
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
                uiState.transactions.isEmpty() -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(text = "No transactions found.")
                    }
                }
                else -> {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        item {
                            SummaryHeader(uiState.totalIncome, uiState.totalExpenses, uiState.netFlow)
                        }
                        items(uiState.transactions) { transaction ->
                            TransactionItem(transaction = transaction)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FilterChips(selectedFilter: TransactionFilter, onFilterSelected: (TransactionFilter) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        TransactionFilter.values().forEach { filter ->
            FilterChip(
                selected = selectedFilter == filter,
                onClick = { onFilterSelected(filter) },
                label = { Text(filter.displayName) }
            )
        }
    }
}

@Composable
fun SummaryHeader(totalIncome: Double, totalExpenses: Double, netFlow: Double) {
    val currencyFormat = remember { NumberFormat.getCurrencyInstance() }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Account Summary", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))

            IncomeExpenseBar(income = totalIncome, expense = totalExpenses, modifier = Modifier.fillMaxWidth())

            Spacer(modifier = Modifier.height(16.dp))

            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                SummaryItem("Income", totalIncome, MaterialTheme.colorScheme.primary, currencyFormat)
                SummaryItem("Expenses", totalExpenses, MaterialTheme.colorScheme.error, currencyFormat)
            }

            Spacer(modifier = Modifier.height(16.dp))

            HorizontalDivider()

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Net Flow", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(
                    text = currencyFormat.format(netFlow),
                    style = MaterialTheme.typography.headlineSmall,
                    color = if (netFlow >= 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun IncomeExpenseBar(income: Double, expense: Double, modifier: Modifier = Modifier) {
    val total = income + expense
    if (total <= 0) {
        // Show empty state bar
        Box(
            modifier = modifier
                .height(8.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        )
        return
    }

    val incomeRatio = (income / total).toFloat()
    val expenseRatio = (expense / total).toFloat()

    Row(modifier = modifier.clip(RoundedCornerShape(8.dp))) {
        if (income > 0) {
            Box(
                modifier = Modifier
                    .weight(incomeRatio)
                    .height(8.dp)
                    .background(MaterialTheme.colorScheme.primary)
            )
        }
        if (expense > 0) {
            Box(
                modifier = Modifier
                    .weight(expenseRatio)
                    .height(8.dp)
                    .background(MaterialTheme.colorScheme.error)
            )
        }
    }
}

@Composable
fun SummaryItem(title: String, amount: Double, color: Color, currencyFormat: NumberFormat) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(title, style = MaterialTheme.typography.titleMedium)
        Text(
            currencyFormat.format(amount),
            style = MaterialTheme.typography.headlineSmall,
            color = color,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun TransactionItem(transaction: FinancialTransaction) {
    var expanded by remember { mutableStateOf(false) }
    val rotationAngle by animateFloatAsState(targetValue = if (expanded) 90f else 0f, label = "rotation")
    val currencyFormat = remember { NumberFormat.getCurrencyInstance() }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable { expanded = !expanded },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column {
            ListItem(
                headlineContent = {
                    Text(
                        transaction.note.ifEmpty { transaction.type.getDisplayName() },
                        fontWeight = FontWeight.SemiBold
                    )
                },
                supportingContent = {
                    Text(formatTransactionDate(transaction.date))
                },
                leadingContent = {
                    val color = transaction.type.getColorScheme()
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(color.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = transaction.type.getIcon(),
                            contentDescription = transaction.type.name,
                            tint = color,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                },
                trailingContent = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = currencyFormat.format(transaction.amount),
                            style = MaterialTheme.typography.titleMedium,
                            color = transaction.type.getColorScheme(),
                            fontWeight = FontWeight.Bold
                        )
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = "Expand",
                            modifier = Modifier.rotate(rotationAngle)
                        )
                    }
                }
            )

            AnimatedVisibility(
                visible = expanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp)) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    Text("Details", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))

                    KeyValueText("Amount", currencyFormat.format(transaction.amount))
                    KeyValueText("Type", transaction.type.getDisplayName())
                    if (transaction.note.isNotEmpty()) {
                        KeyValueText("Note", transaction.note)
                    }
                    KeyValueText("From", transaction.source.toString())
                    transaction.destination?.let { KeyValueText("To", it.toString()) }
                }
            }
        }
    }
}

@Composable
fun KeyValueText(key: String, value: String) {
    Row(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(key, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
        Text(value, modifier = Modifier.weight(2f), textAlign = TextAlign.End)
    }
}

// Extension functions for TransactionType
fun TransactionType.getDisplayName(): String {
    return this.name.replace("_", " ")
}

@Composable
fun TransactionType.getColorScheme(): Color {
    return when (this) {
        TransactionType.INCOME, TransactionType.LOAN_RECEIVE -> MaterialTheme.colorScheme.primary
        TransactionType.EXPENSE, TransactionType.EMI_PAID -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
}

fun TransactionType.getIcon(): ImageVector {
    return when (this) {
        TransactionType.INCOME, TransactionType.LOAN_RECEIVE -> Icons.Default.ArrowUpward
        TransactionType.EXPENSE, TransactionType.EMI_PAID -> Icons.Default.ArrowDownward
        else -> Icons.AutoMirrored.Filled.CompareArrows
    }
}

// Helper function for date formatting
@Composable
fun formatTransactionDate(timestamp: Long): String {
    return remember(timestamp) {
        SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(timestamp))
    }
}

// Add this enum if missing
enum class TransactionFilter(val displayName: String) {
    ALL("All"),
    INCOME("Income"),
    EXPENSE("Expense")

}