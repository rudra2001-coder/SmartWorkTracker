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

    var dailyTravelCostInput by remember { mutableStateOf("") }
    var otherExpensesInput by remember { mutableStateOf("") }
    var otherExpenseDescriptionInput by remember { mutableStateOf("") }

    LaunchedEffect(s.dailyTravelCost, s.otherExpenses, s.otherExpenseDescription) {
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
                    title = { Text("Meal Cost Calculator", fontWeight = FontWeight.Bold) },
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
            LazyColumn(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surface),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item { MonthNavigator(month = monthYearFormat.format(s.selectedDate), onPrevious = { viewModel.goToPreviousMonth() }, onNext = { viewModel.goToNextMonth() }, isLoading = s.isLoading) }

                item { SummaryHeaderCard(officeDays = s.officeDays, totalMealCost = s.totalMealMonthlyCost, totalCost = s.totalExpensePerMonth) }

                item { MealSettingsCard(normalRate = s.normalMealRate, specialRate = s.specialMealRate, mealDays = s.mealDays, onSave = { normal, special, days -> viewModel.saveMealSettings(normal, special, days) }) }

                item { SpecialDatesCalendarCard(selectedDate = s.selectedDate, specialDates = s.specialDates, onToggleDate = { viewModel.toggleSpecialDate(it) }) }

                item { MealSummaryCard(totalMeals = s.totalMeals, normalMeals = s.normalMeals, specialMeals = s.specialMeals, totalMonthly = s.totalMealMonthlyCost, totalQuarterly = s.totalMealQuarterlyCost, totalYearly = s.totalMealYearlyCost, normalRate = s.normalMealRate, specialRate = s.specialMealRate) }

                if (s.monthlyBreakdown.isNotEmpty()) {
                    item { MonthlyBreakdownChart(data = s.monthlyBreakdown, year = viewModel.getCurrentYear()) }
                }

                if (s.officeDays > 0) {
                    item { WorkingDaysPieChart(data = s.pieChartData) }
                } else {
                    item { NoDataPlaceholder("No Work Data", "Log your work days in Calendar to see meal cost", Icons.Default.WorkOutline) }
                }

                item { ExpenseInputSection(dailyTravelCost = dailyTravelCostInput, otherExpenses = otherExpensesInput, otherExpenseDescription = otherExpenseDescriptionInput, onDailyTravelCostChange = { dailyTravelCostInput = it }, onOtherExpensesChange = { otherExpensesInput = it }, onOtherExpenseDescriptionChange = { otherExpenseDescriptionInput = it }, onSaveTravelExpense = { travel, other, desc -> viewModel.saveTravelExpense(travel.toDoubleOrNull() ?: 0.0, other.toDoubleOrNull() ?: 0.0, desc) }, focusManager = focusManager) }

                item { TotalSummaryCard(mealMonthly = s.totalMealMonthlyCost, mealQuarterly = s.totalMealQuarterlyCost, mealYearly = s.totalMealYearlyCost, travelMonthly = s.travelCostPerMonth, travelYearly = s.travelCostPerYear, otherMonthly = s.otherExpensePerMonth, otherYearly = s.otherExpensePerYear, totalMonthly = s.totalExpensePerMonth, totalQuarterly = s.totalExpensePerQuarter, totalYearly = s.totalExpensePerYear) }

                item { Spacer(Modifier.height(16.dp)) }
            }
        }
    }
}

// ─── Meal Settings Card ─────────────────────────────────────────

