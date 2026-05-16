package com.rudra.smartworktracker.ui.screens.settings

import android.app.Application
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.rudra.smartworktracker.ui.components.AppColors
import com.rudra.smartworktracker.ui.components.SectionHeader
import com.rudra.smartworktracker.ui.components.StandardCard
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navController: NavController) {
    val context = LocalContext.current
    val application = context.applicationContext as Application
    val viewModel: SettingsViewModel = viewModel(factory = SettingsViewModelFactory(application))
    val scope = rememberCoroutineScope()

    var showResetDialog by remember { mutableStateOf(false) }
    var showMealRateDialog by remember { mutableStateOf(false) }

    val mealRate by viewModel.mealRate.collectAsState()
    var newMealRate by remember(mealRate) { mutableStateOf(mealRate.toString()) }
    val isDarkTheme by viewModel.isDarkTheme.collectAsState()
    val notificationsEnabled by viewModel.notificationsEnabled.collectAsState()
    val vibrationEnabled by viewModel.vibrationEnabled.collectAsState()
    val autoBackupEnabled by viewModel.autoBackupEnabled.collectAsState()

    val backupLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        uri?.let { viewModel.createBackup(it) }
    }

    val restoreLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let { viewModel.restoreBackup(it) }
    }

    LaunchedEffect(Unit) {
        viewModel.backupResult.collect { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(Unit) {
        viewModel.restoreResult.collect { result ->
            result.onSuccess {
                Toast.makeText(context, "Data restored successfully", Toast.LENGTH_SHORT).show()
            }.onFailure {
                Toast.makeText(context, "Restore failed: ${it.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Settings",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = AppColors.PrimaryText
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = AppColors.PrimaryText)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AppColors.GlobalBackground,
                    titleContentColor = AppColors.PrimaryText
                )
            )
        },
        containerColor = AppColors.GlobalBackground
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            // Appearance Section
            item {
                SectionHeader(text = "Appearance")
                StandardCard {
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
                SectionHeader(text = "Notifications")
                StandardCard {
                    SettingsSwitchItem(
                        icon = Icons.Default.NotificationsActive,
                        title = "Enable Notifications",
                        subtitle = "Receive app notifications",
                        isChecked = notificationsEnabled,
                        onCheckedChange = { viewModel.setNotifications(it) }
                    )
                    Divider(color = AppColors.SecondaryText.copy(alpha = 0.2f), thickness = 0.5.dp)
                    SettingsSwitchItem(
                        icon = Icons.Default.Vibration,
                        title = "Enable Vibration",
                        subtitle = "Vibrate on notifications",
                        isChecked = vibrationEnabled,
                        onCheckedChange = { viewModel.setVibration(it) }
                    )
                }
            }

            // Financial Settings Section
            item {
                SectionHeader(text = "Financial")
                StandardCard {
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
                SectionHeader(text = "Data Management")
                StandardCard {
                    SettingsSwitchItem(
                        icon = Icons.Default.CloudUpload,
                        title = "Auto Backup",
                        subtitle = "Automatically backup your data",
                        isChecked = autoBackupEnabled,
                        onCheckedChange = { viewModel.setAutoBackup(it) }
                    )
                    Divider(color = AppColors.SecondaryText.copy(alpha = 0.2f), thickness = 0.5.dp)
                    SettingsItem(
                        icon = Icons.Default.Backup,
                        title = "Backup Data",
                        subtitle = "Create a backup file of your data",
                        onClick = {
                            backupLauncher.launch("smart_work_tracker_backup_${System.currentTimeMillis()}.json")
                        }
                    )
                    Divider(color = AppColors.SecondaryText.copy(alpha = 0.2f), thickness = 0.5.dp)
                    SettingsItem(
                        icon = Icons.Default.Restore,
                        title = "Restore Data",
                        subtitle = "Restore from previous backup file",
                        onClick = {
                            restoreLauncher.launch(arrayOf("application/json", "application/octet-stream"))
                        }
                    )
                    Divider(color = AppColors.SecondaryText.copy(alpha = 0.2f), thickness = 0.5.dp)
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
                SectionHeader(text = "About")
                StandardCard {
                    SettingsItem(
                        icon = Icons.Default.Shield,
                        title = "Privacy Policy",
                        subtitle = "View our privacy policy",
                        onClick = {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://rudra2001-coder.github.io/my/"))
                            context.startActivity(intent)
                        }
                    )
                    Divider(color = AppColors.SecondaryText.copy(alpha = 0.2f), thickness = 0.5.dp)
                    SettingsItem(
                        icon = Icons.Default.Description,
                        title = "Terms of Service",
                        subtitle = "View terms and conditions",
                        onClick = {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://rudra2001-coder.github.io/my/"))
                            context.startActivity(intent)
                        }
                    )
                    Divider(color = AppColors.SecondaryText.copy(alpha = 0.2f), thickness = 0.5.dp)
                    SettingsItem(
                        icon = Icons.Default.Email,
                        title = "Contact Support",
                        subtitle = "Get help and support",
                        onClick = {
                            val intent = Intent(Intent.ACTION_SENDTO).apply {
                                data = Uri.parse("mailto:mhrudra064@gmail.com\n")
                                putExtra(Intent.EXTRA_SUBJECT, "Smart Work Tracker Support")
                            }
                            try {
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                Toast.makeText(context, "No email app found", Toast.LENGTH_SHORT).show()
                            }
                        }
                    )
                    Divider(color = AppColors.SecondaryText.copy(alpha = 0.2f), thickness = 0.5.dp)
                    SettingsItem(
                        icon = Icons.Default.Star,
                        title = "Rate App",
                        subtitle = "Share your experience",
                        onClick = {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=${context.packageName}"))
                            try {
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=${context.packageName}"))
                                context.startActivity(webIntent)
                            }
                        }
                    )
                }
            }

            // App Version
            item {
                StandardCard {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "Smart Work Tracker",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = AppColors.PrimaryText
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Version 1.0.0",
                            style = MaterialTheme.typography.bodyMedium,
                            color = AppColors.SecondaryText
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
                    fontWeight = FontWeight.SemiBold,
                    color = AppColors.PrimaryText
                )
            },
            text = {
                Column {
                    Text(
                        "Set the default cost per meal for expense calculations",
                        style = MaterialTheme.typography.bodyMedium,
                        color = AppColors.SecondaryText
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
                        prefix = { Text("৳") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AppColors.OfficeBlue,
                            focusedLabelColor = AppColors.OfficeBlue
                        )
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
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AppColors.OfficeBlue)
                ) {
                    Text("Save", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showMealRateDialog = false },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Cancel", color = AppColors.SecondaryText)
                }
            },
            shape = RoundedCornerShape(20.dp),
            containerColor = AppColors.CardBackground
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
                    fontWeight = FontWeight.SemiBold,
                    color = AppColors.PrimaryText
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
                        tint = AppColors.ExpenseRed
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "This will permanently delete:",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = AppColors.PrimaryText
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("• All journal entries", color = AppColors.SecondaryText)
                    Text("• Financial records", color = AppColors.SecondaryText)
                    Text("• User preferences", color = AppColors.SecondaryText)
                    Text("• App settings", color = AppColors.SecondaryText)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "This action cannot be undone!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = AppColors.ExpenseRed,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearAllData()
                        showResetDialog = false
                        Toast.makeText(context, "All data has been reset", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AppColors.ExpenseRed,
                        contentColor = Color.White
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
                    Text("Cancel", color = AppColors.SecondaryText)
                }
            },
            shape = RoundedCornerShape(20.dp),
            containerColor = AppColors.CardBackground
        )
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
        Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
            Icon(
                icon,
                contentDescription = title,
                modifier = Modifier.size(24.dp),
                tint = AppColors.SecondaryText
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = AppColors.PrimaryText
                )
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = AppColors.SecondaryText
                )
            }
        }
        Switch(
            checked = isChecked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = AppColors.IncomeGreen,
                checkedTrackColor = AppColors.IncomeGreen.copy(alpha = 0.5f),
                uncheckedThumbColor = AppColors.SecondaryText,
                uncheckedTrackColor = AppColors.SecondaryText.copy(alpha = 0.3f)
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
    val contentColor = if (isDestructive) AppColors.ExpenseRed else AppColors.PrimaryText
    val subtitleColor = if (isDestructive) AppColors.ExpenseRed.copy(alpha = 0.8f) else AppColors.SecondaryText

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
            tint = AppColors.SecondaryText
        )
    }
}
