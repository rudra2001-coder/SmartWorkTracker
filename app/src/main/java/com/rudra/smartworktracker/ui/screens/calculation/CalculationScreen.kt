package com.rudra.smartworktracker.ui.screens.calculation

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn

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
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import co.yml.charts.common.model.PlotType
import co.yml.charts.ui.piechart.charts.PieChart
import co.yml.charts.ui.piechart.models.PieChartConfig
import co.yml.charts.ui.piechart.models.PieChartData
import com.rudra.smartworktracker.data.entity.DailyMealRate
import com.rudra.smartworktracker.data.entity.MealType
import com.rudra.smartworktracker.data.entity.WeeklyMealRate
import com.rudra.smartworktracker.ui.screens.calendar.SummaryRow
import com.rudra.smartworktracker.ui.theme.SmartWorkTrackerTheme
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

private val CardShape = RoundedCornerShape(16.dp)
private val ChipShape = RoundedCornerShape(12.dp)
private val PillShape = RoundedCornerShape(50.dp)

private val EmeraldGreen = Color(0xFF00C896)
private val CoralRed = Color(0xFFFF5757)
private val SapphireBlue = Color(0xFF3B82F6)
private val GoldenAmber = Color(0xFFF59E0B)
private val VioletPurple = Color(0xFF8B5CF6)
private val SlateGray = Color(0xFF64748B)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalculationScreen(onNavigateBack: () -> Unit) {
    val context = LocalContext.current
    val viewModel: CalculationViewModel = viewModel(factory = CalculationViewModelFactory(context))
    val s by viewModel.state.collectAsState()
    val focusManager = LocalFocusManager.current
    val monthYearFormat = remember { SimpleDateFormat("MMMM yyyy", Locale.getDefault()) }

    var dailyMealRateInput by remember { mutableStateOf("") }
    var dailyTravelCostInput by remember { mutableStateOf("") }
    var otherExpensesInput by remember { mutableStateOf("") }
    var otherExpenseDescriptionInput by remember { mutableStateOf("") }

    LaunchedEffect(s.dailyMealRate, s.dailyTravelCost, s.otherExpenses, s.otherExpenseDescription) {
        if (dailyMealRateInput.toDoubleOrNull() != s.dailyMealRate) dailyMealRateInput = s.dailyMealRate.toString()
        if (dailyTravelCostInput.toDoubleOrNull() != s.dailyTravelCost) dailyTravelCostInput = s.dailyTravelCost.toString()
        if (otherExpensesInput.toDoubleOrNull() != s.otherExpenses) otherExpensesInput = s.otherExpenses.toString()
        if (otherExpenseDescriptionInput != s.otherExpenseDescription) otherExpenseDescriptionInput = s.otherExpenseDescription
    }

    s.errorMessage?.let { msg ->
        LaunchedEffect(msg) {
            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
            delay(3000)
            viewModel.clearErrorMessage()
        }
    }

    SmartWorkTrackerTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Multi-Meal Calculator", fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface),
                    actions = {
                        if (s.isLoading) CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                    }
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { viewModel.exportToExcel(context) },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) { Icon(Icons.Default.Download, "Export") }
            },
            modifier = Modifier.background(MaterialTheme.colorScheme.surface)
        ) { paddingValues ->
            if (s.isLoading && s.mealTypes.isEmpty()) {
                Box(Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(
                    modifier = Modifier.padding(paddingValues).fillMaxSize().background(MaterialTheme.colorScheme.surface),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Section 1: Month Navigator
                    item {
                        MonthNavigator(
                            month = monthYearFormat.format(s.selectedDate),
                            onPrevious = { viewModel.goToPreviousMonth() },
                            onNext = { viewModel.goToNextMonth() },
                            isLoading = s.isLoading
                        )
                    }

                    // Section 2: Summary Header
                    item {
                        SummaryHeaderCard(
                            officeDays = s.officeDays,
                            homeOfficeDays = s.homeOfficeDays,
                            totalCost = s.totalExpensePerMonth,
                            isLoading = s.isLoading
                        )
                    }

                    // Section 3: Meal Types Management
                    item {
                        MealTypesCard(
                            mealTypes = s.mealTypes,
                            onAddMealType = { name, rate -> viewModel.addMealType(name, rate) },
                            onUpdateRate = { id, rate -> viewModel.updateMealTypeRate(id, rate) },
                            onDeleteMealType = { id -> viewModel.deleteMealType(id) }
                        )
                    }

                    // Section 4: Weekly Rate Override
                    item {
                        WeeklyRateCard(
                            mealTypes = s.mealTypes,
                            weeklyRates = s.weeklyRates,
                            selectedWeek = s.selectedWeek,
                            onSelectWeek = { viewModel.setSelectedWeek(it) },
                            onSaveRate = { mealTypeId, rate, week -> viewModel.saveWeeklyMealRate(mealTypeId, rate, week) },
                            onDeleteRate = { mealTypeId, week -> viewModel.deleteWeeklyMealRate(mealTypeId, week) }
                        )
                    }

                    // Section 5: Daily Date Picker with Rate Override
                    item {
                        DateSelectorCard(
                            selectedDate = s.selectedDate,
                            mealTypes = s.mealTypes,
                            dailyRates = s.dailyRates,
                            selectedDates = s.selectedDates,
                            onSelectedDatesChange = { viewModel.setSelectedDates(it) },
                            onSaveDailyRate = { mealTypeId, rate, date -> viewModel.saveDailyMealRateForDate(mealTypeId, rate, date) },
                            onDeleteDailyRate = { mealTypeId, date -> viewModel.deleteDailyMealRate(mealTypeId, date) }
                        )
                    }

                    // Section 6: Monthly Breakdown Chart
                    item {
                        if (s.monthlyBreakdown.isNotEmpty()) {
                            MonthlyBreakdownChart(data = s.monthlyBreakdown, year = viewModel.getCurrentYear())
                        }
                    }

                    // Section 7: Working Days Pie Chart
                    item {
                        if (s.officeDays + s.homeOfficeDays > 0) {
                            WorkingDaysPieChart(data = s.pieChartData, isLoading = s.isLoading)
                        } else {
                            NoDataPlaceholder("No Work Data", "Log your work days to see calculations", Icons.Default.WorkOutline)
                        }
                    }

                    // Section 8: Per-Meal Cost Breakdown
                    item {
                        MealCostSummaryCard(
                            perMealCosts = s.perMealCosts,
                            totalMonthly = s.totalMealMonthly,
                            totalQuarterly = s.totalMealQuarterly,
                            totalYearly = s.totalMealYearly
                        )
                    }

                    // Section 9: Travel & Other Expenses
                    item {
                        ExpenseInputSection(
                            dailyMealRate = dailyMealRateInput,
                            dailyTravelCost = dailyTravelCostInput,
                            otherExpenses = otherExpensesInput,
                            otherExpenseDescription = otherExpenseDescriptionInput,
                            onDailyMealRateChange = { dailyMealRateInput = it },
                            onDailyTravelCostChange = { dailyTravelCostInput = it },
                            onOtherExpensesChange = { otherExpensesInput = it },
                            onOtherExpenseDescriptionChange = { otherExpenseDescriptionInput = it },
                            onSaveMealRate = { rate -> rate.toDoubleOrNull()?.let { viewModel.saveDailyMealRate(it) } },
                            onSaveTravelExpense = { travel, other, desc ->
                                viewModel.saveTravelExpense(travel.toDoubleOrNull() ?: 0.0, other.toDoubleOrNull() ?: 0.0, desc)
                            },
                            focusManager = focusManager
                        )
                    }

                    // Section 10: Total Summary
                    item {
                        TotalSummaryCard(
                            mealMonthly = s.totalMealMonthly,
                            mealQuarterly = s.totalMealQuarterly,
                            mealYearly = s.totalMealYearly,
                            travelMonthly = s.travelCostPerMonth,
                            travelYearly = s.travelCostPerYear,
                            otherMonthly = s.otherExpensePerMonth,
                            otherYearly = s.otherExpensePerYear,
                            totalMonthly = s.totalExpensePerMonth,
                            totalQuarterly = s.totalExpensePerQuarter,
                            totalYearly = s.totalExpensePerYear
                        )
                    }

                    item { Spacer(Modifier.height(16.dp)) }
                }
            }
        }
    }
}

