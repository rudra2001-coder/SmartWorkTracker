package com.rudra.smartworktracker.ui.screens.calculation

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import co.yml.charts.common.model.PlotType
import co.yml.charts.ui.piechart.charts.PieChart
import co.yml.charts.ui.piechart.models.PieChartConfig
import co.yml.charts.ui.piechart.models.PieChartData
import com.rudra.smartworktracker.ui.theme.SmartWorkTrackerTheme
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

private val CardShape = RoundedCornerShape(20.dp)
private val ChipShape = RoundedCornerShape(12.dp)
private val PillShape = RoundedCornerShape(50.dp)

private val EmeraldGreen = Color(0xFF00C896)
private val CoralRed = Color(0xFFFF5757)
private val SapphireBlue = Color(0xFF3B82F6)
private val GoldenAmber = Color(0xFFF59E0B)
private val VioletPurple = Color(0xFF8B5CF6)
private val CyanLight = Color(0xFF06B6D4)

private val dayNames = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalculationScreen(onNavigateBack: () -> Unit) {
    val context = LocalContext.current
    val viewModel: CalculationViewModel = viewModel(factory = CalculationViewModelFactory(context))
    val calculation by viewModel.calculation.collectAsState()
    val travelExpense by viewModel.travelExpense.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val mealCostPerWeek by viewModel.mealCostPerWeek.collectAsState()
    val mealCostPerMonth by viewModel.mealCostPerMonth.collectAsState()
    val mealCostPerYear by viewModel.mealCostPerYear.collectAsState()
    val travelCostPerWeek by viewModel.travelCostPerWeek.collectAsState()
    val travelCostPerMonth by viewModel.travelCostPerMonth.collectAsState()
    val travelCostPerYear by viewModel.travelCostPerYear.collectAsState()
    val otherExpensePerMonth by viewModel.otherExpensePerMonth.collectAsState()
    val otherExpensePerYear by viewModel.otherExpensePerYear.collectAsState()
    val totalExpensePerMonth by viewModel.totalExpensePerMonth.collectAsState()
    val totalExpensePerYear by viewModel.totalExpensePerYear.collectAsState()
    val officeDays by viewModel.officeDays.collectAsState()
    val homeOfficeDays by viewModel.homeOfficeDays.collectAsState()
    val pieChartData by viewModel.pieChartData.collectAsState()
    val monthlyBreakdown by viewModel.monthlyBreakdown.collectAsState()
    val calcMode by viewModel.calcMode.collectAsState()
    val mealRateEntries by viewModel.mealRateEntries.collectAsState()
    val totalMealRate by viewModel.totalMealRate.collectAsState()
    val startDate by viewModel.startDate.collectAsState()
    val endDate by viewModel.endDate.collectAsState()
    val selectedWeekDays by viewModel.selectedWeekDays.collectAsState()
    val customMonthlyCost by viewModel.customMonthlyCost.collectAsState()
    val customSixMonthCost by viewModel.customSixMonthCost.collectAsState()
    val customYearlyCost by viewModel.customYearlyCost.collectAsState()
    val customDaysCount by viewModel.customDaysCount.collectAsState()

    val focusManager = LocalFocusManager.current
    val monthYearFormat = remember { SimpleDateFormat("MMMM yyyy", Locale.getDefault()) }
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }

    var dailyMealRate by remember { mutableStateOf("") }
    var dailyTravelCost by remember { mutableStateOf("") }
    var otherExpenses by remember { mutableStateOf("") }
    var otherExpenseDescription by remember { mutableStateOf("") }
    var showAddRateDialog by remember { mutableStateOf(false) }
    var newRateLabel by remember { mutableStateOf("") }
    var newRateAmount by remember { mutableStateOf("") }
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }

    LaunchedEffect(calculation, travelExpense) {
        calculation?.let {
            val rate = it.dailyMealRate
            if (dailyMealRate.isEmpty() || (rate >= 0 && dailyMealRate.toDoubleOrNull() != rate)) {
                dailyMealRate = if (rate % 1.0 == 0.0) rate.toInt().toString() else rate.toString()
            }
        }
        travelExpense?.let {
            if (dailyTravelCost.isEmpty() || (it.dailyTravelCost >= 0 && dailyTravelCost.toDoubleOrNull() != it.dailyTravelCost)) {
                dailyTravelCost = if (it.dailyTravelCost % 1.0 == 0.0) it.dailyTravelCost.toInt().toString() else it.dailyTravelCost.toString()
            }
            if (otherExpenses.isEmpty() || (it.otherExpenses >= 0 && otherExpenses.toDoubleOrNull() != it.otherExpenses)) {
                otherExpenses = if (it.otherExpenses % 1.0 == 0.0) it.otherExpenses.toInt().toString() else it.otherExpenses.toString()
            }
            if (otherExpenseDescription.isEmpty() || (it.otherExpenseDescription.isNotEmpty() && otherExpenseDescription != it.otherExpenseDescription)) {
                otherExpenseDescription = it.otherExpenseDescription
            }
        }
    }

    errorMessage?.let { message ->
        LaunchedEffect(message) { Toast.makeText(context, message, Toast.LENGTH_LONG).show(); delay(3000); viewModel.clearErrorMessage() }
    }

    SmartWorkTrackerTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Expense Calculator", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface) },
                    navigationIcon = { IconButton(onClick = onNavigateBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = MaterialTheme.colorScheme.onSurface) } },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
                    actions = { if (isLoading) CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp) }
                )
            },
            floatingActionButton = {
                FloatingActionButton(onClick = { viewModel.exportToExcel(context) }, containerColor = VioletPurple, contentColor = Color.White) {
                    Icon(Icons.Default.Download, "Export")
                }
            },
            modifier = Modifier.background(MaterialTheme.colorScheme.background)
        ) { paddingValues ->
            if (isLoading && calculation == null) {
                Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            } else {
                LazyColumn(
                    modifier = Modifier.padding(paddingValues).fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Mode Selector
                    item {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Box(modifier = Modifier.size(36.dp).background(brush = Brush.linearGradient(listOf(VioletPurple, CyanLight)), shape = RoundedCornerShape(10.dp)), contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Calculate, null, tint = Color.White, modifier = Modifier.size(20.dp))
                            }
                            Text("Calculation Mode", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        }
                        Spacer(Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                            listOf(CalcMode.AUTO to "Auto", CalcMode.DATE_RANGE to "Date Range", CalcMode.WEEKLY to "Weekly").forEach { (mode, label) ->
                                FilterChip(
                                    selected = calcMode == mode,
                                    onClick = { viewModel.setCalcMode(mode) },
                                    label = { Text(label, fontWeight = if (calcMode == mode) FontWeight.Bold else FontWeight.Normal) },
                                    modifier = Modifier.weight(1f),
                                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = VioletPurple.copy(alpha = 0.15f), selectedLabelColor = VioletPurple)
                                )
                            }
                        }
                    }

                    // Mode-specific content
                    when (calcMode) {
                        CalcMode.AUTO -> {
                            item { MonthNavigator(month = monthYearFormat.format(selectedDate), onPrevious = { viewModel.goToPreviousMonth() }, onNext = { viewModel.goToNextMonth() }, isLoading = isLoading) }
                            item { SummaryHeaderCard(officeDays = officeDays, homeOfficeDays = homeOfficeDays, totalCost = totalExpensePerMonth, isLoading = isLoading) }
                            if (monthlyBreakdown.isNotEmpty()) {
                                item { MonthlyBreakdownChart(data = monthlyBreakdown, year = viewModel.getCurrentYear()) }
                            }
                            item {
                                ExpenseInputSection(
                                    dailyMealRate = dailyMealRate, dailyTravelCost = dailyTravelCost,
                                    otherExpenses = otherExpenses, otherExpenseDescription = otherExpenseDescription,
                                    onDailyMealRateChange = { dailyMealRate = it },
                                    onDailyTravelCostChange = { dailyTravelCost = it },
                                    onOtherExpensesChange = { otherExpenses = it },
                                    onOtherExpenseDescriptionChange = { otherExpenseDescription = it },
                                    onSaveMealRate = { rate -> 
                                        viewModel.saveAllSettings(
                                            rate.toDoubleOrNull() ?: 0.0,
                                            dailyTravelCost.toDoubleOrNull() ?: 0.0,
                                            otherExpenses.toDoubleOrNull() ?: 0.0,
                                            otherExpenseDescription
                                        )
                                    },
                                    onSaveTravelExpense = { travel, other, desc -> 
                                        viewModel.saveAllSettings(
                                            dailyMealRate.toDoubleOrNull() ?: 0.0,
                                            travel.toDoubleOrNull() ?: 0.0,
                                            other.toDoubleOrNull() ?: 0.0,
                                            desc
                                        )
                                    },
                                    focusManager = focusManager
                                )
                            }
                            item { MealRateManager(entries = mealRateEntries, totalRate = totalMealRate, onAdd = { showAddRateDialog = true }, onRemove = { viewModel.removeMealRateEntry(it) }) }
                            item { CustomSummaryCard(days = customDaysCount, monthly = customMonthlyCost, sixMonth = customSixMonthCost, yearly = customYearlyCost, totalRate = totalMealRate) }
                            val totalDays = officeDays + homeOfficeDays
                            if (totalDays > 0) {
                                item { WorkingDaysPieChart(data = pieChartData, isLoading = isLoading) }
                            } else {
                                item { NoDataPlaceholder(title = "No Work Data", message = "Log your work days to see calculations", icon = Icons.Default.WorkHistory) }
                            }
                            item { ExpandableInfoSection(title = "Meal Costs", icon = Icons.Default.Restaurant, color = EmeraldGreen, items = listOf("Weekly" to String.format("%.2f Taka", mealCostPerWeek), "Monthly" to String.format("%.2f Taka", mealCostPerMonth), "Yearly" to String.format("%.2f Taka", mealCostPerYear))) }
                            item { ExpandableInfoSection(title = "Travel & Other Expenses", icon = Icons.Default.DirectionsCar, color = GoldenAmber, items = listOf("Weekly Travel" to String.format("%.2f Taka", travelCostPerWeek), "Monthly Travel" to String.format("%.2f Taka", travelCostPerMonth), "Yearly Travel" to String.format("%.2f Taka", travelCostPerYear), "Monthly Other" to String.format("%.2f Taka", otherExpensePerMonth), "Yearly Other" to String.format("%.2f Taka", otherExpensePerYear))) }
                            item { ExpandableInfoSection(title = "Total Summary", icon = Icons.Default.Calculate, color = CoralRed, items = listOf("Total Monthly" to String.format("%.2f Taka", totalExpensePerMonth), "Total Yearly" to String.format("%.2f Taka", totalExpensePerYear))) }
                        }

                        CalcMode.DATE_RANGE -> {
                            item { DateRangeSection(startDate = startDate, endDate = endDate, dateFormat = dateFormat, onStartClick = { showStartDatePicker = true }, onEndClick = { showEndDatePicker = true }) }
                            item { MealRateManager(entries = mealRateEntries, totalRate = totalMealRate, onAdd = { showAddRateDialog = true }, onRemove = { viewModel.removeMealRateEntry(it) }) }
                            item { CustomSummaryCard(days = customDaysCount, monthly = customMonthlyCost, sixMonth = customSixMonthCost, yearly = customYearlyCost, totalRate = totalMealRate) }
                        }

                        CalcMode.WEEKLY -> {
                            item { WeekDaySelector(selectedDays = selectedWeekDays, onToggle = { viewModel.toggleWeekDay(it) }) }
                            item { MealRateManager(entries = mealRateEntries, totalRate = totalMealRate, onAdd = { showAddRateDialog = true }, onRemove = { viewModel.removeMealRateEntry(it) }) }
                            item { CustomSummaryCard(days = customDaysCount, monthly = customMonthlyCost, sixMonth = customSixMonthCost, yearly = customYearlyCost, totalRate = totalMealRate) }
                        }
                    }

                    item { Spacer(Modifier.height(80.dp)) }
                }
            }
        }

        if (showAddRateDialog) {
            AlertDialog(
                onDismissRequest = { showAddRateDialog = false; newRateLabel = ""; newRateAmount = "" },
                shape = CardShape,
                title = { Text("Add Meal Rate", fontWeight = FontWeight.Bold) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(value = newRateLabel, onValueChange = { newRateLabel = it }, label = { Text("Label (e.g. Breakfast)") }, modifier = Modifier.fillMaxWidth(), shape = ChipShape, singleLine = true)
                        OutlinedTextField(value = newRateAmount, onValueChange = { newRateAmount = it }, label = { Text("Amount (Taka)") }, leadingIcon = { Text("Tk", modifier = Modifier.padding(start = 8.dp), fontWeight = FontWeight.Bold) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth(), shape = ChipShape, singleLine = true)
                    }
                },
                confirmButton = {
                    Button(onClick = {
                        val rate = newRateAmount.toDoubleOrNull()
                        if (newRateLabel.isNotBlank() && rate != null && rate > 0) {
                            viewModel.addMealRateEntry(newRateLabel, rate)
                            showAddRateDialog = false; newRateLabel = ""; newRateAmount = ""
                        }
                    }, enabled = newRateLabel.isNotBlank() && (newRateAmount.toDoubleOrNull() ?: 0.0) > 0) { Text("Add") }
                },
                dismissButton = { TextButton(onClick = { showAddRateDialog = false; newRateLabel = ""; newRateAmount = "" }) { Text("Cancel") } }
            )
        }

        if (showStartDatePicker) {
            val datePickerState = rememberDatePickerState(initialSelectedDateMillis = startDate.time)
            DatePickerDialog(
                onDismissRequest = { showStartDatePicker = false },
                confirmButton = { TextButton(onClick = { datePickerState.selectedDateMillis?.let { viewModel.setStartDate(Date(it)) }; showStartDatePicker = false }) { Text("OK") } },
                dismissButton = { TextButton(onClick = { showStartDatePicker = false }) { Text("Cancel") } }
            ) { DatePicker(state = datePickerState) }
        }

        if (showEndDatePicker) {
            val datePickerState = rememberDatePickerState(initialSelectedDateMillis = endDate.time)
            DatePickerDialog(
                onDismissRequest = { showEndDatePicker = false },
                confirmButton = { TextButton(onClick = { datePickerState.selectedDateMillis?.let { viewModel.setEndDate(Date(it)) }; showEndDatePicker = false }) { Text("OK") } },
                dismissButton = { TextButton(onClick = { showEndDatePicker = false }) { Text("Cancel") } }
            ) { DatePicker(state = datePickerState) }
        }
    }
}

