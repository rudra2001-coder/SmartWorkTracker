package com.rudra.smartworktracker.ui.screens.add_entry

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rudra.smartworktracker.model.ExpenseCategory
import com.rudra.smartworktracker.model.WorkType
import com.rudra.smartworktracker.ui.EntryType

private val CardShape = RoundedCornerShape(20.dp)
private val PillShape = RoundedCornerShape(50.dp)
private val ChipShape = RoundedCornerShape(12.dp)

private val EmeraldGreen = Color(0xFF00C896)
private val CoralRed = Color(0xFFFF5757)
private val SapphireBlue = Color(0xFF3B82F6)
private val GoldenAmber = Color(0xFFF59E0B)
private val VioletPurple = Color(0xFF8B5CF6)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEntryScreen(onNavigateBack: () -> Boolean) {
    val viewModel: AddEntryViewModel = viewModel(factory = AddEntryViewModel.Factory)
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Add New Entry",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 20.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().shadow(6.dp, CardShape, clip = false),
                    shape = CardShape,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(0.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .background(
                                    brush = Brush.linearGradient(listOf(SapphireBlue, VioletPurple)),
                                    shape = RoundedCornerShape(14.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(28.dp))
                        }
                        Spacer(Modifier.height(12.dp))
                        Text("Add New Entry", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(4.dp))
                        Text("Log your expenses, work time, or meals", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            item {
                EntryTypeSelector(uiState.selectedEntryType, onEntryTypeSelect = viewModel::onEntryTypeChange)
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth().shadow(6.dp, CardShape, clip = false),
                    shape = CardShape,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(0.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        when (uiState.selectedEntryType) {
                            EntryType.EXPENSE -> ExpenseEntryForm(
                                amount = uiState.expenseAmount,
                                onAmountChange = viewModel::onExpenseAmountChange,
                                category = uiState.expenseCategory,
                                onCategoryChange = viewModel::onExpenseCategoryChange,
                                notes = uiState.expenseNotes,
                                onNotesChange = viewModel::onExpenseNotesChange,
                                onSave = viewModel::saveExpense
                            )
                            EntryType.WORK_TIME -> WorkTimeEntryForm(
                                workType = uiState.workType,
                                onWorkTypeChange = viewModel::onWorkTypeChange,
                                startTime = uiState.workStartTime,
                                onStartTimeChange = viewModel::onWorkStartTimeChange,
                                endTime = uiState.workEndTime,
                                onEndTimeChange = viewModel::onWorkEndTimeChange,
                                onSave = viewModel::saveWorkLog
                            )
                            EntryType.MEAL -> MealEntryForm(
                                amount = uiState.mealAmount,
                                onAmountChange = viewModel::onMealAmountChange,
                                notes = uiState.mealNotes,
                                onNotesChange = viewModel::onMealNotesChange,
                                onSave = viewModel::saveMeal
                            )
                        }
                    }
                }
            }

            item { Spacer(Modifier.height(80.dp)) }
        }
    }
}

