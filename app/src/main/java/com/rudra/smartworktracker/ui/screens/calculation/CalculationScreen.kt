package com.rudra.smartworktracker.ui.screens.calculation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
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
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalculationScreen(onNavigateBack: () -> Unit) {
    val context = LocalContext.current
    val viewModel: CalculationViewModel = viewModel(factory = CalculationViewModelFactory(context))

    val calculation by viewModel.calculation.collectAsState()
    val travelExpense by viewModel.travelExpense.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()

    // Meal costs
    val mealCostPerWeek by viewModel.mealCostPerWeek.collectAsState()
    val mealCostPerMonth by viewModel.mealCostPerMonth.collectAsState()
    val mealCostPerYear by viewModel.mealCostPerYear.collectAsState()

    // Travel costs
    val travelCostPerWeek by viewModel.travelCostPerWeek.collectAsState()
    val travelCostPerMonth by viewModel.travelCostPerMonth.collectAsState()
    val travelCostPerYear by viewModel.travelCostPerYear.collectAsState()

    // Other expenses
    val otherExpensePerMonth by viewModel.otherExpensePerMonth.collectAsState()
    val otherExpensePerYear by viewModel.otherExpensePerYear.collectAsState()

    // Total expenses
    val totalExpensePerMonth by viewModel.totalExpensePerMonth.collectAsState()
    val totalExpensePerYear by viewModel.totalExpensePerYear.collectAsState()

    // Office days
    val officeDays by viewModel.officeDays.collectAsState()
    val homeOfficeDays by viewModel.homeOfficeDays.collectAsState()

    // State for input fields
    var dailyMealRate by remember { mutableStateOf("") }
    var dailyTravelCost by remember { mutableStateOf("") }
    var otherExpenses by remember { mutableStateOf("") }
    var otherExpenseDescription by remember { mutableStateOf("") }

    val focusManager = LocalFocusManager.current
    val monthYearFormat = remember { SimpleDateFormat("MMMM yyyy", Locale.getDefault()) }

    // Initialize fields from ViewModel
    LaunchedEffect(calculation, travelExpense) {
        calculation?.let {
            if (dailyMealRate.toDoubleOrNull() != it.dailyMealRate) {
                dailyMealRate = it.dailyMealRate.toString()
            }
        }

        travelExpense?.let {
            if (dailyTravelCost.toDoubleOrNull() != it.dailyTravelCost) {
                dailyTravelCost = it.dailyTravelCost.toString()
            }
            if (otherExpenses.toDoubleOrNull() != it.otherExpenses) {
                otherExpenses = it.otherExpenses.toString()
            }
            if (otherExpenseDescription != it.otherExpenseDescription) {
                otherExpenseDescription = it.otherExpenseDescription
            }
        }
    }

    val infoCardItems = remember(
        officeDays, homeOfficeDays,
        mealCostPerWeek, mealCostPerMonth, mealCostPerYear,
        travelCostPerWeek, travelCostPerMonth, travelCostPerYear,
        otherExpensePerMonth, otherExpensePerYear,
        totalExpensePerMonth, totalExpensePerYear
    ) {
        listOf(
            // Office Days
            "Office Days" to "$officeDays days",
            "Home Office Days" to "$homeOfficeDays days",

            // Meal Costs
            "Weekly Meal Cost" to String.format("%.2f Taka", mealCostPerWeek),
            "Monthly Meal Cost" to String.format("%.2f Taka", mealCostPerMonth),
            "Yearly Meal Cost" to String.format("%.2f Taka", mealCostPerYear),

            // Travel Costs
            "Weekly Travel Cost" to String.format("%.2f Taka", travelCostPerWeek),
            "Monthly Travel Cost" to String.format("%.2f Taka", travelCostPerMonth),
            "Yearly Travel Cost" to String.format("%.2f Taka", travelCostPerYear),

            // Other Expenses
            "Monthly Other Expenses" to String.format("%.2f Taka", otherExpensePerMonth),
            "Yearly Other Expenses" to String.format("%.2f Taka", otherExpensePerYear),

            // Total Expenses
            "Total Monthly Expense" to String.format("%.2f Taka", totalExpensePerMonth),
            "Total Yearly Expense" to String.format("%.2f Taka", totalExpensePerYear)
        )
    }

    SmartWorkTrackerTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            "Expense Calculator",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                "Back",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
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
                item {
                    MonthNavigator(
                        month = monthYearFormat.format(selectedDate),
                        onPrevious = { viewModel.goToPreviousMonth() },
                        onNext = { viewModel.goToNextMonth() }
                    )
                }

                item {
                    SummaryHeaderCard(
                        officeDays = officeDays,
                        homeOfficeDays = homeOfficeDays,
                        totalCost = totalExpensePerMonth
                    )
                }

                // Input Section - Meal Rate
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                "Meal Cost Settings",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )

                            ModernTextField(
                                value = dailyMealRate,
                                onValueChange = { dailyMealRate = it },
                                label = "Daily Meal Rate (Taka)",
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Number,
                                    imeAction = ImeAction.Done
                                ),
                                keyboardActions = KeyboardActions(
                                    onDone = {
                                        dailyMealRate.toDoubleOrNull()?.let {
                                            viewModel.saveDailyMealRate(it)
                                        }
                                        focusManager.clearFocus()
                                    }
                                )
                            )
                        }
                    }
                }

                // Input Section - Travel & Other Expenses
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                "Travel & Other Expenses",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )

                            ModernTextField(
                                value = dailyTravelCost,
                                onValueChange = { dailyTravelCost = it },
                                label = "Daily Travel Cost (Taka)",
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Number,
                                    imeAction = ImeAction.Next
                                ),
                                keyboardActions = KeyboardActions(
                                    onNext = {
                                        dailyTravelCost.toDoubleOrNull()?.let {
                                            viewModel.saveTravelExpense(
                                                it,
                                                otherExpenses.toDoubleOrNull() ?: 0.0,
                                                otherExpenseDescription
                                            )
                                        }
                                        focusManager.clearFocus()
                                    }
                                )
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            ModernTextField(
                                value = otherExpenses,
                                onValueChange = { otherExpenses = it },
                                label = "Other Monthly Expenses (Taka)",
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Number,
                                    imeAction = ImeAction.Next
                                ),
                                keyboardActions = KeyboardActions(
                                    onNext = {
                                        otherExpenses.toDoubleOrNull()?.let {
                                            viewModel.saveTravelExpense(
                                                dailyTravelCost.toDoubleOrNull() ?: 0.0,
                                                it,
                                                otherExpenseDescription
                                            )
                                        }
                                        focusManager.clearFocus()
                                    }
                                )
                            )

                            Spacer(modifier = Modifier.height(12.dp))


                        }
                    }
                }

                item {
                    val totalDays = officeDays + homeOfficeDays
                    if (totalDays > 0) {
                        WorkingDaysPieChart(
                            data = mapOf(
                                "Office" to officeDays.toFloat(),
                                "Home Office" to homeOfficeDays.toFloat()
                            )
                        )
                    } else {
                        NoDataPlaceholder()
                    }
                }

                itemsIndexed(infoCardItems) { index, item ->
                    val cardAppearance = when (index) {
                        // Office Days
                        0 -> InfoCardAppearance(Icons.Default.Business, MaterialTheme.colorScheme.primary)
                        1 -> InfoCardAppearance(Icons.Default.Home, Color(0xFF388E3C))

                        // Meal Costs
                        2 -> InfoCardAppearance(Icons.Default.Restaurant, Color(0xFFF57C00))
                        3 -> InfoCardAppearance(Icons.Default.CalendarToday, Color(0xFF7B1FA2))
                        4 -> InfoCardAppearance(Icons.AutoMirrored.Filled.TrendingUp, Color(0xFF1976D2))

                        // Travel Costs
                        5 -> InfoCardAppearance(Icons.Default.DirectionsCar, Color(0xFF512DA8))
                        6 -> InfoCardAppearance(Icons.Default.DirectionsBus, Color(0xFF00796B))
                        7 -> InfoCardAppearance(Icons.AutoMirrored.Filled.DirectionsWalk, Color(0xFF0288D1))

                        // Other Expenses
                        8 -> InfoCardAppearance(Icons.Default.AttachMoney, Color(0xFFC2185B))
                        9 -> InfoCardAppearance(Icons.Default.Savings, Color(0xFF7B1FA2))

                        // Total Expenses
                        10 -> InfoCardAppearance(Icons.Default.Calculate, MaterialTheme.colorScheme.error)
                        11 -> InfoCardAppearance(Icons.Default.ShowChart, Color(0xFFD32F2F))

                        else -> InfoCardAppearance(Icons.Default.Info, MaterialTheme.colorScheme.primary)
                    }

                    InfoCard(
                        title = item.first,
                        value = item.second,
                        icon = cardAppearance.icon,
                        iconColor = Color(0xFF673AB7),
                        animationDelay = index * 100
                    )
                }
            }
        }
    }
}