@Composable
fun DateRangeSection(startDate: Date, endDate: Date, dateFormat: SimpleDateFormat, onStartClick: () -> Unit, onEndClick: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().shadow(6.dp, CardShape, clip = false), shape = CardShape, elevation = CardDefaults.cardElevation(0.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(modifier = Modifier.size(32.dp).background(brush = Brush.linearGradient(listOf(SapphireBlue, CyanLight)), shape = RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.DateRange, null, tint = Color.White, modifier = Modifier.size(16.dp))
                }
                Text("Select Date Range", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            }
            Spacer(Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(onClick = onStartClick, modifier = Modifier.weight(1f).height(52.dp), shape = ChipShape) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Start", style = MaterialTheme.typography.labelSmall)
                        Text(dateFormat.format(startDate), fontWeight = FontWeight.Bold, fontSize = MaterialTheme.typography.labelMedium.fontSize)
                    }
                }
                Icon(Icons.Default.ArrowForward, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp).align(Alignment.CenterVertically))
                OutlinedButton(onClick = onEndClick, modifier = Modifier.weight(1f).height(52.dp), shape = ChipShape) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("End", style = MaterialTheme.typography.labelSmall)
                        Text(dateFormat.format(endDate), fontWeight = FontWeight.Bold, fontSize = MaterialTheme.typography.labelMedium.fontSize)
                    }
                }
            }
        }
    }
}

