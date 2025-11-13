package com.rudra.smartworktracker.ui.screens.user_profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rudra.smartworktracker.data.entity.Language
import com.rudra.smartworktracker.data.entity.SalaryPeriod
import com.rudra.smartworktracker.data.entity.UserProfile

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserProfileScreen(viewModel: UserProfileViewModel = viewModel(), onNavigateBack: () -> Unit) {
    val userProfile by viewModel.userProfile.collectAsState(initial = null)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("User Profile") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        userProfile?.let {
            UserProfileForm(userProfile = it, onSave = viewModel::saveUserProfile, modifier = Modifier.padding(paddingValues))
        }
    }
}

@Composable
fun UserProfileForm(userProfile: UserProfile, onSave: (UserProfile) -> Unit, modifier: Modifier = Modifier) {
    var name by remember { mutableStateOf(userProfile.name) }
    var monthlySalary by remember { mutableStateOf(userProfile.monthlySalary.toString()) }
    var initialSavings by remember { mutableStateOf(userProfile.initialSavings.toString()) }
    var salaryPeriod by remember { mutableStateOf(userProfile.salaryPeriod) }
    var language by remember { mutableStateOf(userProfile.language) }

    Column(modifier = modifier.padding(16.dp)) {
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Name") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = monthlySalary,
            onValueChange = { monthlySalary = it },
            label = { Text("Monthly Salary") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = initialSavings,
            onValueChange = { initialSavings = it },
            label = { Text("Initial Savings") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        // Add dropdowns for SalaryPeriod and Language
        Button(onClick = {
            onSave(
                userProfile.copy(
                    name = name,
                    monthlySalary = monthlySalary.toDoubleOrNull() ?: 0.0,
                    initialSavings = initialSavings.toDoubleOrNull() ?: 0.0,
                    salaryPeriod = salaryPeriod,
                    language = language
                )
            )
        }) {
            Text("Save")
        }
    }
}
