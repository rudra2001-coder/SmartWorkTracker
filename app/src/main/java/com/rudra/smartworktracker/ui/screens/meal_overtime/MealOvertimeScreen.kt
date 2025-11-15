package com.rudra.smartworktracker.ui.screens.meal_overtime

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.rudra.smartworktracker.data.entity.MonthlyInput
import com.rudra.smartworktracker.data.entity.MonthlySummary
import com.rudra.smartworktracker.data.entity.Settings
import com.rudra.smartworktracker.data.repository.FirstWeekData
import java.time.LocalDate

@Composable
fun MealOvertimeScreen(viewModel: MealOvertimeViewModel, navController: NavController) {
    val uiState by viewModel.uiState.collectAsState()
    val settings by viewModel.settings.collectAsState()

    LaunchedEffect(Unit) {
        val year = LocalDate.now().year.toString()
        val month = LocalDate.now().monthValue.toString().padStart(2, '0')
        viewModel.initializeMonth(year, month)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Meal & Overtime Tracker",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        when (val currentState = uiState) {
            is MealOvertimeUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            is MealOvertimeUiState.FirstTimeSetup -> {
                FirstWeekInputScreen(
                    onCalculate = {
                        val year = LocalDate.now().year.toString()
                        val month = LocalDate.now().monthValue.toString().padStart(2, '0')
                        viewModel.calculateFromFirstWeek(year, month, it)
                    }
                )
            }

            is MealOvertimeUiState.Success -> {
                SuccessScreen(
                    summary = currentState.summary,
                    input = currentState.input,
                    settings = settings,
                    onUpdateInput = viewModel::updateMonthlyInput,
                    onUpdateSettings = viewModel::updateSettings,
                    navController = navController
                )
            }

            is MealOvertimeUiState.Error -> {
                ErrorScreen(message = currentState.message)
            }
        }
    }
}

@Composable
fun FirstWeekInputScreen(onCalculate: (FirstWeekData) -> Unit) {
    var workingDays by remember { mutableStateOf("5") }
    var totalMeals by remember { mutableStateOf("") }
    var totalOvertime by remember { mutableStateOf("") }

    Card(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "First Week Data Input",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                "Enter your first week data to auto-calculate for the whole month.",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )

            OutlinedTextField(
                value = workingDays,
                onValueChange = { workingDays = it },
                label = { Text("Working days in first week") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            OutlinedTextField(
                value = totalMeals,
                onValueChange = { totalMeals = it },
                label = { Text("Total meals in first week") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            OutlinedTextField(
                value = totalOvertime,
                onValueChange = { totalOvertime = it },
                label = { Text("Total overtime hours in first week") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            Button(
                onClick = {
                    val firstWeekData = FirstWeekData(
                        workingDaysInFirstWeek = workingDays.toIntOrNull() ?: 5,
                        totalMeals = totalMeals.toIntOrNull() ?: 0,
                        totalOvertimeHours = totalOvertime.toFloatOrNull() ?: 0f
                    )
                    onCalculate(firstWeekData)
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("Calculate For Whole Month", color = Color.White)
            }
        }
    }
}

@Composable
fun SuccessScreen(
    summary: MonthlySummary,
    input: MonthlyInput,
    settings: Settings?,
    onUpdateInput: (MonthlyInput) -> Unit,
    onUpdateSettings: (Settings) -> Unit,
    navController: NavController
) {
    var showSettings by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        SummaryCard(summary)

        EditableInputCard(input = input, onUpdateInput = onUpdateInput)

        Button(
            onClick = { showSettings = true },
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
        ) {
            Icon(Icons.Default.Settings, contentDescription = "Settings", tint = MaterialTheme.colorScheme.onSecondary)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Settings", color = MaterialTheme.colorScheme.onSecondary)
        }
    }

    if (showSettings) {
        settings?.let {
            SettingsDialog(
                settings = it,
                onUpdateSettings = onUpdateSettings,
                onDismiss = { showSettings = false }
            )
        }
    }
}

@Composable
fun SummaryCard(summary: MonthlySummary) {
    Card(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Text(
                "📊 Monthly Summary",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(12.dp))

            SummaryRow("Total Working Days", summary.totalWorkDays.toString())
            SummaryRow("Total Meals", summary.totalMeals.toString())
            SummaryRow("Meal Cost", "%.2f BDT".format(summary.totalMealCost))
            SummaryRow("Overtime Hours", "%.1f hours".format(summary.totalOvertimeHours))
            SummaryRow("Overtime Pay", "%.2f BDT".format(summary.totalOvertimePay))

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            SummaryRow(
                "Total Expense",
                "%.2f BDT".format(summary.totalExpense),
                textColor = MaterialTheme.colorScheme.error,
                isBold = true
            )
        }
    }
}

@Composable
fun EditableInputCard(input: MonthlyInput, onUpdateInput: (MonthlyInput) -> Unit) {
    Card(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "✏️ Adjust Values",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                "Customize based on sick leaves, holidays, or special circumstances",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )

            EditableRow(
                label = "Working Days",
                value = input.totalWorkingDays.toString()
            ) { newValue ->
                onUpdateInput(
                    input.copy(
                        totalWorkingDays = newValue.toIntOrNull() ?: input.totalWorkingDays,
                        isAutoCalculated = false
                    )
                )
            }

            EditableRow(
                label = "Total Meals",
                value = input.totalMeals.toString()
            ) { newValue ->
                onUpdateInput(
                    input.copy(
                        totalMeals = newValue.toIntOrNull() ?: input.totalMeals,
                        isAutoCalculated = false
                    )
                )
            }

            EditableRow(
                label = "Overtime Hours",
                value = "%.1f".format(input.totalOvertimeHours)
            ) { newValue ->
                onUpdateInput(
                    input.copy(
                        totalOvertimeHours = newValue.toDoubleOrNull() ?: input.totalOvertimeHours,
                        isAutoCalculated = false
                    )
                )
            }
        }
    }
}

@Composable
fun EditableRow(label: String, value: String, onValueChange: (String) -> Unit) {
    var text by remember { mutableStateOf(value) }
    var isEditing by remember { mutableStateOf(false) }

    LaunchedEffect(value) {
        text = value
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .animateContentSize(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            label,
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurface
        )

        if (isEditing) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    modifier = Modifier.width(100.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
                IconButton(
                    onClick = {
                        isEditing = false
                        onValueChange(text)
                    }
                ) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = "Save",
                        tint = Color.Green
                    )
                }
            }
        } else {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { isEditing = true }
            ) {
                Text(
                    value,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                Icon(
                    Icons.Default.Edit,
                    contentDescription = "Edit",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .size(20.dp)
                        .padding(start = 4.dp)
                )
            }
        }
    }
}