@Composable
fun WeekDaySelector(selectedDays: Set<Int>, onToggle: (Int) -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().shadow(6.dp, CardShape, clip = false), shape = CardShape, elevation = CardDefaults.cardElevation(0.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(modifier = Modifier.size(32.dp).background(brush = Brush.linearGradient(listOf(GoldenAmber, CoralRed)), shape = RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.DateRange, null, tint = Color.White, modifier = Modifier.size(16.dp))
                }
                Text("Select Week Days", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            }
            Spacer(Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                (0..6).forEach { day ->
                    val isSelected = selectedDays.contains(day)
                    Box(
                        modifier = Modifier.weight(1f).aspectRatio(1f).clip(ChipShape)
                            .then(if (isSelected) Modifier.background(Brush.linearGradient(listOf(GoldenAmber, CoralRed))) else Modifier.background(MaterialTheme.colorScheme.surfaceVariant))
                            .clickable { onToggle(day) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            dayNames[day].take(1),
                            fontWeight = FontWeight.ExtraBold,
                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.titleSmall
                        )
                    }
                }
            }
            if (selectedDays.isEmpty()) {
                Text("Select at least one day", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 8.dp))
            }
        }
    }
}

@Composable
fun MealRateManager(entries: List<MealRateEntry>, totalRate: Double, onAdd: () -> Unit, onRemove: (Long) -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().shadow(6.dp, CardShape, clip = false), shape = CardShape, elevation = CardDefaults.cardElevation(0.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Box(modifier = Modifier.size(32.dp).background(brush = Brush.linearGradient(listOf(EmeraldGreen, SapphireBlue)), shape = RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Restaurant, null, tint = Color.White, modifier = Modifier.size(16.dp))
                    }
                    Text("Meal Rates", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                }
                FilledTonalIconButton(onClick = onAdd, colors = IconButtonDefaults.filledTonalIconButtonColors(containerColor = EmeraldGreen.copy(alpha = 0.15f), contentColor = EmeraldGreen), modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Default.Add, "Add", modifier = Modifier.size(18.dp))
                }
            }
            Spacer(Modifier.height(12.dp))

            if (entries.isEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Restaurant, null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f), modifier = Modifier.size(32.dp))
                        Spacer(Modifier.height(4.dp))
                        Text("No meal rates added", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("Tap + to add", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            } else {
                entries.forEach { entry ->
                    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Box(modifier = Modifier.size(36.dp).background(EmeraldGreen.copy(alpha = 0.12f), RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Restaurant, null, tint = EmeraldGreen, modifier = Modifier.size(18.dp))
                            }
                            Column {
                                Text(entry.label, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
                                Text("Tk %.2f".format(entry.rate), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        IconButton(onClick = { onRemove(entry.id) }, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Default.Close, "Remove", tint = CoralRed.copy(alpha = 0.6f), modifier = Modifier.size(18.dp))
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
                HorizontalDivider()
                Spacer(Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Total Rate per Day", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    Text("Tk %.2f".format(totalRate), fontWeight = FontWeight.ExtraBold, color = EmeraldGreen)
                }
            }
        }
    }
}

@Composable
fun CustomSummaryCard(days: Int, monthly: Double, sixMonth: Double, yearly: Double, totalRate: Double) {
    Card(modifier = Modifier.fillMaxWidth().shadow(6.dp, CardShape, clip = false), shape = CardShape, elevation = CardDefaults.cardElevation(0.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(modifier = Modifier.size(36.dp).background(brush = Brush.linearGradient(listOf(VioletPurple, CoralRed)), shape = RoundedCornerShape(10.dp)), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Calculate, null, tint = Color.White, modifier = Modifier.size(20.dp))
                }
                Text("Calculation Results", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            }
            Spacer(Modifier.height(16.dp))

            Card(modifier = Modifier.fillMaxWidth(), shape = ChipShape, colors = CardDefaults.cardColors(containerColor = VioletPurple.copy(alpha = 0.08f))) {
                Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Days Calculated", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(4.dp))
                    Text("$days days", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold, color = VioletPurple)
                }
            }

            Spacer(Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                SummaryStatCard("Monthly", monthly, EmeraldGreen, Modifier.weight(1f))
                SummaryStatCard("6 Months", sixMonth, SapphireBlue, Modifier.weight(1f))
                SummaryStatCard("Yearly", yearly, GoldenAmber, Modifier.weight(1f))
            }

            if (totalRate > 0 && days > 0) {
                Spacer(Modifier.height(12.dp))
                Card(modifier = Modifier.fillMaxWidth(), shape = ChipShape, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                    Row(modifier = Modifier.padding(12.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Rate per day", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("Tk %.2f".format(totalRate), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    }
                }
            }
        }
    }
}

@Composable
fun SummaryStatCard(label: String, amount: Double, color: Color, modifier: Modifier = Modifier) {
    Card(modifier = modifier, shape = ChipShape, colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.08f))) {
        Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(4.dp))
            Text("Tk %.0f".format(amount), fontWeight = FontWeight.ExtraBold, color = color, style = MaterialTheme.typography.titleMedium)
        }
    }
}

