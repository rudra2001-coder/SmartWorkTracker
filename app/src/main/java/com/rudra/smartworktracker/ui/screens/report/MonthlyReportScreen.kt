package com.rudra.smartworktracker.ui.screens.report

import android.app.Application
import androidx.compose.foundation.background
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import co.yml.charts.common.model.PlotType
import co.yml.charts.ui.piechart.charts.PieChart
import co.yml.charts.ui.piechart.models.PieChartConfig
import co.yml.charts.ui.piechart.models.PieChartData
import com.rudra.smartworktracker.model.ExpenseByCategory
import com.rudra.smartworktracker.model.IncomeByCategory
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonthlyReportScreen(onNavigateBack: () -> Unit) {
    val application = LocalContext.current.applicationContext as Application
    val viewModel: MonthlyReportViewModel = viewModel(factory = MonthlyReportViewModelFactory(application))
    val uiState by viewModel.uiState.collectAsState()

    var expanded by remember { mutableStateOf(false) }
    var showStartPicker by remember { mutableStateOf(false) }
    var showEndPicker by remember { mutableStateOf(false) }

    val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Monthly Report",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            // ── Date Range Filter Card ──────────────────────────────────────────
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = if (uiState.useCustomRange) "Custom Date Range" else "Select Month",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Toggle: Month vs Custom
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Month",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (!uiState.useCustomRange)
                                MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Switch(
                            checked = uiState.useCustomRange,
                            onCheckedChange = { viewModel.onUseCustomRangeChanged(it) }
                        )
                        Text(
                            text = "Custom",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (uiState.useCustomRange)
                                MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (uiState.useCustomRange) {
                        // Custom date range pickers
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedDateField(
                                label = "Start Date",
                                date = uiState.customStartDate,
                                dateFormat = dateFormat,
                                onClick = { showStartPicker = true },
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedDateField(
                                label = "End Date",
                                date = uiState.customEndDate,
                                dateFormat = dateFormat,
                                onClick = { showEndPicker = true },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    } else {
                        // Month dropdown + Year navigator
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            ExposedDropdownMenuBox(
                                expanded = expanded,
                                onExpandedChange = { expanded = !expanded },
                                modifier = Modifier.weight(1f)
                            ) {
                                TextField(
                                    value = uiState.selectedMonth,
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Month") },
                                    trailingIcon = {
                                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                                    },
                                    colors = ExposedDropdownMenuDefaults.textFieldColors(),
                                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                                    shape = MaterialTheme.shapes.medium
                                )
                                ExposedDropdownMenu(
                                    expanded = expanded,
                                    onDismissRequest = { expanded = false }
                                ) {
                                    viewModel.months.forEach { month ->
                                        DropdownMenuItem(
                                            text = { Text(month) },
                                            onClick = {
                                                viewModel.onMonthSelected(month)
                                                expanded = false
                                            }
                                        )
                                    }
                                }
                            }

                            IconButton(onClick = { viewModel.onYearDecrement() }) {
                                Icon(
                                    Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Previous Year"
                                )
                            }

                            Text(
                                text = uiState.year.toString(),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.width(48.dp),
                                textAlign = TextAlign.Center
                            )

                            IconButton(onClick = { viewModel.onYearIncrement() }) {
                                Icon(
                                    Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = "Next Year"
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Compare toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Compare with previous period",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Switch(
                            checked = uiState.compareWithPrevious,
                            onCheckedChange = { viewModel.onCompareWithPreviousChanged(it) }
                        )
                    }
                }
            }

            if (uiState.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Loading...",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            } else if (uiState.workLogs.isEmpty() &&
                uiState.totalIncome == 0.0 &&
                uiState.totalExpense == 0.0
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = "No data",
                            tint = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(64.dp)
                        )
                        Text(
                            text = "No data available for this period",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.outline,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                // ── Overview Stat Cards ─────────────────────────────────────────
                StatCardsSection(
                    officeCount = uiState.officeCount,
                    homeCount = uiState.homeCount,
                    offCount = uiState.offCount,
                    extraCount = uiState.extraCount,
                    totalIncome = uiState.totalIncome,
                    totalExpense = uiState.totalExpense,
                    netAmount = uiState.netAmount
                )

                Spacer(modifier = Modifier.height(16.dp))

                // ── Work Distribution Pie Chart ─────────────────────────────────
                WorkDistributionChart(
                    officeCount = uiState.officeCount,
                    homeCount = uiState.homeCount,
                    offCount = uiState.offCount,
                    extraCount = uiState.extraCount,
                    totalDays = uiState.workLogs.size
                )

                Spacer(modifier = Modifier.height(16.dp))

                // ── Expense Category Pie Chart ──────────────────────────────────
                if (uiState.expenseByCategory.isNotEmpty()) {
                    ExpenseCategoryChart(
                        expenseByCategory = uiState.expenseByCategory,
                        totalExpense = uiState.totalExpense
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                }

                // ── Income Category Pie Chart ───────────────────────────────────
                if (uiState.incomeByCategory.isNotEmpty()) {
                    IncomeCategoryChart(
                        incomeByCategory = uiState.incomeByCategory,
                        totalIncome = uiState.totalIncome
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                }

                // ── Savings Summary ─────────────────────────────────────────────
                if (uiState.netSavings != 0.0) {
                    SavingsSummaryCard(
                        deposited = uiState.totalSavingsDeposited,
                        withdrawn = uiState.totalSavingsWithdrawn,
                        net = uiState.netSavings
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                }

                // ── Detailed Summary Card ───────────────────────────────────────
                DetailedSummaryCard(
                    officeCount = uiState.officeCount,
                    homeCount = uiState.homeCount,
                    offCount = uiState.offCount,
                    extraCount = uiState.extraCount,
                    overtimeCount = uiState.overtimeCount,
                    totalDays = uiState.workLogs.size,
                    totalIncome = uiState.totalIncome,
                    totalExpense = uiState.totalExpense,
                    netAmount = uiState.netAmount,
                    mealExpense = uiState.mealExpense,
                    savingsDeposited = uiState.totalSavingsDeposited,
                    savingsWithdrawn = uiState.totalSavingsWithdrawn
                )

                Spacer(modifier = Modifier.height(16.dp))

                // ── Comparison Card ─────────────────────────────────────────────
                if (uiState.compareWithPrevious && uiState.previousPeriod != null) {
                    ComparisonCard(
                        current = uiState,
                        previous = uiState.previousPeriod!!
                    )

                    Spacer(modifier = Modifier.height(24.dp))
                }

                // ── Full Data Report ────────────────────────────────────────────
                FullDataReportSection(fullReport = uiState.fullReport)

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    // Date picker dialogs for custom range
    if (showStartPicker) {
        val state = rememberDatePickerState(initialSelectedDateMillis = uiState.customStartDate)
        DatePickerDialog(
            onDismissRequest = { showStartPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let { viewModel.onCustomStartDateChanged(it) }
                    showStartPicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showStartPicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = state)
        }
    }

    if (showEndPicker) {
        val state = rememberDatePickerState(initialSelectedDateMillis = uiState.customEndDate)
        DatePickerDialog(
            onDismissRequest = { showEndPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let { viewModel.onCustomEndDateChanged(it) }
                    showEndPicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showEndPicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = state)
        }
    }
}

// ── Stat Cards Section ─────────────────────────────────────────────────────

@Composable
private fun StatCardsSection(
    officeCount: Int,
    homeCount: Int,
    offCount: Int,
    extraCount: Int,
    totalIncome: Double,
    totalExpense: Double,
    netAmount: Double
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Text(
            text = "Overview",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard(
                icon = Icons.Default.Work,
                title = "Work Days",
                value = "${officeCount + homeCount + extraCount}",
                color = Color(0xFF3498DB),
                modifier = Modifier.weight(1f)
            )
            StatCard(
                icon = Icons.Default.TrendingUp,
                title = "Income",
                value = formatCurrency(totalIncome),
                color = Color(0xFF2ECC71),
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard(
                icon = Icons.Default.TrendingDown,
                title = "Expense",
                value = formatCurrency(totalExpense),
                color = Color(0xFFFF6B6B),
                modifier = Modifier.weight(1f)
            )
            StatCard(
                icon = Icons.Default.AccountBalance,
                title = "Net",
                value = formatCurrency(netAmount),
                color = if (netAmount >= 0) Color(0xFF2ECC71) else Color(0xFFFF6B6B),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun StatCard(
    icon: ImageVector,
    title: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        color.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = color,
                    modifier = Modifier.size(22.dp)
                )
            }
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

// ── Work Distribution Pie Chart ────────────────────────────────────────────

@Composable
private fun WorkDistributionChart(
    officeCount: Int,
    homeCount: Int,
    offCount: Int,
    extraCount: Int,
    totalDays: Int
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Work Distribution",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(16.dp))

            val slices = listOf(
                PieChartData.Slice("Office", officeCount.toFloat(), Color(0xFF58BDFF)),
                PieChartData.Slice("Home", homeCount.toFloat(), Color(0xFF1266F1)),
                PieChartData.Slice("Off", offCount.toFloat(), Color(0xFF00B74A)),
                PieChartData.Slice("Extra", extraCount.toFloat(), Color(0xFFF93154))
            ).filter { it.value > 0 }

            if (slices.isNotEmpty()) {
                val pieData = PieChartData(slices = slices, plotType = PlotType.Pie)
                val config = PieChartConfig(
                    strokeWidth = 100f,
                    activeSliceAlpha = 0.9f,
                    isAnimationEnable = true,
                    labelVisible = true,
                    labelColor = MaterialTheme.colorScheme.onSurface,
                    labelFontSize = 13.sp,
                    showSliceLabels = true
                )

                PieChart(
                    modifier = Modifier.size(260.dp),
                    pieChartData = pieData,
                    pieChartConfig = config
                )

                Spacer(modifier = Modifier.height(12.dp))

                slices.forEach { slice ->
                    ChartLegendRow(
                        color = slice.color,
                        label = slice.label,
                        value = slice.value.toInt().toString(),
                        percentage = if (totalDays > 0)
                            "${(slice.value.toInt() * 100 / totalDays)}%"
                        else "0%"
                    )
                }
            } else {
                Text(
                    "No work data",
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}

@Composable
private fun ChartLegendRow(
    color: Color,
    label: String,
    value: String,
    percentage: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .background(color, shape = RoundedCornerShape(3.dp))
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = percentage,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(40.dp),
            textAlign = TextAlign.End
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.width(32.dp),
            textAlign = TextAlign.End
        )
    }
}

// ── Expense Category Pie Chart ─────────────────────────────────────────────

@Composable
private fun ExpenseCategoryChart(
    expenseByCategory: List<ExpenseByCategory>,
    totalExpense: Double
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Expense by Category",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Total: ${formatCurrency(totalExpense)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            val slices = expenseByCategory.filter { it.total > 0 }.map { cat ->
                PieChartData.Slice(
                    cat.category.displayName,
                    cat.total.toFloat(),
                    cat.category.color
                )
            }

            if (slices.isNotEmpty()) {
                val pieData = PieChartData(slices = slices, plotType = PlotType.Pie)
                val config = PieChartConfig(
                    strokeWidth = 100f,
                    activeSliceAlpha = 0.9f,
                    isAnimationEnable = true,
                    labelVisible = true,
                    labelColor = MaterialTheme.colorScheme.onSurface,
                    labelFontSize = 13.sp,
                    showSliceLabels = true
                )

                PieChart(
                    modifier = Modifier.size(260.dp),
                    pieChartData = pieData,
                    pieChartConfig = config
                )

                Spacer(modifier = Modifier.height(12.dp))

                slices.forEach { slice ->
                    ChartLegendRow(
                        color = slice.color,
                        label = slice.label,
                        value = formatCurrency(slice.value.toDouble()),
                        percentage = if (totalExpense > 0)
                            "${(slice.value.toInt() * 100 / totalExpense.toInt())}%"
                        else "0%"
                    )
                }
            }
        }
    }
}

// ── Income Category Pie Chart ──────────────────────────────────────────────

@Composable
private fun IncomeCategoryChart(
    incomeByCategory: List<IncomeByCategory>,
    totalIncome: Double
) {
    val categoryColors = listOf(
        Color(0xFF2ECC71), Color(0xFF3498DB), Color(0xFF9B59B6),
        Color(0xFFF39C12), Color(0xFF1ABC9C), Color(0xFFE74C3C),
        Color(0xFF34495E), Color(0xFF16A085), Color(0xFFD35400),
        Color(0xFF8E44AD)
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Income by Category",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Total: ${formatCurrency(totalIncome)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            val slices = incomeByCategory.filter { it.total > 0 }.mapIndexed { index, cat ->
                PieChartData.Slice(
                    cat.category,
                    cat.total.toFloat(),
                    categoryColors[index % categoryColors.size]
                )
            }

            if (slices.isNotEmpty()) {
                val pieData = PieChartData(slices = slices, plotType = PlotType.Pie)
                val config = PieChartConfig(
                    strokeWidth = 100f,
                    activeSliceAlpha = 0.9f,
                    isAnimationEnable = true,
                    labelVisible = true,
                    labelColor = MaterialTheme.colorScheme.onSurface,
                    labelFontSize = 13.sp,
                    showSliceLabels = true
                )

                PieChart(
                    modifier = Modifier.size(260.dp),
                    pieChartData = pieData,
                    pieChartConfig = config
                )

                Spacer(modifier = Modifier.height(12.dp))

                slices.forEach { slice ->
                    ChartLegendRow(
                        color = slice.color,
                        label = slice.label,
                        value = formatCurrency(slice.value.toDouble()),
                        percentage = if (totalIncome > 0)
                            "${(slice.value.toInt() * 100 / totalIncome.toInt())}%"
                        else "0%"
                    )
                }
            }
        }
    }
}

// ── Savings Summary Card ───────────────────────────────────────────────────

@Composable
private fun SavingsSummaryCard(
    deposited: Double,
    withdrawn: Double,
    net: Double
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Savings,
                    contentDescription = "Savings",
                    tint = Color(0xFFF39C12),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Savings Activity",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Deposited",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = formatCurrency(deposited),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2ECC71)
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Withdrawn",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = formatCurrency(withdrawn),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFF6B6B)
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Net",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = formatCurrency(net),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (net >= 0) Color(0xFF2ECC71) else Color(0xFFFF6B6B)
                    )
                }
            }
        }
    }
}

