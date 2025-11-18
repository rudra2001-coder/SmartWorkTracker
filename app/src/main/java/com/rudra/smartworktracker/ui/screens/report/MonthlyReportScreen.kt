package com.rudra.smartworktracker.ui.screens.report

import android.app.Application
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import co.yml.charts.common.model.PlotType
import co.yml.charts.ui.piechart.charts.PieChart
import co.yml.charts.ui.piechart.models.PieChartConfig
import co.yml.charts.ui.piechart.models.PieChartData

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonthlyReportScreen(onNavigateBack: () -> Unit) {
    val application = LocalContext.current.applicationContext as Application
    val viewModel: MonthlyReportViewModel = viewModel(factory = MonthlyReportViewModelFactory(application))
    val uiState by viewModel.uiState.collectAsState()
    var expanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Monthly Report") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Month Selector
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                TextField(
                    value = uiState.selectedMonth,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Select Month") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                    },
                    colors = ExposedDropdownMenuDefaults.textFieldColors(),
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    viewModel.months.forEach { month ->
                        DropdownMenuItem(
                            text = { Text(text = month) },
                            onClick = {
                                viewModel.onMonthSelected(month)
                                expanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (uiState.workLogs.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No data available for ${uiState.selectedMonth}",
                        fontSize = 16.sp,
                        color = Color.Gray
                    )
                }
            } else {
                // Pie Chart Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Work Type Distribution",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        val totalDays = uiState.officeCount + uiState.homeCount + uiState.offCount + uiState.extraCount

                        if (totalDays > 0) {
                            val pieChartData = PieChartData(
                                slices = listOf(
                                    PieChartData.Slice("Office", uiState.officeCount.toFloat(), Color(0xFF58BDFF)),
                                    PieChartData.Slice("Home Office", uiState.homeCount.toFloat(), Color(0xFF1266F1)),
                                    PieChartData.Slice("Off Days", uiState.offCount.toFloat(), Color(0xFF00B74A)),
                                    PieChartData.Slice("Extra Work", uiState.extraCount.toFloat(), Color(0xFFF93154))
                                ).filter { it.value > 0 },
                                plotType = PlotType.Pie
                            )

                            val pieChartConfig = PieChartConfig(
                                strokeWidth = 120f,
                                activeSliceAlpha = 0.9f,
                                isAnimationEnable = true,
                                labelVisible = true,
                                labelColor = Color.Black,
                                labelFontSize = 14.sp,
                                showSliceLabels = true
                            )

                            PieChart(
                                modifier = Modifier.size(250.dp),
                                pieChartData = pieChartData,
                                pieChartConfig = pieChartConfig
                            )
                        } else {
                            Text(
                                text = "No work data available",
                                fontSize = 14.sp,
                                color = Color.Gray
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Summary Statistics Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Monthly Summary",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Work Type Breakdown
                        Text(
                            text = "Work Type Breakdown:",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        WorkTypeItem("Office Days", uiState.officeCount, Color(0xFF58BDFF))
                        WorkTypeItem("Home Office Days", uiState.homeCount, Color(0xFF1266F1))
                        WorkTypeItem("Off Days", uiState.offCount, Color(0xFF00B74A))
                        WorkTypeItem("Extra Work Days", uiState.extraCount, Color(0xFFF93154))

                        Spacer(modifier = Modifier.height(16.dp))

                        Divider()

                        Spacer(modifier = Modifier.height(16.dp))

                        // Totals
                        Text(
                            text = "Totals:",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        val totalWorkDays = uiState.officeCount + uiState.homeCount + uiState.extraCount
                        val totalDays = totalWorkDays + uiState.offCount

                        SummaryItem("Total Days Tracked", totalDays.toString())
                        SummaryItem("Total Work Days", totalWorkDays.toString())
                        SummaryItem("Total Off Days", uiState.offCount.toString())
                        SummaryItem("Total Logs", uiState.workLogs.size.toString())
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Quick Stats Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Quick Stats",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )

                        Spacer(modifier = Modifier.height(12.dp))

//                        Row(
//                            modifier = Modifier.fillMaxWidth(),
//                            horizontalArrangement = Arrangement.SpaceBetween
//                        ) {
//                            QuickStatItem("Most Frequent", getMostFrequentWorkType(uiState))
//                            QuickStatItem("Work Rate",
////                                if (totalDays > 0) "${((totalWorkDays.toDouble() / totalDays) * 100).toInt()}%"
////                                else "N/A"
////                            )
//                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun WorkTypeItem(label: String, count: Int, color: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(color, shape = MaterialTheme.shapes.small)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = label, fontSize = 14.sp)
        }
        Text(
            text = count.toString(),
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun SummaryItem(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 14.sp)
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun QuickStatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = Color.Gray
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

private fun getMostFrequentWorkType(uiState: MonthlyReportUiState): String {
    val workTypes = listOf(
        "Office" to uiState.officeCount,
        "Home" to uiState.homeCount,
        "Off" to uiState.offCount,
        "Extra" to uiState.extraCount
    )

    val max = workTypes.maxByOrNull { it.second }
    return if (max != null && max.second > 0) max.first else "N/A"
}