private data class InfoCardAppearance(val icon: ImageVector, val color: Color)

@Composable
fun MonthNavigator(month: String, onPrevious: () -> Unit, onNext: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onPrevious) {
            Icon(Icons.Default.ArrowBackIosNew, contentDescription = "Previous Month")
        }
        AnimatedContent(targetState = month, label = "Month Text") { targetMonth ->
            Text(
                text = targetMonth,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        IconButton(onClick = onNext) {
            Icon(Icons.AutoMirrored.Filled.ArrowForwardIos, contentDescription = "Next Month")
        }
    }
}

@Composable
fun SummaryHeaderCard(officeDays: Int, homeOfficeDays: Int, totalCost: Double) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "This Month's Summary",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                SummaryItem("Office", "$officeDays days")
                SummaryItem("Home", "$homeOfficeDays days")
                SummaryItem("Total", "${officeDays + homeOfficeDays} days")
                SummaryItem("Total Cost", String.format("%.0f Taka", totalCost), isCost = true)
            }
        }
    }
}

@Composable
private fun RowScope.SummaryItem(label: String, value: String, isCost: Boolean = false) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.weight(1f)
    ) {
        Text(text = label, style = MaterialTheme.typography.labelMedium)
        Spacer(Modifier.height(4.dp))
        Text(
            text = value,
            style = if (isCost) MaterialTheme.typography.titleMedium else MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun ModernTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    keyboardOptions: KeyboardOptions,
    keyboardActions: KeyboardActions
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
            focusedIndicatorColor = MaterialTheme.colorScheme.primary,
            unfocusedIndicatorColor = Color.Transparent,
        )
    )
}

