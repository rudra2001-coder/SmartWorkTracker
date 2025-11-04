package com.rudra.smartworktracker.ui.screens.report

import android.content.ContentValues
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import co.yml.charts.common.model.PlotType
import co.yml.charts.ui.piechart.charts.PieChart
import co.yml.charts.ui.piechart.models.PieChartConfig
import co.yml.charts.ui.piechart.models.PieChartData
import com.rudra.smartworktracker.data.SharedPreferenceManager
import com.rudra.smartworktracker.model.WorkType
import java.io.OutputStream
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonthlyReportScreen(onNavigateBack: () -> Unit) {
    val context = LocalContext.current
    val sharedPreferenceManager = remember { SharedPreferenceManager(context) }
    val workLogs = remember { sharedPreferenceManager.getWorkLogs() }

    val calendar = Calendar.getInstance()
    val months = (0..11).map {
        calendar.set(Calendar.MONTH, it)
        calendar.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault())
    }

    var expanded by remember { mutableStateOf(false) }
    var selectedMonth by remember { mutableStateOf(months[Calendar.getInstance().get(Calendar.MONTH)]) }

    val filteredLogs = workLogs.filter {
        val logCalendar = Calendar.getInstance()
        logCalendar.time = it.date
        logCalendar.get(Calendar.MONTH) == months.indexOf(selectedMonth)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            if (selectedMonth != null) {
                TextField(
                    value = selectedMonth,
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                    },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
            }
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                months.forEach { month ->
                    DropdownMenuItem(
                        text = { Text(text = month) },
                        onClick = {
                            selectedMonth = month
                            expanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (filteredLogs.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No data available for $selectedMonth")
            }
        } else {
            val officeCount = filteredLogs.count { it.workType == WorkType.OFFICE }
            val homeCount = filteredLogs.count { it.workType == WorkType.HOME_OFFICE }
            val offCount = filteredLogs.count { it.workType == WorkType.OFF_DAY }
            val extraCount = filteredLogs.count { it.workType == WorkType.EXTRA_WORK }

            // Work Type Breakdown
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Work Type Breakdown", fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))

                    val pieChartData = PieChartData(
                        slices = listOf(
                            PieChartData.Slice("Office", officeCount.toFloat(), Color(0xFF58BDFF)),
                            PieChartData.Slice("Home", homeCount.toFloat(), Color(0xFF1266F1)),
                            PieChartData.Slice("Off", offCount.toFloat(), Color(0xFF00B74A)),
                            PieChartData.Slice("Extra", extraCount.toFloat(), Color(0xFFF93154))
                        ).filter{it.value > 0},
                        plotType = PlotType.Pie
                    )

                    val pieChartConfig = PieChartConfig(
                        strokeWidth = 120f,
                        activeSliceAlpha = .9f,
                        isAnimationEnable = true,
                        labelVisible = true,
                        labelColor = Color.Black,
                       // labelTypeface = FontWeight.Bold,
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

            // Total Summary
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Total Summary", fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Total Days Worked: ${officeCount + homeCount + extraCount}")
                    Text("Total Days Off: $offCount")
                    Text("Total Logs: ${filteredLogs.size}")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(onClick = {
                val report = createReport(selectedMonth, officeCount, homeCount, offCount, extraCount, filteredLogs.size)
                saveReport(context, "Monthly_Report_$selectedMonth.txt", report)
            }, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Default.Share, contentDescription = "Export")
                Spacer(modifier = Modifier.size(8.dp))
                Text("Export Report")
            }
        }
    }
}

private fun createReport(
    month: String, office: Int, home: Int, off: Int, extra: Int, total: Int
): String {
    return """
    Monthly Report for $month
    ---------------------------------
    Work Type Breakdown:
    - Office: $office
    - Home: $home
    - Off: $off
    - Extra: $extra

    Total Summary:
    - Total Days Worked: ${office + home + extra}
    - Total Days Off: $off
    - Total Logs: $total
    """
}

private fun saveReport(context: android.content.Context, fileName: String, content: String) {
    val resolver = context.contentResolver
    val contentValues = ContentValues().apply {
        put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
        put(MediaStore.MediaColumns.MIME_TYPE, "text/plain")
        put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOCUMENTS)
    }

    var stream: OutputStream? = null
    var uri = resolver.insert(MediaStore.Files.getContentUri("external"), contentValues)
    try {
        if(uri == null) {
            uri = MediaStore.Files.getContentUri("external")
        }
        stream = resolver.openOutputStream(uri!!)
        stream?.write(content.toByteArray())
        Toast.makeText(context, "Report saved to Documents", Toast.LENGTH_SHORT).show()
    } catch (e: Exception) {
        Toast.makeText(context, "Failed to save report: ${e.message}", Toast.LENGTH_LONG).show()
    } finally {
        stream?.close()
    }
}
