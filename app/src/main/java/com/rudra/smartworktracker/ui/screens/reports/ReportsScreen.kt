package com.rudra.smartworktracker.ui.screens.reports

import android.app.Application
import android.content.Intent
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rudra.smartworktracker.model.ExpenseCategory
import com.rudra.smartworktracker.model.WorkType
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(onNavigateBack: () -> Unit) {
    val application = LocalContext.current.applicationContext as Application
    val context = LocalContext.current
    val viewModel: ReportsViewModel = viewModel(factory = ReportsViewModelFactory(application))
    val uiState by viewModel.uiState.collectAsState()
    val showCustomDatePicker by viewModel.showCustomDatePicker.collectAsState()
    val customStartDate by viewModel.customStartDate.collectAsState()
    val customEndDate by viewModel.customEndDate.collectAsState()

    var dateRangeExpanded by remember { mutableStateOf(false) }
    var sortMenuExpanded by remember { mutableStateOf(false) }

    val gradientBackground = Brush.verticalGradient(
        colors = listOf(
            MaterialTheme.colorScheme.primaryContainer,
            MaterialTheme.colorScheme.surface
        )
    )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Report Dashboard",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            "Back",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    TextButton(onClick = {
                        val intent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, viewModel.generateTextReport())
                        }
                        context.startActivity(Intent.createChooser(intent, "Share Report"))
                    }) {
                        Icon(Icons.Default.Share, contentDescription = "Share")
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Export")
                    }
                },
                colors = androidx.compose.material3.TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        containerColor = Color.Transparent
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(gradientBackground)
                .padding(paddingValues)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                item {
                    Spacer(modifier = Modifier.height(16.dp))

                    // Segmented Control for Categories (EXISTING - UNCHANGED)
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shadowElevation = 4.dp
                    ) {
                        SingleChoiceSegmentedButtonRow(
                            modifier = Modifier.padding(4.dp)
                        ) {
                            ReportCategory.values().forEachIndexed { index, category ->
                                SegmentedButton(
                                    shape = SegmentedButtonDefaults.itemShape(index = index, count = ReportCategory.values().size),
                                    selected = uiState.selectedCategory == category,
                                    onClick = { viewModel.onCategoryChange(category) },
                                    colors = SegmentedButtonDefaults.colors(
                                        activeContainerColor = MaterialTheme.colorScheme.primary,
                                        inactiveContainerColor = Color.Transparent
                                    )
                                ) {
                                    Text(
                                        category.name,
                                        style = MaterialTheme.typography.labelMedium,
                                        color = if (uiState.selectedCategory == category) MaterialTheme.colorScheme.onPrimary
                                        else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    // Filter Section (EXISTING - UNCHANGED)
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surface,
                        shadowElevation = 8.dp
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                "Filters",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                // Date Range Filter (EXISTING - UNCHANGED)
                                ExposedDropdownMenuBox(
                                    expanded = dateRangeExpanded,
                                    onExpandedChange = { dateRangeExpanded = it },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    TextField(
                                        value = uiState.selectedDateRange.name,
                                        onValueChange = {},
                                        readOnly = true,
                                        trailingIcon = {
                                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = dateRangeExpanded)
                                        },
                                        modifier = Modifier
                                            .menuAnchor()
                                            .fillMaxWidth(),
                                        colors = TextFieldDefaults.colors(
                                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                        ),
                                        shape = RoundedCornerShape(12.dp),
                                        textStyle = MaterialTheme.typography.bodyMedium
                                    )
                                    ExposedDropdownMenu(
                                        expanded = dateRangeExpanded,
                                        onDismissRequest = { dateRangeExpanded = false }
                                    ) {
                                        DateRange.values().forEach { dateRange ->
                                            DropdownMenuItem(
                                                text = { Text(dateRange.name) },
                                                onClick = {
                                                    if (dateRange == DateRange.Custom) {
                                                        viewModel.showCustomDatePicker(true)
                                                    } else {
                                                        viewModel.onDateRangeChange(dateRange)
                                                    }
                                                    dateRangeExpanded = false
                                                }
                                            )
                                        }
                                    }
                                }

                                // Sort Filter (EXISTING - UNCHANGED)
                                ExposedDropdownMenuBox(
                                    expanded = sortMenuExpanded,
                                    onExpandedChange = { sortMenuExpanded = it },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    TextField(
                                        value = uiState.sortOption.name,
                                        onValueChange = {},
                                        readOnly = true,
                                        trailingIcon = {
                                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = sortMenuExpanded)
                                        },
                                        modifier = Modifier
                                            .menuAnchor()
                                            .fillMaxWidth(),
                                        colors = TextFieldDefaults.colors(
                                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                        ),
                                        shape = RoundedCornerShape(12.dp),
                                        textStyle = MaterialTheme.typography.bodyMedium
                                    )
                                    ExposedDropdownMenu(
                                        expanded = sortMenuExpanded,
                                        onDismissRequest = { sortMenuExpanded = false }
                                    ) {
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
                            Spacer(modifier = Modifier.height(12.dp))
                            FilterChips(viewModel = viewModel, uiState = uiState)

                            // NEW: Custom Date Range Section (Added below existing filters)
                            if (uiState.selectedDateRange == DateRange.Custom) {
                                Spacer(modifier = Modifier.height(16.dp))
                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                    border = androidx.compose.material3.MaterialTheme.colorScheme.outline.copy(alpha = 0.2f).let { color ->
                                        androidx.compose.foundation.BorderStroke(1.dp, color)
                                    }
                                ) {
                                    Column(
                                        modifier = Modifier.padding(12.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                "Custom Date Range",
                                                style = MaterialTheme.typography.labelLarge,
                                                fontWeight = FontWeight.SemiBold,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )

                                            IconButton(
                                                onClick = { viewModel.showCustomDatePicker(true) },
                                                modifier = Modifier.size(36.dp)
                                            ) {
                                                Icon(
                                                    Icons.Default.CalendarMonth,
                                                    contentDescription = "Set Custom Dates",
                                                    tint = MaterialTheme.colorScheme.primary
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(8.dp))

                                        // Show selected dates
                                        val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                                        val startDateText = if (customStartDate != null) {
                                            sdf.format(Date(customStartDate!!))
                                        } else "Not selected"
                                        val endDateText = if (customEndDate != null) {
                                            sdf.format(Date(customEndDate!!))
                                        } else "Not selected"

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column {
                                                Text(
                                                    "From:",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                                Text(
                                                    startDateText,
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    fontWeight = FontWeight.Medium,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                            }

                                            Icon(
                                                Icons.Default.ArrowBack,
                                                contentDescription = "To",
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.size(20.dp)
                                            )

                                            Column {
                                                Text(
                                                    "To:",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                                Text(
                                                    endDateText,
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    fontWeight = FontWeight.Medium,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(12.dp))

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            OutlinedButton(
                                                onClick = {
                                                    viewModel.setCustomStartDate(null)
                                                    viewModel.setCustomEndDate(null)
                                                    viewModel.clearCustomDateFilter()
                                                },
                                                modifier = Modifier.weight(1f),
                                                enabled = customStartDate != null || customEndDate != null
                                            ) {
                                                Icon(
                                                    Icons.Default.Refresh,
                                                    contentDescription = "Clear",
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("Clear")
                                            }

                                            Spacer(modifier = Modifier.width(8.dp))

                                            Button(
                                                onClick = { viewModel.applyCustomDateFilter() },
                                                modifier = Modifier.weight(1f),
                                                enabled = customStartDate != null && customEndDate != null
                                            ) {
                                                Text("Apply Filter")
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                item {
                    // Summary Cards (EXISTING - UNCHANGED)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                        SummaryCard(title = "Total Work Hours", value = "${uiState.totalWorkHours}", unit = "hrs", icon = Icons.Default.Work, gradient = Brush.horizontalGradient(listOf(Color(0xFF2196F3), Color(0xFF64B5F6))))
                        SummaryCard(title = "Total Income", value = "${uiState.totalIncome.toInt()}", unit = "TK", icon = Icons.Default.TrendingUp, gradient = Brush.horizontalGradient(listOf(Color(0xFF4CAF50), Color(0xFF81C784))))
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                        SummaryCard(title = "Total Expense", value = "${uiState.totalExpense.toInt()}", unit = "TK", icon = Icons.Default.Wallet, gradient = Brush.horizontalGradient(listOf(Color(0xFFF44336), Color(0xFFE57373))))
                        SummaryCard(title = "Net Profit", value = "${uiState.netProfit.toInt()}", unit = "TK", icon = Icons.Default.TrendingUp, gradient = Brush.horizontalGradient(listOf(Color(0xFFFF9800), Color(0xFFFFB74D))))
                    }
                }

                item {
                    BarChart(income = uiState.totalIncome, expense = uiState.totalExpense)
                }

                item {
                    Text(
                        "Details",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                items(uiState.filteredItems) { item ->
                    when (item) {
                        is WorkLogReportItem -> PremiumWorkLogItem(item.workLog)
                        is IncomeReportItem -> PremiumIncomeItem(item.income)
                        is ExpenseReportItem -> PremiumExpenseItem(item.expense)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            // Custom Date Range Picker Dialog
            if (showCustomDatePicker) {
                CustomDateRangeDialog(
                    viewModel = viewModel,
                    onDismiss = { viewModel.showCustomDatePicker(false) },
                    initialStartDate = customStartDate,
                    initialEndDate = customEndDate
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomDateRangeDialog(
    viewModel: ReportsViewModel,
    onDismiss: () -> Unit,
    initialStartDate: Long? = null,
    initialEndDate: Long? = null
) {
    var selectedTab by remember { mutableStateOf(0) } // 0 for start date, 1 for end date
    var showDatePicker by remember { mutableStateOf(false) }

    val startDateState = rememberDatePickerState(
        initialSelectedDateMillis = initialStartDate
    )
    val endDateState = rememberDatePickerState(
        initialSelectedDateMillis = initialEndDate
    )

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                Text(
                    "Select Date Range",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Date Selection Tabs
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            selectedTab = 0
                            showDatePicker = true
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (selectedTab == 0)
                                MaterialTheme.colorScheme.primaryContainer
                            else MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                "Start Date",
                                style = MaterialTheme.typography.labelMedium
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                if (initialStartDate != null) {
                                    val sdf = SimpleDateFormat("dd MMM", Locale.getDefault())
                                    sdf.format(Date(initialStartDate))
                                } else {
                                    "Select"
                                },
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }

                    OutlinedButton(
                        onClick = {
                            selectedTab = 1
                            showDatePicker = true
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (selectedTab == 1)
                                MaterialTheme.colorScheme.primaryContainer
                            else MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                "End Date",
                                style = MaterialTheme.typography.labelMedium
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                if (initialEndDate != null) {
                                    val sdf = SimpleDateFormat("dd MMM", Locale.getDefault())
                                    sdf.format(Date(initialEndDate))
                                } else {
                                    "Select"
                                },
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Selected Range Preview
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "Selected Range",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))

                        val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                        val startDateText = if (initialStartDate != null) {
                            sdf.format(Date(initialStartDate))
                        } else "Not selected"
                        val endDateText = if (initialEndDate != null) {
                            sdf.format(Date(initialEndDate))
                        } else "Not selected"

                        Text(
                            "$startDateText - $endDateText",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            viewModel.applyCustomDateFilter()
                            onDismiss()
                        },
                        enabled = initialStartDate != null && initialEndDate != null
                    ) {
                        Text("Apply")
                    }
                }
            }
        }
    }

    // Date Picker Dialog
    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                Button(
                    onClick = {
                        val selectedDate = if (selectedTab == 0) {
                            startDateState.selectedDateMillis?.also { date ->
                                viewModel.setCustomStartDate(date)
                            }
                        } else {
                            endDateState.selectedDateMillis?.also { date ->
                                viewModel.setCustomEndDate(date)
                            }
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("Select")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(
                state = if (selectedTab == 0) startDateState else endDateState,
                title = {
                    Text(
                        if (selectedTab == 0) "Select Start Date" else "Select End Date"
                    )
                }
            )
        }
    }
}

// FilterChips, SummaryCard, BarChart, and item composables remain EXACTLY THE SAME
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterChips(viewModel: ReportsViewModel, uiState: ReportUiState) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (uiState.selectedCategory == ReportCategory.All || uiState.selectedCategory == ReportCategory.Work) {
            items(WorkType.values()) { workType ->
                val isSelected = uiState.workTypeFilter == workType.name
                FilterChip(
                    selected = isSelected,
                    onClick = {
                        viewModel.onWorkTypeFilterChange(
                            if (isSelected) null else workType.name
                        )
                    },
                    label = {
                        Text(
                            workType.name,
                            style = MaterialTheme.typography.labelMedium
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        borderColor = Color.Transparent,
                        selectedBorderColor = Color.Transparent,
                        enabled = true,
                        selected = isSelected
                    ),
                    shape = RoundedCornerShape(8.dp)
                )
            }
        }
        if (uiState.selectedCategory == ReportCategory.All || uiState.selectedCategory == ReportCategory.Expense) {
            items(ExpenseCategory.values()) { expenseCategory ->
                 val isSelected = uiState.expenseCategoryFilter == expenseCategory.name
                FilterChip(
                    selected = isSelected,
                    onClick = {
                        viewModel.onExpenseCategoryFilterChange(
                            if (isSelected) null else expenseCategory.name
                        )
                    },
                    label = {
                        Text(
                            expenseCategory.name,
                            style = MaterialTheme.typography.labelMedium
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        borderColor = Color.Transparent,
                        selectedBorderColor = Color.Transparent,
                        enabled = true,
                        selected = isSelected
                    ),
                    shape = RoundedCornerShape(8.dp)
                )
            }
        }
    }
}

@Composable
fun SummaryCard(
    title: String,
    value: String,
    unit: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    gradient: Brush,
    isProfit: Boolean = true
) {
    Card(
        modifier = Modifier
            .width(160.dp)
            .shadow(
                elevation = 12.dp,
                shape = RoundedCornerShape(20.dp),
                clip = true
            ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(20.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(gradient)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        icon,
                        contentDescription = title,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    value,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp
                    ),
                    color = Color.White
                )

                Text(
                    unit,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.8f)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    title,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.9f)
                )
            }
        }
    }
}

@Composable
fun BarChart(income: Double, expense: Double) {
    val maxAmount = maxOf(income, expense, 1.0)
    val incomePercentage = (income / maxAmount).toFloat()
    val expensePercentage = (expense / maxAmount).toFloat()

    val incomeColor = Brush.horizontalGradient(listOf(Color(0xFF11998e), Color(0xFF38ef7d)))
    val expenseColor = Brush.horizontalGradient(listOf(Color(0xFFff416c), Color(0xFFff4b2b)))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                "Income vs Expense",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Bars
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.Bottom
            ) {
                // Income Bar
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "৳${income.toInt()}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .width(40.dp)
                            .height(150.dp * incomePercentage)
                            .clip(RoundedCornerShape(8.dp))
                            .background(incomeColor)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Income",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Expense Bar
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "৳${expense.toInt()}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .width(40.dp)
                            .height(150.dp * expensePercentage)
                            .clip(RoundedCornerShape(8.dp))
                            .background(expenseColor)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Expense",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Legend
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(incomeColor)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Income",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(expenseColor)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Expense",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun PremiumWorkLogItem(workLog: com.rudra.smartworktracker.model.WorkLog) {
    val durationInHours = remember(workLog.startTime, workLog.endTime) {
        if (workLog.startTime != null && workLog.endTime != null) {
            try {
                val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
                val start = sdf.parse(workLog.startTime)
                val end = sdf.parse(workLog.endTime)
                if (start != null && end != null) {
                    val diff = end.time - start.time
                    diff / (1000 * 60 * 60)
                } else 0L
            } catch (e: Exception) {
                0L
            }
        } else 0L
    }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, shape = RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Work,
                        contentDescription = "Work",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        workLog.workType.name,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(workLog.date.time)),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Text(
                "$durationInHours hrs",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun PremiumIncomeItem(income: com.rudra.smartworktracker.data.entity.Income) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, shape = RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFe8f5e8)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.TrendingUp,
                        contentDescription = "Income",
                        tint = Color(0xFF2e7d32),
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        "Income",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        income.category,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Text(
                "৳${income.amount}",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2e7d32)
            )
        }
    }
}

@Composable
fun PremiumExpenseItem(expense: com.rudra.smartworktracker.model.Expense) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, shape = RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFffebee)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Wallet,
                        contentDescription = "Expense",
                        tint = Color(0xFFc62828),
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        "Expense",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        expense.category.name,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Text(
                "৳${expense.amount}",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFc62828)
            )
        }
    }
}
