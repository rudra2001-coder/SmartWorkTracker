package com.rudra.smartworktracker.ui.screens.calculation

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
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
private val SlateGray = Color(0xFF64748B)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalculationScreen(onNavigateBack: () -> Unit) {
    val context = LocalContext.current
    val viewModel: CalculationViewModel = viewModel(factory = CalculationViewModelFactory(context))
    val s by viewModel.state.collectAsState()
    val focusManager = LocalFocusManager.current
    val mdf = remember { SimpleDateFormat("MMMM yyyy", Locale.getDefault()) }
    var travelInput by remember { mutableStateOf("") }
    var otherInput by remember { mutableStateOf("") }
    var otherDesc by remember { mutableStateOf("") }
    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(s.dailyTravelCost, s.otherExpenses, s.otherExpenseDescription) {
        if (travelInput.toDoubleOrNull() != s.dailyTravelCost) travelInput = s.dailyTravelCost.toString()
        if (otherInput.toDoubleOrNull() != s.otherExpenses) otherInput = s.otherExpenses.toString()
        if (otherDesc != s.otherExpenseDescription) otherDesc = s.otherExpenseDescription
    }

    s.errorMessage?.let { msg ->
        LaunchedEffect(msg) {
            snackbar.showSnackbar(msg, duration = SnackbarDuration.Short)
            delay(2000); viewModel.clearErrorMessage()
        }
    }

    SmartWorkTrackerTheme {
        Scaffold(
            snackbarHost = { SnackbarHost(snackbar) },
            topBar = {
                TopAppBar(
                    title = { Text("Meal Calculator", fontWeight = FontWeight.Bold) },
                    navigationIcon = { IconButton(onClick = onNavigateBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") } },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface),
                    actions = {
                        if (s.isLoading) CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                    }
                )
            },
            modifier = Modifier.background(MaterialTheme.colorScheme.surface)
        ) { pad ->
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(pad).background(MaterialTheme.colorScheme.surface),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item { MonthNav(month = mdf.format(s.selectedDate), onPrev = { viewModel.goToPreviousMonth() }, onNext = { viewModel.goToNextMonth() }, loading = s.isLoading) }

                item { SummaryHead(officeDays = s.officeDays, mealCost = s.totalMealMonthlyCost, totalCost = s.totalExpensePerMonth) }

                item { SettingsCard(normalRate = s.normalMealRate, specialRate = s.specialMealRate, mealDays = s.mealDays, onSave = { n, sp, d -> viewModel.saveMealSettings(n, sp, d) }) }

                item { SpecialDatesCard(selectedDate = s.selectedDate, specialDates = s.specialDates, officeDates = s.officeDates, workLogDates = s.workLogDates, onToggle = { viewModel.toggleSpecialDate(it) }) }

                item { SummaryCard(totalMeals = s.totalMeals, normalMeals = s.normalMeals, specialMeals = s.specialMeals, totalMonthly = s.totalMealMonthlyCost, totalQuarterly = s.totalMealQuarterlyCost, totalYearly = s.totalMealYearlyCost, normalRate = s.normalMealRate, specialRate = s.specialMealRate, breakdown = s.dayBreakdown) }

                if (s.monthlyBreakdown.isNotEmpty()) {
                    item { ChartCard(data = s.monthlyBreakdown, year = viewModel.getCurrentYear()) }
                }

                if (s.officeDays > 0) {
                    item { PieCard(count = s.officeDays) }
                } else {
                    item { EmptyCard() }
                }

                item { ExpenseCard(travelInput = travelInput, otherInput = otherInput, otherDesc = otherDesc, onTravelChange = { travelInput = it }, onOtherChange = { otherInput = it }, onDescChange = { otherDesc = it }, onSave = { t, o, d -> viewModel.saveTravelExpense(t.toDoubleOrNull() ?: 0.0, o.toDoubleOrNull() ?: 0.0, d) }, focusManager = focusManager) }

                item { TotalCard(mealMonthly = s.totalMealMonthlyCost, mealQuarterly = s.totalMealQuarterlyCost, mealYearly = s.totalMealYearlyCost, travelMonthly = s.travelCostPerMonth, travelYearly = s.travelCostPerYear, otherMonthly = s.otherExpensePerMonth, otherYearly = s.otherExpensePerYear, totalMonthly = s.totalExpensePerMonth, totalQuarterly = s.totalExpensePerQuarter, totalYearly = s.totalExpensePerYear) }

                item { ManualCalendarCard(
                    selectedMonth = s.manualSelectedMonth,
                    selectedDates = s.manualSelectedDates,
                    normalRate = s.manualNormalRate,
                    specialRate = s.manualSpecialRate,
                    manualTotal = s.manualTotal,
                    manualNormal = s.manualNormal,
                    manualSpecial = s.manualSpecial,
                    manualCost = s.manualCost,
                    manualCalculated = s.manualCalculated,
                    onPrevMonth = { viewModel.manualGoToPreviousMonth() },
                    onNextMonth = { viewModel.manualGoToNextMonth() },
                    onToggleDate = { date, type -> viewModel.toggleManualDate(date, type) },
                    onClearAll = { viewModel.clearAllManualDates() },
                    onSaveRates = { n, s -> viewModel.saveManualRates(n, s) },
                    onCalculate = { viewModel.calculateManual() }
                ) }

                item { Spacer(Modifier.height(8.dp)) }
            }
        }
    }
}

// ─── Month Navigator ─────────────────────────────────────────────

@Composable
private fun MonthNav(month: String, onPrev: () -> Unit, onNext: () -> Unit, loading: Boolean) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = onPrev, enabled = !loading) { Icon(Icons.Default.ArrowBackIosNew, "Prev") }
        Text(month, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        IconButton(onClick = onNext, enabled = !loading) { Icon(Icons.AutoMirrored.Filled.ArrowForwardIos, "Next") }
    }
}

