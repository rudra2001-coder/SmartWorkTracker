package com.rudra.smartworktracker.ui.screens.backup

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
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
    val uiState by viewModel.uiState.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) }
    var showRestoreConfirmDialog by remember { mutableStateOf<android.net.Uri?>(null) }
    var entryToDelete by remember { mutableStateOf<BackupEntry?>(null) }
    var showRetentionCountDialog by remember { mutableStateOf(false) }
    var showRetentionAgeDialog by remember { mutableStateOf(false) }
    var retentionCountInput by remember { mutableIntStateOf(uiState.retentionLimit) }
    var retentionAgeInput by remember { mutableIntStateOf(uiState.retentionDays) }
    var showTimePickerDialog by remember { mutableStateOf(false) }
    var showFrequencyDialog by remember { mutableStateOf(false) }
    var showEncryptPasswordDialog by remember { mutableStateOf(false) }
    var showRestorePasswordDialog by remember { mutableStateOf(false) }
    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy - hh:mm a", Locale.getDefault()) }

    LaunchedEffect(backupState) {
        when (val state = backupState) {
            is BackupState.Success -> { Toast.makeText(context, state.message, Toast.LENGTH_LONG).show(); viewModel.onStateConsumed() }
            is BackupState.Error -> { Toast.makeText(context, state.message, Toast.LENGTH_LONG).show(); viewModel.onStateConsumed() }
            else -> {}
        }
    }

    LaunchedEffect(uiState.retentionLimit) { retentionCountInput = uiState.retentionLimit }
    LaunchedEffect(uiState.retentionDays) { retentionAgeInput = uiState.retentionDays }

    val restorePickLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri ->
            uri?.let {
                if (uiState.restorePassword.isNotBlank() || showRestorePasswordDialog) {
                    viewModel.previewBackup(it)
                    showRestoreConfirmDialog = it
                } else {
                    showRestorePasswordDialog = true
                    // Store URI for later use
                    showRestoreConfirmDialog = it
                }
            }
        }
    )

    val backupLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json"),
        onResult = { uri -> uri?.let { viewModel.createBackup(it) } }
    )

    val tabs = listOf("Overview", "Export", "Settings")

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
                title = {},
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                actions = {
                    Row(Modifier.padding(end = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(40.dp).background(brush = Brush.linearGradient(listOf(SapphireBlue, VioletPurple)), shape = RoundedCornerShape(10.dp)), contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Backup, null, tint = Color.White, modifier = Modifier.size(22.dp))
                        }
                        Spacer(Modifier.width(10.dp))
                        Column {
                            Text("Backup & Restore", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                            Text("Protect your data", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(Modifier.fillMaxSize().padding(paddingValues)) {
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                edgePadding = 16.dp,
                containerColor = MaterialTheme.colorScheme.background,
                contentColor = SapphireBlue,
                divider = {}
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title, fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal) }
                    )
                }
            }

            when (selectedTab) {
                0 -> OverviewTab(uiState, backupState, dateFormat, viewModel, backupLauncher, restorePickLauncher, onDeleteEntry = { entryToDelete = it })
                1 -> ExportTab(uiState, backupState, viewModel, backupLauncher)
                2 -> SettingsTab(uiState, viewModel, {
                    showRetentionCountDialog = true
                }, {
                    showRetentionAgeDialog = true
                }, {
                    showTimePickerDialog = true
                }, {
                    showFrequencyDialog = true
                })
            }
        }
    }

    if (showRestoreConfirmDialog != null && uiState.restorePreview != null) {
        RestorePreviewDialog(
            preview = uiState.restorePreview!!,
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
            text = { Text("Delete \"${entryToDelete!!.fileName}\"? This will permanently remove the backup file.") },
            confirmButton = {
                Button(onClick = { entryToDelete?.let { viewModel.deleteBackupEntry(it) }; entryToDelete = null },
                    colors = ButtonDefaults.buttonColors(containerColor = CoralRed)) { Text("Delete File") }
            },
            dismissButton = { TextButton(onClick = { entryToDelete = null }) { Text("Cancel") } }
        )
    }

    if (showRetentionCountDialog) {
        RetentionCountDialog(uiState.retentionLimit, uiState.backupHistory.size, { viewModel.setRetentionLimit(it); showRetentionCountDialog = false }, { showRetentionCountDialog = false })
    }

    if (showRetentionAgeDialog) {
        RetentionAgeDialog(uiState.retentionDays, { viewModel.setRetentionDays(it); showRetentionAgeDialog = false }, { showRetentionAgeDialog = false })
    }

    if (showTimePickerDialog) {
        BackupTimePickerDialog(uiState.backupHour, uiState.backupMinute, { h, m -> viewModel.updateBackupTime(h, m); showTimePickerDialog = false }, { showTimePickerDialog = false })
    }

    if (showFrequencyDialog) {
        BackupFrequencyDialog(uiState.backupFrequency, { viewModel.setBackupFrequency(it); showFrequencyDialog = false }, { showFrequencyDialog = false })
    }
}