// ── Detailed Summary Card ──────────────────────────────────────────────────

@Composable
private fun DetailedSummaryCard(
    officeCount: Int,
    homeCount: Int,
    offCount: Int,
    extraCount: Int,
    overtimeCount: Int,
    totalDays: Int,
    totalIncome: Double,
    totalExpense: Double,
    netAmount: Double,
    mealExpense: Double,
    savingsDeposited: Double,
    savingsWithdrawn: Double
) {
    val totalWorkDays = officeCount + homeCount + extraCount

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "Detailed Summary",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Work Breakdown
            Text(
                text = "Work Breakdown",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            DetailRow("Office Days", officeCount.toString())
            DetailRow("Home Office Days", homeCount.toString())
            DetailRow("Off Days", offCount.toString())
            DetailRow("Extra Work Days", extraCount.toString())
            DetailRow("Overtime Days", overtimeCount.toString())

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
            Spacer(modifier = Modifier.height(8.dp))

            DetailRow("Total Tracked Days", totalDays.toString())
            DetailRow("Total Work Days", totalWorkDays.toString(), bold = true)

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
            Spacer(modifier = Modifier.height(16.dp))

            // Financial Summary
            Text(
                text = "Financial Summary",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            DetailRow("Total Income", formatCurrency(totalIncome), color = Color(0xFF2ECC71))
            DetailRow("Total Expense", formatCurrency(totalExpense), color = Color(0xFFFF6B6B))
            DetailRow("Meal Expense", formatCurrency(mealExpense))
            DetailRow("Net Amount", formatCurrency(netAmount),
                color = if (netAmount >= 0) Color(0xFF2ECC71) else Color(0xFFFF6B6B),
                bold = true)

            if (savingsDeposited > 0 || savingsWithdrawn > 0) {
                Spacer(modifier = Modifier.height(8.dp))
                DetailRow("Savings Deposited", formatCurrency(savingsDeposited), color = Color(0xFF2ECC71))
                DetailRow("Savings Withdrawn", formatCurrency(savingsWithdrawn), color = Color(0xFFFF6B6B))
            }
        }
    }
}