@Composable
fun InfoCard(title: String, value: String, icon: ImageVector, iconColor: Color, animationDelay: Int) {
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(title) {
        delay(animationDelay.toLong())
        visible = true
    }

    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(durationMillis = 300), label = "alpha"
    )

    val translationY by animateDpAsState(
        targetValue = if (visible) 0.dp else 25.dp,
        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing), label = "translationY"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(alpha)
            .offset(y = translationY),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(imageVector = icon, contentDescription = title, tint = iconColor)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                color = iconColor,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
@Composable
fun WorkingDaysPieChart(data: Map<String, Float>) {
    val pieChartColors = listOf(
        MaterialTheme.colorScheme.primary,
        MaterialTheme.colorScheme.secondary
    )

    val officeValue = data["Office"] ?: 0f
    val homeValue = data["Home Office"] ?: 0f
    val totalValue = officeValue + homeValue
    val officePercentage = if (totalValue > 0) (officeValue / totalValue) * 100f else 0f

    val pieChartData = PieChartData(
        slices = data.entries.mapIndexed { index, entry ->
            PieChartData.Slice(entry.key, entry.value, pieChartColors[index % pieChartColors.size])
        },
        plotType = PlotType.Donut
    )

    val pieChartConfig = PieChartConfig(
        isAnimationEnable = true,
        showSliceLabels = true,
        sliceLabelTextSize = 12.sp,
        sliceLabelTextColor = MaterialTheme.colorScheme.onPrimary,
        strokeWidth = 35f,
        chartPadding = 25
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1.5f)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            PieChart(
                modifier = Modifier.fillMaxSize(),
                pieChartData = pieChartData,
                pieChartConfig = pieChartConfig
            )
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "%.0f%%".format(officePercentage),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Office",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun NoDataPlaceholder() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1.5f)
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerHigh),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "No work data for this month.\nLog your work to see calculations.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}
// Add @Preview annotations for preview
@Preview(showBackground = true)
@Composable
fun CalculationScreenPreview() {
    SmartWorkTrackerTheme {
        CalculationScreen(onNavigateBack = {})
    }
}