@Composable
private fun OverviewTab(
    uiState: BackupUiState,
    backupState: BackupState,
    dateFormat: SimpleDateFormat,
    viewModel: BackupViewModel,
    backupLauncher: androidx.activity.result.ActivityResultLauncher<String>,
    restorePickLauncher: androidx.activity.result.ActivityResultLauncher<Array<String>>,
    onDeleteEntry: (BackupEntry) -> Unit = {}
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            val accentColor = if (uiState.isAutoBackupEnabled) EmeraldGreen else CoralRed
            val statusIcon = if (uiState.isAutoBackupEnabled) Icons.Default.CloudDone else Icons.Default.CloudOff
            val statusText = if (uiState.isAutoBackupEnabled) "Automatic Protection Active" else "Manual Protection Only"
            val lastBackupStr = if (uiState.lastBackupTime > 0) dateFormat.format(Date(uiState.lastBackupTime)) else "Never"

            Card(Modifier.fillMaxWidth().shadow(8.dp, CardShape, clip = false), shape = CardShape,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(0.dp)) {
                Column(Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Box(Modifier.size(36.dp).background(brush = Brush.linearGradient(listOf(accentColor, if (uiState.isAutoBackupEnabled) EmeraldGreen else CoralRed)),
                            shape = RoundedCornerShape(10.dp)), contentAlignment = Alignment.Center) {
                            Icon(statusIcon, null, tint = Color.White, modifier = Modifier.size(18.dp))
                        }
                        Column {
                            Text("Backup Status", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                            Text(statusText, style = MaterialTheme.typography.labelSmall, color = accentColor, fontWeight = FontWeight.SemiBold)
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        StatBadge(Modifier.weight(1f), "Last Backup", lastBackupStr, SapphireBlue, BlueSurface, Icons.Default.Update)
                        StatBadge(Modifier.weight(1f), "Records", uiState.lastExportResult?.let { formatCount(it.totalRows) } ?: "\u2014", EmeraldGreen, GreenSurface, Icons.Default.Description)
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        StatBadge(Modifier.weight(1f), "File Size", uiState.lastExportResult?.let { formatFileSize(it.fileSizeBytes) } ?: "\u2014", VioletPurple, PurpleSurface, Icons.Default.Info)
                        StatBadge(Modifier.weight(1f), "Frequency", uiState.backupFrequency.replaceFirstChar { it.uppercase() }, GoldenAmber, AmberSurface, Icons.Default.Schedule)
                    }
                }
            }
        }

        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                ActionTile(Modifier.weight(1f), "Export Now", Icons.Default.FileUpload, EmeraldGreen,
                    "Create full backup", null,
                    enabled = backupState !is BackupState.InProgress && backupState !is BackupState.Exporting,
                    onClick = {
                        val ts = SimpleDateFormat("yyyyMMdd_HHmm", Locale.getDefault()).format(Date())
                        val ext = buildString {
                            append(".json")
                            if (uiState.compressEnabled) append(".gz")
                            if (uiState.encryptionEnabled) append(".enc")
                        }
                        backupLauncher.launch("smart_work_backup_$ts$ext")
                    })
                ActionTile(Modifier.weight(1f), "Restore", Icons.Default.FileDownload, VioletPurple,
                    "Restore from file", null,
                    enabled = backupState !is BackupState.InProgress && backupState !is BackupState.Restoring,
                    onClick = {
                        restorePickLauncher.launch(arrayOf("application/json", "application/octet-stream", "*/*"))
                    })
            }
        }

        if (backupState is BackupState.Exporting) {
            item { ProgressCard((backupState as BackupState.Exporting).progress, EmeraldGreen) }
        }
        if (backupState is BackupState.Restoring) {
            item { ProgressCard((backupState as BackupState.Restoring).progress, VioletPurple) }
        }

        item { DataIntegrityCard() }

        if (uiState.backupHistory.isNotEmpty()) {
            item {
                Card(Modifier.fillMaxWidth().shadow(8.dp, CardShape, clip = false), shape = CardShape,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(0.dp)) {
                    Column(Modifier.padding(20.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Box(Modifier.size(36.dp).background(brush = Brush.linearGradient(listOf(GoldenAmber, CoralRed)), shape = RoundedCornerShape(10.dp)), contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.History, null, tint = Color.White, modifier = Modifier.size(18.dp))
                            }
                            Column { Text("Backup History", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium); Text("${uiState.backupHistory.size} backup(s) saved", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary) }
                        }
                        Spacer(Modifier.height(14.dp))
                        val totalSize = uiState.backupHistory.sumOf { it.fileSizeBytes }
                        val totalRows = uiState.backupHistory.sumOf { it.totalRows }
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            StatBadge(Modifier.weight(1f), "Total Size", formatFileSize(totalSize), SapphireBlue, BlueSurface, Icons.Default.Storage)
                            StatBadge(Modifier.weight(1f), "Total Rows", formatCount(totalRows), EmeraldGreen, GreenSurface, Icons.Default.Description)
                            StatBadge(Modifier.weight(1f), "Auto/Manual", "${uiState.backupHistory.count { it.isManual }}/${uiState.backupHistory.count { !it.isManual }}", VioletPurple, PurpleSurface, Icons.Default.SwapHoriz)
                        }
                        Spacer(Modifier.height(12.dp))
                        uiState.backupHistory.take(8).forEach { entry ->
                            Spacer(Modifier.height(4.dp))
                            BackupHistoryItem(entry = entry, dateFormat = dateFormat, onDelete = { onDeleteEntry(entry) })
                        }
                        if (uiState.backupHistory.size > 8) {
                            Spacer(Modifier.height(6.dp))
                            Text("+ ${uiState.backupHistory.size - 8} more...", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
        item { Spacer(Modifier.height(30.dp)) }
    }
}

@Composable
private fun ExportTab(
    uiState: BackupUiState,
    backupState: BackupState,
    viewModel: BackupViewModel,
    backupLauncher: androidx.activity.result.ActivityResultLauncher<String>
) {
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Card(Modifier.fillMaxWidth().shadow(8.dp, CardShape, clip = false), shape = CardShape,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(0.dp)) {
            Column(Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Box(Modifier.size(36.dp).background(brush = Brush.linearGradient(listOf(EmeraldGreen, SapphireBlue)), shape = RoundedCornerShape(10.dp)), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.FileUpload, null, tint = Color.White, modifier = Modifier.size(18.dp))
                    }
                    Column { Text("Export Options", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium); Text("Configure your backup export", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                }
                Spacer(Modifier.height(16.dp))

                // Compression toggle
                Surface(shape = ChipShape, color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)) {
                    Row(Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Icon(Icons.Default.Compress, null, tint = if (uiState.compressEnabled) EmeraldGreen else MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
                            Column {
                                Text("Compress Backup (GZIP)", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
                                Text("Smaller file size, auto-decompressed on restore", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        Switch(checked = uiState.compressEnabled, onCheckedChange = { viewModel.toggleCompress(it) },
                            colors = SwitchDefaults.colors(checkedThumbColor = EmeraldGreen, checkedTrackColor = EmeraldGreen.copy(alpha = 0.3f)))
                    }
                }

                // Encryption toggle
                Surface(shape = ChipShape, color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)) {
                    Column {
                        Row(Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                Icon(Icons.Default.Lock, null, tint = if (uiState.encryptionEnabled) GoldenAmber else MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
                                Column {
                                    Text("Encrypt Backup (AES-256)", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
                                    Text("Password-protected, required for restore", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                            Switch(checked = uiState.encryptionEnabled, onCheckedChange = { viewModel.toggleEncryption(it) },
                                colors = SwitchDefaults.colors(checkedThumbColor = GoldenAmber, checkedTrackColor = GoldenAmber.copy(alpha = 0.3f)))
                        }
                        AnimatedVisibility(visible = uiState.encryptionEnabled) {
                            Column(Modifier.padding(start = 14.dp, end = 14.dp, bottom = 12.dp)) {
                                HorizontalDivider(Modifier.padding(bottom = 8.dp))
                                OutlinedTextField(
                                    value = uiState.encryptionPassword,
                                    onValueChange = { viewModel.setEncryptionPassword(it) },
                                    label = { Text("Encryption Password") },
                                    placeholder = { Text("Enter a strong password") },
                                    singleLine = true,
                                    visualTransformation = if (uiState.showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                                    trailingIcon = {
                                        IconButton(onClick = { viewModel.toggleShowPassword() }) {
                                            Icon(if (uiState.showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility, null)
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = ChipShape,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done)
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(4.dp))
                Button(
                    onClick = {
                        val ts = SimpleDateFormat("yyyyMMdd_HHmm", Locale.getDefault()).format(Date())
                        val ext = buildString {
                            append(".json")
                            if (uiState.compressEnabled) append(".gz")
                            if (uiState.encryptionEnabled) append(".enc")
                        }
                        backupLauncher.launch("smart_work_backup_$ts$ext")
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = ChipShape,
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                    enabled = backupState !is BackupState.InProgress && backupState !is BackupState.Exporting &&
                        (!uiState.encryptionEnabled || uiState.encryptionPassword.length >= 4)
                ) {
                    if (backupState is BackupState.Exporting) {
                        CircularProgressIndicator(Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                        Spacer(Modifier.width(8.dp))
                    }
                    Icon(Icons.Default.FileUpload, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Export Backup", fontWeight = FontWeight.SemiBold)
                }
            }
        }

        Card(Modifier.fillMaxWidth().shadow(8.dp, CardShape, clip = false), shape = CardShape,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(0.dp)) {
            Column(Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Box(Modifier.size(36.dp).background(brush = Brush.linearGradient(listOf(SapphireBlue, VioletPurple)), shape = RoundedCornerShape(10.dp)), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Tune, null, tint = Color.White, modifier = Modifier.size(18.dp))
                    }
                    Column { Text("Entity Selection", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium); Text("Choose data to include", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                }
                Spacer(Modifier.height(12.dp))
                Surface(shape = ChipShape, color = if (uiState.selectAllTypes) EmeraldGreen.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    modifier = Modifier.clickable { viewModel.selectAllTypes() }) {
                    Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.SelectAll, null, tint = if (uiState.selectAllTypes) EmeraldGreen else MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
                            Text("All Entity Types", fontWeight = if (uiState.selectAllTypes) FontWeight.Bold else FontWeight.Normal)
                        }
                        if (uiState.selectAllTypes) Icon(Icons.Default.Check, null, tint = EmeraldGreen, modifier = Modifier.size(18.dp))
                    }
                }
                Spacer(Modifier.height(8.dp))

                val shownTypes = uiState.availableTypes.take(12)
                val hasMore = uiState.availableTypes.size > 12

                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(shownTypes) { type ->
                        val selected = type in uiState.selectedTypes || uiState.selectAllTypes
                        Surface(
                            onClick = { viewModel.toggleType(type) },
                            shape = PillShape,
                            color = if (selected) SapphireBlue.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                            border = if (selected) androidx.compose.foundation.BorderStroke(1.dp, SapphireBlue.copy(alpha = 0.4f)) else null
                        ) {
                            Row(Modifier.padding(horizontal = 10.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                if (selected) Icon(Icons.Default.Check, null, tint = SapphireBlue, modifier = Modifier.size(14.dp))
                                Text(type, style = MaterialTheme.typography.labelSmall, fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal)
                            }
                        }
                    }
                }
                if (hasMore) {
                    Spacer(Modifier.height(6.dp))
                    Text("+ ${uiState.availableTypes.size - 12} more types...", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        Spacer(Modifier.height(30.dp))
    }
}

@Composable
private fun SettingsTab(
    uiState: BackupUiState,
    viewModel: BackupViewModel,
    onRetentionCountClick: () -> Unit,
    onRetentionAgeClick: () -> Unit,
    onTimePickClick: () -> Unit,
    onFrequencyClick: () -> Unit
) {
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Card(Modifier.fillMaxWidth().shadow(8.dp, CardShape, clip = false), shape = CardShape,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(0.dp)) {
            Column(Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Box(Modifier.size(36.dp).background(brush = Brush.linearGradient(listOf(EmeraldGreen, SapphireBlue)), shape = RoundedCornerShape(10.dp)), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.CloudDone, null, tint = Color.White, modifier = Modifier.size(18.dp))
                    }
                    Column { Text("Auto Backup", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium); Text("Schedule automatic protection", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                }
                Spacer(Modifier.height(14.dp))

                Surface(shape = ChipShape, color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)) {
                    Row(Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text("Enable Auto-Backup", fontWeight = FontWeight.SemiBold)
                            Text(if (uiState.isAutoBackupEnabled) "Active" else "Inactive", style = MaterialTheme.typography.labelSmall, color = if (uiState.isAutoBackupEnabled) EmeraldGreen else CoralRed)
                        }
                        Switch(checked = uiState.isAutoBackupEnabled, onCheckedChange = { viewModel.toggleAutoBackup(it) },
                            colors = SwitchDefaults.colors(checkedThumbColor = EmeraldGreen, checkedTrackColor = EmeraldGreen.copy(alpha = 0.3f)))
                    }
                }
                AnimatedVisibility(visible = uiState.isAutoBackupEnabled) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Spacer(Modifier.height(4.dp))
                        Surface(shape = ChipShape, color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), modifier = Modifier.clickable { onFrequencyClick() }) {
                            Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Icon(Icons.Default.Repeat, null, tint = GoldenAmber, modifier = Modifier.size(18.dp))
                                    Column {
                                        Text("Frequency", style = MaterialTheme.typography.bodyMedium)
                                        Text(uiState.backupFrequency.replaceFirstChar { it.uppercase() }, style = MaterialTheme.typography.labelSmall, color = GoldenAmber)
                                    }
                                }
                                Icon(Icons.Default.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        Surface(shape = ChipShape, color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), modifier = Modifier.clickable { onTimePickClick() }) {
                            Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Icon(Icons.Default.Schedule, null, tint = SapphireBlue, modifier = Modifier.size(18.dp))
                                    Column {
                                        Text("Scheduled Time", style = MaterialTheme.typography.bodyMedium)
                                        Text(uiState.backupTimeDisplay, style = MaterialTheme.typography.labelSmall, color = SapphireBlue, fontWeight = FontWeight.SemiBold)
                                    }
                                }
                                Icon(Icons.Default.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        }

        Card(Modifier.fillMaxWidth().shadow(8.dp, CardShape, clip = false), shape = CardShape,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(0.dp)) {
            Column(Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Box(Modifier.size(36.dp).background(brush = Brush.linearGradient(listOf(GoldenAmber, CoralRed)), shape = RoundedCornerShape(10.dp)), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.DeleteSweep, null, tint = Color.White, modifier = Modifier.size(18.dp))
                    }
                    Column { Text("Retention Policy", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium); Text("Auto-cleanup rules", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                }
                Spacer(Modifier.height(14.dp))
                Surface(shape = ChipShape, color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), modifier = Modifier.clickable { onRetentionCountClick() }) {
                    Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Icon(Icons.Default.Numbers, null, tint = SapphireBlue, modifier = Modifier.size(18.dp))
                            Column {
                                Text("Max Backup Count", style = MaterialTheme.typography.bodyMedium)
                                Text(if (uiState.retentionLimit > 0) "Keep latest ${uiState.retentionLimit}" else "Keep all", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        Icon(Icons.Default.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Spacer(Modifier.height(6.dp))
                Surface(shape = ChipShape, color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), modifier = Modifier.clickable { onRetentionAgeClick() }) {
                    Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Icon(Icons.Default.DateRange, null, tint = GoldenAmber, modifier = Modifier.size(18.dp))
                            Column {
                                Text("Max Backup Age", style = MaterialTheme.typography.bodyMedium)
                                Text(if (uiState.retentionDays > 0) "Delete after ${uiState.retentionDays} days" else "Keep forever", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        Icon(Icons.Default.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }

        Card(Modifier.fillMaxWidth().shadow(8.dp, CardShape, clip = false), shape = CardShape,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(0.dp)) {
            Column(Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Box(Modifier.size(36.dp).background(brush = Brush.linearGradient(listOf(VioletPurple, EmeraldGreen)), shape = RoundedCornerShape(10.dp)), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Info, null, tint = Color.White, modifier = Modifier.size(18.dp))
                    }
                    Column { Text("Backup Tips", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium); Text("Best practices", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                }
                Spacer(Modifier.height(12.dp))
                val tips = listOf(
                    "Always keep at least one backup before major app updates.",
                    "Use encryption for cloud-stored backups.",
                    "Compressed backups save up to 80% storage space.",
                    "Enable auto-backup for daily data protection.",
                    "Regularly export manual backups to a safe location."
                )
                tips.forEachIndexed { i, tip ->
                    Row(Modifier.padding(vertical = 3.dp), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.Top) {
                        Text("${i + 1}.", style = MaterialTheme.typography.bodySmall, color = EmeraldGreen, fontWeight = FontWeight.Bold)
                        Text(tip, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
        Spacer(Modifier.height(30.dp))
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
            Spacer(Modifier.height(10.dp))
            Text("Your data stays on-device. Backups are JSON snapshots with GZIP compression and AES-256 encryption options. The restore system auto-migrates backups from any previous version.",
                style = MaterialTheme.typography.bodySmall, lineHeight = 18.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
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

@Composable
private fun RetentionCountDialog(current: Int, historySize: Int, onConfirm: (Int) -> Unit, onDismiss: () -> Unit) {
    var input by remember { mutableIntStateOf(current) }
    AlertDialog(onDismissRequest = onDismiss, shape = CardShape,
        icon = {
            Box(Modifier.size(40.dp).background(GoldenAmber.copy(alpha = 0.1f), RoundedCornerShape(10.dp)), contentAlignment = Alignment.Center) {
                Icon(Icons.Default.Numbers, null, tint = GoldenAmber, modifier = Modifier.size(20.dp))
            }
        },
        title = { Text("Max Backup Count", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Auto-delete oldest backups when count exceeds limit. Set to 0 to keep all.", style = MaterialTheme.typography.bodyMedium)
                Text("Current: $historySize backup(s) saved", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(4.dp))
                Surface(shape = ChipShape, color = BlueSurface) {
                    Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Keep latest", style = MaterialTheme.typography.bodyMedium)
                        OutlinedTextField(value = if (input <= 0) "" else "$input", onValueChange = { input = it.filter { c -> c.isDigit() }.take(3).toIntOrNull() ?: 0 },
                            modifier = Modifier.width(72.dp), singleLine = true, shape = ChipShape, placeholder = { Text("0") })
                        Text("backup(s)", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        },
        confirmButton = { Button(onClick = { onConfirm(input) }, colors = ButtonDefaults.buttonColors(containerColor = GoldenAmber)) { Text("Apply") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
private fun RetentionAgeDialog(current: Int, onConfirm: (Int) -> Unit, onDismiss: () -> Unit) {
    var input by remember { mutableIntStateOf(current) }
    AlertDialog(onDismissRequest = onDismiss, shape = CardShape,
        icon = {
            Box(Modifier.size(40.dp).background(GoldenAmber.copy(alpha = 0.1f), RoundedCornerShape(10.dp)), contentAlignment = Alignment.Center) {
                Icon(Icons.Default.DateRange, null, tint = GoldenAmber, modifier = Modifier.size(20.dp))
            }
        },
        title = { Text("Max Backup Age", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Auto-delete backups older than the specified number of days.", style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.height(4.dp))
                Surface(shape = ChipShape, color = AmberSurface) {
                    Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Delete after", style = MaterialTheme.typography.bodyMedium)
                        OutlinedTextField(value = if (input <= 0) "" else "$input", onValueChange = { input = it.filter { c -> c.isDigit() }.take(3).toIntOrNull() ?: 0 },
                            modifier = Modifier.width(72.dp), singleLine = true, shape = ChipShape, placeholder = { Text("0") })
                        Text("day(s)", style = MaterialTheme.typography.bodyMedium)
                    }
                }
                Spacer(Modifier.height(4.dp))
                Text("Set to 0 to keep all backups indefinitely.", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        },
        confirmButton = { Button(onClick = { onConfirm(input) }, colors = ButtonDefaults.buttonColors(containerColor = GoldenAmber)) { Text("Apply") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
private fun BackupFrequencyDialog(current: String, onConfirm: (String) -> Unit, onDismiss: () -> Unit) {
    val options = listOf("daily", "weekly", "monthly")
    val labels = mapOf("daily" to "Daily", "weekly" to "Weekly", "monthly" to "Monthly")
    val icons = mapOf("daily" to Icons.Default.Today, "weekly" to Icons.Default.DateRange, "monthly" to Icons.Default.CalendarMonth)

    AlertDialog(onDismissRequest = onDismiss, shape = CardShape,
        icon = {
            Box(Modifier.size(40.dp).background(GoldenAmber.copy(alpha = 0.1f), RoundedCornerShape(10.dp)), contentAlignment = Alignment.Center) {
                Icon(Icons.Default.Repeat, null, tint = GoldenAmber, modifier = Modifier.size(20.dp))
            }
        },
        title = { Text("Backup Frequency", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Choose how often to automatically back up your data.", style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.height(4.dp))
                options.forEach { opt ->
                    Surface(
                        onClick = { onConfirm(opt) },
                        shape = ChipShape,
                        color = if (current == opt) SapphireBlue.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                        border = if (current == opt) BorderStroke(1.5.dp, SapphireBlue) else null
                    ) {
                        Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                Icon(icons[opt]!!, null, tint = if (current == opt) SapphireBlue else MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
                                Text(labels[opt]!!, fontWeight = if (current == opt) FontWeight.Bold else FontWeight.Normal)
                            }
                            if (current == opt) Icon(Icons.Default.Check, null, tint = SapphireBlue, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BackupTimePickerDialog(currentHour: Int, currentMinute: Int, onConfirm: (Int, Int) -> Unit, onDismiss: () -> Unit) {
    val timePickerState = rememberTimePickerState(initialHour = currentHour, initialMinute = currentMinute, is24Hour = false)
    AlertDialog(onDismissRequest = onDismiss,
        icon = {
            Box(Modifier.size(36.dp).background(brush = Brush.linearGradient(listOf(SapphireBlue, VioletPurple)), shape = RoundedCornerShape(10.dp)), contentAlignment = Alignment.Center) {
                Icon(Icons.Default.Update, null, tint = Color.White, modifier = Modifier.size(18.dp))
            }
        },
        title = { Text("Set Backup Time", fontWeight = FontWeight.Bold) },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                TimePicker(state = timePickerState,
                    colors = TimePickerDefaults.colors(clockDialColor = SapphireBlue.copy(alpha = 0.1f), selectorColor = SapphireBlue, clockDialSelectedContentColor = Color.White, clockDialUnselectedContentColor = MaterialTheme.colorScheme.onSurface))
                Spacer(Modifier.height(8.dp))
            }
        },
        confirmButton = { TextButton(onClick = { onConfirm(timePickerState.hour, timePickerState.minute) }) { Text("Set Time", fontWeight = FontWeight.SemiBold, color = SapphireBlue) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

private fun formatCount(count: Long): String = when { count < 1000 -> "$count"; count < 1_000_000 -> "${count / 1000}K"; else -> "%.1fM".format(count / 1_000_000.0) }
private fun formatFileSize(bytes: Long): String = when { bytes < 1024 -> "$bytes B"; bytes < 1024 * 1024 -> "%.1f KB".format(bytes / 1024.0); else -> "%.1f MB".format(bytes / (1024.0 * 1024.0)) }