@Composable
private fun DetailRow(
    label: String,
    value: String,
    color: Color = MaterialTheme.colorScheme.onSurface,
    bold: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal,
            color = color
        )
    }
}

// ── Comparison Card ────────────────────────────────────────────────────────

@Composable
private fun ComparisonCard(
    current: MonthlyReportUiState,
    previous: PeriodComparison
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "Period Comparison",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Current vs ${previous.label}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Headers
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Metric",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1.2f)
                )
                Text(
                    text = "Current",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2ECC71),
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "Previous",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFF6B6B),
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "Change",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(0.8f),
                    textAlign = TextAlign.End
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
            Spacer(modifier = Modifier.height(8.dp))

            ComparisonRow("Work Days",
                "${current.officeCount + current.homeCount + current.extraCount}",
                "${previous.officeCount + previous.homeCount + previous.extraCount}")
            ComparisonRow("Off Days",
                "${current.offCount}",
                "${previous.offCount}")
            ComparisonRow("Income",
                formatCurrency(current.totalIncome),
                formatCurrency(previous.totalIncome))
            ComparisonRow("Expense",
                formatCurrency(current.totalExpense),
                formatCurrency(previous.totalExpense))
            ComparisonRow("Net",
                formatCurrency(current.netAmount),
                formatCurrency(previous.netAmount),
                isNet = true)
        }
    }
}