@Composable
fun MealSettingsCard(normalRate: Double, specialRate: Double, mealDays: Set<Int>, onSave: (Double, Double, Set<Int>) -> Unit) {
    var expanded by remember { mutableStateOf(true) }
    var normalInput by remember(normalRate) { mutableStateOf(if (normalRate == 0.0) "" else normalRate.toString()) }
    var specialInput by remember(specialRate) { mutableStateOf(if (specialRate == 0.0) "" else specialRate.toString()) }
    var selectedDays by remember(mealDays) { mutableStateOf(mealDays) }

    val dayLabels = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
    val dayValues = listOf(Calendar.SUNDAY, Calendar.MONDAY, Calendar.TUESDAY, Calendar.WEDNESDAY, Calendar.THURSDAY, Calendar.FRIDAY, Calendar.SATURDAY)

    Card(modifier = Modifier.fillMaxWidth(), shape = CardShape, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth().clickable { expanded = !expanded }, horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Settings, contentDescription = null, tint = SapphireBlue, modifier = Modifier.size(24.dp))
                    Spacer(Modifier.width(10.dp))
                    Text("Meal Settings", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
                Icon(if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore, contentDescription = null, tint = SlateGray)
            }

            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.padding(top = 12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(value = normalInput, onValueChange = { normalInput = it }, label = { Text("Normal Meal Rate") }, leadingIcon = { Text("\u09F3", fontWeight = FontWeight.Bold) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), singleLine = true, modifier = Modifier.weight(1f), shape = ChipShape)
                        OutlinedTextField(value = specialInput, onValueChange = { specialInput = it }, label = { Text("Special Meal Rate") }, leadingIcon = { Text("\u09F3", fontWeight = FontWeight.Bold) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), singleLine = true, modifier = Modifier.weight(1f), shape = ChipShape)
                    }

                    Text("Meal Weekdays (select which days you get meal)", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        dayValues.forEachIndexed { i, dv ->
                            val sel = dv in selectedDays
                            FilterChip(selected = sel, onClick = { selectedDays = if (sel) selectedDays - dv else selectedDays + dv }, label = { Text(dayLabels[i], fontSize = 12.sp) }, shape = PillShape, colors = FilterChipDefaults.filterChipColors(selectedContainerColor = SapphireBlue.copy(alpha = 0.15f), selectedLabelColor = SapphireBlue), modifier = Modifier.weight(1f))
                        }
                    }

                    Button(onClick = { onSave(normalInput.toDoubleOrNull() ?: normalRate, specialInput.toDoubleOrNull() ?: specialRate, selectedDays) }, modifier = Modifier.fillMaxWidth(), shape = ChipShape, colors = ButtonDefaults.buttonColors(containerColor = SapphireBlue)) {
                        Text("Save Settings")
                    }
                }
            }
        }
    }
}

// ─── Special Dates Calendar Card ────────────────────────────────

@Composable
fun SpecialDatesCalendarCard(selectedDate: Date, specialDates: List<Long>, onToggleDate: (Long) -> Unit) {
    var expanded by remember { mutableStateOf(true) }
    val cal = remember { Calendar.getInstance() }
    cal.time = selectedDate
    val year = cal.get(Calendar.YEAR)
    val month = cal.get(Calendar.MONTH)
    val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
    val firstDayOfWeek = Calendar.getInstance().apply { set(year, month, 1) }.get(Calendar.DAY_OF_WEEK) - 1

    Card(modifier = Modifier.fillMaxWidth(), shape = CardShape, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth().clickable { expanded = !expanded }, horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = GoldenAmber, modifier = Modifier.size(24.dp))
                    Spacer(Modifier.width(10.dp))
                    Text("Special Meal Dates", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
                Icon(if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore, contentDescription = null, tint = SlateGray)
            }

            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    Text("Tap a date to mark/unmark it as a special meal day (higher rate applies)", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(8.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                        listOf("S", "M", "T", "W", "T", "F", "S").forEach { d ->
                            Text(d, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = SlateGray, modifier = Modifier.width(36.dp), textAlign = TextAlign.Center)
                        }
                    }
                    Spacer(Modifier.height(4.dp))

                    val rows = ((firstDayOfWeek + daysInMonth) + 6) / 7
                    for (row in 0 until rows) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                            for (col in 0..6) {
                                val dayNum = row * 7 + col - firstDayOfWeek + 1
                                if (dayNum in 1..daysInMonth) {
                                    val dateTs = Calendar.getInstance().apply { set(year, month, dayNum, 0, 0, 0); set(Calendar.MILLISECOND, 0) }.timeInMillis
                                    val isSpecial = dateTs in specialDates
                                    Box(modifier = Modifier.size(36.dp).clip(CircleShape).background(if (isSpecial) CoralRed.copy(alpha = 0.25f) else Color.Transparent).then(if (isSpecial) Modifier.border(1.5.dp, CoralRed, CircleShape) else Modifier).clickable { onToggleDate(dateTs) }, contentAlignment = Alignment.Center) {
                                        Text("$dayNum", style = MaterialTheme.typography.bodySmall, fontWeight = if (isSpecial) FontWeight.Bold else FontWeight.Normal, color = if (isSpecial) CoralRed else MaterialTheme.colorScheme.onSurface)
                                    }
                                } else {
                                    Box(modifier = Modifier.size(36.dp))
                                }
                            }
                        }
                    }

                    if (specialDates.isNotEmpty()) {
                        Spacer(Modifier.height(12.dp))
                        val monthStart = Calendar.getInstance().apply { set(year, month, 1, 0, 0, 0); set(Calendar.MILLISECOND, 0) }.timeInMillis
                        val monthEnd = Calendar.getInstance().apply { set(year, month, daysInMonth, 23, 59, 59); set(Calendar.MILLISECOND, 999) }.timeInMillis
                        val monthSpecialCount = specialDates.count { it in monthStart..monthEnd }
                        Surface(shape = ChipShape, color = CoralRed.copy(alpha = 0.1f)) {
                            Row(modifier = Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Info, contentDescription = null, tint = CoralRed, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("$monthSpecialCount special meal(s) this month", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold, color = CoralRed)
                            }
                        }
                    }
                }
            }
        }
    }
}