// ===================== Meal Types Card =====================

@Composable
fun MealTypesCard(
    mealTypes: List<MealType>,
    onAddMealType: (String, Double) -> Unit,
    onUpdateRate: (Int, Double) -> Unit,
    onDeleteMealType: (Int) -> Unit
) {
    var expanded by remember { mutableStateOf(true) }
    var showAddDialog by remember { mutableStateOf(false) }
    var newName by remember { mutableStateOf("") }
    var newRate by remember { mutableStateOf("") }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = CardShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth().clickable { expanded = !expanded },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Restaurant, contentDescription = null, tint = SapphireBlue, modifier = Modifier.size(24.dp))
                    Spacer(Modifier.width(10.dp))
                    Text("Meal Types & Rates", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
                Icon(if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore, contentDescription = null, tint = SlateGray)
            }

            AnimatedVisibility(visible = expanded) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 12.dp)) {
                    mealTypes.forEach { mt ->
                        var rateText by remember(mt.id, mt.defaultRate) { mutableStateOf(mt.defaultRate.let { if (it == 0.0) "" else it.toString() }) }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Surface(shape = PillShape, color = SapphireBlue.copy(alpha = 0.1f)) {
                                Text(mt.name, modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold, color = SapphireBlue)
                            }
                            Spacer(Modifier.weight(1f))
                            OutlinedTextField(
                                value = rateText,
                                onValueChange = { rateText = it; it.toDoubleOrNull()?.let { v -> onUpdateRate(mt.id, v) } },
                                label = { Text("৳ Rate") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Done),
                                singleLine = true,
                                modifier = Modifier.width(110.dp),
                                shape = ChipShape,
                                textStyle = MaterialTheme.typography.bodyMedium
                            )
                            IconButton(onClick = { onDeleteMealType(mt.id) }, modifier = Modifier.size(32.dp)) {
                                Icon(Icons.Default.Close, contentDescription = "Delete", tint = CoralRed, modifier = Modifier.size(18.dp))
                            }
                        }
                    }

                    Spacer(Modifier.height(4.dp))
                    TextButton(onClick = { showAddDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Add Meal Type")
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            shape = CardShape,
            title = { Text("Add Meal Type", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(value = newName, onValueChange = { newName = it }, label = { Text("Meal Name") }, singleLine = true, shape = ChipShape)
                    OutlinedTextField(value = newRate, onValueChange = { newRate = it }, label = { Text("Default Rate (৳)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), singleLine = true, shape = ChipShape)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newName.isNotBlank()) {
                            onAddMealType(newName.trim(), newRate.toDoubleOrNull() ?: 0.0)
                            showAddDialog = false
                            newName = ""; newRate = ""
                        }
                    },
                    enabled = newName.isNotBlank(),
                    shape = ChipShape
                ) { Text("Add") }
            },
            dismissButton = { TextButton(onClick = { showAddDialog = false }) { Text("Cancel") } }
        )
    }
}