// ─── Summary Header ──────────────────────────────────────────────

@Composable
private fun SummaryHead(officeDays: Int, mealCost: Double, totalCost: Double) {
    Card(Modifier.fillMaxWidth(), shape = CardShape, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer, contentColor = MaterialTheme.colorScheme.onPrimaryContainer)) {
        Column(Modifier.padding(16.dp)) {
            Text("This Month", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(12.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                StatCol("Office Days", "$officeDays", null)
                StatCol("Meal Cost", "\u09F3${"%,.0f".format(mealCost)}", EmeraldGreen)
                StatCol("Total Cost", "\u09F3${"%,.0f".format(totalCost)}", MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
private fun StatCol(label: String, value: String, color: Color?) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f))
        Spacer(Modifier.height(4.dp))
        Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = color ?: MaterialTheme.colorScheme.onPrimaryContainer)
    }
}

// ─── Meal Settings Card ──────────────────────────────────────────

@Composable
private fun SettingsCard(normalRate: Double, specialRate: Double, mealDays: Set<Int>, onSave: (Double, Double, Set<Int>) -> Unit) {
    var exp by remember { mutableStateOf(true) }
    var nInput by remember(normalRate) { mutableStateOf(if (normalRate == 0.0) "" else normalRate.toString()) }
    var sInput by remember(specialRate) { mutableStateOf(if (specialRate == 0.0) "" else specialRate.toString()) }
    var days by remember(mealDays) { mutableStateOf(mealDays) }
    var saved by remember { mutableStateOf(false) }

    val labels = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
    val values = listOf(Calendar.SUNDAY, Calendar.MONDAY, Calendar.TUESDAY, Calendar.WEDNESDAY, Calendar.THURSDAY, Calendar.FRIDAY, Calendar.SATURDAY)

    Card(Modifier.fillMaxWidth(), shape = CardShape, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)) {
        Column(Modifier.padding(16.dp)) {
            Row(Modifier.fillMaxWidth().clickable { exp = !exp }, horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.RestaurantMenu, contentDescription = null, modifier = Modifier.size(24.dp), tint = SapphireBlue)
                    Spacer(Modifier.width(10.dp))
                    Text("Meal Settings", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
                Icon(if (exp) Icons.Default.ExpandLess else Icons.Default.ExpandMore, contentDescription = null, modifier = Modifier.size(24.dp), tint = SlateGray)
            }
            AnimatedVisibility(visible = exp) {
                Column(Modifier.padding(top = 12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(value = nInput, onValueChange = { nInput = it; saved = false }, label = { Text("Normal Rate") }, leadingIcon = { Text("\u09F3", fontWeight = FontWeight.Bold) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), singleLine = true, modifier = Modifier.weight(1f), shape = ChipShape)
                        OutlinedTextField(value = sInput, onValueChange = { sInput = it; saved = false }, label = { Text("Special Rate") }, leadingIcon = { Text("\u09F3", fontWeight = FontWeight.Bold) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), singleLine = true, modifier = Modifier.weight(1f), shape = ChipShape)
                    }

                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Meal Weekdays", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            TextButton(onClick = { days = setOf(Calendar.MONDAY, Calendar.TUESDAY, Calendar.WEDNESDAY, Calendar.THURSDAY, Calendar.FRIDAY); saved = false }, contentPadding = PaddingValues(horizontal = 8.dp)) { Text("Wkdays", fontSize = 11.sp) }
                            TextButton(onClick = { days = emptySet(); saved = false }, contentPadding = PaddingValues(horizontal = 8.dp)) { Text("Clear", fontSize = 11.sp) }
                        }
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        values.forEachIndexed { i, v ->
                            val sel = v in days
                            FilterChip(selected = sel, onClick = { days = if (sel) days - v else days + v; saved = false }, label = { Text(labels[i], fontSize = 11.sp) }, shape = PillShape, colors = FilterChipDefaults.filterChipColors(selectedContainerColor = SapphireBlue.copy(alpha = 0.15f), selectedLabelColor = SapphireBlue), modifier = Modifier.weight(1f))
                        }
                    }

                    Button(onClick = { onSave(nInput.toDoubleOrNull() ?: normalRate, sInput.toDoubleOrNull() ?: specialRate, days); saved = true }, modifier = Modifier.fillMaxWidth(), shape = ChipShape, colors = ButtonDefaults.buttonColors(containerColor = SapphireBlue)) {
                        if (saved) { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp)); Spacer(Modifier.width(6.dp)) }
                        Text(if (saved) "Saved!" else "Save Settings")
                    }
                }
            }
        }
    }
}