// ─── Meal Summary Card ──────────────────────────────────────────

@Composable
fun MealSummaryCard(totalMeals: Int, normalMeals: Int, specialMeals: Int, totalMonthly: Double, totalQuarterly: Double, totalYearly: Double, normalRate: Double, specialRate: Double) {
    var expanded by remember { mutableStateOf(true) }

    Card(modifier = Modifier.fillMaxWidth(), shape = CardShape, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth().clickable { expanded = !expanded }, horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.FoodBank, contentDescription = null, tint = EmeraldGreen, modifier = Modifier.size(24.dp))
                    Spacer(Modifier.width(10.dp))
                    Text("Meal Summary", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
                Icon(if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore, contentDescription = null, tint = SlateGray)
            }

            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    if (totalMeals == 0) {
                        Text("No meal-eligible work days this month. Make sure you have:\n1. Work days logged in Calendar (OFFICE type)\n2. Meal weekdays selected in Settings above", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    } else {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                            StatChip("Total", "$totalMeals days", EmeraldGreen)
                            StatChip("Normal", "$normalMeals days", SapphireBlue)
                            StatChip("Special", "$specialMeals days", GoldenAmber)
                        }

                        Spacer(Modifier.height(12.dp))
                        HorizontalDivider()
                        Spacer(Modifier.height(8.dp))

                        Row(modifier = Modifier.fillMaxWidth()) {
                            Text("Type", modifier = Modifier.weight(1f), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = SlateGray)
                            Text("Days", modifier = Modifier.width(50.dp), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = SlateGray, textAlign = TextAlign.End)
                            Text("Rate", modifier = Modifier.width(70.dp), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = SlateGray, textAlign = TextAlign.End)
                            Text("Cost", modifier = Modifier.width(80.dp), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = SlateGray, textAlign = TextAlign.End)
                        }
                        HorizontalDivider()
                        Spacer(Modifier.height(4.dp))

                        Row(Modifier.fillMaxWidth().padding(vertical = 3.dp)) {
                            Text("Normal", modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
                            Text("$normalMeals", modifier = Modifier.width(50.dp), style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.End, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("\u09F3${"%,.0f".format(normalRate)}", modifier = Modifier.width(70.dp), style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.End, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("\u09F3${"%,.0f".format(normalMeals * normalRate)}", modifier = Modifier.width(80.dp), style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.End, fontWeight = FontWeight.SemiBold)
                        }
                        Row(Modifier.fillMaxWidth().padding(vertical = 3.dp)) {
                            Text("Special", modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
                            Text("$specialMeals", modifier = Modifier.width(50.dp), style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.End, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("\u09F3${"%,.0f".format(specialRate)}", modifier = Modifier.width(70.dp), style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.End, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("\u09F3${"%,.0f".format(specialMeals * specialRate)}", modifier = Modifier.width(80.dp), style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.End, fontWeight = FontWeight.SemiBold)
                        }

                        Spacer(Modifier.height(8.dp))
                        HorizontalDivider()
                        Spacer(Modifier.height(8.dp))

                        Surface(shape = ChipShape, color = EmeraldGreen.copy(alpha = 0.1f)) {
                            Column(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Monthly Total", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                    Text("\u09F3${"%,.0f".format(totalMonthly)}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = EmeraldGreen)
                                }
                                Spacer(Modifier.height(4.dp))
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Quarterly (3mo)", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("\u09F3${"%,.0f".format(totalQuarterly)}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = EmeraldGreen)
                                }
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Yearly", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("\u09F3${"%,.0f".format(totalYearly)}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = EmeraldGreen)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ─── Total Summary Card ─────────────────────────────────────────

@Composable
private fun RowScope.SummaryRow(label: String, monthly: Double, yearly: Double, color: Color, bold: Boolean = false) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
        Text(label, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium, fontWeight = if (bold) FontWeight.Bold else FontWeight.Medium, color = color)
        Text("\u09F3${"%,.0f".format(monthly)}", modifier = Modifier.width(90.dp), style = MaterialTheme.typography.bodyMedium, fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal, textAlign = TextAlign.End, color = color)
        Text("\u09F3${"%,.0f".format(yearly)}", modifier = Modifier.width(90.dp), style = MaterialTheme.typography.bodyMedium, fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal, textAlign = TextAlign.End, color = color)
    }
}

@Composable
fun TotalSummaryCard(mealMonthly: Double, mealQuarterly: Double, mealYearly: Double, travelMonthly: Double, travelYearly: Double, otherMonthly: Double, otherYearly: Double, totalMonthly: Double, totalQuarterly: Double, totalYearly: Double) {
    var expanded by remember { mutableStateOf(true) }
    Card(modifier = Modifier.fillMaxWidth(), shape = CardShape, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth().clickable { expanded = !expanded }, horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
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
                    HorizontalDivider()
                    Row(Modifier.fillMaxWidth()) { SummaryRow("Meal", mealMonthly, mealYearly, EmeraldGreen) }
                    Row(Modifier.fillMaxWidth()) { SummaryRow("Travel", travelMonthly, travelYearly, Color(0xFF388E3C)) }
                    Row(Modifier.fillMaxWidth()) { SummaryRow("Other", otherMonthly, otherYearly, GoldenAmber) }
                    HorizontalDivider()
                    Row(Modifier.fillMaxWidth()) { SummaryRow("Total", totalMonthly, totalYearly, MaterialTheme.colorScheme.error, bold = true) }
                    Spacer(Modifier.height(4.dp))
                    Surface(shape = ChipShape, color = VioletPurple.copy(alpha = 0.1f)) {
                        Row(modifier = Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Quarterly Total (3 months)", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = VioletPurple)
                            Text("\u09F3${"%,.0f".format(totalQuarterly)}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = VioletPurple)
                        }
                    }
                }
            }
        }
    }
}

// ─── Stat Chip ──────────────────────────────────────────────────

@Composable
private fun StatChip(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(shape = PillShape, color = color.copy(alpha = 0.1f)) {
            Text(value, modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp), style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = color)
        }
        Spacer(Modifier.height(4.dp))
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

// ─── Summary Header ─────────────────────────────────────────────

@Composable
fun SummaryHeaderCard(officeDays: Int, totalMealCost: Double, totalCost: Double) {
    Card(modifier = Modifier.fillMaxWidth(), shape = CardShape, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer, contentColor = MaterialTheme.colorScheme.onPrimaryContainer)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("This Month", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Office Days", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f))
                    Text("$officeDays", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Meal Cost", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f))
                    Text("\u09F3${"%,.0f".format(totalMealCost)}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Total Cost", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f))
                    Text("\u09F3${"%,.0f".format(totalCost)}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// ─── Month Navigator ────────────────────────────────────────────

@Composable
fun MonthNavigator(month: String, onPrevious: () -> Unit, onNext: () -> Unit, isLoading: Boolean) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = onPrevious, enabled = !isLoading) { Icon(Icons.Default.ArrowBackIosNew, "Previous") }
        Text(month, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        IconButton(onClick = onNext, enabled = !isLoading) { Icon(Icons.AutoMirrored.Filled.ArrowForwardIos, "Next") }
    }
}

// ─── Monthly Breakdown Chart ────────────────────────────────────

@Composable
fun MonthlyBreakdownChart(data: List<Pair<String, Double>>, year: Int) {
    val maxValue = data.maxOfOrNull { it.second } ?: 1.0
    Card(modifier = Modifier.fillMaxWidth().height(280.dp), shape = CardShape, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Monthly Breakdown $year", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth().height(180.dp), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.Bottom) {
                data.forEach { (month, value) ->
                    val h = if (maxValue > 0) (value / maxValue * 120).dp else 0.dp
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Bottom) {
                        Box(modifier = Modifier.width(20.dp).height(h).clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)).background(MaterialTheme.colorScheme.primary))
                        Spacer(Modifier.height(4.dp))
                        Text(month, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("%.0f".format(value), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// ─── Working Days Pie Chart ─────────────────────────────────────

@Composable
fun WorkingDaysPieChart(data: Map<String, Float>) {
    val officeValue = data["Office"] ?: 0f
    val pieChartData = PieChartData(slices = data.entries.mapIndexed { index, entry -> PieChartData.Slice(label = entry.key, value = entry.value, color = listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary)[index % 2]) }, plotType = PlotType.Donut)
    val pieChartConfig = PieChartConfig(isAnimationEnable = true, showSliceLabels = false, strokeWidth = 28f, chartPadding = 20)

    Card(modifier = Modifier.fillMaxWidth(), shape = CardShape, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)) {
        Box(modifier = Modifier.fillMaxWidth().height(220.dp), contentAlignment = Alignment.Center) {
            PieChart(modifier = Modifier.size(180.dp), pieChartData = pieChartData, pieChartConfig = pieChartConfig)
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("100%", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Text("Office", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

// ─── Placeholder ────────────────────────────────────────────────

@Composable
fun NoDataPlaceholder(title: String, message: String, icon: ImageVector) {
    Box(modifier = Modifier.fillMaxWidth().height(200.dp).clip(CardShape).background(MaterialTheme.colorScheme.surfaceContainerHigh), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
            Spacer(Modifier.height(8.dp))
            Text(title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(4.dp))
            Text(message, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center, modifier = Modifier.padding(horizontal = 32.dp))
        }
    }
}

// ─── Expense Input Section ──────────────────────────────────────

@Composable
fun ExpenseInputSection(dailyTravelCost: String, otherExpenses: String, otherExpenseDescription: String, onDailyTravelCostChange: (String) -> Unit, onOtherExpensesChange: (String) -> Unit, onOtherExpenseDescriptionChange: (String) -> Unit, onSaveTravelExpense: (String, String, String) -> Unit, focusManager: FocusManager) {
    var showDetails by remember { mutableStateOf(false) }
    Card(modifier = Modifier.fillMaxWidth(), shape = CardShape, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Travel & Other Expenses", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 16.dp))
            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow), shape = ChipShape) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.DirectionsCar, contentDescription = null, tint = Color(0xFF388E3C))
                        Spacer(Modifier.width(8.dp))
                        Text("Daily Travel Cost", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.weight(1f))
                        IconButton(onClick = { showDetails = !showDetails }, modifier = Modifier.size(24.dp)) { Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary) }
                    }
                    OutlinedTextField(value = dailyTravelCost, onValueChange = onDailyTravelCostChange, label = { Text("Daily Travel Cost") }, leadingIcon = { Text("\u09F3", fontWeight = FontWeight.Bold) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next), modifier = Modifier.fillMaxWidth().padding(top = 8.dp), shape = ChipShape)
                    AnimatedVisibility(visible = showDetails) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 12.dp)) {
                            OutlinedTextField(value = otherExpenses, onValueChange = onOtherExpensesChange, label = { Text("Monthly Other Expenses") }, leadingIcon = { Text("\u09F3", fontWeight = FontWeight.Bold) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth(), shape = ChipShape)
                            OutlinedTextField(value = otherExpenseDescription, onValueChange = onOtherExpenseDescriptionChange, label = { Text("Description (optional)") }, keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done), keyboardActions = KeyboardActions(onDone = { onSaveTravelExpense(dailyTravelCost, otherExpenses, otherExpenseDescription); focusManager.clearFocus() }), modifier = Modifier.fillMaxWidth(), shape = ChipShape, maxLines = 2)
                        }
                    }
                    Button(onClick = { onSaveTravelExpense(dailyTravelCost, otherExpenses, otherExpenseDescription); focusManager.clearFocus() }, modifier = Modifier.fillMaxWidth().padding(top = 12.dp), shape = ChipShape, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF388E3C))) { Text("Save Travel Settings") }
                }
            }
        }
    }
}
