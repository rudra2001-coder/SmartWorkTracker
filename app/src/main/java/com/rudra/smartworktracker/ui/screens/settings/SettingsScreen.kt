package com.rudra.smartworktracker.ui.screens.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navController: NavController) {
    val context = LocalContext.current
    val viewModel: SettingsViewModel = viewModel(factory = SettingsViewModelFactory(context))
    val userProfile by viewModel.userProfile.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            SettingsSection(title = "User Profile") {
                SettingsItem(title = userProfile?.name ?: "Manage Profile", onClick = { navController.navigate("user_profile") })
            }
            Spacer(modifier = Modifier.height(16.dp))
            SettingsSection(title = "Calculation Settings") {
                SettingsItem(title = "Meal & Overtime Rates", onClick = { /* Navigate to Calculation Settings */ })
            }
            Spacer(modifier = Modifier.height(16.dp))
            SettingsSection(title = "Data Management") {
                SettingsItem(title = "Backup & Restore", onClick = { /* Navigate to Backup/Restore */ })
            }
            Spacer(modifier = Modifier.height(16.dp))
            SettingsSection(title = "Appearance") {
                SettingsItem(title = "Theme & Language", onClick = { /* Navigate to Appearance Settings */ })
            }
        }
    }
}

@Composable
fun SettingsSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column {
        Text(text = title, style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))
        Card {
            Column {
                content()
            }
        }
    }
}

@Composable
fun SettingsItem(title: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = title, modifier = Modifier.weight(1f))
        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
    }
}