@Composable
fun MonthlyBreakdownChart(data: List<Pair<String, Double>>, year: Int) {
    val maxValue = data.maxOfOrNull { it.second } ?: 0.0
    Card(modifier = Modifier.fillMaxWidth().height(280.dp).shadow(6.dp, CardShape, clip = false), shape = CardShape, elevation = CardDefaults.cardElevation(0.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(modifier = Modifier.size(28.dp).background(VioletPurple.copy(alpha = 0.15f), RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.BarChart, null, tint = VioletPurple, modifier = Modifier.size(16.dp))
                    }
                    Text("Monthly Breakdown $year", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                }
            }
            Spacer(Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth().height(180.dp), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.Bottom) {
                data.forEach { (month, value) ->
                    val heightPercent = if (maxValue > 0) value / maxValue else 0.0
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Bottom) {
                        Box(modifier = Modifier.width(20.dp).height((heightPercent * 120).dp)
                            .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                            .background(if (heightPercent > 0.7) CoralRed else if (heightPercent > 0.4) GoldenAmber else EmeraldGreen))
                        Spacer(Modifier.height(4.dp))
                        Text(month, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("%.0f".format(value), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    }
                }
            }
        }
    }
}

@Composable
fun ExpandableInfoSection(title: String, icon: ImageVector, color: Color, items: List<Pair<String, String>>) {
    var expanded by remember { mutableStateOf(true) }
    Card(modifier = Modifier.fillMaxWidth().shadow(6.dp, CardShape, clip = false), shape = CardShape, elevation = CardDefaults.cardElevation(0.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            Row(modifier = Modifier.fillMaxWidth().clickable { expanded = !expanded }.padding(vertical = 16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(modifier = Modifier.size(28.dp).background(color.copy(alpha = 0.15f), RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) {
                        Icon(icon, title, tint = color, modifier = Modifier.size(16.dp))
                    }
                    Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                }
                Icon(if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore, if (expanded) "Collapse" else "Expand", tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            AnimatedVisibility(visible = expanded, enter = fadeIn() + expandVertically(), exit = fadeOut() + shrinkVertically()) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(bottom = 16.dp)) {
                    items.forEach { (label, value) ->
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ExpenseInputSection(
    dailyMealRate: String, dailyTravelCost: String, otherExpenses: String, otherExpenseDescription: String,
    onDailyMealRateChange: (String) -> Unit, onDailyTravelCostChange: (String) -> Unit,
    onOtherExpensesChange: (String) -> Unit, onOtherExpenseDescriptionChange: (String) -> Unit,
    onSaveMealRate: (String) -> Unit, onSaveTravelExpense: (String, String, String) -> Unit, focusManager: FocusManager
) {
    var showOtherExpenseDetails by remember { mutableStateOf(false) }
    Card(modifier = Modifier.fillMaxWidth().shadow(6.dp, CardShape, clip = false), shape = CardShape, elevation = CardDefaults.cardElevation(0.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Expense Settings", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 16.dp), color = MaterialTheme.colorScheme.onSurface)
            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)), shape = ChipShape) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 8.dp)) {
                        Box(modifier = Modifier.size(24.dp).background(EmeraldGreen.copy(alpha = 0.15f), RoundedCornerShape(6.dp)), contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Restaurant, null, tint = EmeraldGreen, modifier = Modifier.size(14.dp))
                        }
                        Spacer(Modifier.width(8.dp))
                        Text("Daily Meal Rate", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                    }
                    OutlinedTextField(value = dailyMealRate, onValueChange = onDailyMealRateChange, label = { Text("Amount in Taka") }, leadingIcon = { Text("Tk", modifier = Modifier.padding(start = 8.dp), fontWeight = FontWeight.Bold) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done), keyboardActions = KeyboardActions(onDone = { onSaveMealRate(dailyMealRate); focusManager.clearFocus() }), modifier = Modifier.fillMaxWidth(), shape = ChipShape, singleLine = true)
                }
            }
            Spacer(Modifier.height(12.dp))
            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)), shape = ChipShape) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(24.dp).background(GoldenAmber.copy(alpha = 0.15f), RoundedCornerShape(6.dp)), contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.DirectionsCar, null, tint = GoldenAmber, modifier = Modifier.size(14.dp))
                            }
                            Spacer(Modifier.width(8.dp))
                            Text("Travel & Other", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                        }
                        IconButton(onClick = { showOtherExpenseDetails = !showOtherExpenseDetails }, modifier = Modifier.size(24.dp)) {
                            Icon(if (showOtherExpenseDetails) Icons.Default.Info else Icons.Default.Warning, "More info", tint = GoldenAmber)
                        }
                    }
                    OutlinedTextField(value = dailyTravelCost, onValueChange = onDailyTravelCostChange, label = { Text("Daily Travel Cost") }, leadingIcon = { Text("Tk", modifier = Modifier.padding(start = 8.dp), fontWeight = FontWeight.Bold) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next), modifier = Modifier.fillMaxWidth().padding(top = 8.dp), shape = ChipShape, singleLine = true)
                    AnimatedVisibility(visible = showOtherExpenseDetails) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 12.dp)) {
                            OutlinedTextField(value = otherExpenses, onValueChange = onOtherExpensesChange, label = { Text("Monthly Other Expenses") }, leadingIcon = { Text("Tk", modifier = Modifier.padding(start = 8.dp), fontWeight = FontWeight.Bold) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next), modifier = Modifier.fillMaxWidth(), shape = ChipShape, singleLine = true)
                            OutlinedTextField(value = otherExpenseDescription, onValueChange = onOtherExpenseDescriptionChange, label = { Text("Description (optional)") }, keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done), keyboardActions = KeyboardActions(onDone = { onSaveTravelExpense(dailyTravelCost, otherExpenses, otherExpenseDescription); focusManager.clearFocus() }), modifier = Modifier.fillMaxWidth(), shape = ChipShape, maxLines = 2)
                        }
                    }
                    Button(onClick = { onSaveTravelExpense(dailyTravelCost, otherExpenses, otherExpenseDescription); focusManager.clearFocus() }, modifier = Modifier.fillMaxWidth().padding(top = 12.dp), shape = ChipShape, colors = ButtonDefaults.buttonColors(containerColor = GoldenAmber, contentColor = Color.White)) {
                        Text("Save Travel Settings")
                    }
                }
            }
        }
    }
}

