package com.rudra.smartworktracker.ui.screens.backup

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Update
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDefaults
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rudra.smartworktracker.data.backup.BackupEntry
import com.rudra.smartworktracker.data.backup.ExportResult as BackupExportResult
import com.rudra.smartworktracker.data.backup.RestorePreview
import java.text.SimpleDateFormat
import java.util.*

private val CardShape = RoundedCornerShape(20.dp)
private val ChipShape = RoundedCornerShape(12.dp)
private val PillShape = RoundedCornerShape(50.dp)
private val EmeraldGreen = Color(0xFF00C896)
private val CoralRed = Color(0xFFFF5757)
private val SapphireBlue = Color(0xFF3B82F6)
private val GoldenAmber = Color(0xFFF59E0B)
private val VioletPurple = Color(0xFF8B5CF6)
private val GreenSurface = Color(0xFFE6FBF4)
private val RedSurface = Color(0xFFFFEDED)
private val BlueSurface = Color(0xFFEFF6FF)
private val AmberSurface = Color(0xFFFFFBEB)
private val PurpleSurface = Color(0xFFF5F3FF)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackupScreen(onNavigateBack: () -> Unit) {
    val context = LocalContext.current
    val viewModel: BackupViewModel = viewModel(factory = BackupViewModelFactory(context))
    val backupState by viewModel.backupState.collectAsState()
    val lastBackupTime by viewModel.lastBackupTime.collectAsState()
    val isAutoBackupEnabled by viewModel.isAutoBackupEnabled.collectAsState()
    val lastExportResult by viewModel.lastExportResult.collectAsState()
    val restorePreview by viewModel.restorePreview.collectAsState()
    val hasStoredBackup by viewModel.hasStoredBackup.collectAsState()
    val backupHistory by viewModel.backupHistory.collectAsState()
    val retentionLimit by viewModel.retentionLimit.collectAsState()
    val backupHour by viewModel.backupHour.collectAsState()
    val backupMinute by viewModel.backupMinute.collectAsState()
    val backupTimeDisplay by viewModel.backupTimeDisplay.collectAsState()

    var showRestoreConfirmDialog by remember { mutableStateOf<android.net.Uri?>(null) }
    var entryToDelete by remember { mutableStateOf<BackupEntry?>(null) }
    var showRetentionDialog by remember { mutableStateOf(false) }
    var retentionInput by remember { mutableIntStateOf(retentionLimit) }
    var showTimePickerDialog by remember { mutableStateOf(false) }
    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy - hh:mm a", Locale.getDefault()) }

    val restorePickLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri ->
            uri?.let {
                viewModel.previewBackup(it)
                showRestoreConfirmDialog = it
            }
        }
    )

    val backupLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json"),
        onResult = { uri -> uri?.let { viewModel.createBackup(it) } }
    )

    LaunchedEffect(backupState) {
        when (val state = backupState) {
            is BackupState.Success -> {
                Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
                viewModel.onStateConsumed()
            }
            is BackupState.Error -> {
                Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
                viewModel.onStateConsumed()
            }
            else -> {}
        }
    }

    LaunchedEffect(retentionLimit) { retentionInput = retentionLimit }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {}
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .background(
                                brush = Brush.linearGradient(listOf(SapphireBlue, VioletPurple)),
                                shape = RoundedCornerShape(14.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Backup, null, tint = Color.White, modifier = Modifier.size(28.dp))
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Backup & Restore", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
                        Text("Protect your data", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            }

            item { StatusHeaderCard(lastBackupTime, isAutoBackupEnabled, dateFormat, lastExportResult) }

            item { AutoBackupToggleCard(isAutoBackupEnabled, backupTimeDisplay, onTimeClick = { showTimePickerDialog = true }, onToggle = { viewModel.toggleAutoBackup(it) }) }

            item { RetentionSettingsCard(retentionLimit) { showRetentionDialog = true } }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ActionTile(
                        modifier = Modifier.weight(1f),
                        title = "Export Now",
                        icon = Icons.Default.FileUpload,
                        color = EmeraldGreen,
                        description = "Create full JSON backup",
                        badge = lastExportResult?.let { "${it.totalRows} records" },
                        enabled = backupState !is BackupState.InProgress &&
                            backupState !is BackupState.Exporting,
                        onClick = {
                            val ts = SimpleDateFormat("yyyyMMdd_HHmm", Locale.getDefault()).format(Date())
                            backupLauncher.launch("smart_work_backup_$ts.json")
                        }
                    )
                    ActionTile(
                        modifier = Modifier.weight(1f),
                        title = "Restore",
                        icon = Icons.Default.FileDownload,
                        color = VioletPurple,
                        description = "Restore from backup file",
                        badge = null,
                        enabled = backupState !is BackupState.InProgress &&
                            backupState !is BackupState.Restoring,
                        onClick = { restorePickLauncher.launch(arrayOf("application/json")) }
                    )
                }
            }

            item { DataIntegrityCard() }

            if (backupState is BackupState.Exporting) {
                item { ProgressCard((backupState as BackupState.Exporting).progress, EmeraldGreen) }
            }
            if (backupState is BackupState.Restoring) {
                item { ProgressCard((backupState as BackupState.Restoring).progress, VioletPurple) }
            }

            if (backupHistory.isNotEmpty()) {
                item { BackupHistoryListCard(backupHistory, dateFormat, { entryToDelete = it }) }
            }

            item { Spacer(Modifier.height(40.dp)) }
        }
    }

    if (showRestoreConfirmDialog != null && restorePreview != null) {
        RestorePreviewDialog(
            preview = restorePreview!!,
            isRestoring = backupState is BackupState.Restoring,
            onConfirm = { showRestoreConfirmDialog?.let { viewModel.restoreBackup(it) } },
            onDismiss = { showRestoreConfirmDialog = null; viewModel.clearRestorePreview() }
        )
    }

    if (entryToDelete != null) {
        AlertDialog(
            onDismissRequest = { entryToDelete = null },
            shape = CardShape,
            icon = {
                Box(Modifier.size(40.dp).background(CoralRed.copy(alpha = 0.1f), RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Delete, null, tint = CoralRed, modifier = Modifier.size(20.dp))
                }
            },
            title = { Text("Delete Backup", fontWeight = FontWeight.Bold) },
            text = {
                Text("Delete \"${entryToDelete!!.fileName}\"? This will permanently remove the backup file from your device.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        entryToDelete?.let { viewModel.deleteBackupEntry(it) }
                        entryToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CoralRed)
                ) { Text("Delete File", fontWeight = FontWeight.SemiBold) }
            },
            dismissButton = { TextButton(onClick = { entryToDelete = null }) { Text("Cancel") } }
        )
    }

    if (showRetentionDialog) {
        AlertDialog(
            onDismissRequest = { showRetentionDialog = false },
            shape = CardShape,
            icon = {
                Box(Modifier.size(40.dp).background(GoldenAmber.copy(alpha = 0.1f), RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Settings, null, tint = GoldenAmber, modifier = Modifier.size(20.dp))
                }
            },
            title = { Text("Backup Retention", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Set how many backups to keep. When exceeded, the oldest backups are auto-deleted.", style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.height(4.dp))
                    Text("Current: ${backupHistory.size} backup(s) saved", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(8.dp))
                    Surface(shape = ChipShape, color = BlueSurface) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text("Keep latest", style = MaterialTheme.typography.bodyMedium)
                            OutlinedTextField(
                                value = if (retentionInput <= 0) "" else "$retentionInput",
                                onValueChange = {
                                    retentionInput = it.filter { c -> c.isDigit() }.take(2).toIntOrNull() ?: 0
                                },
                                modifier = Modifier.width(72.dp),
                                singleLine = true,
                                placeholder = { Text("0") },
                                shape = ChipShape
                            )
                            Text("backup(s)", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                    Text("Set to 0 to keep all backups indefinitely.", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.setRetentionLimit(retentionInput)
                        showRetentionDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GoldenAmber)
                ) { Text("Apply", fontWeight = FontWeight.SemiBold) }
            },
            dismissButton = { TextButton(onClick = { showRetentionDialog = false }) { Text("Cancel") } }
        )
    }

    if (showTimePickerDialog) {
        BackupTimePickerDialog(
            currentHour = backupHour,
            currentMinute = backupMinute,
            onConfirm = { hour, minute -> viewModel.updateBackupTime(hour, minute); showTimePickerDialog = false },
            onDismiss = { showTimePickerDialog = false }
        )
    }
}