@Composable
private fun ComparisonRow(
    label: String,
    currentValue: String,
    previousValue: String,
    isNet: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1.2f)
        )
        Text(
            text = currentValue,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold,
            color = if (isNet && currentValue.startsWith("-")) Color(0xFFFF6B6B)
                   else if (isNet) Color(0xFF2ECC71)
                   else MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center
        )
        Text(
            text = previousValue,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold,
            color = if (isNet && previousValue.startsWith("-")) Color(0xFFFF6B6B)
                   else if (isNet) Color(0xFF2ECC71)
                   else MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center
        )
        Text(
            text = computeChange(currentValue, previousValue),
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            color = computeChangeColor(currentValue, previousValue, isNet),
            modifier = Modifier.weight(0.8f),
            textAlign = TextAlign.End
        )
    }
}

private fun computeChange(current: String, previous: String): String {
    val cur = current.replace(Regex("[^0-9.-]"), "").toDoubleOrNull() ?: return "-"
    val prev = previous.replace(Regex("[^0-9.-]"), "").toDoubleOrNull() ?: return "-"
    if (prev == 0.0) return if (cur > 0) "+∞" else "-"
    val pct = ((cur - prev) / prev * 100)
    return "${if (pct >= 0) "+" else ""}${"%.1f".format(pct)}%"
}