@Composable
fun MonthNavigator(month: String, onPrevious: () -> Unit, onNext: () -> Unit, isLoading: Boolean) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = onPrevious, enabled = !isLoading) { Icon(Icons.Default.ArrowBackIosNew, "Previous Month") }
        AnimatedContent(targetState = month, label = "Month Text") { targetMonth ->
            Text(targetMonth, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        }
        IconButton(onClick = onNext, enabled = !isLoading) { Icon(Icons.AutoMirrored.Filled.ArrowForwardIos, "Next Month") }
    }
}

@Composable
fun SummaryHeaderCard(officeDays: Int, homeOfficeDays: Int, totalCost: Double, isLoading: Boolean) {
    Card(modifier = Modifier.fillMaxWidth().shadow(6.dp, CardShape, clip = false), shape = CardShape, elevation = CardDefaults.cardElevation(0.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(modifier = Modifier.size(36.dp).background(brush = Brush.linearGradient(listOf(VioletPurple, CyanLight)), shape = RoundedCornerShape(10.dp)), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Summarize, null, tint = Color.White, modifier = Modifier.size(20.dp))
                }
                Column {
                    Text("This Month's Summary", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    Text("Office vs Home Office days", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Spacer(Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                SummaryItem("Office", "$officeDays days")
                SummaryItem("Home", "$homeOfficeDays days")
                SummaryItem("Total", "${officeDays + homeOfficeDays} days")
                SummaryItem("Total Cost", "Tk %.0f".format(totalCost))
            }
        }
    }
}

@Composable
private fun RowScope.SummaryItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(4.dp))
        Text(value, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
fun WorkingDaysPieChart(data: Map<String, Float>, isLoading: Boolean) {
    val pieChartColors = listOf(VioletPurple, GoldenAmber)
    val officeValue = data["Office"] ?: 0f
    val homeValue = data["Home Office"] ?: 0f
    val totalValue = officeValue + homeValue
    val officePercentage = if (totalValue > 0f) (officeValue / totalValue) * 100f else 0f
    val pieChartData = PieChartData(
        slices = data.entries.mapIndexed { index, entry -> PieChartData.Slice(label = entry.key, value = entry.value, color = pieChartColors[index % pieChartColors.size]) },
        plotType = PlotType.Donut
    )
    val pieChartConfig = PieChartConfig(isAnimationEnable = true, showSliceLabels = false, strokeWidth = 28f, chartPadding = 20)
    Card(modifier = Modifier.fillMaxWidth().shadow(6.dp, CardShape, clip = false), shape = CardShape, elevation = CardDefaults.cardElevation(0.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Box(modifier = Modifier.fillMaxWidth().height(260.dp), contentAlignment = Alignment.Center) {
            PieChart(modifier = Modifier.size(220.dp), pieChartData = pieChartData, pieChartConfig = pieChartConfig)
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("${officePercentage.toInt()}%", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = VioletPurple)
                Text("Office", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (isLoading) {
                Box(modifier = Modifier.matchParentSize().background(Color.White.copy(alpha = 0.3f)), contentAlignment = Alignment.Center) { CircularProgressIndicator(modifier = Modifier.size(32.dp)) }
            }
        }
    }
}

@Composable
fun NoDataPlaceholder(title: String, message: String, icon: ImageVector) {
    Card(modifier = Modifier.fillMaxWidth().height(200.dp).shadow(6.dp, CardShape, clip = false), shape = CardShape, elevation = CardDefaults.cardElevation(0.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(icon, null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
                Spacer(Modifier.height(8.dp))
                Text(title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(message, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center, modifier = Modifier.padding(horizontal = 32.dp))
            }
        }
    }
}