// ─── Special Dates Calendar Card ─────────────────────────────────

@Composable
private fun SpecialDatesCard(selectedDate: Date, specialDates: List<Long>, officeDates: List<Long>, workLogDates: List<Long>, onToggle: (Long) -> Unit) {
    var exp by remember { mutableStateOf(true) }
    val cal = remember { Calendar.getInstance() }.apply { time = selectedDate }
    val year = cal.get(Calendar.YEAR)
    val month = cal.get(Calendar.MONTH)
    val dim = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
    val fdow = Calendar.getInstance().apply { set(year, month, 1) }.get(Calendar.DAY_OF_WEEK) - 1
    val df = remember { SimpleDateFormat("dd MMM", Locale.getDefault()) }

    val monthStart = Calendar.getInstance().apply { set(year, month, 1, 0, 0, 0); set(Calendar.MILLISECOND, 0) }.timeInMillis
    val monthEnd = Calendar.getInstance().apply { set(year, month, dim, 23, 59, 59); set(Calendar.MILLISECOND, 999) }.timeInMillis
    val monthSpecials = specialDates.filter { it in monthStart..monthEnd }

    Card(Modifier.fillMaxWidth(), shape = CardShape, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)) {
        Column(Modifier.padding(16.dp)) {
            Row(Modifier.fillMaxWidth().clickable { exp = !exp }, horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Event, contentDescription = null, modifier = Modifier.size(24.dp), tint = GoldenAmber)
                    Spacer(Modifier.width(10.dp))
                    Text("Special Meal Dates", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
                Icon(if (exp) Icons.Default.ExpandLess else Icons.Default.ExpandMore, contentDescription = null, modifier = Modifier.size(24.dp), tint = SlateGray)
            }
            AnimatedVisibility(visible = exp) {
                Column(Modifier.padding(top = 8.dp)) {
                    Text("Tap a day to mark it as a special meal (higher rate applies)", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(4.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        DotLegend(CoralRed, "Special")
                        DotLegend(EmeraldGreen, "Office")
                    }
                    Spacer(Modifier.height(8.dp))

                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                        listOf("S", "M", "T", "W", "T", "F", "S").forEach { d ->
                            Text(d, modifier = Modifier.width(36.dp), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = SlateGray, textAlign = TextAlign.Center)
                        }
                    }
                    Spacer(Modifier.height(4.dp))

                    val rows = (fdow + dim + 6) / 7
                    for (r in 0 until rows) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                            for (c in 0..6) {
                                val dn = r * 7 + c - fdow + 1
                                if (dn in 1..dim) {
                                    val ts = Calendar.getInstance().apply { set(year, month, dn, 0, 0, 0); set(Calendar.MILLISECOND, 0) }.timeInMillis
                                    val isSpecial = ts in specialDates
                                    val isOffice = ts in officeDates
                                    val hasWork = ts in workLogDates
                                    Box(
                                        modifier = Modifier.size(36.dp)
                                            .clip(CircleShape)
                                            .background(when { isSpecial -> CoralRed.copy(alpha = 0.25f); isOffice -> EmeraldGreen.copy(alpha = 0.12f); else -> Color.Transparent })
                                            .then(if (isSpecial) Modifier.border(1.5.dp, CoralRed, CircleShape) else if (isOffice) Modifier.border(1.dp, EmeraldGreen.copy(alpha = 0.4f), CircleShape) else Modifier)
                                            .clickable { onToggle(ts) },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text("$dn", style = MaterialTheme.typography.bodySmall, fontWeight = if (isSpecial) FontWeight.Bold else FontWeight.Normal, color = if (isSpecial) CoralRed else if (hasWork) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
                                            if (isOffice && !isSpecial) Box(Modifier.size(3.dp).clip(CircleShape).background(EmeraldGreen))
                                        }
                                    }
                                } else {
                                    Box(Modifier.size(36.dp))
                                }
                            }
                        }
                    }

                    if (monthSpecials.isNotEmpty()) {
                        Spacer(Modifier.height(12.dp))
                        Surface(shape = ChipShape, color = CoralRed.copy(alpha = 0.08f)) {
                            Column(Modifier.fillMaxWidth().padding(12.dp)) {
                                Text("${monthSpecials.size} special meal date(s)", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold, color = CoralRed)
                                Spacer(Modifier.height(4.dp))
                                monthSpecials.sorted().forEach { d ->
                                    Text("\u2022 ${df.format(Date(d))}", style = MaterialTheme.typography.bodySmall, color = CoralRed.copy(alpha = 0.8f))
                                }
                            }
                        }
                    } else {
                        Spacer(Modifier.height(8.dp))
                        Text("No special meal dates this month. Tap a date above to add one.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

@Composable
private fun DotLegend(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(8.dp).clip(CircleShape).background(color))
        Spacer(Modifier.width(4.dp))
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

// ─── Meal Summary Card ───────────────────────────────────────────

@Composable
private fun SummaryCard(totalMeals: Int, normalMeals: Int, specialMeals: Int, totalMonthly: Double, totalQuarterly: Double, totalYearly: Double, normalRate: Double, specialRate: Double, breakdown: List<DayMealBreakdown>) {
    var exp by remember { mutableStateOf(true) }
    var showDays by remember { mutableStateOf(false) }

    Card(Modifier.fillMaxWidth(), shape = CardShape, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)) {
        Column(Modifier.padding(16.dp)) {
            Row(Modifier.fillMaxWidth().clickable { exp = !exp }, horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.FoodBank, contentDescription = null, modifier = Modifier.size(24.dp), tint = EmeraldGreen)
                    Spacer(Modifier.width(10.dp))
                    Text("Meal Summary", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
                Icon(if (exp) Icons.Default.ExpandLess else Icons.Default.ExpandMore, contentDescription = null, modifier = Modifier.size(24.dp), tint = SlateGray)
            }
            AnimatedVisibility(visible = exp) {
                Column(Modifier.padding(top = 12.dp)) {
                    if (totalMeals == 0) {
                        Text("No meal-eligible days this month.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.height(4.dp))
                        Text("Requirements:\n1. Log OFFICE work days in Calendar\n2. Select meal weekdays in Settings above\n3. Optionally mark special meal dates", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    } else {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                            ChipStat("Total", "$totalMeals", EmeraldGreen)
                            ChipStat("Normal", "$normalMeals", SapphireBlue)
                            ChipStat("Special", "$specialMeals", GoldenAmber)
                        }
                        Spacer(Modifier.height(12.dp))
                        HorizontalDivider()
                        Spacer(Modifier.height(8.dp))

                        Row(Modifier.fillMaxWidth()) {
                            Text("Type", Modifier.weight(1f), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = SlateGray)
                            Text("Days", Modifier.width(45.dp), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = SlateGray, textAlign = TextAlign.End)
                            Text("Rate", Modifier.width(65.dp), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = SlateGray, textAlign = TextAlign.End)
                            Text("Cost", Modifier.width(75.dp), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = SlateGray, textAlign = TextAlign.End)
                        }
                        HorizontalDivider()
                        Spacer(Modifier.height(4.dp))
                        BreakdownRow("Normal", normalMeals, normalRate, normalMeals * normalRate, SapphireBlue)
                        BreakdownRow("Special", specialMeals, specialRate, specialMeals * specialRate, GoldenAmber)
                        Spacer(Modifier.height(8.dp))
                        HorizontalDivider()
                        Spacer(Modifier.height(8.dp))
                        Surface(shape = ChipShape, color = EmeraldGreen.copy(alpha = 0.08f)) {
                            Column(Modifier.fillMaxWidth().padding(12.dp)) {
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text("Monthly Total", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold); Text("\u09F3${"%,.0f".format(totalMonthly)}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = EmeraldGreen) }
                                Spacer(Modifier.height(2.dp))
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text("Quarterly (3mo)", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant); Text("\u09F3${"%,.0f".format(totalQuarterly)}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = EmeraldGreen) }
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text("Yearly (12mo)", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant); Text("\u09F3${"%,.0f".format(totalYearly)}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = EmeraldGreen) }
                            }
                        }

                        if (breakdown.isNotEmpty()) {
                            Spacer(Modifier.height(12.dp))
                            TextButton(onClick = { showDays = !showDays }) {
                                Icon(imageVector = if (showDays) Icons.Default.ExpandLess else Icons.Default.ListAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(4.dp))
                                Text(if (showDays) "Hide daily breakdown" else "Show daily breakdown (${breakdown.size} days)")
                            }
                            AnimatedVisibility(visible = showDays) {
                                Column(Modifier.padding(top = 4.dp)) {
                                    Row(Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = 0.5f), shape = RoundedCornerShape(8.dp)).padding(horizontal = 8.dp, vertical = 6.dp)) {
                                        Text("Date", Modifier.weight(1f), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = SlateGray)
                                        Text("Type", Modifier.width(36.dp), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = SlateGray, textAlign = TextAlign.Center)
                                        Text("Rate", Modifier.width(50.dp), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = SlateGray, textAlign = TextAlign.End)
                                        Text("Cost", Modifier.width(55.dp), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = SlateGray, textAlign = TextAlign.End)
                                    }
                                    breakdown.forEach { d ->
                                        val bg = if (d.cost > 0) MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = 0.3f) else Color.Transparent
                                        Row(Modifier.fillMaxWidth().background(bg, shape = RoundedCornerShape(4.dp)).padding(horizontal = 8.dp, vertical = 5.dp), verticalAlignment = Alignment.CenterVertically) {
                                            Row(Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                                                Text(d.dateLabel, style = MaterialTheme.typography.bodySmall, fontWeight = if (d.cost > 0) FontWeight.Medium else FontWeight.Normal, color = if (d.cost > 0) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                                                Spacer(Modifier.width(4.dp))
                                                Text("(${d.dayName.take(3)})", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                                            }
                                            val icon = when { d.workType == "OFFICE" && d.isSpecial -> Icons.Default.Star; d.workType == "OFFICE" -> Icons.Default.CheckCircle; else -> Icons.Default.RemoveCircleOutline }
                                            val iconTint = when { d.workType == "OFFICE" && d.isSpecial -> GoldenAmber; d.workType == "OFFICE" -> EmeraldGreen; else -> SlateGray.copy(alpha = 0.4f) }
                                            Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(16.dp), tint = iconTint)
                                            Spacer(Modifier.width(2.dp))
                                            Text(if (d.cost > 0) "\u09F3${"%,.0f".format(d.rate)}" else "-", Modifier.width(50.dp), style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.End, color = if (d.cost > 0) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f))
                                            Text(if (d.cost > 0) "\u09F3${"%,.0f".format(d.cost)}" else "0", Modifier.width(55.dp), style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.End, fontWeight = if (d.cost > 0) FontWeight.SemiBold else FontWeight.Normal, color = if (d.cost > 0) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BreakdownRow(label: String, count: Int, rate: Double, cost: Double, color: Color) {
    Row(Modifier.fillMaxWidth().padding(vertical = 3.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            Box(Modifier.size(8.dp).clip(CircleShape).background(color))
            Spacer(Modifier.width(6.dp))
            Text(label, style = MaterialTheme.typography.bodyMedium)
        }
        Text("$count", Modifier.width(45.dp), style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.End, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text("\u09F3${"%,.0f".format(rate)}", Modifier.width(65.dp), style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.End, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text("\u09F3${"%,.0f".format(cost)}", Modifier.width(75.dp), style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.End, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun ChipStat(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(shape = PillShape, color = color.copy(alpha = 0.1f)) {
            Text(value, Modifier.padding(horizontal = 16.dp, vertical = 6.dp), style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = color)
        }
        Spacer(Modifier.height(2.dp))
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

// ─── Total Summary Card ──────────────────────────────────────────

@Composable
private fun TotalCard(mealMonthly: Double, mealQuarterly: Double, mealYearly: Double, travelMonthly: Double, travelYearly: Double, otherMonthly: Double, otherYearly: Double, totalMonthly: Double, totalQuarterly: Double, totalYearly: Double) {
    var exp by remember { mutableStateOf(true) }
    Card(Modifier.fillMaxWidth(), shape = CardShape, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)) {
        Column(Modifier.padding(16.dp)) {
            Row(Modifier.fillMaxWidth().clickable { exp = !exp }, horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Calculate, contentDescription = null, modifier = Modifier.size(24.dp), tint = MaterialTheme.colorScheme.error)
                    Spacer(Modifier.width(10.dp))
                    Text("Total Cost Summary", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
                Icon(if (exp) Icons.Default.ExpandLess else Icons.Default.ExpandMore, contentDescription = null, modifier = Modifier.size(24.dp), tint = SlateGray)
            }
            AnimatedVisibility(visible = exp) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.padding(top = 12.dp)) {
                    Row(Modifier.fillMaxWidth()) {
                        Text("Category", Modifier.weight(1f), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = SlateGray)
                        Text("Monthly", Modifier.width(90.dp), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = SlateGray, textAlign = TextAlign.End)
                        Text("Yearly", Modifier.width(90.dp), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = SlateGray, textAlign = TextAlign.End)
                    }
                    HorizontalDivider()
                    Row(Modifier.fillMaxWidth()) { TSummaryRow("Meal", mealMonthly, mealYearly, EmeraldGreen) }
                    Row(Modifier.fillMaxWidth()) { TSummaryRow("Travel", travelMonthly, travelYearly, Color(0xFF388E3C)) }
                    Row(Modifier.fillMaxWidth()) { TSummaryRow("Other", otherMonthly, otherYearly, GoldenAmber) }
                    HorizontalDivider()
                    Row(Modifier.fillMaxWidth()) { TSummaryRow("Total", totalMonthly, totalYearly, MaterialTheme.colorScheme.error, bold = true) }
                    Spacer(Modifier.height(4.dp))
                    Surface(shape = ChipShape, color = VioletPurple.copy(alpha = 0.1f)) {
                        Row(Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Quarterly Total", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = VioletPurple)
                            Text("\u09F3${"%,.0f".format(totalQuarterly)}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = VioletPurple)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RowScope.TSummaryRow(label: String, monthly: Double, yearly: Double, color: Color, bold: Boolean = false) {
    Row(Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
        Text(label, Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium, fontWeight = if (bold) FontWeight.Bold else FontWeight.Medium, color = color)
        Text("\u09F3${"%,.0f".format(monthly)}", Modifier.width(90.dp), style = MaterialTheme.typography.bodyMedium, fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal, textAlign = TextAlign.End, color = color)
        Text("\u09F3${"%,.0f".format(yearly)}", Modifier.width(90.dp), style = MaterialTheme.typography.bodyMedium, fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal, textAlign = TextAlign.End, color = color)
    }
}

// ─── Monthly Breakdown Chart ─────────────────────────────────────

@Composable
private fun ChartCard(data: List<Pair<String, Double>>, year: Int) {
    val mx = data.maxOfOrNull { it.second } ?: 1.0
    Card(Modifier.fillMaxWidth().height(280.dp), shape = CardShape, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)) {
        Column(Modifier.padding(16.dp)) {
            Text("Monthly Breakdown $year", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(16.dp))
            Row(Modifier.fillMaxWidth().height(180.dp), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.Bottom) {
                data.forEach { (m, v) ->
                    val h = if (mx > 0) (v / mx * 120).dp else 0.dp
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Bottom) {
                        Box(Modifier.width(20.dp).height(h).clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)).background(MaterialTheme.colorScheme.primary))
                        Spacer(Modifier.height(4.dp))
                        Text(m, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("%.0f".format(v), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// ─── Pie Chart ──────────────────────────────────────────────────

@Composable
private fun PieCard(count: Int) {
    val data = PieChartData(slices = listOf(PieChartData.Slice("Office", count.toFloat(), MaterialTheme.colorScheme.primary)), plotType = PlotType.Donut)
    val cfg = PieChartConfig(isAnimationEnable = true, showSliceLabels = false, strokeWidth = 28f, chartPadding = 20)
    Card(Modifier.fillMaxWidth(), shape = CardShape, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)) {
        Box(Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
            PieChart(Modifier.size(160.dp), pieChartData = data, pieChartConfig = cfg)
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("$count", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Text("Office days", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

// ─── Empty Card ──────────────────────────────────────────────────

@Composable
private fun EmptyCard() {
    Card(Modifier.fillMaxWidth(), shape = CardShape, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)) {
        Box(Modifier.fillMaxWidth().height(160.dp), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(24.dp)) {
                Icon(imageVector = Icons.Default.WorkOutline, contentDescription = null, modifier = Modifier.size(40.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
                Spacer(Modifier.height(8.dp))
                Text("No office days logged", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("Log your work days in the Calendar to calculate meal costs", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
            }
        }
    }
}

// ─── Expense Input Section ───────────────────────────────────────

@Composable
private fun ExpenseCard(travelInput: String, otherInput: String, otherDesc: String, onTravelChange: (String) -> Unit, onOtherChange: (String) -> Unit, onDescChange: (String) -> Unit, onSave: (String, String, String) -> Unit, focusManager: FocusManager) {
    var show by remember { mutableStateOf(false) }
    Card(Modifier.fillMaxWidth(), shape = CardShape, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.DirectionsCar, contentDescription = null, modifier = Modifier.size(20.dp), tint = Color(0xFF388E3C))
                Spacer(Modifier.width(8.dp))
                Text("Travel & Other Expenses", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(Modifier.weight(1f))
                IconButton(onClick = { show = !show }, Modifier.size(28.dp)) { Icon(Icons.Default.MoreHoriz, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant) }
            }
            Spacer(Modifier.height(12.dp))
            Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow), shape = ChipShape) {
                Column(Modifier.padding(12.dp)) {
                    OutlinedTextField(value = travelInput, onValueChange = onTravelChange, label = { Text("Daily Travel Cost") }, leadingIcon = { Text("\u09F3", fontWeight = FontWeight.Bold) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true, modifier = Modifier.fillMaxWidth(), shape = ChipShape)
                    AnimatedVisibility(visible = show) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 12.dp)) {
                            OutlinedTextField(value = otherInput, onValueChange = onOtherChange, label = { Text("Monthly Other Expenses") }, leadingIcon = { Text("\u09F3", fontWeight = FontWeight.Bold) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true, modifier = Modifier.fillMaxWidth(), shape = ChipShape)
                            OutlinedTextField(value = otherDesc, onValueChange = onDescChange, label = { Text("Description (optional)") }, keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done), keyboardActions = KeyboardActions(onDone = { onSave(travelInput, otherInput, otherDesc); focusManager.clearFocus() }), modifier = Modifier.fillMaxWidth(), shape = ChipShape, maxLines = 2)
                        }
                    }
                    Button(onClick = { onSave(travelInput, otherInput, otherDesc); focusManager.clearFocus() }, modifier = Modifier.fillMaxWidth().padding(top = 12.dp), shape = ChipShape, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF388E3C))) { Text("Save") }
                }
            }
        }
    }
}

// ─── Manual Calendar Calculator ──────────────────────────────────

@Composable
private fun ManualCalendarCard(
    selectedMonth: Date,
    selectedDates: Map<Long, ManualDateType>,
    normalRate: Double,
    specialRate: Double,
    manualTotal: Int,
    manualNormal: Int,
    manualSpecial: Int,
    manualCost: Double,
    manualCalculated: Boolean,
    onPrevMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onToggleDate: (Long, ManualDateType) -> Unit,
    onClearAll: () -> Unit,
    onSaveRates: (Double, Double) -> Unit,
    onCalculate: () -> Unit
) {
    var markMode by remember { mutableStateOf(ManualDateType.NORMAL) }
    var nInput by remember(normalRate) { mutableStateOf(normalRate.let { if (it == 0.0) "" else it.toString() }) }
    var sInput by remember(specialRate) { mutableStateOf(specialRate.let { if (it == 0.0) "" else it.toString() }) }
    var saved by remember { mutableStateOf(false) }
    val cal = remember { Calendar.getInstance() }.apply { time = selectedMonth }
    val year = cal.get(Calendar.YEAR)
    val month = cal.get(Calendar.MONTH)
    val dim = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
    val fdow = Calendar.getInstance().apply { set(year, month, 1) }.get(Calendar.DAY_OF_WEEK) - 1
    val mdf = remember { SimpleDateFormat("MMM yyyy", Locale.getDefault()) }
    val df = remember { SimpleDateFormat("dd MMM", Locale.getDefault()) }

    Card(Modifier.fillMaxWidth(), shape = CardShape, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.size(36.dp).clip(RoundedCornerShape(10.dp)).background(VioletPurple.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) { Icon(Icons.Default.TouchApp, contentDescription = null, modifier = Modifier.size(20.dp), tint = VioletPurple) }
                Spacer(Modifier.width(10.dp))
                Text("Manual Meal Calculator", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(4.dp))
            Text("Tap dates to mark as Normal or Special, enter rates, and calculate", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(12.dp))

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = nInput, onValueChange = { nInput = it; saved = false }, label = { Text("Normal Rate") }, leadingIcon = { Text("\u09F3", fontWeight = FontWeight.Bold) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), singleLine = true, modifier = Modifier.weight(1f), shape = ChipShape)
                OutlinedTextField(value = sInput, onValueChange = { sInput = it; saved = false }, label = { Text("Special Rate") }, leadingIcon = { Text("\u09F3", fontWeight = FontWeight.Bold) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), singleLine = true, modifier = Modifier.weight(1f), shape = ChipShape)
            }
            Spacer(Modifier.height(8.dp))

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onPrevMonth, modifier = Modifier.size(28.dp)) { Icon(Icons.Default.ArrowBackIosNew, "Prev", modifier = Modifier.size(16.dp)) }
                    Text(mdf.format(selectedMonth), style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                    IconButton(onClick = onNextMonth, modifier = Modifier.size(28.dp)) { Icon(Icons.AutoMirrored.Filled.ArrowForwardIos, "Next", modifier = Modifier.size(16.dp)) }
                }
                TextButton(onClick = onClearAll, contentPadding = PaddingValues(horizontal = 4.dp)) { Text("Clear", fontSize = 11.sp, color = CoralRed) }
            }

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                FilterChip(
                    selected = markMode == ManualDateType.NORMAL,
                    onClick = { markMode = ManualDateType.NORMAL },
                    label = { Text("Normal", fontSize = 11.sp) },
                    leadingIcon = { Box(Modifier.size(8.dp).clip(CircleShape).background(EmeraldGreen)) },
                    shape = PillShape,
                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = EmeraldGreen.copy(alpha = 0.12f), selectedLabelColor = EmeraldGreen),
                    modifier = Modifier.weight(1f)
                )
                FilterChip(
                    selected = markMode == ManualDateType.SPECIAL,
                    onClick = { markMode = ManualDateType.SPECIAL },
                    label = { Text("Special", fontSize = 11.sp) },
                    leadingIcon = { Box(Modifier.size(8.dp).clip(CircleShape).background(CoralRed)) },
                    shape = PillShape,
                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = CoralRed.copy(alpha = 0.12f), selectedLabelColor = CoralRed),
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(Modifier.height(6.dp))

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                listOf("S", "M", "T", "W", "T", "F", "S").forEach { d ->
                    Text(d, modifier = Modifier.width(32.dp), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = SlateGray, textAlign = TextAlign.Center)
                }
            }
            Spacer(Modifier.height(2.dp))

            val rows = (fdow + dim + 6) / 7
            for (r in 0 until rows) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    for (c in 0..6) {
                        val dn = r * 7 + c - fdow + 1
                        if (dn in 1..dim) {
                            val ts = Calendar.getInstance().apply { set(year, month, dn, 0, 0, 0); set(Calendar.MILLISECOND, 0) }.timeInMillis
                            val existing = selectedDates[ts]
                            val bgColor = when (existing) {
                                ManualDateType.NORMAL -> EmeraldGreen.copy(alpha = 0.18f)
                                ManualDateType.SPECIAL -> CoralRed.copy(alpha = 0.18f)
                                null -> Color.Transparent
                            }
                            val borderColor = when (existing) {
                                ManualDateType.NORMAL -> EmeraldGreen
                                ManualDateType.SPECIAL -> CoralRed
                                null -> Color.Transparent
                            }
                            Box(
                                modifier = Modifier.size(32.dp)
                                    .clip(CircleShape).background(bgColor)
                                    .then(if (existing != null) Modifier.border(1.5.dp, borderColor, CircleShape) else Modifier)
                                    .clickable { onToggleDate(ts, markMode) },
                                contentAlignment = Alignment.Center
                            ) {
                                Text("$dn", style = MaterialTheme.typography.bodySmall, fontWeight = if (existing != null) FontWeight.Bold else FontWeight.Normal, color = if (existing != null) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
                            }
                        } else {
                            Box(Modifier.size(32.dp))
                        }
                    }
                }
            }

            Spacer(Modifier.height(6.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) { Box(Modifier.size(7.dp).clip(CircleShape).background(EmeraldGreen)); Spacer(Modifier.width(4.dp)); Text("Normal", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                Row(verticalAlignment = Alignment.CenterVertically) { Box(Modifier.size(7.dp).clip(CircleShape).background(CoralRed)); Spacer(Modifier.width(4.dp)); Text("Special", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                Text("${selectedDates.size} selected", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
            }

            if (selectedDates.isNotEmpty()) {
                Spacer(Modifier.height(6.dp))
                Surface(shape = ChipShape, color = VioletPurple.copy(alpha = 0.05f)) {
                    Row(Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 6.dp)) {
                        val sorted = selectedDates.entries.sortedBy { it.key }
                        val preview = sorted.take(5).joinToString(", ") { (ts, type) ->
                            val prefix = if (type == ManualDateType.NORMAL) "\u25CF" else "\u2605"
                            "$prefix ${df.format(Date(ts))}"
                        }
                        val more = if (sorted.size > 5) " +${sorted.size - 5} more" else ""
                        Text(preview + more, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
                    }
                }
            }

            Spacer(Modifier.height(8.dp))
            Button(
                onClick = {
                    onSaveRates(nInput.toDoubleOrNull() ?: normalRate, sInput.toDoubleOrNull() ?: specialRate)
                    onCalculate()
                    saved = true
                },
                enabled = selectedDates.isNotEmpty(),
                modifier = Modifier.fillMaxWidth(), shape = ChipShape,
                colors = ButtonDefaults.buttonColors(containerColor = VioletPurple)
            ) {
                if (saved) { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp)); Spacer(Modifier.width(6.dp)) }
                Text(if (saved) "Saved!" else "Calculate")
            }

            if (manualCalculated) {
                Spacer(Modifier.height(12.dp))
                val costAnim = remember(manualCost) { Animatable(0f) }
                LaunchedEffect(manualCost) { costAnim.animateTo(manualCost.toFloat(), animationSpec = tween(800)) }

                Surface(shape = ChipShape, color = VioletPurple.copy(alpha = 0.1f)) {
                    Column(Modifier.fillMaxWidth().padding(12.dp)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Total Meals", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("$manualTotal", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = VioletPurple)
                        }
                        Spacer(Modifier.height(2.dp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Normal (\u09F3${"%,.0f".format(nInput.toDoubleOrNull() ?: normalRate)} each)", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("$manualNormal", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = SapphireBlue)
                        }
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Special (\u09F3${"%,.0f".format(sInput.toDoubleOrNull() ?: specialRate)} each)", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("$manualSpecial", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = GoldenAmber)
                        }
                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Monthly Cost", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                            Text("\u09F3${"%,.0f".format(costAnim.value)}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = VioletPurple)
                        }
                    }
                }

                var showBreakdown by remember { mutableStateOf(false) }
                if (selectedDates.isNotEmpty()) {
                    TextButton(onClick = { showBreakdown = !showBreakdown }) {
                        Icon(if (showBreakdown) Icons.Default.ExpandLess else Icons.Default.ListAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(if (showBreakdown) "Hide day list" else "Show selected days")
                    }
                    AnimatedVisibility(visible = showBreakdown) {
                        Column(Modifier.heightIn(max = 200.dp)) {
                            val sorted = selectedDates.entries.sortedBy { it.key }
                            sorted.forEach { (ts, type) ->
                                Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 3.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(Modifier.size(6.dp).clip(CircleShape).background(if (type == ManualDateType.NORMAL) EmeraldGreen else CoralRed))
                                        Spacer(Modifier.width(6.dp))
                                        Text(df.format(Date(ts)), style = MaterialTheme.typography.bodySmall)
                                    }
                                    Text(if (type == ManualDateType.NORMAL) "\u09F3${"%,.0f".format(nInput.toDoubleOrNull() ?: normalRate)}" else "\u09F3${"%,.0f".format(sInput.toDoubleOrNull() ?: specialRate)}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold, color = if (type == ManualDateType.NORMAL) EmeraldGreen else CoralRed)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
