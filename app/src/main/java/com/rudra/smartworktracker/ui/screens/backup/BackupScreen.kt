package com.rudra.smartworktracker.ui.screens.backup

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackupScreen(onNavigateBack: () -> Unit) {
    val context = LocalContext.current
    val viewModel: BackupViewModel = viewModel(factory = BackupViewModelFactory(context))
    val isLoading by viewModel.isLoading.collectAsState()
    val lastBackupTime by viewModel.lastBackupTime.collectAsState()
    val nextBackupTime by viewModel.nextBackupTime.collectAsState()
    val isAutoBackupEnabled by viewModel.isAutoBackupEnabled.collectAsState()

    var showRestoreConfirmDialog by remember { mutableStateOf<android.net.Uri?>(null) }
    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy - hh:mm a", Locale.getDefault()) }

    val restoreLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri -> uri?.let { showRestoreConfirmDialog = it } }
    )

    val backupLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json"),
        onResult = { uri -> uri?.let { viewModel.createBackup(it) } }
    )

    LaunchedEffect(Unit) {
        viewModel.backupResult.collect { result ->
            when (result) {
                is BackupResult.Success -> Toast.makeText(context, result.message, Toast.LENGTH_LONG).show()
                is BackupResult.Error -> Toast.makeText(context, result.message, Toast.LENGTH_LONG).show()
            }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Backup & Security", fontWeight = FontWeight.ExtraBold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.95f))
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // --- System Status Header ---
            StatusHeader(
                lastBackup = if (lastBackupTime > 0) dateFormat.format(Date(lastBackupTime)) else "Never",
                nextBackup = if (isAutoBackupEnabled && nextBackupTime > 0) dateFormat.format(Date(nextBackupTime)) else "Disabled",
                isAutoEnabled = isAutoBackupEnabled
            )

            // --- Auto Backup Toggle ---
            AutoBackupToggleCard(
                isEnabled = isAutoBackupEnabled,
                onToggle = { viewModel.toggleAutoBackup(it) }
            )

            // --- Primary Actions ---
            Text(
                "Manual Actions",
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                ActionTile(
                    modifier = Modifier.weight(1f),
                    title = "Export",
                    icon = Icons.Default.FileUpload,
                    color = MaterialTheme.colorScheme.primary,
                    description = "Create manual JSON",
                    onClick = {
                        val ts = SimpleDateFormat("yyyyMMdd_HHmm", Locale.getDefault()).format(Date())
                        backupLauncher.launch("smart_work_backup_$ts.json")
                    }
                )
                ActionTile(
                    modifier = Modifier.weight(1f),
                    title = "Import",
                    icon = Icons.Default.FileDownload,
                    color = MaterialTheme.colorScheme.tertiary,
                    description = "Restore from file",
                    onClick = { restoreLauncher.launch(arrayOf("application/json")) }
                )
            }

            // --- Detailed Info Section ---
            Text(
                "System Intelligence",
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            DetailedInfoCard(
                title = "Auto-Backup Intelligence",
                info = "Scheduled for 12:05 AM daily",
                icon = Icons.Default.Update,
                detail = "When enabled, the system automatically creates a survival snapshot in your Downloads folder. This process is 100% safe, non-destructive, and never affects your app's performance or core database version."
            )

            DetailedInfoCard(
                title = "Data Integrity Shield",
                info = "Offline-First Standard",
                icon = Icons.Default.Security,
                detail = "Your data remains under your total control. Manual backups allow you to move your history between devices safely without touching the app's internal structural integrity."
            )

            // --- Refresh System ---
            OutlinedButton(
                onClick = { 
                    viewModel.loadBackupStatus()
                    Toast.makeText(context, "System Status Refreshed", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Refresh Backup Engine Status")
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }

    if (showRestoreConfirmDialog != null) {
        AlertDialog(
            onDismissRequest = { showRestoreConfirmDialog = null },
            icon = { Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
            title = { Text("Confirm Restore Operation") },
            text = { 
                Text("Restoring will merge the backup data with your current records. Any existing data with matching IDs will be safely updated. This process is purely data-level and will NOT change your database version or app structure.\n\nProceed with the restore?") 
            },
            confirmButton = {
                Button(
                    onClick = {
                        showRestoreConfirmDialog?.let { viewModel.restoreBackup(it) }
                        showRestoreConfirmDialog = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                    } else {
                        Text("Restore Data")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showRestoreConfirmDialog = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun StatusHeader(lastBackup: String, nextBackup: String, isAutoEnabled: Boolean) {
    val statusColor = if (isAutoEnabled) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
    val onStatusColor = if (isAutoEnabled) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = statusColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(onStatusColor.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    if (isAutoEnabled) Icons.Default.CloudDone else Icons.Default.CloudOff,
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                    tint = onStatusColor
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                if (isAutoEnabled) "Automatic Protection ON" else "Manual Protection Mode",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = onStatusColor
            )
            Spacer(modifier = Modifier.height(24.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                InfoColumn("Last Backup", lastBackup, Alignment.Start, onStatusColor)
                InfoColumn("Next Schedule", nextBackup, Alignment.End, onStatusColor)
            }
        }
    }
}

@Composable
fun InfoColumn(label: String, value: String, alignment: Alignment.Horizontal, color: Color) {
    Column(horizontalAlignment = alignment) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = color.copy(alpha = 0.7f))
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = color)
    }
}

@Composable
fun AutoBackupToggleCard(isEnabled: Boolean, onToggle: (Boolean) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Daily Auto-Backup", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                Text(
                    "Automatically backup data every day at 12:05 AM.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Switch(
                checked = isEnabled,
                onCheckedChange = onToggle,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = MaterialTheme.colorScheme.primary,
                    checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    }
}

@Composable
fun ActionTile(
    modifier: Modifier = Modifier,
    title: String,
    icon: ImageVector,
    color: Color,
    description: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(140.dp),
        shape = RoundedCornerShape(20.dp),
        color = color.copy(alpha = 0.1f),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.2f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(32.dp))
            Spacer(modifier = Modifier.height(12.dp))
            Text(title, fontWeight = FontWeight.Bold, color = color)
            Text(description, style = MaterialTheme.typography.labelSmall, color = color.copy(alpha = 0.7f), textAlign = TextAlign.Center)
        }
    }
}

@Composable
fun DetailedInfoCard(title: String, info: String, icon: ImageVector, detail: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                    Text(info, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                detail,
                style = MaterialTheme.typography.bodySmall,
                lineHeight = 18.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