@Composable
fun SettingsDialog(
    settings: Settings,
    onUpdateSettings: (Settings) -> Unit,
    onDismiss: () -> Unit
) {
    var mealRate by remember { mutableStateOf(settings.mealRate.toString()) }
    var overtimeRate by remember { mutableStateOf(settings.overtimeRate.toString()) }
    var dailyHours by remember { mutableStateOf(settings.dailyWorkHours.toString()) }
    var workingDays by remember { mutableStateOf(settings.workingDaysPerWeek.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Settings") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = mealRate,
                    onValueChange = { mealRate = it },
                    label = { Text("Meal Rate (BDT)") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
                )
                OutlinedTextField(
                    value = overtimeRate,
                    onValueChange = { overtimeRate = it },
                    label = { Text("Overtime Rate (BDT/hour)") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
                )
                OutlinedTextField(
                    value = dailyHours,
                    onValueChange = { dailyHours = it },
                    label = { Text("Daily Work Hours") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
                )
                OutlinedTextField(
                    value = workingDays,
                    onValueChange = { workingDays = it },
                    label = { Text("Working Days Per Week") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onUpdateSettings(
                        settings.copy(
                            mealRate = mealRate.toDoubleOrNull() ?: settings.mealRate,
                            overtimeRate = overtimeRate.toDoubleOrNull() ?: settings.overtimeRate,
                            dailyWorkHours = dailyHours.toDoubleOrNull() ?: settings.dailyWorkHours,
                            workingDaysPerWeek = workingDays.toIntOrNull() ?: settings.workingDaysPerWeek
                        )
                    )
                    onDismiss()
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun SummaryRow(label: String, value: String, textColor: Color = MaterialTheme.colorScheme.onSurface, isBold: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
        Text(
            value,
            fontSize = 14.sp,
            color = textColor,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
fun ErrorScreen(message: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                Icons.Default.Error,
                contentDescription = "Error",
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(48.dp)
            )
            Text("Error: $message", color = MaterialTheme.colorScheme.error)
        }
    }
}
