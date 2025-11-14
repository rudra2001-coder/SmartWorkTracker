package com.rudra.smartworktracker.ui.screens.income

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun IncomeScreen(
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val viewModel: IncomeViewModel = viewModel(factory = IncomeViewModelFactory(context))
    var incomeInput by remember { mutableStateOf(TextFieldValue("")) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val savedIncome by viewModel.income.collectAsState()

    Column(modifier = modifier.padding(16.dp)) {
        OutlinedTextField(
            value = incomeInput,
            onValueChange = {
                incomeInput = it
                errorMessage = null // Clear error when user starts typing
            },
            label = { Text("Enter your income") },
            modifier = Modifier.fillMaxWidth(),
            isError = errorMessage != null,
            supportingText = { errorMessage?.let { Text(it) } }
        )

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = {
                val incomeValue = incomeInput.text.toDoubleOrNull()
                if (incomeValue != null && incomeValue > 0) {
                    viewModel.saveIncome(incomeValue)
                    incomeInput = TextFieldValue("") // Clear input after saving
                    errorMessage = null
                } else {
                    errorMessage = "Please enter a valid positive number"
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Save Income")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text("Saved Income: $${String.format("%.2f", savedIncome)}")
    }
}