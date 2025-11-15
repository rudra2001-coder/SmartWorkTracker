package com.rudra.smartworktracker.ui.screens.income

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
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

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Log Your Income",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        OutlinedTextField(
            value = incomeInput,
            onValueChange = {
                incomeInput = it
                errorMessage = null // Clear error when user starts typing
            },
            label = { Text("Enter your income") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.AttachMoney,
                    contentDescription = "Amount"
                )
            },
            modifier = Modifier.fillMaxWidth(),
            isError = errorMessage != null,
            supportingText = { errorMessage?.let { Text(it) } },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )

        Spacer(modifier = Modifier.height(16.dp))

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

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Saved Income: ৳${String.format("%.2f", savedIncome)}",
            style = MaterialTheme.typography.bodyLarge
        )
    }
}