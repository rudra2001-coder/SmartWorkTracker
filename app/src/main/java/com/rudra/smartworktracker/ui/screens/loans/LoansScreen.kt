package com.rudra.smartworktracker.ui.screens.loans

import android.app.Application
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rudra.smartworktracker.data.entity.Loan
import com.rudra.smartworktracker.data.entity.LoanType
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoansScreen() {
    val context = LocalContext.current
    val viewModel: LoanViewModel = viewModel(factory = LoanViewModelFactory(context.applicationContext as Application))
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Loan Management") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { viewModel.openAddLoanDialog() }) {
                Icon(Icons.Default.Add, contentDescription = "Add Loan")
            }
        }
    ) {
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(it)
            ) {
                if (uiState.showAddLoanDialog) {
                    AddLoanDialog(
                        onDismiss = { viewModel.closeAddLoanDialog() },
                        onConfirm = {
                            viewModel.addLoan(it.personName, it.initialAmount, it.loanType, it.notes)
                            viewModel.closeAddLoanDialog()
                        }
                    )
                }
                uiState.showRepayDialogForLoan?.let { loan ->
                    RepayLoanDialog(
                        loan = loan,
                        onDismiss = { viewModel.closeRepayDialog() },
                        onConfirm = {
                            if (loan.loanType == LoanType.BORROWED) {
                                viewModel.repayLoan(loan, it)
                            } else {
                                viewModel.receiveLoanRepayment(loan, it)
                            }
                            viewModel.closeRepayDialog()
                        }
                    )
                }
                if (uiState.loans.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No loans to display.")
                    }
                } else {
                    LazyColumn(contentPadding = PaddingValues(bottom = 80.dp)) {
                        items(uiState.loans) { loan ->
                            LoanItem(loan = loan, onRepayClick = { viewModel.openRepayDialog(loan) })
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LoanItem(loan: Loan, onRepayClick: () -> Unit) {
    val dateFormat = remember { SimpleDateFormat("dd MMM, yyyy", Locale.getDefault()) }
    val cardColor = if (loan.loanType == LoanType.BORROWED) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primaryContainer

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor.copy(alpha = 0.2f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = loan.personName,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = loan.loanType.name,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
            LoanDetailRow("Total Amount:", "${loan.initialAmount}")
            LoanDetailRow("Remaining:", "${loan.remainingAmount}", isRemaining = true)
            LoanDetailRow("Date:", dateFormat.format(Date(loan.date)))
            AnimatedVisibility(visible = loan.notes?.isNotBlank() == true) {
                loan.notes?.let { LoanDetailRow("Notes:", it) }
            }
            Spacer(modifier = Modifier.height(16.dp))
            LinearProgressIndicator(
                progress = { (loan.initialAmount - loan.remainingAmount).toFloat() / loan.initialAmount.toFloat() },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedButton(onClick = onRepayClick, modifier = Modifier.fillMaxWidth()) {
                Text(if (loan.loanType == LoanType.BORROWED) "Make a Repayment" else "Receive a Payment")
            }
        }
    }
}

@Composable
fun LoanDetailRow(label: String, value: String, isRemaining: Boolean = false) {
    Row(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(label, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
        Text(value, fontWeight = if (isRemaining) FontWeight.Bold else FontWeight.Normal)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddLoanDialog(onDismiss: () -> Unit, onConfirm: (Loan) -> Unit) {
    var personName by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var loanType by remember { mutableStateOf(LoanType.BORROWED) }
    var notes by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = MaterialTheme.shapes.medium, tonalElevation = 8.dp) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Add New Loan", style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(value = personName, onValueChange = { personName = it }, label = { Text("Person's Name") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = amount, onValueChange = { amount = it }, label = { Text("Amount") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                    TextButton(onClick = { loanType = LoanType.BORROWED }, enabled = loanType != LoanType.BORROWED) { Text("I Borrowed") }
                    TextButton(onClick = { loanType = LoanType.LENT }, enabled = loanType != LoanType.LENT) { Text("I Lent") }
                }
                OutlinedTextField(value = notes, onValueChange = { notes = it }, label = { Text("Notes (Optional)") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Spacer(Modifier.height(8.dp))
                    Button(onClick = {
                        onConfirm(
                            Loan(
                                personName = personName,
                                initialAmount = amount.toDoubleOrNull() ?: 0.0,
                                remainingAmount = amount.toDoubleOrNull() ?: 0.0,
                                loanType = loanType,
                                date = System.currentTimeMillis(),
                                notes = notes.takeIf { it.isNotBlank() }
                            )
                        )
                    }) { Text("Add Loan") }
                }
            }
        }
    }
}

@Composable
fun RepayLoanDialog(loan: Loan, onDismiss: () -> Unit, onConfirm: (Double) -> Unit) {
    var amount by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = MaterialTheme.shapes.medium, tonalElevation = 8.dp) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Record Payment", style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(8.dp))
                Text("For loan with ${loan.personName}", style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Amount") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Spacer(Modifier.height(8.dp))
                    Button(onClick = { onConfirm(amount.toDoubleOrNull() ?: 0.0) }) { Text("Confirm Payment") }
                }
            }
        }
    }
}
