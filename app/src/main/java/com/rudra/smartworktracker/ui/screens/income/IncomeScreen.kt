package com.rudra.smartworktracker.ui.screens.income

import android.app.Application
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.sp
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rudra.smartworktracker.data.entity.AccountType
import com.rudra.smartworktracker.data.entity.IncomeCategories
import com.rudra.smartworktracker.data.entity.Account
import java.text.SimpleDateFormat
import java.util.*

private val CardShape = RoundedCornerShape(20.dp)
private val ChipShape = RoundedCornerShape(12.dp)

private val EmeraldGreen = Color(0xFF00C896)
private val CoralRed = Color(0xFFFF5757)
private val SapphireBlue = Color(0xFF3B82F6)
private val GoldenAmber = Color(0xFFF59E0B)
private val VioletPurple = Color(0xFF8B5CF6)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IncomeScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val application = context.applicationContext as Application
    val viewModel: IncomeViewModel = viewModel(factory = IncomeViewModelFactory(application))
    var incomeInput by remember { mutableStateOf(TextFieldValue("")) }
    var descriptionInput by remember { mutableStateOf(TextFieldValue("")) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val savedIncome by viewModel.income.collectAsState()
    val recentIncomes by viewModel.recentIncomes.collectAsState()
    val accounts by viewModel.accounts.collectAsState()
    var selectedDate by remember { mutableStateOf<Date?>(null) }
    var selectedAccount by remember(accounts) {
        mutableStateOf(accounts.find { it.name == "Cash" } ?: accounts.firstOrNull())
    }
    var showDatePicker by remember { mutableStateOf(false) }

    val incomeCategories = IncomeCategories.categories
    var expanded by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf(incomeCategories[0]) }
    val accountTypes = AccountType.values()
    var accountTypeExpanded by remember { mutableStateOf(false) }
    var selectedAccountType by remember { mutableStateOf<AccountType?>(null) }

    val textFieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = EmeraldGreen,
        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
        focusedLabelColor = EmeraldGreen,
        unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
        focusedTextColor = MaterialTheme.colorScheme.onSurface,
        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
        cursorColor = EmeraldGreen
    )

    val animatedBalance by animateIntAsState(
        targetValue = savedIncome.toInt(),
        animationSpec = tween(1000),
        label = "income_anim"
    )

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {}
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 20.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier.size(52.dp).background(
                            brush = Brush.linearGradient(listOf(EmeraldGreen, SapphireBlue)),
                            shape = RoundedCornerShape(14.dp)
                        ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.AttachMoney, contentDescription = null, tint = Color.White, modifier = Modifier.size(28.dp))
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Log Your Income", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        Text("Track and manage your earnings", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth().shadow(6.dp, CardShape, clip = false),
                    shape = CardShape,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(0.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text("Income Amount", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(bottom = 8.dp))
                        OutlinedTextField(
                            value = incomeInput,
                            onValueChange = { incomeInput = it; errorMessage = null },
                            label = { Text("Enter amount") },
                            leadingIcon = { Icon(Icons.Default.AttachMoney, "Amount", tint = EmeraldGreen) },
                            modifier = Modifier.fillMaxWidth(),
                            isError = errorMessage != null,
                            supportingText = { errorMessage?.let { Text(it) } },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = ChipShape,
                            colors = textFieldColors,
                            singleLine = true
                        )
                        Spacer(Modifier.height(12.dp))

                        Text("Description", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(bottom = 8.dp))
                        OutlinedTextField(
                            value = descriptionInput,
                            onValueChange = { descriptionInput = it },
                            label = { Text("Optional description") },
                            leadingIcon = { Icon(Icons.Default.Description, "Description", tint = EmeraldGreen) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = ChipShape,
                            colors = textFieldColors,
                            singleLine = true
                        )
                        Spacer(Modifier.height(12.dp))

                        Text("Category", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(bottom = 8.dp))
                        ExposedDropdownMenuBox(
                            expanded = expanded,
                            onExpandedChange = { expanded = !expanded },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                modifier = Modifier.fillMaxWidth().menuAnchor(),
                                readOnly = true,
                                value = selectedCategory,
                                onValueChange = {},
                                label = { Text("Select category") },
                                leadingIcon = { Icon(Icons.Default.Category, "Category", tint = EmeraldGreen) },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                                colors = textFieldColors,
                                shape = ChipShape
                            )
                            ExposedDropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false },
                                modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                            ) {
                                incomeCategories.forEach { selectionOption ->
                                    DropdownMenuItem(
                                        text = { Text(selectionOption, color = MaterialTheme.colorScheme.onSurface) },
                                        onClick = { selectedCategory = selectionOption; expanded = false },
                                        contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                                    )
                                }
                            }
                        }
                        Spacer(Modifier.height(12.dp))

                        if (accounts.isNotEmpty()) {
                            Text("Select Account", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(bottom = 8.dp))
                            var accountExpanded by remember { mutableStateOf(false) }
                            ExposedDropdownMenuBox(
                                expanded = accountExpanded,
                                onExpandedChange = { accountExpanded = !accountExpanded },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                OutlinedTextField(
                                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                                    readOnly = true,
                                    value = selectedAccount?.let { it.nickname ?: it.name } ?: "Select Account",
                                    onValueChange = {},
                                    label = { Text("Select Account") },
                                    leadingIcon = { Icon(Icons.Default.AccountBalance, "Account", tint = EmeraldGreen) },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = accountExpanded) },
                                    colors = textFieldColors,
                                    shape = ChipShape
                                )
                                ExposedDropdownMenu(
                                    expanded = accountExpanded,
                                    onDismissRequest = { accountExpanded = false },
                                    modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                                ) {
                                    accounts.forEach { account ->
                                        DropdownMenuItem(
                                            text = {
                                                Column {
                                                    Text(account.nickname ?: account.name, color = MaterialTheme.colorScheme.onSurface)
                                                    Text("Balance: ৳ ${account.balance.toInt()}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                }
                                            },
                                            onClick = { selectedAccount = account; accountExpanded = false },
                                            contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                                        )
                                    }
                                }
                            }
                            Spacer(Modifier.height(12.dp))
                        }

                        Text("Date", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(bottom = 8.dp))
                        OutlinedTextField(
                            value = selectedDate?.let { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(it) } ?: "Select a date",
                            onValueChange = {},
                            label = { Text("Date") },
                            leadingIcon = { Icon(Icons.Default.CalendarToday, "Date", tint = EmeraldGreen) },
                            modifier = Modifier.fillMaxWidth().clickable { showDatePicker = true },
                            shape = ChipShape,
                            colors = textFieldColors,
                            readOnly = true,
                            enabled = false
                        )
                    }
                }
            }

            item {
                Button(
                    onClick = {
                        val incomeValue = incomeInput.text.toDoubleOrNull()
                        if (incomeValue != null && incomeValue > 0) {
                            viewModel.saveIncome(
                                amount = incomeValue,
                                description = descriptionInput.text,
                                category = selectedCategory,
                                source = "Primary Job",
                                accountType = selectedAccountType,
                                timestamp = selectedDate?.time ?: System.currentTimeMillis(),
                                selectedAccountId = selectedAccount?.id
                            )
                            incomeInput = TextFieldValue("")
                            descriptionInput = TextFieldValue("")
                            selectedAccount = null
                            errorMessage = null
                        } else {
                            errorMessage = "Please enter a valid positive number"
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = ChipShape,
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                        Icon(Icons.Default.Savings, "Save", tint = Color.White, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(12.dp))
                        Text("Save Income", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth().shadow(6.dp, CardShape, clip = false),
                    shape = CardShape,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(0.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp).fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                            Box(
                                modifier = Modifier.size(36.dp).background(
                                    brush = Brush.linearGradient(listOf(EmeraldGreen, SapphireBlue)),
                                    shape = RoundedCornerShape(10.dp)
                                ),
                                contentAlignment = Alignment.Center
                            ) { Icon(Icons.Default.Savings, null, tint = Color.White, modifier = Modifier.size(18.dp)) }
                            Text("Current Balance", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }
                        Spacer(Modifier.height(12.dp))
                        Text("৳${String.format("%,.2f", animatedBalance.toDouble())}", style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.ExtraBold, color = EmeraldGreen, textAlign = TextAlign.Center)
                        Spacer(Modifier.height(12.dp))
                        LinearProgressIndicator(
                            progress = { if (savedIncome > 10000) 1f else (savedIncome / 10000).toFloat() },
                            modifier = Modifier.fillMaxWidth().height(8.dp).clip(ChipShape),
                            color = EmeraldGreen,
                            trackColor = EmeraldGreen.copy(alpha = 0.15f)
                        )
                        Spacer(Modifier.height(8.dp))
                        Text("Keep tracking your income!", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            if (recentIncomes.isNotEmpty()) {
                item {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.padding(start = 2.dp)) {
                        Box(Modifier.size(32.dp).background(
                            brush = Brush.linearGradient(listOf(EmeraldGreen, SapphireBlue)),
                            shape = RoundedCornerShape(10.dp)
                        ), contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.History, null, tint = Color.White, modifier = Modifier.size(16.dp))
                        }
                        Text("Recent Incomes", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                }
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth().shadow(6.dp, CardShape, clip = false),
                        shape = CardShape,
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(0.dp)
                    ) {
                        LazyColumn(modifier = Modifier.heightIn(max = 400.dp).padding(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            items(recentIncomes) { income ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = ChipShape,
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                                    elevation = CardDefaults.cardElevation(0.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.weight(1f)) {
                                            Box(Modifier.size(36.dp).background(EmeraldGreen.copy(alpha = 0.1f), ChipShape), contentAlignment = Alignment.Center) {
                                                Icon(Icons.Default.AttachMoney, null, tint = EmeraldGreen, modifier = Modifier.size(18.dp))
                                            }
                                            Column {
                                                Text(income.description, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
                                                Text("৳${income.amount}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            }
                                        }
                                        IconButton(onClick = { viewModel.deleteIncome(income) }) {
                                            Icon(Icons.Default.Delete, "Delete Income", tint = CoralRed.copy(alpha = 0.6f))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item { Spacer(Modifier.height(80.dp)) }
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { selectedDate = Date(it) }
                    showDatePicker = false
                }) { Text("OK", color = EmeraldGreen) }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant) }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}
