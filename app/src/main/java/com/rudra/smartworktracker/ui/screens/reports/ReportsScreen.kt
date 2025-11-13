package com.rudra.smartworktracker.ui.screens.reports

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rudra.smartworktracker.model.Expense
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen() {
    val context = LocalContext.current
    val viewModel: ReportsViewModel = viewModel(factory = ReportsViewModelFactory(context))
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Monthly Reports") })
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            MonthSelector(selectedDate = uiState.selectedDate, onDateChange = viewModel::onDateChange)
            if (uiState.expenses.isNotEmpty()) {
                ExpenseSummary(totalExpenses = uiState.totalExpenses)
                ExpenseList(expenses = uiState.expenses)
            } else {
                Text("No expenses for this month.")
            }
        }
    }
}

@Composable
fun MonthSelector(selectedDate: Calendar, onDateChange: (Calendar) -> Unit) {
    val dateFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = {
            val newDate = selectedDate.clone() as Calendar
            newDate.add(Calendar.MONTH, -1)
            onDateChange(newDate)
        }) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Previous Month")
        }
        Text(
            text = dateFormat.format(selectedDate.time),
            style = MaterialTheme.typography.titleMedium
        )
        IconButton(onClick = {
            val newDate = selectedDate.clone() as Calendar
            newDate.add(Calendar.MONTH, 1)
            onDateChange(newDate)
        }) {
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next Month")
        }
    }
}

@Composable
fun ExpenseSummary(totalExpenses: Double) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Total Expenses", style = MaterialTheme.typography.titleMedium)
            Text("%.2f BDT".format(totalExpenses), style = MaterialTheme.typography.headlineSmall)
        }
    }
}

@Composable
fun ExpenseList(expenses: List<Expense>) {
    LazyColumn {
        items(expenses) { expense ->
            Card(modifier = Modifier.padding(vertical = 4.dp)) {
                ListItem(
                    headlineContent = { Text(expense.category.name) },
                    supportingContent = { Text(expense.notes ?: "") },
                    trailingContent = { Text("%.2f BDT".format(expense.amount)) }
                )
            }
        }
    }
}
