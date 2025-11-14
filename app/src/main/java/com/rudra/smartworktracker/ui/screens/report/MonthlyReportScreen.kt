package com.rudra.smartworktracker.ui.screens.report

import android.app.Application
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            TextField(
                value = uiState.selectedMonth,
                onValueChange = {},
                readOnly = true,
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                },
                modifier = Modifier.menuAnchor().fillMaxWidth()
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

        Spacer(modifier = Modifier.height(16.dp))

        if (uiState.workLogs.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No data available for ${uiState.selectedMonth}")
            }
        } else {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Work Type Breakdown", fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))

                    val pieChartData = PieChartData(
                        slices = listOf(
                            PieChartData.Slice("Office", uiState.officeCount.toFloat(), Color(0xFF58BDFF)),
                            PieChartData.Slice("Home", uiState.homeCount.toFloat(), Color(0xFF1266F1)),
                            PieChartData.Slice("Off", uiState.offCount.toFloat(), Color(0xFF00B74A)),
                            PieChartData.Slice("Extra", uiState.extraCount.toFloat(), Color(0xFFF93154))
                        ).filter { it.value > 0 },
                        plotType = PlotType.Pie
                    )

                    val pieChartConfig = PieChartConfig(
                        strokeWidth = 120f,
                        activeSliceAlpha = .9f,
                        isAnimationEnable = true,
                        labelVisible = true,
                        labelColor = Color.Black,
                        labelFontSize = 14.sp
                    )
                    PieChart(
                        modifier = Modifier.size(200.dp),
                        pieChartData = pieChartData,
                        pieChartConfig = pieChartConfig
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Total Summary", fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Total Days Worked: ${uiState.officeCount + uiState.homeCount + uiState.extraCount}")
                    Text("Total Days Off: ${uiState.offCount}")
                    Text("Total Logs: ${uiState.workLogs.size}")
                }
            }
        }
    }
}
