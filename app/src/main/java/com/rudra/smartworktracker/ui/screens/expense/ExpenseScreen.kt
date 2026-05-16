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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rudra.smartworktracker.data.entity.AccountType
import com.rudra.smartworktracker.data.entity.Account
import com.rudra.smartworktracker.model.ExpenseCategory
import com.rudra.smartworktracker.ui.components.AppColors
import com.rudra.smartworktracker.ui.components.SectionHeader
import com.rudra.smartworktracker.ui.components.StandardCard
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

    // Custom text field colors
    val textFieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = AppColors.ExpenseRed,
        unfocusedBorderColor = AppColors.SecondaryText.copy(alpha = 0.3f),
        focusedLabelColor = AppColors.ExpenseRed,
        unfocusedLabelColor = AppColors.SecondaryText,
        focusedTextColor = AppColors.PrimaryText,
        unfocusedTextColor = AppColors.PrimaryText,
        cursorColor = AppColors.ExpenseRed,
        errorBorderColor = AppColors.ExpenseRed,
        errorLabelColor = AppColors.ExpenseRed,
        errorSupportingTextColor = AppColors.ExpenseRed
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.GlobalBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            StandardCard {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(AppColors.ExpenseRed.copy(alpha = 0.2f))
                            .padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoneyOff,
                            contentDescription = "Expense",
                            tint = AppColors.ExpenseRed,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Log Your Expense",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = AppColors.PrimaryText
                        ),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Track and manage your spending",
                        style = MaterialTheme.typography.bodyMedium,
                        color = AppColors.SecondaryText,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Expense Input Card
            StandardCard {
                // Amount Field
                Text(
                    text = "Amount",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = AppColors.SecondaryText
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
                            tint = AppColors.ExpenseRed
                        )
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = textFieldColors
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Categories Section
                Text(
                    text = "Category",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = AppColors.SecondaryText
                    ),
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                val categories = ExpenseCategory.entries.chunked(4)
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    categories.forEach { rowCategories ->
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            rowCategories.forEach { category ->
                                val isSelected = selectedCategory == category
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .shadow(
                                            elevation = if (isSelected) 8.dp else 4.dp,
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(
                                            color = if (isSelected) AppColors.ExpenseRed else AppColors.CardBackground,
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                        .border(
                                            width = if (isSelected) 2.dp else 1.dp,
                                            color = if (isSelected) AppColors.ExpenseRed else AppColors.SecondaryText.copy(alpha = 0.3f),
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
                                            tint = if (isSelected) Color.White else AppColors.SecondaryText,
                                            modifier = Modifier.size(20.dp)
                                        )

                                        Text(
                                            text = category.name.lowercase()
                                                .replaceFirstChar { it.uppercase() },
                                            style = TextStyle(
                                                fontSize = 10.sp,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                color = if (isSelected) Color.White else AppColors.PrimaryText
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
                Spacer(modifier = Modifier.height(16.dp))

                // Account Type Dropdown
                Text(
                    text = "Account Type",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = AppColors.SecondaryText
                    ),
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Text(
                    text = "Select Account",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = AppColors.SecondaryText
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
                                    tint = AppColors.ExpenseRed
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
                            modifier = Modifier.background(AppColors.CardBackground)
                        ) {
                            accounts.forEach { account ->
                                DropdownMenuItem(
                                    text = {
                                        Column {
                                            Text(account.nickname ?: account.name, color = AppColors.PrimaryText)
                                            Text("Balance: ৳ ${account.balance.toInt()}", 
                                                style = MaterialTheme.typography.bodySmall,
                                                color = AppColors.SecondaryText)
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
                        color = AppColors.SecondaryText
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
                            tint = AppColors.ExpenseRed
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
                        color = AppColors.SecondaryText
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
                            tint = AppColors.ExpenseRed
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
                        color = AppColors.SecondaryText
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
                            tint = AppColors.ExpenseRed
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

            Spacer(modifier = Modifier.height(16.dp))

            // Save Button
            Button(
                onClick = {
                    if (amount.isNotBlank()) {
                        viewModel.saveExpense(
                            amount = amount.toDouble(),
                            currency = "BDT",
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
                    containerColor = AppColors.ExpenseRed
                )
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
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Recent Expenses
            SectionHeader(text = "Recent Expenses")
            LazyColumn(modifier = Modifier.height(300.dp)) {
                items(recentExpenses) { expense ->
                    StandardCard {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = expense.notes ?: "", 
                                    fontWeight = FontWeight.Bold,
                                    color = AppColors.PrimaryText
                                )
                                Text(
                                    text = "৳${expense.amount}", 
                                    color = AppColors.ExpenseRed
                                )
                            }
                            IconButton(onClick = { viewModel.deleteExpense(expense) }) {
                                Icon(
                                    Icons.Default.Delete, 
                                    contentDescription = "Delete Expense",
                                    tint = AppColors.ExpenseRed
                                )
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
                        Text("OK", color = AppColors.ExpenseRed)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDatePicker = false }) {
                        Text("Cancel", color = AppColors.SecondaryText)
                    }
                }
            ) {
                DatePicker(state = datePickerState)
            }
        }
    }
}
