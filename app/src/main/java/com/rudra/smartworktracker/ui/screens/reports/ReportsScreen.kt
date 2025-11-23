package com.rudra.smartworktracker.ui.screens.reports

import android.app.Application
import android.content.Intent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat.startActivity
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rudra.smartworktracker.model.ExpenseCategory
import com.rudra.smartworktracker.model.WorkType
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(onNavigateBack: () -> Unit) {
    val application = LocalContext.current.applicationContext as Application
    val context = LocalContext.current
    val viewModel: ReportsViewModel = viewModel(factory = ReportsViewModelFactory(application))
    val uiState by viewModel.uiState.collectAsState()
    var dateRangeExpanded by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()
    var sortMenuExpanded by remember { mutableStateOf(false) }


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
                                    if (dateRange == DateRange.Custom) {
                                        showDatePicker = true
                                    } else {
                                        viewModel.onDateRangeChange(dateRange)
                                    }
                                    dateRangeExpanded = false
                                }
                            )
                        }
                    }
                }
                ExposedDropdownMenuBox(expanded = sortMenuExpanded, onExpandedChange = { sortMenuExpanded = it }) {
                    TextField(
                        value = uiState.sortOption.name,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = sortMenuExpanded) },
                        modifier = Modifier.menuAnchor()
                    )
                    ExposedDropdownMenu(expanded = sortMenuExpanded, onDismissRequest = { sortMenuExpanded = false }) {
                        SortOption.values().forEach { sortOption ->
                            DropdownMenuItem(
                                text = { Text(sortOption.name) },
                                onClick = {
                                    viewModel.onSortOptionChange(sortOption)
                                    sortMenuExpanded = false
                                }
                            )
                        }
                    }
                }
            }
            if (showDatePicker) {
                DatePickerDialog(
                    onDismissRequest = { showDatePicker = false },
                    confirmButton = {
                        Button(
                            onClick = {
                                datePickerState.selectedDateMillis?.let {
                                    val selectedDate = it
                                    // For simplicity, we'll just use the selected date as both start and end
                                    viewModel.onCustomDateRangeChange(selectedDate, selectedDate)
                                }
                                showDatePicker = false
                            }
                        ) {
                            Text("OK")
                        }
                    }
                ) {
                    DatePicker(state = datePickerState)
                }
            }
            FilterChips(viewModel = viewModel, uiState = uiState)

            // Summary Cards
            Row(modifier = Modifier.fillMaxWidth().padding(8.dp), horizontalArrangement = Arrangement.SpaceAround) {
                SummaryCard(title = "Total Work Hours", value = "${uiState.totalWorkHours} hrs")
                SummaryCard(title = "Total Income", value = "${uiState.totalIncome} TK")
            }
            Row(modifier = Modifier.fillMaxWidth().padding(8.dp), horizontalArrangement = Arrangement.SpaceAround) {
                SummaryCard(title = "Total Expense", value = "${uiState.totalExpense} TK")
                SummaryCard(title = "Net Profit", value = "${uiState.netProfit} TK")
            }
            BarChart(income = uiState.totalIncome, expense = uiState.totalExpense)
            Button(onClick = { 
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, viewModel.generateTextReport())
                }
                context.startActivity(Intent.createChooser(intent, "Share Report"))
             }) {
                Text("Export to text")
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
fun FilterChips(viewModel: ReportsViewModel, uiState: ReportUiState) {
    LazyRow(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
        if (uiState.selectedCategory == ReportCategory.All || uiState.selectedCategory == ReportCategory.Work) {
            items(WorkType.values()) { workType ->
                FilterChip(
                    selected = uiState.workTypeFilter == workType.name,
                    onClick = { viewModel.onWorkTypeFilterChange(if (uiState.workTypeFilter == workType.name) null else workType.name) },
                    label = { Text(workType.name) }
                )
            }
        }
        if (uiState.selectedCategory == ReportCategory.All || uiState.selectedCategory == ReportCategory.Income) {
            // Add income categories here
        }
        if (uiState.selectedCategory == ReportCategory.All || uiState.selectedCategory == ReportCategory.Expense) {
            items(ExpenseCategory.values()) { expenseCategory ->
                FilterChip(
                    selected = uiState.expenseCategoryFilter == expenseCategory.name,
                    onClick = { viewModel.onExpenseCategoryFilterChange(if (uiState.expenseCategoryFilter == expenseCategory.name) null else expenseCategory.name) },
                    label = { Text(expenseCategory.name) }
                )
            }
        }
    }
}

@Composable
fun SummaryCard(title: String, value: String) {
    Card(modifier = Modifier.padding(8.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = title, style = MaterialTheme.typography.bodyLarge)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun BarChart(income: Double, expense: Double) {
    val maxAmount = (income + expense).toFloat()
    val incomeColor = MaterialTheme.colorScheme.primary
    val expenseColor = MaterialTheme.colorScheme.error

    Card(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "Income vs Expense", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(16.dp))
            Box(modifier = Modifier.height(200.dp).fillMaxWidth()) {
                Canvas(modifier = Modifier.fillMaxSize()) {                    val barWidth = size.width / 4
                    val incomeHeight = if (maxAmount > 0) (income.toFloat() / maxAmount) * size.height else 0f
                    val expenseHeight = if (maxAmount > 0) (expense.toFloat() / maxAmount) * size.height else 0f

                    drawRect(
                        color = incomeColor,
                        topLeft = Offset(x = barWidth / 2, y = size.height - incomeHeight),
                        size = androidx.compose.ui.geometry.Size(barWidth, incomeHeight)
                    )
                    drawRect(
                        color = expenseColor,
                        topLeft = Offset(x = barWidth * 2.5f, y = size.height - expenseHeight),
                        size = androidx.compose.ui.geometry.Size(barWidth, expenseHeight)
                    )
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
            Text(text = "Date: ${SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(workLog.date.time))}", style = MaterialTheme.typography.bodySmall)
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
