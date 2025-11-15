package com.rudra.smartworktracker.ui.screens.reports

import android.app.Application
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(onNavigateBack: () -> Unit) {
    val application = LocalContext.current.applicationContext as Application
    val viewModel: ReportsViewModel = viewModel(factory = ReportsViewModelFactory(application))
    val uiState by viewModel.uiState.collectAsState()
    var dateRangeExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Reports") },
                navigationIcon = { IconButton(onClick = onNavigateBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") } }
            )
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) {
            TabRow(selectedTabIndex = uiState.selectedCategory.ordinal) {
                ReportCategory.values().forEach { category ->
                    Tab(
                        selected = uiState.selectedCategory == category,
                        onClick = { viewModel.onCategoryChange(category) },
                        text = { Text(category.name) }
                    )
                }
            }
            Row(modifier = Modifier.fillMaxWidth()) {
                ExposedDropdownMenuBox(expanded = dateRangeExpanded, onExpandedChange = { dateRangeExpanded = it }) {
                    TextField(
                        value = uiState.selectedDateRange.name,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dateRangeExpanded) },
                        modifier = Modifier.menuAnchor()
                    )
                    ExposedDropdownMenu(expanded = dateRangeExpanded, onDismissRequest = { dateRangeExpanded = false }) {
                        DateRange.values().forEach { dateRange ->
                            DropdownMenuItem(
                                text = { Text(dateRange.name) },
                                onClick = {
                                    viewModel.onDateRangeChange(dateRange)
                                    dateRangeExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(uiState.filteredItems) { item ->
                    when (item) {
                        is WorkLogReportItem -> ReportWorkLogItem(item.workLog)
                        is IncomeReportItem -> ReportIncomeItem(item.income)
                        is ExpenseReportItem -> ReportExpenseItem(item.expense)
                    }
                }
            }
        }
    }
}

@Composable
fun ReportWorkLogItem(workLog: com.rudra.smartworktracker.model.WorkLog) {
    Card(modifier = Modifier.padding(8.dp).fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "Work Log: ${workLog.workType}", style = MaterialTheme.typography.bodyLarge)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "Date: ${workLog.date}", style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
fun ReportIncomeItem(income: com.rudra.smartworktracker.data.entity.Income) {
    Card(modifier = Modifier.padding(8.dp).fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "Income: ${income.amount}", style = MaterialTheme.typography.bodyLarge)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "Category: ${income.category}", style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
fun ReportExpenseItem(expense: com.rudra.smartworktracker.model.Expense) {
    Card(modifier = Modifier.padding(8.dp).fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "Expense: ${expense.amount}", style = MaterialTheme.typography.bodyLarge)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "Category: ${expense.category}", style = MaterialTheme.typography.bodySmall)
        }
    }
}
