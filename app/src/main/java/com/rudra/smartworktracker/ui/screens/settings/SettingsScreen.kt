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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import kotlinx.coroutines.launch

private val CardShape = RoundedCornerShape(20.dp)
private val ChipShape = RoundedCornerShape(12.dp)
private val PillShape = RoundedCornerShape(50.dp)

private val EmeraldGreen = Color(0xFF00C896)
private val CoralRed = Color(0xFFFF5757)
private val SapphireBlue = Color(0xFF3B82F6)
private val GoldenAmber = Color(0xFFF59E0B)
private val VioletPurple = Color(0xFF8B5CF6)

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
    ) { uri -> uri?.let { viewModel.createBackup(it) } }

    val restoreLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri -> uri?.let { viewModel.restoreBackup(it) } }

    LaunchedEffect(Unit) { viewModel.backupResult.collect { message -> Toast.makeText(context, message, Toast.LENGTH_SHORT).show() } }
    LaunchedEffect(Unit) { viewModel.restoreResult.collect { result -> result.onSuccess { Toast.makeText(context, "Data restored successfully", Toast.LENGTH_SHORT).show() }.onFailure { Toast.makeText(context, "Restore failed: ${it.message}", Toast.LENGTH_SHORT).show() } } }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {}
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 20.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier.size(52.dp).background(
                            brush = Brush.linearGradient(listOf(SapphireBlue, VioletPurple)),
                            shape = RoundedCornerShape(14.dp)
                        ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Settings, contentDescription = null, tint = Color.White, modifier = Modifier.size(28.dp))
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Settings", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        Text("Customize your experience", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            item { SettingsSectionHeader("Appearance", SapphireBlue) }
            item {
                Card(shape = CardShape, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), elevation = CardDefaults.cardElevation(0.dp), modifier = Modifier.fillMaxWidth().shadow(6.dp, CardShape, clip = false)) {
                    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                        SettingsSwitchItem(icon = Icons.Default.DarkMode, title = "Dark Theme", subtitle = "Switch between light and dark mode", isChecked = isDarkTheme, onCheckedChange = { viewModel.setDarkTheme(it) })
                    }
                }
            }

            item { SettingsSectionHeader("Notifications", VioletPurple) }
            item {
                Card(shape = CardShape, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), elevation = CardDefaults.cardElevation(0.dp), modifier = Modifier.fillMaxWidth().shadow(6.dp, CardShape, clip = false)) {
                    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                        SettingsSwitchItem(icon = Icons.Default.NotificationsActive, title = "Enable Notifications", subtitle = "Receive app notifications", isChecked = notificationsEnabled, onCheckedChange = { viewModel.setNotifications(it) })
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), thickness = 0.5.dp, modifier = Modifier.padding(start = 40.dp))
                        SettingsSwitchItem(icon = Icons.Default.Vibration, title = "Enable Vibration", subtitle = "Vibrate on notifications", isChecked = vibrationEnabled, onCheckedChange = { viewModel.setVibration(it) })
                    }
                }
            }

            item { SettingsSectionHeader("Financial", GoldenAmber) }
            item {
                Card(shape = CardShape, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), elevation = CardDefaults.cardElevation(0.dp), modifier = Modifier.fillMaxWidth().shadow(6.dp, CardShape, clip = false)) {
                    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                        SettingsItem(icon = Icons.Default.Restaurant, title = "Meal Rate", subtitle = "Current: ৳$mealRate per meal", onClick = { showMealRateDialog = true })
                    }
                }
            }

            item { SettingsSectionHeader("Data Management", CoralRed) }
            item {
                Card(shape = CardShape, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), elevation = CardDefaults.cardElevation(0.dp), modifier = Modifier.fillMaxWidth().shadow(6.dp, CardShape, clip = false)) {
                    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                        SettingsSwitchItem(icon = Icons.Default.CloudUpload, title = "Auto Backup", subtitle = "Automatically backup your data", isChecked = autoBackupEnabled, onCheckedChange = { viewModel.setAutoBackup(it) })
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), thickness = 0.5.dp, modifier = Modifier.padding(start = 40.dp))
                        SettingsItem(icon = Icons.Default.Backup, title = "Backup Data", subtitle = "Create a backup file of your data", onClick = { backupLauncher.launch("smart_work_tracker_backup_${System.currentTimeMillis()}.json") })
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), thickness = 0.5.dp, modifier = Modifier.padding(start = 40.dp))
                        SettingsItem(icon = Icons.Default.Restore, title = "Restore Data", subtitle = "Restore from previous backup file", onClick = { restoreLauncher.launch(arrayOf("application/json", "application/octet-stream")) })
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), thickness = 0.5.dp, modifier = Modifier.padding(start = 40.dp))
                        SettingsItem(icon = Icons.Default.Delete, title = "Reset All Data", subtitle = "Permanently delete all app data", onClick = { showResetDialog = true }, isDestructive = true)
                    }
                }
            }

            item { SettingsSectionHeader("About", SapphireBlue) }
            item {
                Card(shape = CardShape, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), elevation = CardDefaults.cardElevation(0.dp), modifier = Modifier.fillMaxWidth().shadow(6.dp, CardShape, clip = false)) {
                    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                        SettingsItem(icon = Icons.Default.Shield, title = "Privacy Policy", subtitle = "View our privacy policy", onClick = { val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://rudra2001-coder.github.io/my/")); context.startActivity(intent) })
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), thickness = 0.5.dp, modifier = Modifier.padding(start = 40.dp))
                        SettingsItem(icon = Icons.Default.Description, title = "Terms of Service", subtitle = "View terms and conditions", onClick = { val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://rudra2001-coder.github.io/my/")); context.startActivity(intent) })
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), thickness = 0.5.dp, modifier = Modifier.padding(start = 40.dp))
                        SettingsItem(icon = Icons.Default.Email, title = "Contact Support", subtitle = "Get help and support", onClick = { val intent = Intent(Intent.ACTION_SENDTO).apply { data = Uri.parse("mailto:mhrudra064@gmail.com"); putExtra(Intent.EXTRA_SUBJECT, "Smart Work Tracker Support") }; try { context.startActivity(intent) } catch (e: Exception) { Toast.makeText(context, "No email app found", Toast.LENGTH_SHORT).show() } })
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), thickness = 0.5.dp, modifier = Modifier.padding(start = 40.dp))
                        SettingsItem(icon = Icons.Default.Star, title = "Rate App", subtitle = "Share your experience", onClick = { val intent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=${context.packageName}")); try { context.startActivity(intent) } catch (e: Exception) { val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=${context.packageName}")); context.startActivity(webIntent) } })
                    }
                }
            }

            item {
                Card(shape = CardShape, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), elevation = CardDefaults.cardElevation(0.dp), modifier = Modifier.fillMaxWidth().shadow(6.dp, CardShape, clip = false)) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Smart Work Tracker", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                        Spacer(Modifier.height(4.dp))
                        Text("Version 1.0.0", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            item { Spacer(Modifier.height(20.dp)) }
        }
    }

    if (showMealRateDialog) {
        AlertDialog(
            onDismissRequest = { showMealRateDialog = false },
            shape = CardShape,
            title = { Text("Set Meal Rate", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold) },
            text = {
                Column {
                    Text("Set the default cost per meal for expense calculations", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(16.dp))
                    OutlinedTextField(
                        value = newMealRate,
                        onValueChange = { if (it.isEmpty() || it.toDoubleOrNull() != null) newMealRate = it },
                        label = { Text("Meal Rate (৳)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth(),
                        shape = ChipShape,
                        prefix = { Text("৳") },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = SapphireBlue, focusedLabelColor = SapphireBlue)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { newMealRate.toDoubleOrNull()?.let { rate -> if (rate >= 0) { viewModel.setMealRate(rate); showMealRateDialog = false } } },
                    enabled = newMealRate.toDoubleOrNull() != null && newMealRate.toDoubleOrNull()!! >= 0,
                    shape = ChipShape,
                    colors = ButtonDefaults.buttonColors(containerColor = SapphireBlue)
                ) { Text("Save", color = Color.White) }
            },
            dismissButton = { TextButton(onClick = { showMealRateDialog = false }) { Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant) } },
            containerColor = MaterialTheme.colorScheme.surface
        )
    }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            shape = CardShape,
            title = { Text("Reset All Data", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold) },
            text = {
                Column {
                    Icon(Icons.Default.Warning, contentDescription = "Warning", modifier = Modifier.size(48.dp).align(Alignment.CenterHorizontally), tint = CoralRed)
                    Spacer(Modifier.height(16.dp))
                    Text("This will permanently delete:", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
                    Spacer(Modifier.height(8.dp))
                    listOf("All journal entries", "Financial records", "User preferences", "App settings").forEach { Text("• $it", color = MaterialTheme.colorScheme.onSurfaceVariant) }
                    Spacer(Modifier.height(16.dp))
                    Text("This action cannot be undone!", style = MaterialTheme.typography.bodyMedium, color = CoralRed, fontWeight = FontWeight.SemiBold)
                }
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.clearAllData(); showResetDialog = false; Toast.makeText(context, "All data has been reset", Toast.LENGTH_SHORT).show() },
                    colors = ButtonDefaults.buttonColors(containerColor = CoralRed, contentColor = Color.White),
                    shape = ChipShape
                ) { Text("Reset All Data") }
            },
            dismissButton = { TextButton(onClick = { showResetDialog = false }) { Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant) } },
            containerColor = MaterialTheme.colorScheme.surface
        )
    }
}

@Composable
private fun SettingsSectionHeader(text: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(start = 2.dp, top = 4.dp)) {
        Box(Modifier.size(6.dp).background(color, PillShape))
        Text(text, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = title, modifier = Modifier.size(24.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.width(16.dp))
            Column {
                Text(title, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
                Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Switch(
            checked = isChecked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = EmeraldGreen,
                checkedTrackColor = EmeraldGreen.copy(alpha = 0.5f),
                uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant,
                uncheckedTrackColor = MaterialTheme.colorScheme.outline
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
    val contentColor = if (isDestructive) CoralRed else MaterialTheme.colorScheme.onSurface
    val subtitleColor = if (isDestructive) CoralRed.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant

    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = title, modifier = Modifier.size(24.dp), tint = if (isDestructive) CoralRed else MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge, color = contentColor)
            Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = subtitleColor)
        }
        Icon(Icons.Default.ChevronRight, contentDescription = "Navigate", modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