private fun computeChangeColor(current: String, previous: String, isNet: Boolean): Color {
    val cur = current.replace(Regex("[^0-9.-]"), "").toDoubleOrNull() ?: return Color.Gray
    val prev = previous.replace(Regex("[^0-9.-]"), "").toDoubleOrNull() ?: return Color.Gray
    if (prev == 0.0) return if (cur > 0) Color(0xFF2ECC71) else Color(0xFFFF6B6B)
    val change = cur - prev
    if (isNet) {
        return if (change >= 0) Color(0xFF2ECC71) else Color(0xFFFF6B6B)
    }
    return if (change >= 0) Color(0xFF2ECC71) else Color(0xFFFF6B6B)
}

// ── Date Field Component ───────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun OutlinedDateField(
    label: String,
    date: Long?,
    dateFormat: SimpleDateFormat,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    TextField(
        value = date?.let { dateFormat.format(Date(it)) } ?: "",
        onValueChange = {},
        readOnly = true,
        label = { Text(label) },
        trailingIcon = {
            Icon(
                Icons.Default.CalendarMonth,
                contentDescription = "Pick date"
            )
        },
        modifier = modifier.clickable(onClick = onClick),
        shape = MaterialTheme.shapes.medium
    )
}

// ── Full Data Report Section ───────────────────────────────────────────────

