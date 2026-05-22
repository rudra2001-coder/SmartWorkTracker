package com.rudra.smartworktracker.ui.screens.expense

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rudra.smartworktracker.data.entity.Account
import com.rudra.smartworktracker.model.ExpenseCategory
import java.text.SimpleDateFormat
import java.util.*

private val CardShape = RoundedCornerShape(20.dp)
private val ChipShape = RoundedCornerShape(12.dp)
private val PillShape = RoundedCornerShape(50.dp)

private val EmeraldGreen = Color(0xFF00C896)
private val CoralRed = Color(0xFFFF5757)
private val SapphireBlue = Color(0xFF3B82F6)
private val GoldenAmber = Color(0xFFF59E0B)
private val VioletPurple = Color(0xFF8B5CF6)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseScreen(viewModel: ExpenseViewModel = viewModel()) {
    var amount by remember { mutableStateOf("") }
    var merchant by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(ExpenseCategory.MEAL) }
    var selectedDate by remember { mutableStateOf<Date?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val recentExpenses by viewModel.recentExpenses.collectAsState()
    val accounts by viewModel.accounts.collectAsState()
    var selectedAccount by remember(accounts) {
        mutableStateOf(accounts.find { it.name == "Cash" } ?: accounts.firstOrNull())
    }
    var showInsufficientBalanceDialog by remember { mutableStateOf(false) }

    val totalExpense by viewModel.totalExpense.collectAsState()
    val latest20Expenses by viewModel.latest20Expenses.collectAsState()

    val textFieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = CoralRed,
        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
        focusedLabelColor = CoralRed,
        unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
        focusedTextColor = MaterialTheme.colorScheme.onSurface,
        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
        cursorColor = CoralRed
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
                            brush = Brush.linearGradient(listOf(CoralRed, GoldenAmber)),
                            shape = RoundedCornerShape(14.dp)
                        ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.MoneyOff, contentDescription = null, tint = Color.White, modifier = Modifier.size(28.dp))
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Log Your Expense", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        Text("Track and manage your spending", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Box(
                            modifier = Modifier.size(56.dp).background(
                                brush = Brush.linearGradient(listOf(CoralRed, GoldenAmber)),
                                shape = RoundedCornerShape(14.dp)
                            ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.MoneyOff, null, tint = Color.White, modifier = Modifier.size(28.dp))
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Total Expenses", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                "৳${"%,.0f".format(totalExpense)}",
                                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                                color = CoralRed
                            )
                            Text("All time spending", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
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
                        Text("Amount", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(bottom = 8.dp))
                        OutlinedTextField(
                            value = amount,
                            onValueChange = { amount = it },
                            label = { Text("Enter amount") },
                            leadingIcon = { Icon(Icons.Default.MoneyOff, null, tint = CoralRed) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth(),
                            shape = ChipShape,
                            colors = textFieldColors,
                            singleLine = true
                        )
                        Spacer(Modifier.height(16.dp))

                        Text("Category", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(bottom = 12.dp))
                        val categories = ExpenseCategory.entries.chunked(4)
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            categories.forEach { rowCategories ->
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    rowCategories.forEach { category ->
                                        val isSelected = selectedCategory == category
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .shadow(if (isSelected) 8.dp else 4.dp, ChipShape)
                                                .clip(ChipShape)
                                                .background(
                                                    color = if (isSelected) CoralRed else MaterialTheme.colorScheme.surface,
                                                    shape = ChipShape
                                                )
                                                .clickable { selectedCategory = category }
                                                .padding(12.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                                val icon = when (category) {
                                                    ExpenseCategory.MEAL -> Icons.Default.ShoppingCart
                                                    ExpenseCategory.TRANSPORT -> Icons.Default.DirectionsCar
                                                    ExpenseCategory.SHOPPING -> Icons.Default.Store
                                                    ExpenseCategory.ENTERTAINMENT -> Icons.Default.Movie
                                                    ExpenseCategory.BILLS -> Icons.Default.Receipt
                                                    ExpenseCategory.HEALTHCARE -> Icons.Default.LocalHospital
                                                    ExpenseCategory.EDUCATION -> Icons.Default.School
                                                    ExpenseCategory.PERSONAL_CARE -> Icons.Default.Face
                                                    ExpenseCategory.GIFTS -> Icons.Default.CardGiftcard
                                                    ExpenseCategory.TRAVEL -> Icons.Default.Flight
                                                    ExpenseCategory.SUBSCRIPTIONS -> Icons.Default.Subscriptions
                                                    ExpenseCategory.OTHER -> Icons.AutoMirrored.Filled.List
                                                }
                                                Icon(icon, contentDescription = category.name, tint = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
                                                Text(
                                                    category.name.lowercase().replaceFirstChar { it.uppercase() },
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                                    maxLines = 1
                                                )
                                            }
                                        }
                                    }
                                    val remaining = 4 - rowCategories.size
                                    if (remaining > 0) { Spacer(Modifier.weight(remaining.toFloat())) }
                                }
                            }
                        }
                        Spacer(Modifier.height(16.dp))

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
                                    value = selectedAccount?.nickname ?: selectedAccount?.name ?: "Select Account",
                                    onValueChange = {},
                                    label = { Text("Select Account (Optional)") },
                                    leadingIcon = { Icon(Icons.Default.AccountBalance, "Account", tint = CoralRed) },
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
                                            onClick = {
                                                selectedAccount = account
                                                accountExpanded = false
                                            },
                                            contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                                        )
                                    }
                                }
                            }
                            Spacer(Modifier.height(16.dp))
                        }

                        Text("Merchant (Optional)", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(bottom = 8.dp))
                        OutlinedTextField(
                            value = merchant,
                            onValueChange = { merchant = it },
                            label = { Text("Store name, restaurant, etc.") },
                            leadingIcon = { Icon(Icons.Default.Store, null, tint = CoralRed) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = ChipShape,
                            colors = textFieldColors,
                            singleLine = true
                        )
                        Spacer(Modifier.height(16.dp))

                        Text("Notes (Optional)", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(bottom = 8.dp))
                        OutlinedTextField(
                            value = notes,
                            onValueChange = { notes = it },
                            label = { Text("Add any notes") },
                            leadingIcon = { Icon(Icons.Default.Notes, null, tint = CoralRed) },
                            modifier = Modifier.fillMaxWidth().height(100.dp),
                            shape = ChipShape,
                            colors = textFieldColors,
                            maxLines = 4
                        )
                        Spacer(Modifier.height(16.dp))

                        Text("Date", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(bottom = 8.dp))
                        OutlinedTextField(
                            value = selectedDate?.let { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(it) } ?: "Select a date",
                            onValueChange = {},
                            label = { Text("Date") },
                            leadingIcon = { Icon(Icons.Default.CalendarToday, "Date", tint = CoralRed) },
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
                    val accountId = selectedAccount?.id
                    if (amount.isNotBlank() && accountId != null && accountId > 0) {
                        val expenseAmount = amount.toDouble()
                        val account = selectedAccount!!
                        if (account.balance >= expenseAmount) {
                            viewModel.saveExpense(
                                amount = expenseAmount,
                                currency = "BDT",
                                category = selectedCategory,
                                merchant = merchant.ifBlank { null },
                                notes = notes.ifBlank { null },
                                timestamp = selectedDate?.time ?: System.currentTimeMillis(),
                                selectedAccountId = accountId
                            )
                            amount = ""
                            merchant = ""
                            notes = ""
                            selectedAccount = null
                            Toast.makeText(context, "✓ Expense Saved Successfully", Toast.LENGTH_SHORT).show()
                        } else {
                            showInsufficientBalanceDialog = true
                        }
                    } else {
                        Toast.makeText(context, "Please enter an amount and select an account", Toast.LENGTH_SHORT).show()
                    }
                },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = ChipShape,
                    colors = ButtonDefaults.buttonColors(containerColor = CoralRed)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                        Icon(Icons.Default.Save, "Save", tint = Color.White, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(12.dp))
                        Text("Save Expense", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }
            }

            if (latest20Expenses.isNotEmpty()) {
                item {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.padding(start = 2.dp)) {
                        Box(Modifier.size(32.dp).background(
                            brush = Brush.linearGradient(listOf(CoralRed, GoldenAmber)),
                            shape = RoundedCornerShape(10.dp)
                        ), contentAlignment = Alignment.Center) {
                            Icon(Icons.AutoMirrored.Filled.List, null, tint = Color.White, modifier = Modifier.size(16.dp))
                        }
                        Text("Latest 20 Transactions", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                }
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth().shadow(6.dp, CardShape, clip = false),
                        shape = CardShape,
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(0.dp)
                    ) {
                        Column {
                            val displayExpenses = latest20Expenses.take(20)
                            displayExpenses.forEachIndexed { index, expense ->
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.weight(1f)) {
                                        Box(Modifier.size(36.dp).background(CoralRed.copy(alpha = 0.1f), ChipShape), contentAlignment = Alignment.Center) {
                                            Icon(Icons.Default.MoneyOff, null, tint = CoralRed, modifier = Modifier.size(18.dp))
                                        }
                                        Column {
                                            Text(
                                                expense.notes?.take(30) ?: expense.category.name,
                                                fontWeight = FontWeight.Medium,
                                                color = MaterialTheme.colorScheme.onSurface,
                                                maxLines = 1
                                            )
                                            Text(
                                                SimpleDateFormat("MMM dd", Locale.getDefault()).format(Date(expense.timestamp)),
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            "৳${"%,.0f".format(expense.amount)}",
                                            fontWeight = FontWeight.Bold,
                                            color = CoralRed
                                        )
                                        Text(
                                            expense.merchant ?: "",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 1
                                        )
                                    }
                                }
                                if (index < displayExpenses.lastIndex) {
                                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                                }
                            }
                            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.outlineVariant)
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Total (${displayExpenses.size} items)", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                                Text(
                                    "৳${"%,.0f".format(displayExpenses.sumOf { it.amount })}",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = CoralRed
                                )
                            }
                        }
                    }
                }
            }

            item { Spacer(Modifier.height(80.dp)) }
        }
    }

    if (showInsufficientBalanceDialog) {
        AlertDialog(
            onDismissRequest = { showInsufficientBalanceDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Warning, null, tint = CoralRed)
                    Text("Insufficient Balance", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Text("Your account has not sufficient balance. Please change it or add some money there for completing the transaction.")
            },
            confirmButton = {
                TextButton(onClick = { showInsufficientBalanceDialog = false }) {
                    Text("OK", color = CoralRed, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(20.dp)
        )
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { selectedDate = Date(it) }
                    showDatePicker = false
                }) { Text("OK", color = CoralRed) }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant) }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}
