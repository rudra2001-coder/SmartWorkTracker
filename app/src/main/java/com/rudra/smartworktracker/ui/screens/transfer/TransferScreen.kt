package com.rudra.smartworktracker.ui.screens.transfer

import android.app.Application
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rudra.smartworktracker.data.entity.AccountType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransferScreen() {
    val context = LocalContext.current
    val viewModel: TransferViewModel = viewModel(factory = TransferViewModelFactory(context.applicationContext as Application))

    var fromAccount by remember { mutableStateOf<AccountType?>(null) }
    var toAccount by remember { mutableStateOf<AccountType?>(null) }
    var amount by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Make a Transfer") })
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            AccountSelector(label = "From Account", selectedAccount = fromAccount, onAccountSelected = { fromAccount = it })
            AccountSelector(label = "To Account", selectedAccount = toAccount, onAccountSelected = { toAccount = it })

            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it },
                label = { Text("Amount") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Notes (Optional)") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    val from = fromAccount
                    val to = toAccount
                    val transferAmount = amount.toDoubleOrNull()

                    if (from != null && to != null && transferAmount != null && from != to) {
                        viewModel.makeTransfer(transferAmount, from, to, notes)
                        // Clear fields after transfer
                        fromAccount = null
                        toAccount = null
                        amount = ""
                        notes = ""
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = fromAccount != null && toAccount != null && amount.isNotBlank() && fromAccount != toAccount
            ) {
                Text("Confirm Transfer")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountSelector(label: String, selectedAccount: AccountType?, onAccountSelected: (AccountType) -> Unit) {
    val accountTypes = AccountType.values()
    var isExpanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(expanded = isExpanded, onExpandedChange = { isExpanded = it }) {
        OutlinedTextField(
            value = selectedAccount?.name?.replace("_", " ")?.lowercase()?.replaceFirstChar { it.titlecase() } ?: "",
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isExpanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor()
        )
        ExposedDropdownMenu(expanded = isExpanded, onDismissRequest = { isExpanded = false }) {
            accountTypes.forEach { accountType ->
                DropdownMenuItem(
                    text = { Text(accountType.name.replace("_", " ").lowercase().replaceFirstChar { it.titlecase() }) },
                    onClick = {
                        onAccountSelected(accountType)
                        isExpanded = false
                    }
                )
            }
        }
    }
}