@Composable
private fun FullDataReportSection(fullReport: FullReportData) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.ListAlt,
                    contentDescription = "Full Report",
                    tint = Color(0xFF6C5CE7),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Full Data Report",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
            Spacer(modifier = Modifier.height(16.dp))

            // Work Section
            ReportSubsection(
                title = "Work",
                icon = Icons.Default.Work,
                color = Color(0xFF3498DB),
                metrics = listOf(
                    ReportMetric("Work Sessions", fullReport.workSessionCount.toString(), Color(0xFF3498DB)),
                    ReportMetric("Work Session Hours", "%.1f".format(fullReport.workSessionHours) + "h", Color(0xFF3498DB)),
                    ReportMetric("Work Days (Log)", fullReport.workDayCount.toString(), Color(0xFF5DADE2))
                )
            )

            // Productivity Section
            ReportSubsection(
                title = "Productivity",
                icon = Icons.Default.FitnessCenter,
                color = Color(0xFF2ECC71),
                metrics = listOf(
                    ReportMetric("Habits", fullReport.habitCount.toString(), Color(0xFF2ECC71)),
                    ReportMetric("Focus Sessions", fullReport.focusSessionCount.toString(), Color(0xFF27AE60)),
                    ReportMetric("Focus Minutes", fullReport.focusSessionMinutes.toString(), Color(0xFF27AE60)),
                    ReportMetric("Health Metrics", fullReport.healthMetricCount.toString(), Color(0xFF2ECC71)),
                    ReportMetric("Achievements", "${fullReport.achievementsUnlocked}/${fullReport.achievementCount}", Color(0xFFF1C40F))
                )
            )

            // Financial Section (Loans, Cards, EMIs)
            ReportSubsection(
                title = "Loans & Credit",
                icon = Icons.Default.CreditCard,
                color = Color(0xFFE74C3C),
                metrics = listOf(
                    ReportMetric("Active Loans", fullReport.activeLoanCount.toString(), Color(0xFFE74C3C)),
                    ReportMetric("Borrowed (Remaining)", formatCurrency(fullReport.totalBorrowedRemaining), Color(0xFFE74C3C)),
                    ReportMetric("Lent (Remaining)", formatCurrency(fullReport.totalLentRemaining), Color(0xFF2ECC71)),
                    ReportMetric("Credit Cards", fullReport.creditCardCount.toString(), Color(0xFF9B59B6)),
                    ReportMetric("Card Debt", formatCurrency(fullReport.totalCreditCardDebt), Color(0xFFE74C3C)),
                    ReportMetric("Card Limits", formatCurrency(fullReport.totalCreditCardLimit), Color(0xFF2ECC71))
                )
            )

            // EMIs Section
            ReportSubsection(
                title = "EMIs",
                icon = Icons.Default.Home,
                color = Color(0xFFE67E22),
                metrics = listOf(
                    ReportMetric("Active EMIs", fullReport.activeEmiCount.toString(), Color(0xFFE67E22)),
                    ReportMetric("Pending EMIs", fullReport.pendingEmiCount.toString(), Color(0xFFE74C3C)),
                    ReportMetric("Overdue EMIs", fullReport.overdueEmiCount.toString(), Color(0xFFE74C3C)),
                    ReportMetric("Pending Amount", formatCurrency(fullReport.totalPendingEmiAmount), Color(0xFFE67E22))
                )
            )

            // Journal & Check-in Section
            ReportSubsection(
                title = "Journal & Check-in",
                icon = Icons.Default.SelfImprovement,
                color = Color(0xFF9B59B6),
                metrics = listOf(
                    ReportMetric("Daily Journals", fullReport.journalCount.toString(), Color(0xFF9B59B6)),
                    ReportMetric("Check-ins", fullReport.checkInCount.toString(), Color(0xFF8E44AD)),
                    ReportMetric("Decisions", fullReport.decisionCount.toString(), Color(0xFF9B59B6)),
                    ReportMetric("Positive", fullReport.positiveDecisions.toString(), Color(0xFF2ECC71)),
                    ReportMetric("Negative", fullReport.negativeDecisions.toString(), Color(0xFFE74C3C))
                )
            )

            // Reality & Debt Section
            ReportSubsection(
                title = "Reality & Debt",
                icon = Icons.Default.CheckCircle,
                color = Color(0xFF1ABC9C),
                metrics = listOf(
                    ReportMetric("Reality Entries", fullReport.realityPlanned.toString(), Color(0xFF1ABC9C)),
                    ReportMetric("Completed", fullReport.realityCompleted.toString(), Color(0xFF2ECC71)),
                    ReportMetric("Consequence Debt", "%.0f".format(fullReport.totalDebtAmount), Color(0xFFE74C3C)),
                    ReportMetric("Weekly Reports", fullReport.weeklyReportCount.toString(), Color(0xFF1ABC9C)),
                    ReportMetric("Monthly Inputs", fullReport.monthlyInputCount.toString(), Color(0xFF1ABC9C))
                )
            )

            // Recurring Section
            ReportSubsection(
                title = "Recurring",
                icon = Icons.Default.Repeat,
                color = Color(0xFF2980B9),
                metrics = listOf(
                    ReportMetric("Active Rules", fullReport.activeRecurringRules.toString(), Color(0xFF2980B9)),
                    ReportMetric("Transactions", fullReport.recurringTxCount.toString(), Color(0xFF2980B9)),
                    ReportMetric("Pending", fullReport.pendingRecurringTxCount.toString(), Color(0xFFE74C3C))
                )
            )

            // Financial Transactions Section
            ReportSubsection(
                title = "Financial Transactions",
                icon = Icons.Default.TrendingUp,
                color = Color(0xFF2ECC71),
                metrics = listOf(
                    ReportMetric("Total Transactions", fullReport.financialTxCount.toString(), Color(0xFF2ECC71)),
                    ReportMetric("Income", formatCurrency(fullReport.financialTxIncome), Color(0xFF2ECC71)),
                    ReportMetric("Expense", formatCurrency(fullReport.financialTxExpense), Color(0xFFE74C3C))
                )
            )

            // Meal Section
            ReportSubsection(
                title = "Meals",
                icon = Icons.Default.Favorite,
                color = Color(0xFFE74C3C),
                metrics = listOf(
                    ReportMetric("Total Meals", fullReport.mealCount.toString(), Color(0xFFE74C3C)),
                    ReportMetric("Meal Cost", formatCurrency(fullReport.mealTotalCost), Color(0xFFE74C3C)),
                    ReportMetric("Special Dates", fullReport.specialMealDateCount.toString(), Color(0xFF9B59B6))
                )
            )

            // Other Section
            ReportSubsection(
                title = "Other",
                icon = Icons.Default.Groups,
                color = Color(0xFF95A5A6),
                metrics = listOf(
                    ReportMetric("Colleagues", fullReport.colleagueCount.toString(), Color(0xFF95A5A6)),
                    ReportMetric("Schedules", fullReport.scheduleCount.toString(), Color(0xFF95A5A6)),
                    ReportMetric("Travel Expense", formatCurrency(fullReport.travelExpenseAmount), Color(0xFF95A5A6))
                )
            )
        }
    }
}