@Composable
fun EntryTypeSelector(selected: EntryType, onEntryTypeSelect: (EntryType) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().shadow(4.dp, CardShape, clip = false),
        shape = CardShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Entry Type", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(bottom = 10.dp, start = 2.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                EntryType.values().forEach { entryType ->
                    val isSelected = selected == entryType
                    val icon = when (entryType) {
                        EntryType.EXPENSE -> Icons.Default.MoneyOff
                        EntryType.WORK_TIME -> Icons.Default.Work
                        EntryType.MEAL -> Icons.Default.Restaurant
                    }
                    val color = when (entryType) {
                        EntryType.EXPENSE -> CoralRed
                        EntryType.WORK_TIME -> SapphireBlue
                        EntryType.MEAL -> GoldenAmber
                    }
                    Surface(
                        onClick = { onEntryTypeSelect(entryType) },
                        modifier = Modifier.weight(1f),
                        shape = ChipShape,
                        color = if (isSelected) color else color.copy(alpha = 0.08f),
                        border = null
                    ) {
                        Column(
                            modifier = Modifier.padding(vertical = 10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(icon, contentDescription = null, tint = if (isSelected) Color.White else color, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.height(4.dp))
                            Text(
                                entryType.name.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() },
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ExpenseEntryForm(
    amount: String,
    onAmountChange: (String) -> Unit,
    category: ExpenseCategory,
    onCategoryChange: (ExpenseCategory) -> Unit,
    notes: String,
    onNotesChange: (String) -> Unit,
    onSave: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(
                modifier = Modifier.size(32.dp).background(
                    brush = Brush.linearGradient(listOf(CoralRed, GoldenAmber)),
                    shape = RoundedCornerShape(10.dp)
                ),
                contentAlignment = Alignment.Center
            ) { Icon(Icons.Default.MoneyOff, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp)) }
            Text("Expense Details", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }

        val textFieldColors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = CoralRed,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
            focusedLabelColor = CoralRed,
            unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
            cursorColor = CoralRed
        )

        OutlinedTextField(
            value = amount,
            onValueChange = onAmountChange,
            label = { Text("Amount") },
            leadingIcon = { Icon(Icons.Default.MoneyOff, null, tint = CoralRed) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            shape = ChipShape,
            colors = textFieldColors,
            singleLine = true
        )

        Text("Category", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            val catList = ExpenseCategory.entries
            for (startIdx in catList.indices step 4) {
                val endIdx = minOf(startIdx + 4, catList.size)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    for (idx in startIdx until endIdx) {
                        val cat = catList[idx]
                        val isSel = category == cat
                        val catColor = when (cat) {
                            ExpenseCategory.MEAL -> EmeraldGreen
                            ExpenseCategory.TRANSPORT -> SapphireBlue
                            ExpenseCategory.SHOPPING -> VioletPurple
                            ExpenseCategory.ENTERTAINMENT -> GoldenAmber
                            ExpenseCategory.BILLS -> CoralRed
                            else -> MaterialTheme.colorScheme.primary
                        }
                        Surface(
                            onClick = { onCategoryChange(cat) },
                            modifier = Modifier.weight(1f),
                            shape = ChipShape,
                            color = if (isSel) catColor else catColor.copy(alpha = 0.08f),
                            border = null
                        ) {
                            Column(
                                modifier = Modifier.padding(vertical = 8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    Icons.Default.Circle,
                                    contentDescription = null,
                                    tint = if (isSel) Color.White else catColor,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(Modifier.height(2.dp))
                                Text(
                                    cat.name.lowercase().replaceFirstChar { it.uppercase() },
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSel) Color.White else MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                    val rem = 4 - (endIdx - startIdx)
                    if (rem > 0) { Spacer(Modifier.weight(rem.toFloat())) }
                }
            }
        }

        OutlinedTextField(
            value = notes,
            onValueChange = onNotesChange,
            label = { Text("Notes") },
            leadingIcon = { Icon(Icons.Default.Notes, null, tint = CoralRed) },
            modifier = Modifier.fillMaxWidth(),
            shape = ChipShape,
            colors = textFieldColors,
            maxLines = 3
        )

        Button(
            onClick = onSave,
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape = ChipShape,
            colors = ButtonDefaults.buttonColors(containerColor = CoralRed)
        ) {
            Icon(Icons.Default.Save, null, tint = Color.White, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("Save Expense", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun WorkTimeEntryForm(
    workType: WorkType,
    onWorkTypeChange: (WorkType) -> Unit,
    startTime: String,
    onStartTimeChange: (String) -> Unit,
    endTime: String,
    onEndTimeChange: (String) -> Unit,
    onSave: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(
                modifier = Modifier.size(32.dp).background(
                    brush = Brush.linearGradient(listOf(SapphireBlue, VioletPurple)),
                    shape = RoundedCornerShape(10.dp)
                ),
                contentAlignment = Alignment.Center
            ) { Icon(Icons.Default.Work, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp)) }
            Text("Work Time Details", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }

        val textFieldColors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = SapphireBlue,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
            focusedLabelColor = SapphireBlue,
            unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
            cursorColor = SapphireBlue
        )

        Text("Work Type", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            WorkType.values().forEach { type ->
                val isSel = workType == type
                val wtColor = when (type) {
                    WorkType.OFFICE -> SapphireBlue
                    WorkType.HOME_OFFICE -> EmeraldGreen
                    WorkType.OFF_DAY -> VioletPurple
                    WorkType.EXTRA_WORK -> GoldenAmber
                    WorkType.OVERTIME -> CoralRed
                }
                Surface(
                    onClick = { onWorkTypeChange(type) },
                    modifier = Modifier.weight(1f),
                    shape = ChipShape,
                    color = if (isSel) wtColor else wtColor.copy(alpha = 0.08f),
                    border = null
                ) {
                    Text(
                        type.name.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() },
                        modifier = Modifier.padding(vertical = 8.dp).fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSel) Color.White else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        OutlinedTextField(
            value = startTime,
            onValueChange = onStartTimeChange,
            label = { Text("Start Time") },
            leadingIcon = { Icon(Icons.Default.Schedule, null, tint = SapphireBlue) },
            modifier = Modifier.fillMaxWidth(),
            shape = ChipShape,
            colors = textFieldColors,
            singleLine = true
        )

        OutlinedTextField(
            value = endTime,
            onValueChange = onEndTimeChange,
            label = { Text("End Time") },
            leadingIcon = { Icon(Icons.Default.Schedule, null, tint = SapphireBlue) },
            modifier = Modifier.fillMaxWidth(),
            shape = ChipShape,
            colors = textFieldColors,
            singleLine = true
        )

        Button(
            onClick = onSave,
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape = ChipShape,
            colors = ButtonDefaults.buttonColors(containerColor = SapphireBlue)
        ) {
            Icon(Icons.Default.Save, null, tint = Color.White, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("Save Work Log", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun MealEntryForm(
    amount: String,
    onAmountChange: (String) -> Unit,
    notes: String,
    onNotesChange: (String) -> Unit,
    onSave: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(
                modifier = Modifier.size(32.dp).background(
                    brush = Brush.linearGradient(listOf(GoldenAmber, CoralRed)),
                    shape = RoundedCornerShape(10.dp)
                ),
                contentAlignment = Alignment.Center
            ) { Icon(Icons.Default.Restaurant, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp)) }
            Text("Meal Details", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }

        val textFieldColors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = GoldenAmber,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
            focusedLabelColor = GoldenAmber,
            unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
            cursorColor = GoldenAmber
        )

        OutlinedTextField(
            value = amount,
            onValueChange = onAmountChange,
            label = { Text("Amount") },
            leadingIcon = { Icon(Icons.Default.MoneyOff, null, tint = GoldenAmber) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            shape = ChipShape,
            colors = textFieldColors,
            singleLine = true
        )

        OutlinedTextField(
            value = notes,
            onValueChange = onNotesChange,
            label = { Text("Notes") },
            leadingIcon = { Icon(Icons.Default.Notes, null, tint = GoldenAmber) },
            modifier = Modifier.fillMaxWidth(),
            shape = ChipShape,
            colors = textFieldColors,
            maxLines = 3
        )

        Button(
            onClick = onSave,
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape = ChipShape,
            colors = ButtonDefaults.buttonColors(containerColor = GoldenAmber)
        ) {
            Icon(Icons.Default.Save, null, tint = Color.White, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("Save Meal", fontWeight = FontWeight.Bold)
        }
    }
}
