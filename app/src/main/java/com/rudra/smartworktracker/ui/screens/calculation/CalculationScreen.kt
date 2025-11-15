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
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.ViewWeek
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
    val selectedDate by viewModel.selectedDate.collectAsState()
    val mealCostPerWeek by viewModel.mealCostPerWeek.collectAsState()
    val mealCostPerMonth by viewModel.mealCostPerMonth.collectAsState()
    val mealCostPerYear by viewModel.mealCostPerYear.collectAsState()
    val officeDays by viewModel.officeDays.collectAsState()
    val homeOfficeDays by viewModel.homeOfficeDays.collectAsState()

    var dailyMealRate by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current

    val monthYearFormat = remember { SimpleDateFormat("MMMM yyyy", Locale.getDefault()) }

    LaunchedEffect(calculation) {
        calculation?.let {
            if (dailyMealRate.toDoubleOrNull() != it.dailyMealRate) {
                dailyMealRate = it.dailyMealRate.toString()
            }
        }
    }

    val infoCardItems = remember(officeDays, homeOfficeDays, mealCostPerWeek, mealCostPerMonth, mealCostPerYear) {
        listOf(
            "Office Days" to "$officeDays days",
            "Home Office Days" to "$homeOfficeDays days",
            "Est. Weekly Meal Cost" to String.format("%.2f Taka", mealCostPerWeek),
            "Monthly Meal Cost" to String.format("%.2f Taka", mealCostPerMonth),
            "Projected Yearly Cost" to String.format("%.2f Taka", mealCostPerYear)
        )
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
                        totalCost = mealCostPerMonth
                    )
                }

                item {
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
                    val primaryColor = MaterialTheme.colorScheme.primary
                    val errorColor = MaterialTheme.colorScheme.error
                    val cardData = when (index) {
                        0 -> InfoCardData(Icons.Default.Business, primaryColor)
                        1 -> InfoCardData(Icons.Default.Home, Color(0xFF388E3C))
                        2 -> InfoCardData(Icons.Default.ViewWeek, Color(0xFFF57C00))
                        3 -> InfoCardData(Icons.Default.CalendarToday, Color(0xFF7B1FA2))
                        else -> InfoCardData(Icons.AutoMirrored.Filled.TrendingUp, errorColor)
                    }
                    InfoCard(
                        title = item.first,
                        value = item.second,
                        icon = cardData.icon,
                        iconColor = cardData.color,
                        animationDelay = index * 100
                    )
                }
            }
        }
    }
}

private data class InfoCardData(val icon: ImageVector, val color: Color)

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
            Text(text = targetMonth, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
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
            Text("This Month's Summary", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                SummaryItem("Office", "$officeDays days")
                SummaryItem("Home", "$homeOfficeDays days")
                SummaryItem("Total", "${officeDays + homeOfficeDays} days")
                SummaryItem("Cost", String.format("%.0f Taka", totalCost), isCost = true)
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
                Text(text = title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
            }
            Text(text = value, style = MaterialTheme.typography.bodyLarge, color = iconColor, fontWeight = FontWeight.Bold)
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
                .height(200.dp)
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
            .height(200.dp)
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
