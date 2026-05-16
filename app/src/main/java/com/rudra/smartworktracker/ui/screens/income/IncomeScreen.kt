package com.rudra.smartworktracker.ui.screens.income

import android.app.DatePickerDialog
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rudra.smartworktracker.data.entity.AccountType
import com.rudra.smartworktracker.data.entity.IncomeCategories
import com.rudra.smartworktracker.data.entity.Account
import com.rudra.smartworktracker.data.entity.displayName
import com.rudra.smartworktracker.data.entity.icon
import com.rudra.smartworktracker.ui.components.AppColors
import com.rudra.smartworktracker.ui.components.SectionHeader
import com.rudra.smartworktracker.ui.components.StandardCard
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IncomeScreen(
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val application = context.applicationContext as android.app.Application
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

    // Custom text field colors
    val textFieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = AppColors.IncomeGreen,
        unfocusedBorderColor = AppColors.SecondaryText.copy(alpha = 0.3f),
        focusedLabelColor = AppColors.IncomeGreen,
        unfocusedLabelColor = AppColors.SecondaryText,
        focusedTextColor = AppColors.PrimaryText,
        unfocusedTextColor = AppColors.PrimaryText,
        cursorColor = AppColors.IncomeGreen,
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
                Text(
                    text = "Log Your Income",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = AppColors.PrimaryText
                    ),
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Income Input Card
            StandardCard {
                // Income Amount Field
                Text(
                    text = "Income Amount",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = AppColors.SecondaryText
                    ),
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                OutlinedTextField(
                    value = incomeInput,
                    onValueChange = {
                        incomeInput = it
                        errorMessage = null
                    },
                    label = {
                        Text("Enter amount")
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.AttachMoney,
                            contentDescription = "Amount",
                            tint = AppColors.IncomeGreen
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    isError = errorMessage != null,
                    supportingText = {
                        errorMessage?.let {
                            Text(it)
                        }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp),
                    colors = textFieldColors
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Description Field
                Text(
                    text = "Description",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = AppColors.SecondaryText
                    ),
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                OutlinedTextField(
                    value = descriptionInput,
                    onValueChange = { descriptionInput = it },
                    label = {
                        Text("Optional description")
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Description,
                            contentDescription = "Description",
                            tint = AppColors.IncomeGreen
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = textFieldColors
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Category Dropdown
                Text(
                    text = "Category",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = AppColors.SecondaryText
                    ),
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        readOnly = true,
                        value = selectedCategory,
                        onValueChange = {},
                        label = {
                            Text("Select category")
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Category,
                                contentDescription = "Category",
                                tint = AppColors.IncomeGreen
                            )
                        },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                        },
                        colors = textFieldColors,
                        shape = RoundedCornerShape(12.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier.background(AppColors.CardBackground)
                    ) {
                        incomeCategories.forEach { selectionOption ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        selectionOption,
                                        color = AppColors.PrimaryText
                                    )
                                },
                                onClick = {
                                    selectedCategory = selectionOption
                                    expanded = false
                                },
                                contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))

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
                            value = selectedAccount?.let { it.nickname ?: it.name } ?: "Select Account",
                            onValueChange = {},
                            label = {
                                Text("Select Account")
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.AccountBalance,
                                    contentDescription = "Account",
                                    tint = AppColors.IncomeGreen
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
                    Spacer(modifier = Modifier.height(10.dp))
                }

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
                            tint = AppColors.IncomeGreen
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
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AppColors.IncomeGreen
                )
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Savings,
                        contentDescription = "Save",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        "Save Income",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Saved Income Display Card
            StandardCard {
                Column(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Current Balance",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = AppColors.SecondaryText
                        )
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "৳${String.format("%,.2f", savedIncome)}",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = AppColors.PrimaryText
                        )
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Visual indicator
                    LinearProgressIndicator(
                        progress = if (savedIncome > 10000) 1f else (savedIncome / 10000).toFloat(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = AppColors.IncomeGreen,
                        trackColor = AppColors.SecondaryText.copy(alpha = 0.2f)
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Keep tracking your income!",
                        style = MaterialTheme.typography.bodySmall,
                        color = AppColors.SecondaryText
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Recent Incomes
            SectionHeader(text = "Recent Incomes")
            LazyColumn(modifier = Modifier.height(300.dp)) {
                items(recentIncomes) { income ->
                    StandardCard {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = income.description, 
                                    fontWeight = FontWeight.Bold,
                                    color = AppColors.PrimaryText
                                )
                                Text(
                                    text = "৳${income.amount}", 
                                    color = AppColors.SecondaryText
                                )
                            }
                            IconButton(onClick = { viewModel.deleteIncome(income) }) {
                                Icon(
                                    Icons.Default.Delete, 
                                    contentDescription = "Delete Income",
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
                        Text("OK", color = AppColors.IncomeGreen)
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
