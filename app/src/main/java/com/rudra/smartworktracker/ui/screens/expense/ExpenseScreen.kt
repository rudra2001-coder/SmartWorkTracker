package com.rudra.smartworktracker.ui.screens.expense

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.MoneyOff
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.Subscriptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rudra.smartworktracker.data.entity.AccountType
import com.rudra.smartworktracker.data.entity.Account
import com.rudra.smartworktracker.model.ExpenseCategory
import com.rudra.smartworktracker.utils.CurrencyManager
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
    
    val accountTypes = AccountType.values()
    var accountTypeExpanded by remember { mutableStateOf(false) }
    var selectedAccountType by remember { mutableStateOf<AccountType?>(null) }

    // Premium gradient colors
    val primaryGradient = Brush.horizontalGradient(
        colors = listOf(
            Color(0xFFFF6B6B), // Coral red
            Color(0xFFFF8E53)  // Orange
        )
    )

    val secondaryGradient = Brush.horizontalGradient(
        colors = listOf(
            Color(0xFF36D1DC), // Cyan
            Color(0xFF5B86E5)  // Light blue
        )
    )

    val categoryGradients = mapOf(
        ExpenseCategory.MEAL to Brush.horizontalGradient(
            colors = listOf(Color(0xFFFF9A9E), Color(0xFFFAD0C4))
        ),
        ExpenseCategory.TRANSPORT to Brush.horizontalGradient(
            colors = listOf(Color(0xFFA1C4FD), Color(0xFFC2E9FB))
        ),
        ExpenseCategory.SHOPPING to Brush.horizontalGradient(
            colors = listOf(Color(0xFFFFD1FF), Color(0xFFF9FFA4))
        ),
        ExpenseCategory.ENTERTAINMENT to Brush.horizontalGradient(
            colors = listOf(Color(0xFFFBC2EB), Color(0xFFA6C1EE))
        ),
        ExpenseCategory.BILLS to Brush.horizontalGradient(
            colors = listOf(Color(0xFF43CBFF), Color(0xFF9708CC))
        ),

        ExpenseCategory.OTHER to Brush.horizontalGradient(
            colors = listOf(Color(0xFFD4D4D4), Color(0xFFA0A0A0))
        )
    )

    // Custom text field colors
    val textFieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = Color(0xFFFF6B6B),
        unfocusedBorderColor = Color(0xFFE2E8F0),
        focusedLabelColor = Color(0xFFFF6B6B),
        unfocusedLabelColor = Color(0xFF718096),
        focusedTextColor = Color(0xFF2D3748),
        unfocusedTextColor = Color(0xFF4A5568),
        cursorColor = Color(0xFFFF6B6B),
        errorBorderColor = Color(0xFFE53E3E),
        errorLabelColor = Color(0xFFE53E3E),
        errorSupportingTextColor = Color(0xFFE53E3E)
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFFFF5F5), // Very light red tint
                        Color(0xFFFFEBEB)  // Slightly deeper tint
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Premium Header
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp)
                    .shadow(
                        elevation = 16.dp,
                        shape = RoundedCornerShape(20.dp),
                        clip = true
                    ),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Icon Badge
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(primaryGradient)
                            .padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoneyOff,
                            contentDescription = "Expense",
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Log Your Expense",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2D3748)
                        ),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Track and manage your spending",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = Color(0xFF718096)
                        ),
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Expense Input Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
                    .shadow(
                        elevation = 8.dp,
                        shape = RoundedCornerShape(16.dp),
                        clip = true
                    ),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    // Amount Field
                    Text(
                        text = "Amount",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF4A5568)
                        ),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    OutlinedTextField(
                        value = amount,
                        onValueChange = { amount = it },
                        label = { Text("Enter amount") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.MoneyOff,
                                contentDescription = "Amount",
                                tint = Color(0xFFFF6B6B)
                            )
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = textFieldColors
                    )
                    Spacer(modifier = Modifier.height(24.dp))

                    // Categories Section
                    Text(
                        text = "Category",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF4A5568)
                        ),
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    val categories = ExpenseCategory.entries.chunked(4)
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        categories.forEach { rowCategories ->
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                rowCategories.forEach { category ->
                                    val isSelected = selectedCategory == category
                                    val gradient = categoryGradients[category] ?: Brush.horizontalGradient(
                                        listOf(Color(0xFFD4D4D4), Color(0xFFA0A0A0))
                                    )
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .shadow(
                                                elevation = if (isSelected) 8.dp else 4.dp,
                                                shape = RoundedCornerShape(12.dp)
                                            )
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(
                                                brush = if (isSelected) gradient
                                                else Brush.linearGradient(
                                                    colors = listOf(Color.White, Color.White)
                                                ),
                                                shape = RoundedCornerShape(12.dp)
                                            )
                                            .border(
                                                width = if (isSelected) 2.dp else 1.dp,
                                                color = if (isSelected) Color(0xFFFF6B6B) else Color(0xFFE2E8F0),
                                                shape = RoundedCornerShape(12.dp)
                                            )
                                            .clickable { selectedCategory = category }
                                            .padding(12.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
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

                                            Icon(
                                                imageVector = icon,
                                                contentDescription = category.name,
                                                tint = if (isSelected) Color.White else Color(0xFF4A5568),
                                                modifier = Modifier.size(20.dp)
                                            )

                                            Text(
                                                text = category.name.lowercase()
                                                    .replaceFirstChar { it.uppercase() },
                                                style = TextStyle(
                                                    fontSize = 10.sp,
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                    color = if (isSelected) Color.White else Color(0xFF4A5568)
                                                ),
                                                maxLines = 1
                                            )
                                        }
                                    }
                                }
                                val remaining = 4 - rowCategories.size
                                if (remaining > 0) {
                                    Spacer(modifier = Modifier.weight(remaining.toFloat()))
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))

                    // Account Type Dropdown
                    Text(
                        text = "Account Type",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF4A5568)
                        ),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Text(
                        text = "Select Account",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF4A5568)
                        ),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    if (accounts.isNotEmpty()) {
                        var accountExpanded by remember { mutableStateOf(false) }
                        
                        ExposedDropdownMenuBox(
                            expanded = accountExpanded,
                            onExpandedChange = { accountExpanded = !accountExpanded },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(),
                                readOnly = true,
                                value = selectedAccount?.nickname ?: selectedAccount?.name ?: "Select Account",
                                onValueChange = {},
                                label = {
                                    Text("Select Account (Optional)")
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.AccountBalance,
                                        contentDescription = "Account",
                                        tint = Color(0xFFFF6B6B)
                                    )
                                },
                                trailingIcon = {
                                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = accountExpanded)
                                },
                                colors = textFieldColors,
                                shape = RoundedCornerShape(12.dp)
                            )
                            ExposedDropdownMenu(
                                expanded = accountExpanded,
                                onDismissRequest = { accountExpanded = false },
                                modifier = Modifier.background(Color.White)
                            ) {
                                accounts.forEach { account ->
                                    DropdownMenuItem(
                                        text = {
                                            Column {
                                                Text(account.nickname ?: account.name, color = Color(0xFF4A5568))
                                                Text("Balance: ৳ ${account.balance.toInt()}", 
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = Color.Gray)
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
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // Merchant Field
                    Text(
                        text = "Merchant (Optional)",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF4A5568)
                        ),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    OutlinedTextField(
                        value = merchant,
                        onValueChange = { merchant = it },
                        label = { Text("Store name, restaurant, etc.") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Store,
                                contentDescription = "Merchant",
                                tint = Color(0xFFFF6B6B)
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = textFieldColors
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    // Notes Field
                    Text(
                        text = "Notes (Optional)",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF4A5568)
                        ),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Add any notes") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Notes,
                                contentDescription = "Notes",
                                tint = Color(0xFFFF6B6B)
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = textFieldColors,
                        maxLines = 4
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Date Field
                    Text(
                        text = "Date",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF4A5568)
                        ),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    OutlinedTextField(
                        value = selectedDate?.let { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(it) } ?: "Select a date",
                        onValueChange = {},
                        label = { Text("Date") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.CalendarToday,
                                contentDescription = "Date",
                                tint = Color(0xFFFF6B6B)
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showDatePicker = true },
                        shape = RoundedCornerShape(12.dp),
                        colors = textFieldColors,
                        readOnly = true,
                        enabled = false
                    )

                }
            }

            // Save Button
            Button(
                onClick = {
                    if (amount.isNotBlank()) {
                        viewModel.saveExpense(
                            amount = amount.toDouble(),
                            currency = CurrencyManager.getCurrencyCode(),
                            category = selectedCategory,
                            merchant = merchant.ifBlank { null },
                            notes = notes.ifBlank { null },
                            accountType = selectedAccountType,
                            timestamp = selectedDate?.time ?: System.currentTimeMillis(),
                            selectedAccountId = selectedAccount?.id
                        )
                        amount = ""
                        merchant = ""
                        notes = ""
                        selectedAccount = null
                        // Show success toast
                        Toast.makeText(context, "✓ Expense Saved Successfully", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "Please enter an amount", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent
                ),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 0.dp
                )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(14.dp))
                        .background(secondaryGradient)
                        .padding(horizontal = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Save,
                            contentDescription = "Save",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        Text(
                            text = "Save Expense",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            ),
                            color = Color.White
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))

            // Recent Expenses
            Text(
                text = "Recent Expenses",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2D3748)
                ),
                modifier = Modifier.padding(bottom = 16.dp)
            )
            LazyColumn(modifier = Modifier.height(300.dp)) {
                items(recentExpenses) { expense ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(text = expense.notes ?: "", fontWeight = FontWeight.Bold)
                                Text(text = "৳${expense.amount}", color = Color.Gray)
                            }
                            IconButton(onClick = { viewModel.deleteExpense(expense) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete Expense")
                            }
                        }
                    }
                }
            }
        }

        if (showDatePicker) {
            val datePickerState = rememberDatePickerState()
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        datePickerState.selectedDateMillis?.let {
                            selectedDate = Date(it)
                        }
                        showDatePicker = false
                    }) {
                        Text("OK")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDatePicker = false }) {
                        Text("Cancel")
                    }
                }
            ) {
                DatePicker(state = datePickerState)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ExpenseScreenPreview() {
    ExpenseScreen()
}
