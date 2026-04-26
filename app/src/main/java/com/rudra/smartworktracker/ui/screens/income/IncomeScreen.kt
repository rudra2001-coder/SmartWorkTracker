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
    val viewModel: IncomeViewModel = viewModel(factory = IncomeViewModelFactory(context))
    var incomeInput by remember { mutableStateOf(TextFieldValue("")) }
    var descriptionInput by remember { mutableStateOf(TextFieldValue("")) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val savedIncome by viewModel.income.collectAsState()
    val recentIncomes by viewModel.recentIncomes.collectAsState()
    var selectedDate by remember { mutableStateOf<Date?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }

    val incomeCategories = IncomeCategories.categories
    var expanded by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf(incomeCategories[0]) }
    val accountTypes = AccountType.values()
    var accountTypeExpanded by remember { mutableStateOf(false) }
    var selectedAccountType by remember { mutableStateOf(accountTypes[0]) }

    // Premium gradient colors
    val primaryGradient = Brush.horizontalGradient(
        colors = listOf(
            Color(0xFF6C63FF), // Vibrant purple
            Color(0xFF4A44C6)  // Deep blue
        )
    )

    val secondaryGradient = Brush.horizontalGradient(
        colors = listOf(
            Color(0xFF36D1DC), // Cyan
            Color(0xFF5B86E5)  // Light blue
        )
    )

    // Custom text field colors
    val textFieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = Color(0xFF6C63FF),
        unfocusedBorderColor = Color(0xFFE2E8F0),
        focusedLabelColor = Color(0xFF6C63FF),
        unfocusedLabelColor = Color(0xFF718096),
        focusedTextColor = Color(0xFF2D3748),
        unfocusedTextColor = Color(0xFF4A5568),
        cursorColor = Color(0xFF6C63FF),
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
                        Color(0xFFF8F9FF), // Very light purple tint
                        Color(0xFFEFF2FF)  // Slightly deeper tint
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()), // Added scroll here
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

                    Text(
                        text = "Log Your Income",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2D3748) // Dark gray
                        ),
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Income Input Card
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
                    // Income Amount Field
                    Text(
                        text = "Income Amount",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF4A5568)
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
                                tint = Color(0xFF6C63FF)
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
                            color = Color(0xFF4A5568)
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
                                tint = Color(0xFF6C63FF)
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
                            color = Color(0xFF4A5568)
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
                                    tint = Color(0xFF6C63FF)
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
                            modifier = Modifier.background(Color.White)
                        ) {
                            incomeCategories.forEach { selectionOption ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            selectionOption,
                                            color = Color(0xFF4A5568)
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
                            color = Color(0xFF4A5568)
                        ),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    ExposedDropdownMenuBox(
                        expanded = accountTypeExpanded,
                        onExpandedChange = { accountTypeExpanded = !accountTypeExpanded },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            readOnly = true,
                            value = selectedAccountType.name,
                            onValueChange = {},
                            label = {
                                Text("Select account type")
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.AccountBalance,
                                    contentDescription = "Account Type",
                                    tint = Color(0xFF6C63FF)
                                )
                            },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = accountTypeExpanded)
                            },
                            colors = textFieldColors,
                            shape = RoundedCornerShape(12.dp)
                        )
                        ExposedDropdownMenu(
                            expanded = accountTypeExpanded,
                            onDismissRequest = { accountTypeExpanded = false },
                            modifier = Modifier.background(Color.White)
                        ) {
                            accountTypes.forEach { selectionOption ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            selectionOption.name,
                                            color = Color(0xFF4A5568)
                                        )
                                    },
                                    onClick = {
                                        selectedAccountType = selectionOption
                                        accountTypeExpanded = false
                                    },
                                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))

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
                                tint = Color(0xFF6C63FF)
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
                    val incomeValue = incomeInput.text.toDoubleOrNull()
                    if (incomeValue != null && incomeValue > 0) {
                        viewModel.saveIncome(
                            amount = incomeValue,
                            description = descriptionInput.text,
                            category = selectedCategory,
                            source = "Primary Job",
                            accountType = selectedAccountType,
                            timestamp = selectedDate?.time ?: System.currentTimeMillis()
                        )
                        incomeInput = TextFieldValue("")
                        descriptionInput = TextFieldValue("")
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
                            imageVector = Icons.Default.Savings,
                            contentDescription = "Save",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        Text(
                            text = "Save Income",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            ),
                            color = Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Saved Income Display Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp) // Added bottom padding for better scroll
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
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Current Balance",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF718096)
                        )
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "৳${String.format("%,.2f", savedIncome)}",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = Color(0xFF2D3748)
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
                        color = Color(0xFF6C63FF),
                        trackColor = Color(0xFFE2E8F0)
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Keep tracking your income!",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color(0xFF718096)
                        )
                    )
                }
            }

            // Recent Incomes
            Text(
                text = "Recent Incomes",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2D3748)
                ),
                modifier = Modifier.padding(bottom = 16.dp)
            )
            LazyColumn(modifier = Modifier.height(300.dp)) {
                items(recentIncomes) { income ->
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
                                Text(text = income.description, fontWeight = FontWeight.Bold)
                                Text(text = "৳${income.amount}", color = Color.Gray)
                            }
                            IconButton(onClick = { viewModel.deleteIncome(income) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete Income")
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
