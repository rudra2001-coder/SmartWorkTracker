package com.rudra.smartworktracker.ui.screens.calculation

import androidx.compose.animation.animateContentSize
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import co.yml.charts.common.model.PlotType
import co.yml.charts.ui.piechart.charts.PieChart
import co.yml.charts.ui.piechart.models.PieChartConfig
import co.yml.charts.ui.piechart.models.PieChartData
import com.rudra.smartworktracker.ui.theme.SmartWorkTrackerTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalculationScreen(onNavigateBack: () -> Unit) {
    val context = LocalContext.current
    val viewModel: CalculationViewModel = viewModel(factory = CalculationViewModelFactory(context))
    val calculation by viewModel.calculation.collectAsState()
    val mealRatePerDay by viewModel.mealRatePerDay.collectAsState()
    val mealCostPerWeek by viewModel.mealCostPerWeek.collectAsState()
    val mealCostPerMonth by viewModel.mealCostPerMonth.collectAsState()
    val mealCostPerYear by viewModel.mealCostPerYear.collectAsState()
    val totalOfficeDays by viewModel.totalOfficeDays.collectAsState()

    var mealRate by remember { mutableStateOf("") }
    var overtimeRate by remember { mutableStateOf("") }
    var mealCost by remember { mutableStateOf("") }
    var totalWorkingDays by remember { mutableStateOf("") }
    var homeOfficeDays by remember { mutableStateOf("") }

    LaunchedEffect(calculation) {
        calculation?.let {
            mealRate = it.mealRate.toString()
            overtimeRate = it.overtimeRate.toString()
            mealCost = it.mealCost.toString()
            totalWorkingDays = it.totalWorkingDays.toString()
            homeOfficeDays = it.homeOfficeDays.toString()
        }
    }

    SmartWorkTrackerTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Meal & Overtime Rates", color = Color.White) },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                )
            }
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .padding(paddingValues)
                    .padding(16.dp)
                    .animateContentSize()
            ) {
                item {
                    Column {
                        ModernTextField(
                            value = mealCost,
                            onValueChange = { mealCost = it },
                            label = "Total Meal Cost",
                            keyboardType = KeyboardType.Number
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        ModernTextField(
                            value = totalWorkingDays,
                            onValueChange = { totalWorkingDays = it },
                            label = "Total Working Days",
                            keyboardType = KeyboardType.Number
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        ModernTextField(
                            value = homeOfficeDays,
                            onValueChange = { homeOfficeDays = it },
                            label = "Home Office Days",
                            keyboardType = KeyboardType.Number
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = {
                                viewModel.saveCalculation(
                                    mealRate = mealRate.toDoubleOrNull() ?: 0.0,
                                    overtimeRate = overtimeRate.toDoubleOrNull() ?: 0.0,
                                    mealCost = mealCost.toDoubleOrNull() ?: 0.0,
                                    totalWorkingDays = totalWorkingDays.toIntOrNull() ?: 0,
                                    homeOfficeDays = homeOfficeDays.toIntOrNull() ?: 0
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Calculate", fontSize = 16.sp)
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }

                item {
                    Column {
                        InfoCard("Meal Rate per Day", String.format("%.2f", mealRatePerDay))
                        InfoCard("Meal Cost per Week", String.format("%.2f", mealCostPerWeek))
                        InfoCard("Meal Cost per Month", String.format("%.2f", mealCostPerMonth))
                        InfoCard("Meal Cost per Year", String.format("%.2f", mealCostPerYear))
                        InfoCard("Total Office Days", "$totalOfficeDays days")
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(24.dp))
                    val officeDays = totalOfficeDays.toFloat()
                    val homeDays = (totalWorkingDays.toIntOrNull() ?: 0) - totalOfficeDays
                    if (officeDays > 0 || homeDays > 0) {
                        PieChart(
                            data = mapOf(
                                "Office Days" to officeDays,
                                "Home Office" to homeDays.toFloat()
                            )
                        )
                    }
                }
            }
        }
    }
}


@Composable
fun ModernTextField(value: String, onValueChange: (String) -> Unit, label: String, keyboardType: KeyboardType) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = TextFieldDefaults.colors(
            focusedIndicatorColor = MaterialTheme.colorScheme.primary,
            unfocusedIndicatorColor = Color.LightGray,
        )
    )
}

@Composable
fun InfoCard(title: String, value: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text(text = value, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
fun PieChart(data: Map<String, Float>) {
    val pieChartData = PieChartData(
        slices = data.map { (label, value) ->
            PieChartData.Slice(label, value, randomColor())
        },
        plotType = PlotType.Pie
    )
    val pieChartConfig = PieChartConfig(
        isAnimationEnable = true,
        showSliceLabels = true,
        sliceLabelTextSize = 14.sp,
        sliceLabelTextColor = Color.Black,
        strokeWidth = 120f,
        
    )
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(250.dp)
    ) {
        PieChart(
            modifier = Modifier.fillMaxSize(),
            pieChartData = pieChartData,
            pieChartConfig = pieChartConfig
        )
    }
}

fun randomColor(): Color {
    return Color(
        red = (0..255).random(),
        green = (0..255).random(),
        blue = (0..255).random()
    )
}
