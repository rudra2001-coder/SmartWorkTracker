package com.rudra.smartworktracker.ui.screens.emi

import android.app.Application
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rudra.smartworktracker.data.entity.Emi
import com.rudra.smartworktracker.data.entity.Loan
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmiScreen() {
    val context = LocalContext.current
    val viewModel: EmiViewModel = viewModel(factory = EmiViewModelFactory(context.applicationContext as Application))
    val emis by viewModel.emis.collectAsState()
    val loans by viewModel.loans.collectAsState()
    var showDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("EMI Management") })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add EMI")
            }
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
        ) {
            if (showDialog) {
                AddEmiDialog(
                    loans = loans,
                    onDismiss = { showDialog = false },
                    onConfirm = {
                        viewModel.addEmi(it.loanId, it.amount, it.dueDateOfMonth)
                        showDialog = false
                    }
                )
            }
            LazyColumn {
                items(emis) { emi ->
                    EmiItem(emi = emi, onPay = { viewModel.payEmi(emi, emi.amount) })
                }
            }
        }
    }
}

@Composable
fun EmiItem(emi: Emi, onPay: () -> Unit) {
    val dateFormat = remember { SimpleDateFormat("dd MMM, yyyy", Locale.getDefault()) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Loan #${emi.loanId}", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Amount: ${emi.amount}")
            Text("Next Due: ${dateFormat.format(Date(emi.nextDueDate))}")
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = onPay) {
                Text("Mark as Paid")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEmiDialog(loans: List<Loan>, onDismiss: () -> Unit, onConfirm: (Emi) -> Unit) {
    var selectedLoan by remember { mutableStateOf<Loan?>(null) }
    var amount by remember { mutableStateOf("") }
    var dueDate by remember { mutableStateOf("") }
    var isLoansExpanded by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = MaterialTheme.shapes.medium) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Add New EMI", style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(16.dp))

                ExposedDropdownMenuBox(
                    expanded = isLoansExpanded,
                    onExpandedChange = { isLoansExpanded = it }
                ) {
                    OutlinedTextField(
                        value = selectedLoan?.personName ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Select Loan") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = isLoansExpanded)
                        },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(expanded = isLoansExpanded, onDismissRequest = { isLoansExpanded = false }) {
                        loans.forEach { loan ->
                            DropdownMenuItem(
                                text = { Text("${loan.personName} (${loan.remainingAmount})") },
                                onClick = {
                                    selectedLoan = loan
                                    isLoansExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("EMI Amount") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = dueDate,
                    onValueChange = { dueDate = it },
                    label = { Text("Due Day of Month") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row {
                    Button(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    Button(onClick = {
                        val loanId = selectedLoan?.id ?: return@Button
                        val emiAmount = amount.toDoubleOrNull() ?: 0.0
                        val dueDay = dueDate.toIntOrNull() ?: 0
                        if (emiAmount > 0 && dueDay in 1..31) {
                            onConfirm(
                                Emi(
                                    loanId = loanId,
                                    amount = emiAmount,
                                    dueDateOfMonth = dueDay,
                                    nextDueDate = 0 // Will be calculated in ViewModel
                                )
                            )
                        }
                    }) {
                        Text("Add")
                    }
                }
            }
        }
    }
}