private data class ReportMetric(
    val label: String,
    val value: String,
    val color: Color = Color.Unspecified
)

@Composable
private fun ReportSubsection(
    title: String,
    icon: ImageVector,
    color: Color,
    metrics: List<ReportMetric>
) {
    var expanded by remember { mutableStateOf(false) }

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded }
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = color,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = if (expanded) "▲" else "▼",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        AnimatedVisibility(visible = expanded) {
            Column {
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                Spacer(modifier = Modifier.height(4.dp))
                metrics.forEach { metric ->
                    ReportRow(metric.label, metric.value, metric.color, if (metric.label == "Meal Cost" || metric.label.startsWith("Card") || metric.label.startsWith("Pending Amount") || metric.label.startsWith("Borrowed") || metric.label.startsWith("Lent")) metric.color else Color.Unspecified)
                }
                Spacer(modifier = Modifier.height(4.dp))
            }
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.08f))
    }
}

@Composable
private fun ReportRow(label: String, value: String, valueColor: Color = Color.Unspecified, overrideColor: Color? = null) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp, horizontal = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            color = overrideColor ?: valueColor
        )
    }
}

// ── Utility ────────────────────────────────────────────────────────────────

private fun formatCurrency(amount: Double): String {
    return if (amount >= 0) {
        "\u09F3${"%,.0f".format(amount)}"
    } else {
        "-\u09F3${"%,.0f".format(-amount)}"
    }
}
