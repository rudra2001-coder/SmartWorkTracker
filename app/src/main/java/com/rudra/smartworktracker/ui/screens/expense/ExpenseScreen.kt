package com.rudra.smartworktracker.ui.screens.expense

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rudra.smartworktracker.model.ExpenseCategory

@Composable
fun ExpenseScreen(viewModel: ExpenseViewModel = viewModel()) {
    var amount by remember { mutableStateOf("") }
    var merchant by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(ExpenseCategory.MEAL) }
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TextField(
            value = amount,
            onValueChange = { amount = it },
            label = { Text("Amount") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            RadioButton(
                selected = selectedCategory == ExpenseCategory.MEAL,
                onClick = { selectedCategory = ExpenseCategory.MEAL }
            )
            Text("Meal", modifier = Modifier.align(Alignment.CenterVertically))
            Spacer(modifier = Modifier.height(16.dp))
            RadioButton(
                selected = selectedCategory == ExpenseCategory.OTHER,
                onClick = { selectedCategory = ExpenseCategory.OTHER }
            )
            Text("Other", modifier = Modifier.align(Alignment.CenterVertically))
        }
        Spacer(modifier = Modifier.height(16.dp))
        TextField(
            value = merchant,
            onValueChange = { merchant = it },
            label = { Text("Merchant (Optional)") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        TextField(
            value = notes,
            onValueChange = { notes = it },
            label = { Text("Notes (Optional)") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(32.dp))
        Button(onClick = {
            if (amount.isNotBlank()) {
                viewModel.saveExpense(
                    amount = amount.toDouble(),
                    currency = "BDT", // Defaulting to BDT for now
                    category = selectedCategory,
                    merchant = merchant.takeIf { it.isNotBlank() },
                    notes = notes.takeIf { it.isNotBlank() }
                )
                // Clear fields after saving
                amount = ""
                merchant = ""
                notes = ""
                Toast.makeText(context, "Expense Saved", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Please enter an amount", Toast.LENGTH_SHORT).show()
            }
        }, modifier = Modifier.fillMaxWidth()) {
            Text("Save Expense")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ExpenseScreenPreview() {
    ExpenseScreen()
}