@Composable
private fun StatusHeaderCard(
    lastBackupTime: Long, isAutoBackupEnabled: Boolean,
    dateFormat: SimpleDateFormat, lastExportResult: BackupExportResult?
) {
    val accentColor = if (isAutoBackupEnabled) EmeraldGreen else CoralRed
    val statusIcon = if (isAutoBackupEnabled) Icons.Default.CloudDone else Icons.Default.CloudOff
    val statusText = if (isAutoBackupEnabled) "Automatic Protection Active" else "Manual Protection Only"
    val lastBackupStr = if (lastBackupTime > 0) dateFormat.format(Date(lastBackupTime)) else "Never"

    Card(modifier = Modifier.fillMaxWidth().shadow(8.dp, CardShape, clip = false),
        shape = CardShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(0.dp)) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(Modifier.size(36.dp).background(
                    brush = Brush.linearGradient(listOf(accentColor, if (isAutoBackupEnabled) EmeraldGreen else CoralRed)),
                    shape = RoundedCornerShape(10.dp)), contentAlignment = Alignment.Center) {
                    Icon(statusIcon, null, tint = Color.White, modifier = Modifier.size(18.dp))
                }
                Column {
                    Text("Backup Status", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    Text(statusText, style = MaterialTheme.typography.labelSmall, color = accentColor, fontWeight = FontWeight.SemiBold)
                }
            }
            Spacer(Modifier.height(16.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatBadge(Modifier.weight(1f), "Last Backup", lastBackupStr, SapphireBlue, BlueSurface, Icons.Default.Update)
                StatBadge(Modifier.weight(1f), "Records", lastExportResult?.let { formatCount(it.totalRows) } ?: "—", EmeraldGreen, GreenSurface, Icons.Default.Description)
            }
            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatBadge(Modifier.weight(1f), "File Size", lastExportResult?.let { formatFileSize(it.fileSizeBytes) } ?: "—", VioletPurple, PurpleSurface, Icons.Default.Info)
                StatBadge(Modifier.weight(1f), "Auto Backup", if (isAutoBackupEnabled) "Enabled" else "Off",
                    if (isAutoBackupEnabled) EmeraldGreen else CoralRed,
                    if (isAutoBackupEnabled) GreenSurface else RedSurface,
                    if (isAutoBackupEnabled) Icons.Default.CheckCircle else Icons.Default.Warning)
            }
        }
    }
}