// ===================== Weekly Rate Card =====================

@Composable
fun WeeklyRateCard(
    mealTypes: List<MealType>,
    weeklyRates: List<WeeklyMealRate>,
    selectedWeek: Int,
    onSelectWeek: (Int) -> Unit,
    onSaveRate: (Int, Double, Int) -> Unit,
    onDeleteRate: (Int, Int) -> Unit
) {
    var expanded by remember { mutableStateOf(true) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = CardShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth().clickable { expanded = !expanded },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.DateRange, contentDescription = null, tint = GoldenAmber, modifier = Modifier.size(24.dp))
                    Spacer(Modifier.width(10.dp))
                    Text("Weekly Rate Override", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
                Icon(if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore, contentDescription = null, tint = SlateGray)
            }

            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        (1..5).forEach { week ->
                            FilterChip(
                                selected = selectedWeek == week,
                                onClick = { onSelectWeek(week) },
                                label = { Text("W$week", fontSize = 12.sp) },
                                shape = PillShape,
                                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = GoldenAmber.copy(alpha = 0.2f))
                            )
                        }
                    }

                    Spacer(Modifier.height(12.dp))
                    Text("Week $selectedWeek Rates", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(8.dp))

                    mealTypes.forEach { mt ->
                        val existingRate = weeklyRates.find { it.mealTypeId == mt.id && it.weekNumber == selectedWeek }
                        var rateText by remember(mt.id, selectedWeek, existingRate?.rate) {
                            mutableStateOf(existingRate?.rate?.let { if (it == 0.0) "" else it.toString() } ?: "")
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(mt.name, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
                            OutlinedTextField(
                                value = rateText,
                                onValueChange = { rateText = it },
                                label = { Text("Rate") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Done),
                                keyboardActions = KeyboardActions(onDone = { rateText.toDoubleOrNull()?.let { v -> onSaveRate(mt.id, v, selectedWeek) } }),
                                singleLine = true,
                                modifier = Modifier.width(100.dp),
                                shape = ChipShape,
                                textStyle = MaterialTheme.typography.bodySmall
                            )
                            IconButton(
                                onClick = {
                                    rateText = ""
                                    onDeleteRate(mt.id, selectedWeek)
                                },
                                modifier = Modifier.size(28.dp),
                                enabled = existingRate != null
                            ) {
                                Icon(Icons.Default.Clear, contentDescription = "Reset", tint = if (existingRate != null) CoralRed else SlateGray.copy(alpha = 0.3f), modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

// ===================== Date Selector Card =====================

@Composable
fun DateSelectorCard(
    selectedDate: Date,
    mealTypes: List<MealType>,
    dailyRates: List<DailyMealRate>,
    selectedDates: List<Long>,
    onSelectedDatesChange: (List<Long>) -> Unit,
    onSaveDailyRate: (Int, Double, Long) -> Unit,
    onDeleteDailyRate: (Int, Long) -> Unit
) {
    var expanded by remember { mutableStateOf(true) }
    val cal = remember { Calendar.getInstance() }
    cal.time = selectedDate
    val year = cal.get(Calendar.YEAR)
    val month = cal.get(Calendar.MONTH)
    val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
    val firstDayOfWeek = Calendar.getInstance().apply { set(year, month, 1) }.get(Calendar.DAY_OF_WEEK) - 1

    val dateFormat = remember { SimpleDateFormat("dd MMM, yyyy", Locale.getDefault()) }
    val dayMs = 86400000L

    var editingDate by remember { mutableStateOf<Long?>(null) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = CardShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth().clickable { expanded = !expanded },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = VioletPurple, modifier = Modifier.size(24.dp))
                    Spacer(Modifier.width(10.dp))
                    Text("Date-Specific Rates", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
                Icon(if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore, contentDescription = null, tint = SlateGray)
            }

            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    Text("Tap dates to set custom meal rates", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(8.dp))

                    val dayHeaders = listOf("S", "M", "T", "W", "T", "F", "S")
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                        dayHeaders.forEach { day ->
                            Text(day, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = SlateGray, modifier = Modifier.width(36.dp), textAlign = TextAlign.Center)
                        }
                    }
                    Spacer(Modifier.height(4.dp))

                    val totalCells = firstDayOfWeek + daysInMonth
                    val rows = (totalCells + 6) / 7

                    for (row in 0 until rows) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                            for (col in 0..6) {
                                val dayNum = row * 7 + col - firstDayOfWeek + 1
                                if (dayNum in 1..daysInMonth) {
                                    val dateTs = Calendar.getInstance().apply { set(year, month, dayNum, 0, 0, 0); set(Calendar.MILLISECOND, 0) }.timeInMillis
                                    val isSelected = dateTs in selectedDates
                                    val hasOverride = dailyRates.any { it.date == dateTs }

                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(
                                                when {
                                                    isSelected && hasOverride -> EmeraldGreen.copy(alpha = 0.3f)
                                                    isSelected -> SapphireBlue.copy(alpha = 0.2f)
                                                    hasOverride -> GoldenAmber.copy(alpha = 0.2f)
                                                    else -> Color.Transparent
                                                }
                                            )
                                            .then(
                                                if (isSelected) Modifier.border(1.5.dp, SapphireBlue, CircleShape)
                                                else if (hasOverride) Modifier.border(1.dp, GoldenAmber, CircleShape)
                                                else Modifier
                                            )
                                            .clickable { onSelectedDatesChange(if (isSelected) selectedDates - dateTs else selectedDates + dateTs) },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            "$dayNum",
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = if (isSelected || hasOverride) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSelected) SapphireBlue else MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                } else {
                                    Box(modifier = Modifier.size(36.dp))
                                }
                            }
                        }
                    }

                    if (selectedDates.isNotEmpty()) {
                        Spacer(Modifier.height(12.dp))

                        if (selectedDates.size == 1) {
                            val singleDate = selectedDates.first()
                            editingDate = singleDate
                            Text("Set rates for ${dateFormat.format(Date(singleDate))}:", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                            Spacer(Modifier.height(8.dp))
                            mealTypes.forEach { mt ->
                                val existing = dailyRates.find { it.mealTypeId == mt.id && it.date == singleDate }
                                var rateText by remember(mt.id, singleDate) { mutableStateOf(existing?.rate?.let { if (it == 0.0) "" else it.toString() } ?: "") }
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)
                                ) {
                                    Text(mt.name, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
                                    OutlinedTextField(
                                        value = rateText,
                                        onValueChange = { rateText = it },
                                        label = { Text("Rate") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Done),
                                        keyboardActions = KeyboardActions(onDone = { rateText.toDoubleOrNull()?.let { v -> onSaveDailyRate(mt.id, v, singleDate) } }),
                                        singleLine = true,
                                        modifier = Modifier.width(100.dp),
                                        shape = ChipShape,
                                        textStyle = MaterialTheme.typography.bodySmall
                                    )
                                    IconButton(
                                        onClick = { rateText = ""; onDeleteDailyRate(mt.id, singleDate) },
                                        modifier = Modifier.size(28.dp),
                                        enabled = existing != null
                                    ) {
                                        Icon(Icons.Default.Clear, contentDescription = "Reset", tint = if (existing != null) CoralRed else SlateGray.copy(alpha = 0.3f), modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        } else {
                            Text("${selectedDates.size} dates selected", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = SapphireBlue)
                            Text("Select a single date to set rates", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}

// ===================== Meal Cost Summary Card =====================

@Composable
fun MealCostSummaryCard(
    perMealCosts: List<PerMealCost>,
    totalMonthly: Double,
    totalQuarterly: Double,
    totalYearly: Double
) {
    var expanded by remember { mutableStateOf(true) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = CardShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth().clickable { expanded = !expanded },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.FoodBank, contentDescription = null, tint = EmeraldGreen, modifier = Modifier.size(24.dp))
                    Spacer(Modifier.width(10.dp))
                    Text("Meal Cost Breakdown", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
                Icon(if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore, contentDescription = null, tint = SlateGray)
            }

            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    if (perMealCosts.isEmpty()) {
                        Text("No meal types configured", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    } else {
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Text("Meal", modifier = Modifier.weight(1f), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = SlateGray)
                            Text("Weekly", modifier = Modifier.width(80.dp), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = SlateGray, textAlign = TextAlign.End)
                            Text("Monthly", modifier = Modifier.width(80.dp), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = SlateGray, textAlign = TextAlign.End)
                            Text("Yearly", modifier = Modifier.width(80.dp), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = SlateGray, textAlign = TextAlign.End)
                        }
                        Spacer(Modifier.height(6.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                        Spacer(Modifier.height(6.dp))

                        perMealCosts.forEach { pmc ->
                            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp)) {
                                Text(pmc.mealTypeName, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                                Text("৳${"%,.0f".format(pmc.weekly)}", modifier = Modifier.width(80.dp), style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.End, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("৳${"%,.0f".format(pmc.monthly)}", modifier = Modifier.width(80.dp), style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.End, fontWeight = FontWeight.SemiBold)
                                Text("৳${"%,.0f".format(pmc.yearly)}", modifier = Modifier.width(80.dp), style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.End, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }

                        Spacer(Modifier.height(8.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                        Spacer(Modifier.height(8.dp))

                        Row(modifier = Modifier.fillMaxWidth()) {
                            Text("Total Meal", modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                            Text("৳${"%,.0f".format(totalMonthly / 4.33)}", modifier = Modifier.width(80.dp), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, textAlign = TextAlign.End, color = EmeraldGreen)
                            Text("৳${"%,.0f".format(totalMonthly)}", modifier = Modifier.width(80.dp), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, textAlign = TextAlign.End, color = EmeraldGreen)
                            Text("৳${"%,.0f".format(totalYearly)}", modifier = Modifier.width(80.dp), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, textAlign = TextAlign.End, color = EmeraldGreen)
                        }

                        Spacer(Modifier.height(8.dp))
                        Surface(shape = ChipShape, color = EmeraldGreen.copy(alpha = 0.1f)) {
                            Row(modifier = Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Quarterly (3 months)", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                                Text("৳${"%,.0f".format(totalQuarterly)}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = EmeraldGreen)
                            }
                        }
                    }
                }
            }
        }
    }
}

// ===================== Total Summary Card =====================

@Composable
fun TotalSummaryCard(
    mealMonthly: Double, mealQuarterly: Double, mealYearly: Double,
    travelMonthly: Double, travelYearly: Double,
    otherMonthly: Double, otherYearly: Double,
    totalMonthly: Double, totalQuarterly: Double, totalYearly: Double
) {
    var expanded by remember { mutableStateOf(true) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = CardShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth().clickable { expanded = !expanded },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Calculate, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(24.dp))
                    Spacer(Modifier.width(10.dp))
                    Text("Total Cost Summary", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
                Icon(if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore, contentDescription = null, tint = SlateGray)
            }

            AnimatedVisibility(visible = expanded) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.padding(top = 12.dp)) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Text("Category", modifier = Modifier.weight(1f), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = SlateGray)
                        Text("Monthly", modifier = Modifier.width(90.dp), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = SlateGray, textAlign = TextAlign.End)
                        Text("Yearly", modifier = Modifier.width(90.dp), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = SlateGray, textAlign = TextAlign.End)
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                    Row(Modifier.fillMaxWidth()) { SummaryRow("Meal", mealMonthly, mealYearly, EmeraldGreen) }
                    Row(Modifier.fillMaxWidth()) { SummaryRow("Travel", travelMonthly, travelYearly, Color(0xFF388E3C)) }
                    Row(Modifier.fillMaxWidth()) { SummaryRow("Other", otherMonthly, otherYearly, GoldenAmber) }
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    Row(Modifier.fillMaxWidth()) { SummaryRow("Total", totalMonthly, totalYearly, MaterialTheme.colorScheme.error, bold = true) }
                    Spacer(Modifier.height(4.dp))

                    Surface(shape = ChipShape, color = VioletPurple.copy(alpha = 0.1f)) {
                        Row(modifier = Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Quarterly Total (3 months)", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = VioletPurple)
                            Text("৳${"%,.0f".format(totalQuarterly)}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = VioletPurple)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RowScope.SummaryRow(label: String, monthly: Double, yearly: Double, color: Color, bold: Boolean = false) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
        Text(label, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium, fontWeight = if (bold) FontWeight.Bold else FontWeight.Medium, color = color)
        Text("৳${"%,.0f".format(monthly)}", modifier = Modifier.width(90.dp), style = MaterialTheme.typography.bodyMedium, fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal, textAlign = TextAlign.End, color = color)
        Text("৳${"%,.0f".format(yearly)}", modifier = Modifier.width(90.dp), style = MaterialTheme.typography.bodyMedium, fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal, textAlign = TextAlign.End, color = color)
    }
}

// ===================== Existing Components (preserved) =====================

@Composable
fun MonthlyBreakdownChart(data: List<Pair<String, Double>>, year: Int) {
    val maxValue = data.maxOfOrNull { it.second } ?: 0.0
    Card(
        modifier = Modifier.fillMaxWidth().height(280.dp),
        shape = CardShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Monthly Breakdown $year", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Icon(Icons.Default.BarChart, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            }
            Spacer(Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth().height(180.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom
            ) {
                data.forEach { (month, value) ->
                    val heightPercent = if (maxValue > 0) value / maxValue else 0.0
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Bottom) {
                        Box(
                            modifier = Modifier
                                .width(20.dp)
                                .height((heightPercent * 120).dp)
                                .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                                .background(MaterialTheme.colorScheme.primary)
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(month, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("%.0f".format(value), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun ExpandableInfoSection(title: String, icon: ImageVector, color: Color, items: List<Pair<String, String>>) {
    var expanded by remember { mutableStateOf(true) }
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = CardShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth().clickable { expanded = !expanded }.padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(icon, contentDescription = title, tint = color, modifier = Modifier.size(24.dp))
                    Spacer(Modifier.width(12.dp))
                    Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
                Icon(if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            AnimatedVisibility(
                visible = expanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(bottom = 16.dp)) {
                    items.forEach { (label, value) ->
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
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
    onSaveMealRate: (String) -> Unit, onSaveTravelExpense: (String, String, String) -> Unit,
    focusManager: FocusManager
) {
    var showOtherExpenseDetails by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = CardShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Default Rates", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 16.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                shape = ChipShape
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 8.dp)) {
                        Icon(Icons.Default.Restaurant, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(8.dp))
                        Text("Daily Meal Rate (fallback)", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                    }
                    OutlinedTextField(
                        value = dailyMealRate,
                        onValueChange = onDailyMealRateChange,
                        label = { Text("Amount in Taka") },
                        leadingIcon = { Text("৳", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 8.dp)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { onSaveMealRate(dailyMealRate); focusManager.clearFocus() }),
                        modifier = Modifier.fillMaxWidth(),
                        shape = ChipShape
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                shape = ChipShape
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.DirectionsCar, contentDescription = null, tint = Color(0xFF388E3C))
                            Spacer(Modifier.width(8.dp))
                            Text("Travel & Other", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                        }
                        IconButton(onClick = { showOtherExpenseDetails = !showOtherExpenseDetails }, modifier = Modifier.size(24.dp)) {
                            Icon(if (showOtherExpenseDetails) Icons.Default.Info else Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                    OutlinedTextField(
                        value = dailyTravelCost,
                        onValueChange = onDailyTravelCostChange,
                        label = { Text("Daily Travel Cost") },
                        leadingIcon = { Text("৳", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 8.dp)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        shape = ChipShape
                    )
                    AnimatedVisibility(visible = showOtherExpenseDetails) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 12.dp)) {
                            OutlinedTextField(
                                value = otherExpenses, onValueChange = onOtherExpensesChange,
                                label = { Text("Monthly Other Expenses") },
                                leadingIcon = { Text("৳", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 8.dp)) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                                modifier = Modifier.fillMaxWidth(), shape = ChipShape
                            )
                            OutlinedTextField(
                                value = otherExpenseDescription, onValueChange = onOtherExpenseDescriptionChange,
                                label = { Text("Description (optional)") },
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                                keyboardActions = KeyboardActions(onDone = { onSaveTravelExpense(dailyTravelCost, otherExpenses, otherExpenseDescription); focusManager.clearFocus() }),
                                modifier = Modifier.fillMaxWidth(), shape = ChipShape, maxLines = 2
                            )
                        }
                    }
                    Button(
                        onClick = { onSaveTravelExpense(dailyTravelCost, otherExpenses, otherExpenseDescription); focusManager.clearFocus() },
                        modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                        shape = ChipShape,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF388E3C))
                    ) { Text("Save Travel Settings") }
                }
            }
        }
    }
}

@Composable
fun MonthNavigator(month: String, onPrevious: () -> Unit, onNext: () -> Unit, isLoading: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onPrevious, enabled = !isLoading) {
            Icon(Icons.Default.ArrowBackIosNew, contentDescription = "Previous")
        }
        AnimatedContent(targetState = month, label = "Month") { targetMonth ->
            Text(targetMonth, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }
        IconButton(onClick = onNext, enabled = !isLoading) {
            Icon(Icons.AutoMirrored.Filled.ArrowForwardIos, contentDescription = "Next")
        }
    }
}

@Composable
fun SummaryHeaderCard(officeDays: Int, homeOfficeDays: Int, totalCost: Double, isLoading: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = CardShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer, contentColor = MaterialTheme.colorScheme.onPrimaryContainer)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("This Month's Summary", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                SummaryItem("Office", "$officeDays days")
                SummaryItem("Home", "$homeOfficeDays days")
                SummaryItem("Total", "${officeDays + homeOfficeDays} days")
                SummaryItem("Cost", "%.0f ৳".format(totalCost), isCost = true)
            }
        }
    }
}

@Composable
private fun RowScope.SummaryItem(label: String, value: String, isCost: Boolean = false) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
        Text(label, style = MaterialTheme.typography.labelMedium)
        Spacer(Modifier.height(4.dp))
        Text(value, style = if (isCost) MaterialTheme.typography.titleMedium else MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun WorkingDaysPieChart(data: Map<String, Float>, isLoading: Boolean) {
    val pieChartColors = listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary)
    val officeValue = data["Office"] ?: 0f
    val homeValue = data["Home Office"] ?: 0f
    val totalValue = officeValue + homeValue
    val officePercentage = if (totalValue > 0f) (officeValue / totalValue) * 100f else 0f

    val pieChartData = PieChartData(
        slices = data.entries.mapIndexed { index, entry ->
            PieChartData.Slice(label = entry.key, value = entry.value, color = pieChartColors[index % pieChartColors.size])
        },
        plotType = PlotType.Donut
    )
    val pieChartConfig = PieChartConfig(isAnimationEnable = true, showSliceLabels = false, strokeWidth = 28f, chartPadding = 20)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = CardShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
    ) {
        Box(modifier = Modifier.fillMaxWidth().height(260.dp), contentAlignment = Alignment.Center) {
            PieChart(modifier = Modifier.size(220.dp), pieChartData = pieChartData, pieChartConfig = pieChartConfig)
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("${officePercentage.toInt()}%", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Text("Office", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (isLoading) {
                Box(modifier = Modifier.matchParentSize().background(Color.White.copy(alpha = 0.3f)), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(modifier = Modifier.size(32.dp))
                }
            }
        }
    }
}

@Composable
fun NoDataPlaceholder(title: String, message: String, icon: ImageVector) {
    Box(
        modifier = Modifier.fillMaxWidth().height(200.dp).clip(CardShape).background(MaterialTheme.colorScheme.surfaceContainerHigh),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
            Spacer(Modifier.height(8.dp))
            Text(title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(message, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center, modifier = Modifier.padding(horizontal = 32.dp))
        }
    }
}
