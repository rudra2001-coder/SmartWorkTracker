package com.rudra.smartworktracker.ui.screens.settings

import android.app.Application
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navController: NavController) {
    val application = LocalContext.current.applicationContext as Application
    val viewModel: SettingsViewModel = viewModel(factory = SettingsViewModelFactory(application))

    var showResetDialog by remember { mutableStateOf(false) }
    var showMealRateDialog by remember { mutableStateOf(false) }
    var showBackupDialog by remember { mutableStateOf(false) }

    val mealRate by viewModel.mealRate.collectAsState()
    var newMealRate by remember(mealRate) { mutableStateOf(mealRate.toString()) }
    val isDarkTheme by viewModel.isDarkTheme.collectAsState()
    val notificationsEnabled by viewModel.notificationsEnabled.collectAsState()
    val vibrationEnabled by remember { mutableStateOf(true) }
    val autoBackupEnabled by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Settings",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Appearance Section
            item {
                SettingsSection(title = "Appearance", icon = Icons.Default.Palette) {
                    SettingsSwitchItem(
                        icon = Icons.Default.DarkMode,
                        title = "Dark Theme",
                        subtitle = "Switch between light and dark mode",
                        isChecked = isDarkTheme,
                        onCheckedChange = { viewModel.setDarkTheme(it) }
                    )
                }
            }

            // Notifications Section
            item {
                SettingsSection(title = "Notifications", icon = Icons.Default.Notifications) {
                    SettingsSwitchItem(
                        icon = Icons.Default.NotificationsActive,
                        title = "Enable Notifications",
                        subtitle = "Receive app notifications",
                        isChecked = notificationsEnabled,
                        onCheckedChange = { viewModel.setNotifications(it) }
                    )
                    SettingsSwitchItem(
                        icon = Icons.Default.Vibration,
                        title = "Enable Vibration",
                        subtitle = "Vibrate on notifications",
                        isChecked = vibrationEnabled,
                        onCheckedChange = { /* Handle vibration */ }
                    )
                }
            }

            // Financial Settings Section
            item {
                SettingsSection(title = "Financial", icon = Icons.Default.AttachMoney) {
                    SettingsItem(
                        icon = Icons.Default.Restaurant,
                        title = "Meal Rate",
                        subtitle = "Current: ৳$mealRate per meal",
                        onClick = { showMealRateDialog = true }
                    )
                }
            }

            // Data Management Section
            item {
                SettingsSection(title = "Data Management", icon = Icons.Default.Storage) {
                    SettingsSwitchItem(
                        icon = Icons.Default.CloudUpload,
                        title = "Auto Backup",
                        subtitle = "Automatically backup your data",
                        isChecked = autoBackupEnabled,
                        onCheckedChange = { /* Handle auto backup */ }
                    )
                    SettingsItem(
                        icon = Icons.Default.Backup,
                        title = "Backup Data",
                        subtitle = "Create a backup of your data",
                        onClick = { showBackupDialog = true }
                    )
                    SettingsItem(
                        icon = Icons.Default.Restore,
                        title = "Restore Data",
                        subtitle = "Restore from previous backup",
                        onClick = { /* Handle restore */ }
                    )
                    SettingsItem(
                        icon = Icons.Default.Delete,
                        title = "Reset All Data",
                        subtitle = "Permanently delete all app data",
                        onClick = { showResetDialog = true },
                        isDestructive = true
                    )
                }
            }

            // About Section
            item {
                SettingsSection(title = "About", icon = Icons.Default.Info) {
                    SettingsItem(
                        icon = Icons.Default.Shield,
                        title = "Privacy Policy",
                        subtitle = "View our privacy policy",
                        onClick = { /* Navigate to privacy policy */ }
                    )
                    SettingsItem(
                        icon = Icons.Default.Description,
                        title = "Terms of Service",
                        subtitle = "View terms and conditions",
                        onClick = { /* Navigate to terms */ }
                    )
                    SettingsItem(
                        icon = Icons.Default.Email,
                        title = "Contact Support",
                        subtitle = "Get help and support",
                        onClick = { /* Open contact form */ }
                    )
                    SettingsItem(
                        icon = Icons.Default.Star,
                        title = "Rate App",
                        subtitle = "Share your experience",
                        onClick = { /* Open app store */ }
                    )
                }
            }

            // App Version
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "Smart Work Tracker",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Version 1.0.0",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                        )
                        Text(
                            "Build 2024.01.01",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(20.dp)) }
        }
    }

    // Meal Rate Dialog
    if (showMealRateDialog) {
        AlertDialog(
            onDismissRequest = { showMealRateDialog = false },
            title = {
                Text(
                    "Set Meal Rate",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )
            },
            text = {
                Column {
                    Text(
                        "Set the default cost per meal for expense calculations",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = newMealRate,
                        onValueChange = {
                            if (it.isEmpty() || it.toDoubleOrNull() != null) {
                                newMealRate = it
                            }
                        },
                        label = { Text("Meal Rate (৳)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        prefix = { Text("৳") }
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        newMealRate.toDoubleOrNull()?.let { rate ->
                            if (rate >= 0) {
                                viewModel.setMealRate(rate)
                                showMealRateDialog = false
                            }
                        }
                    },
                    enabled = newMealRate.toDoubleOrNull() != null && newMealRate.toDoubleOrNull()!! >= 0,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showMealRateDialog = false },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Cancel")
                }
            },
            shape = RoundedCornerShape(20.dp)
        )
    }

    // Reset Data Dialog
    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = {
                Text(
                    "Reset All Data",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )
            },
            text = {
                Column {
                    Icon(
                        Icons.Default.Warning,
                        contentDescription = "Warning",
                        modifier = Modifier
                            .size(48.dp)
                            .align(Alignment.CenterHorizontally),
                        tint = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "This will permanently delete:",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("• All journal entries")
                    Text("• Financial records")
                    Text("• User preferences")
                    Text("• App settings")
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "This action cannot be undone!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearAllData()
                        showResetDialog = false
                        // Show success message
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Reset All Data")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showResetDialog = false },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Cancel")
                }
            },
            shape = RoundedCornerShape(20.dp)
        )
    }

    // Backup Dialog
    if (showBackupDialog) {
        AlertDialog(
            onDismissRequest = { showBackupDialog = false },
            title = {
                Text(
                    "Backup Data",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )
            },
            text = {
                Column {
                    Icon(
                        Icons.Default.CloudUpload,
                        contentDescription = "Backup",
                        modifier = Modifier
                            .size(48.dp)
                            .align(Alignment.CenterHorizontally),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "Create a backup of your data to cloud storage?",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        // Handle backup
                        showBackupDialog = false
                    },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Backup Now")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showBackupDialog = false },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Later")
                }
            },
            shape = RoundedCornerShape(20.dp)
        )
    }
}

@Composable
fun SettingsSection(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Section Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    icon,
                    contentDescription = title,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            // Section Content
            content()
        }
    }
}

@Composable
fun SettingsSwitchItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                icon,
                contentDescription = title,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Switch(
            checked = isChecked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.primary,
                checkedTrackColor = MaterialTheme.colorScheme.primaryContainer,
                uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        )
    }
}

@Composable
fun SettingsItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    isDestructive: Boolean = false
) {
    val contentColor = if (isDestructive) MaterialTheme.colorScheme.error
    else MaterialTheme.colorScheme.onSurface

    val subtitleColor = if (isDestructive) MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
    else MaterialTheme.colorScheme.onSurfaceVariant

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = title,
            modifier = Modifier.size(24.dp),
            tint = contentColor
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                title,
                style = MaterialTheme.typography.bodyLarge,
                color = contentColor
            )
            Text(
                subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = subtitleColor
            )
        }
        Icon(
            Icons.Default.ChevronRight,
            contentDescription = "Navigate",
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}