@Composable
private fun StatBadge(modifier: Modifier, label: String, value: String, color: Color, bgColor: Color, icon: ImageVector) {
    Surface(modifier = modifier, shape = ChipShape, color = bgColor) {
        Row(Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(Modifier.size(26.dp).background(color.copy(alpha = 0.15f), RoundedCornerShape(7.dp)), contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = color, modifier = Modifier.size(13.dp))
            }
            Column {
                Text(value, fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.labelMedium, color = color, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
            }
        }
    }
}

@Composable
private fun AutoBackupToggleCard(isEnabled: Boolean, backupTimeDisplay: String, onTimeClick: () -> Unit, onToggle: (Boolean) -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().shadow(8.dp, CardShape, clip = false), shape = CardShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), elevation = CardDefaults.cardElevation(0.dp)) {
        Row(Modifier.padding(20.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(Modifier.size(36.dp).background(
                    brush = Brush.linearGradient(listOf(if (isEnabled) EmeraldGreen else CoralRed, if (isEnabled) SapphireBlue else VioletPurple)),
                    shape = RoundedCornerShape(10.dp)), contentAlignment = Alignment.Center) {
                    Icon(if (isEnabled) Icons.Default.CloudDone else Icons.Default.CloudOff, null, tint = Color.White, modifier = Modifier.size(18.dp))
                }
                Column(Modifier.weight(1f)) {
                    Text("Daily Auto-Backup", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                    if (isEnabled) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Active — runs at ", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            TextButton(onClick = onTimeClick, contentPadding = PaddingValues(0.dp), modifier = Modifier.height(20.dp)) {
                                Text(backupTimeDisplay, fontWeight = FontWeight.SemiBold, color = SapphireBlue, style = MaterialTheme.typography.bodySmall)
                            }
                            Text(" daily", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    } else {
                        Text("Inactive — enable for automatic protection",
                            style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            Switch(checked = isEnabled, onCheckedChange = onToggle,
                colors = SwitchDefaults.colors(checkedThumbColor = EmeraldGreen, checkedTrackColor = EmeraldGreen.copy(alpha = 0.3f)))
        }
    }
}

@Composable
private fun RetentionSettingsCard(currentLimit: Int, onConfigure: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().shadow(8.dp, CardShape, clip = false), shape = CardShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), elevation = CardDefaults.cardElevation(0.dp)) {
        Row(Modifier.padding(20.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(Modifier.size(36.dp).background(
                brush = Brush.linearGradient(listOf(GoldenAmber, CoralRed)), shape = RoundedCornerShape(10.dp)), contentAlignment = Alignment.Center) {
                Icon(Icons.Default.Settings, null, tint = Color.White, modifier = Modifier.size(18.dp))
            }
            Column(Modifier.weight(1f)) {
                Text("Backup Retention", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Text(
                    if (currentLimit > 0) "Keep latest $currentLimit backup(s)" else "Keep all backups",
                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            TextButton(onClick = onConfigure) { Text("Configure", fontWeight = FontWeight.SemiBold, color = GoldenAmber) }
        }
    }
}

@Composable
private fun ActionTile(modifier: Modifier, title: String, icon: ImageVector, color: Color, description: String, badge: String?, enabled: Boolean, onClick: () -> Unit) {
    Surface(onClick = onClick, modifier = modifier, shape = CardShape, color = MaterialTheme.colorScheme.surface, enabled = enabled) {
        Column(Modifier.shadow(8.dp, CardShape, clip = false).background(MaterialTheme.colorScheme.surface, CardShape).padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(Modifier.size(48.dp).background(brush = Brush.linearGradient(listOf(color.copy(alpha = 0.7f), color)), shape = RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = Color.White, modifier = Modifier.size(24.dp))
            }
            Spacer(Modifier.height(12.dp))
            Text(title, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            Text(description, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center, maxLines = 2)
            if (badge != null) { Spacer(Modifier.height(8.dp)); Surface(shape = PillShape, color = color.copy(alpha = 0.1f)) { Text(badge, modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold, color = color) } }
        }
    }
}

@Composable
private fun ProgressCard(progress: String, accentColor: Color) {
    Card(modifier = Modifier.fillMaxWidth().shadow(8.dp, CardShape, clip = false), shape = CardShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), elevation = CardDefaults.cardElevation(0.dp)) {
        Column(Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(color = accentColor, modifier = Modifier.size(32.dp), strokeWidth = 3.dp)
            Spacer(Modifier.height(12.dp)); Text("Processing...", fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(4.dp))
            Text(progress, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
        }
    }
}

@Composable
private fun DataIntegrityCard() {
    Card(modifier = Modifier.fillMaxWidth().shadow(8.dp, CardShape, clip = false), shape = CardShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), elevation = CardDefaults.cardElevation(0.dp)) {
        Column(Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(Modifier.size(36.dp).background(brush = Brush.linearGradient(listOf(EmeraldGreen, SapphireBlue)), shape = RoundedCornerShape(10.dp)), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Security, null, tint = Color.White, modifier = Modifier.size(18.dp))
                }
                Column { Text("Data Integrity", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium); Text("Offline-first storage", style = MaterialTheme.typography.labelSmall, color = SapphireBlue) }
            }
            Spacer(Modifier.height(12.dp))
            Text("Your data stays entirely on-device. Backups are JSON snapshots that can be safely transferred between devices. The restore process uses a transaction-safe mechanism — if anything goes wrong, your existing data is preserved.",
                style = MaterialTheme.typography.bodySmall, lineHeight = 18.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun BackupHistoryListCard(entries: List<BackupEntry>, dateFormat: SimpleDateFormat, onDelete: (BackupEntry) -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().shadow(8.dp, CardShape, clip = false), shape = CardShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), elevation = CardDefaults.cardElevation(0.dp)) {
        Column(Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(Modifier.size(36.dp).background(brush = Brush.linearGradient(listOf(GoldenAmber, CoralRed)), shape = RoundedCornerShape(10.dp)), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.History, null, tint = Color.White, modifier = Modifier.size(18.dp))
                }
                Column { Text("Backup History", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium); Text("${entries.size} backup(s) saved", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary) }
            }
            Spacer(Modifier.height(16.dp))

            entries.take(20).forEach { entry ->
                Spacer(Modifier.height(4.dp))
                BackupHistoryItem(entry = entry, dateFormat = dateFormat, onDelete = { onDelete(entry) })
            }
            if (entries.size > 20) {
                Spacer(Modifier.height(8.dp))
                Text("... and ${entries.size - 20} more", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun BackupHistoryItem(entry: BackupEntry, dateFormat: SimpleDateFormat, onDelete: () -> Unit) {
    Surface(shape = ChipShape, color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)) {
        Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(Modifier.size(32.dp).background(
                if (entry.isManual) VioletPurple.copy(alpha = 0.12f) else EmeraldGreen.copy(alpha = 0.12f),
                RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) {
                Icon(if (entry.isManual) Icons.Default.Person else Icons.Default.CloudDone,
                    null, tint = if (entry.isManual) VioletPurple else EmeraldGreen, modifier = Modifier.size(16.dp))
            }
            Column(Modifier.weight(1f)) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(entry.fileName, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f, fill = false).width(120.dp))
                    Surface(shape = PillShape, color = if (entry.isManual) VioletPurple.copy(alpha = 0.1f) else EmeraldGreen.copy(alpha = 0.1f)) {
                        Text(entry.displayType, modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp), style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp), fontWeight = FontWeight.SemiBold, color = if (entry.isManual) VioletPurple else EmeraldGreen)
                    }
                }
                Spacer(Modifier.height(2.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(try { dateFormat.format(Date(entry.timestamp)) } catch (_: Exception) { "Unknown" }, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
                    if (entry.totalRows > 0) Text("${entry.displayRows} rows", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    if (entry.fileSizeBytes > 0) Text(entry.displaySize, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.Delete, "Delete", tint = CoralRed.copy(alpha = 0.7f), modifier = Modifier.size(16.dp))
            }
        }
    }
}

@Composable
private fun RestorePreviewDialog(preview: RestorePreview, isRestoring: Boolean, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(onDismissRequest = { if (!isRestoring) onDismiss() }, shape = CardShape,
        icon = {
            Box(Modifier.size(40.dp).background(brush = Brush.linearGradient(listOf(VioletPurple, SapphireBlue)), shape = RoundedCornerShape(10.dp)), contentAlignment = Alignment.Center) {
                Icon(if (preview.isValid) Icons.Default.Backup else Icons.Default.Warning, null, tint = Color.White, modifier = Modifier.size(20.dp))
            }
        },
        title = { Text(if (preview.isValid) "Restore Preview" else "Invalid Backup", fontWeight = FontWeight.Bold) },
        text = {
            if (preview.isValid) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("This backup contains ${preview.totalRows} records from version ${preview.version}.", style = MaterialTheme.typography.bodyMedium)
                    if (preview.validationMessage != null) {
                        Surface(shape = ChipShape, color = GoldenAmber.copy(alpha = 0.1f)) {
                            Row(Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Icon(Icons.Default.Warning, null, tint = GoldenAmber, modifier = Modifier.size(16.dp))
                                Text(preview.validationMessage, style = MaterialTheme.typography.labelSmall, color = GoldenAmber)
                            }
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                    val dateStr = try { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(preview.timestamp)) } catch (_: Exception) { "Unknown date" }
                    Text("Backup date: $dateStr", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(4.dp))
                    Text("Entity breakdown:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                    preview.entityCounts.entries.sortedByDescending { it.value }.take(8).forEach { (name, count) ->
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(name, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("$count", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Surface(shape = ChipShape, color = CoralRed.copy(alpha = 0.08f)) {
                        Row(Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Default.Warning, null, tint = CoralRed, modifier = Modifier.size(16.dp))
                            Text("Existing data will be replaced. This cannot be undone.", style = MaterialTheme.typography.labelSmall, color = CoralRed, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Warning, null, tint = CoralRed, modifier = Modifier.size(48.dp))
                    Text(preview.validationMessage ?: "The selected file is not a valid backup.", style = MaterialTheme.typography.bodyMedium)
                }
            }
        },
        confirmButton = {
            if (preview.isValid) {
                Button(onClick = onConfirm, enabled = !isRestoring, colors = ButtonDefaults.buttonColors(containerColor = CoralRed)) {
                    if (isRestoring) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                    } else Text("Restore ${preview.totalRows} Records", fontWeight = FontWeight.SemiBold)
                }
            }
        },
        dismissButton = { TextButton(onClick = onDismiss, enabled = !isRestoring) { Text(if (preview.isValid) "Cancel" else "Close") } }
    )
}

private fun formatCount(count: Long): String = when { count < 1000 -> "$count"; count < 1_000_000 -> "${count / 1000}K"; else -> "%.1fM".format(count / 1_000_000.0) }
private fun formatFileSize(bytes: Long): String = when { bytes < 1024 -> "$bytes B"; bytes < 1024 * 1024 -> "%.1f KB".format(bytes / 1024.0); else -> "%.1f MB".format(bytes / (1024.0 * 1024.0)) }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BackupTimePickerDialog(currentHour: Int, currentMinute: Int, onConfirm: (Int, Int) -> Unit, onDismiss: () -> Unit) {
    val timePickerState = rememberTimePickerState(
        initialHour = currentHour,
        initialMinute = currentMinute,
        is24Hour = false
    )
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Box(Modifier.size(36.dp).background(
                brush = Brush.linearGradient(listOf(SapphireBlue, VioletPurple)),
                shape = RoundedCornerShape(10.dp)), contentAlignment = Alignment.Center) {
                Icon(Icons.Default.Update, null, tint = Color.White, modifier = Modifier.size(18.dp))
            }
        },
        title = { Text("Set Backup Time", fontWeight = FontWeight.Bold) },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                TimePicker(
                    state = timePickerState,
                    colors = TimePickerDefaults.colors(
                        clockDialColor = SapphireBlue.copy(alpha = 0.1f),
                        selectorColor = SapphireBlue,
                        clockDialSelectedContentColor = Color.White,
                        clockDialUnselectedContentColor = MaterialTheme.colorScheme.onSurface
                    )
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "Auto-backup will run daily at ${timePickerState.hour.toDisplayTime(timePickerState.minute)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(timePickerState.hour, timePickerState.minute) }) {
                Text("Set Time", fontWeight = FontWeight.SemiBold, color = SapphireBlue)
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

private fun Int.toDisplayTime(minute: Int): String {
    val h = if (this == 0) 12 else if (this > 12) this - 12 else this
    val amPm = if (this < 12) "AM" else "PM"
    return "%d:%02d %s".format(h, minute, amPm)
}
