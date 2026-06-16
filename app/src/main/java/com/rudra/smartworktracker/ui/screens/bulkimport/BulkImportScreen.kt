package com.rudra.smartworktracker.ui.screens.bulkimport

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.rudra.smartworktracker.data.importexport.ImportEntityType
import com.rudra.smartworktracker.data.importexport.ImportResult

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

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun BulkImportScreen(onNavigateBack: () -> Unit) {
    val context = LocalContext.current
    val viewModel: BulkImportViewModel = viewModel(factory = BulkImportViewModelFactory(context))
    val uiState by viewModel.uiState.collectAsState()
    val importEvent by viewModel.event.collectAsState()

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri ->
            uri?.let {
                val fileName = uri.lastPathSegment ?: "unknown_file"
                viewModel.onFileSelected(uri, fileName)
            }
        }
    )

    val templateSaveLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"),
        onResult = { uri ->
            uri?.let { viewModel.generateTemplate(it) }
        }
    )

    LaunchedEffect(importEvent) {
        when (val event = importEvent) {
            is BulkImportEvent.Success -> {
                val r = event.result
                Toast.makeText(context, "Imported ${r.successCount}/${r.totalRows} ${r.type.displayName}", Toast.LENGTH_LONG).show()
            }
            is BulkImportEvent.Error -> {
                Toast.makeText(context, event.message, Toast.LENGTH_LONG).show()
            }
            is BulkImportEvent.TemplateSaved -> {
                Toast.makeText(context, "Template saved: ${event.fileName}", Toast.LENGTH_LONG).show()
            }
            else -> {}
        }
    }

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
            item { HeaderRow(onNavigateBack, uiState) }

            item { StepIndicator(uiState) }

            if (uiState.selectedType == null && uiState.parsedRows.isEmpty()) {
                item { TypeSelectorCard(uiState.detectedType) { viewModel.selectType(it) } }
            }

            val currentType = uiState.selectedType ?: uiState.detectedType
            if (currentType != null && uiState.parsedRows.isEmpty()) {
                item { TypeInfoChip(currentType) { viewModel.reset() } }
            }

            if (uiState.parsedRows.isEmpty() && !uiState.isLoading) {
                item { FilePickerCard { filePickerLauncher.launch(arrayOf("text/csv", "text/comma-separated-values", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "*/*")) } }
            }

            if (uiState.isLoading) {
                item { LoadingCard() }
            }

            if (uiState.parsedRows.isNotEmpty() && !uiState.isLoading) {
                item { PreviewCard(uiState, { viewModel.executeImport() }, { viewModel.reset() }) }
            }

            uiState.importResult?.let { result ->
                item { ImportResultCard(result) }
            }

            if (currentType != null && uiState.parsedRows.isEmpty() && !uiState.isLoading) {
                item {
                    DownloadTemplateCard(
                        type = currentType,
                        isGenerating = uiState.isGeneratingTemplate,
                        onDownloadXlsx = {
                            templateSaveLauncher.launch("SmartWorkTracker_${currentType.displayName.replace(" ", "_")}_Template.xlsx")
                        }
                    )
                }
            }

            item { Spacer(Modifier.height(40.dp)) }
        }
    }
}

@Composable
private fun HeaderRow(onNavigateBack: () -> Unit, uiState: BulkImportUiState) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .background(
                    brush = Brush.linearGradient(listOf(EmeraldGreen, SapphireBlue)),
                    shape = RoundedCornerShape(14.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.FileUpload, contentDescription = null, tint = Color.White, modifier = Modifier.size(28.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text("Bulk Data Import", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            Text("CSV & Excel bulk import", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        IconButton(onClick = onNavigateBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
        }
    }
}

@Composable
private fun StepIndicator(uiState: BulkImportUiState) {
    val steps = listOf(
        "Select Type" to (uiState.selectedType != null || uiState.detectedType != null),
        "Pick File" to (uiState.parsedRows.isNotEmpty()),
        "Import" to (uiState.importResult != null)
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        steps.forEachIndexed { index, (label, done) ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Surface(
                    shape = PillShape,
                    color = if (done) EmeraldGreen.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .background(
                                    if (done) EmeraldGreen else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                                    RoundedCornerShape(10.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (done) {
                                Icon(Icons.Default.CheckCircle, null, tint = Color.White, modifier = Modifier.size(12.dp))
                            } else {
                                Text("${index + 1}", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                        Text(
                            label,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (done) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (done) EmeraldGreen else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TypeSelectorCard(
    selectedType: ImportEntityType?,
    onTypeSelected: (ImportEntityType) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp, CardShape, clip = false),
        shape = CardShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(
                            brush = Brush.linearGradient(listOf(SapphireBlue, VioletPurple)),
                            shape = RoundedCornerShape(10.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Backup, null, tint = Color.White, modifier = Modifier.size(18.dp))
                }
                Column {
                    Text("Select Data Type", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    Text("Choose what to import — or auto-detect from file", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Spacer(Modifier.height(16.dp))

            val types = ImportEntityType.entries
            val rows = types.chunked(2)
            rows.forEach { rowTypes ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    rowTypes.forEach { type ->
                        TypeButton(
                            modifier = Modifier.weight(1f),
                            type = type,
                            isSelected = type == selectedType,
                            onClick = { onTypeSelected(type) }
                        )
                    }
                    if (rowTypes.size < 2) {
                        Spacer(Modifier.weight(1f))
                    }
                }
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun TypeButton(
    modifier: Modifier = Modifier,
    type: ImportEntityType,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val color = when (type) {
        ImportEntityType.EXPENSE -> CoralRed
        ImportEntityType.INCOME -> EmeraldGreen
        ImportEntityType.WORK_LOG -> SapphireBlue
        ImportEntityType.ACCOUNT -> VioletPurple
        ImportEntityType.LOAN -> GoldenAmber
        ImportEntityType.SAVINGS -> EmeraldGreen
        ImportEntityType.HABIT -> VioletPurple
        ImportEntityType.HEALTH_METRIC -> CoralRed
        ImportEntityType.DAILY_JOURNAL -> SapphireBlue
        ImportEntityType.CREDIT_CARD -> GoldenAmber
        ImportEntityType.RECURRING_RULE -> VioletPurple
        ImportEntityType.COLLEAGUE -> CoralRed
        ImportEntityType.FINANCIAL_TRANSACTION -> EmeraldGreen
    }

    Surface(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        shape = ChipShape,
        color = if (isSelected) color.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant,
        border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, color) else null
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(if (isSelected) color else color.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
            )
            Text(
                type.displayName,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) color else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun TypeInfoChip(type: ImportEntityType, onChange: () -> Unit) {
    Surface(
        shape = PillShape,
        color = SapphireBlue.copy(alpha = 0.1f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(Icons.Default.Notifications, null, tint = SapphireBlue, modifier = Modifier.size(16.dp))
            Text(type.displayName, fontWeight = FontWeight.SemiBold, color = SapphireBlue, style = MaterialTheme.typography.labelMedium)
            Text("•", color = SapphireBlue.copy(alpha = 0.5f))
            Text("${type.description}", style = MaterialTheme.typography.labelSmall, color = SapphireBlue.copy(alpha = 0.7f))
            Spacer(Modifier.weight(1f))
            TextButton(onClick = onChange) {
                Text("Change", style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

@Composable
private fun FilePickerCard(onPickFile: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp, CardShape, clip = false),
        shape = CardShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(
                            brush = Brush.linearGradient(listOf(EmeraldGreen, SapphireBlue)),
                            shape = RoundedCornerShape(10.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.FileUpload, null, tint = Color.White, modifier = Modifier.size(18.dp))
                }
                Text("Select File", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            }

            Spacer(Modifier.height(20.dp))

            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(EmeraldGreen.copy(alpha = 0.1f), RoundedCornerShape(40.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Description, null, tint = EmeraldGreen, modifier = Modifier.size(40.dp))
            }

            Spacer(Modifier.height(16.dp))
            Text("Choose a CSV or Excel file", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyLarge)
            Spacer(Modifier.height(6.dp))
            Text(
                "Headers in the first row are used for column mapping. Supports .csv and .xlsx formats.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                lineHeight = 18.sp
            )

            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Surface(shape = PillShape, color = GreenSurface) {
                    Text("   .csv   ", modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp), style = MaterialTheme.typography.labelSmall, color = EmeraldGreen, fontWeight = FontWeight.SemiBold)
                }
                Surface(shape = PillShape, color = BlueSurface) {
                    Text("  .xlsx  ", modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp), style = MaterialTheme.typography.labelSmall, color = SapphireBlue, fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(Modifier.height(20.dp))
            Button(
                onClick = onPickFile,
                shape = ChipShape,
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                modifier = Modifier.height(48.dp)
            ) {
                Icon(Icons.Default.FileUpload, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Browse Files", fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun LoadingCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp, CardShape, clip = false),
        shape = CardShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator(color = SapphireBlue)
            Spacer(Modifier.height(16.dp))
            Text("Processing file...", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("Parsing rows and validating structure", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun PreviewCard(
    uiState: BulkImportUiState,
    onImport: () -> Unit,
    onClear: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp, CardShape, clip = false),
        shape = CardShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(
                            brush = Brush.linearGradient(listOf(EmeraldGreen, SapphireBlue)),
                            shape = RoundedCornerShape(10.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Backup, null, tint = Color.White, modifier = Modifier.size(18.dp))
                }
                Column {
                    Text("Data Preview", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    Text(uiState.fileName, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }

            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                GlassMetricChip(
                    modifier = Modifier.weight(1f),
                    label = "Rows",
                    value = "${uiState.parsedRows.size}",
                    color = SapphireBlue,
                    bgColor = BlueSurface,
                    icon = Icons.Default.Description
                )
                GlassMetricChip(
                    modifier = Modifier.weight(1f),
                    label = "Columns",
                    value = "${uiState.previewHeaders.size}",
                    color = VioletPurple,
                    bgColor = PurpleSurface,
                    icon = Icons.Default.Backup
                )
                GlassMetricChip(
                    modifier = Modifier.weight(1f),
                    label = "Detected",
                    value = uiState.detectedType?.displayName?.take(10) ?: uiState.selectedType?.displayName?.take(10) ?: "Manual",
                    color = EmeraldGreen,
                    bgColor = GreenSurface,
                    icon = Icons.Default.CheckCircle
                )
            }

            if (uiState.parsedRows.isNotEmpty()) {
                Spacer(Modifier.height(16.dp))

                Text(
                    "First ${minOf(3, uiState.parsedRows.size)} rows:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(8.dp))

                val previewHeaders = uiState.previewHeaders.take(5)
                val previewRows = uiState.parsedRows.take(3)

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(modifier = Modifier.fillMaxWidth()) {
                            previewHeaders.forEach { header ->
                                Text(
                                    header,
                                    modifier = Modifier.weight(1f),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            if (uiState.previewHeaders.size > 5) {
                                Text(
                                    "+${uiState.previewHeaders.size - 5}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp), thickness = 0.5.dp)
                        previewRows.forEach { row ->
                            Spacer(Modifier.height(4.dp))
                            Row(modifier = Modifier.fillMaxWidth()) {
                                previewHeaders.forEach { header ->
                                    Text(
                                        row[header]?.take(15) ?: "",
                                        modifier = Modifier.weight(1f),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onClear,
                    modifier = Modifier.weight(1f),
                    shape = ChipShape
                ) {
                    Text("Clear", fontWeight = FontWeight.SemiBold)
                }
                Button(
                    onClick = onImport,
                    modifier = Modifier.weight(1f),
                    shape = ChipShape,
                    enabled = !uiState.isLoading && uiState.parsedRows.isNotEmpty(),
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen)
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(Icons.Default.FileUpload, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Import ${uiState.parsedRows.size} Rows", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

@Composable
private fun GlassMetricChip(
    modifier: Modifier,
    label: String,
    value: String,
    color: Color,
    bgColor: Color,
    icon: ImageVector
) {
    Surface(
        modifier = modifier,
        shape = ChipShape,
        color = bgColor
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .background(color.copy(alpha = 0.15f), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = color, modifier = Modifier.size(14.dp))
            }
            Column {
                Text(
                    value,
                    fontWeight = FontWeight.ExtraBold,
                    style = MaterialTheme.typography.titleSmall,
                    color = color,
                    maxLines = 1
                )
                Text(
                    label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun ImportResultCard(result: ImportResult) {
    val hasErrors = result.errorCount > 0
    val primaryColor = if (hasErrors) GoldenAmber else EmeraldGreen

    var animatedSuccess by remember { mutableIntStateOf(0) }
    var animatedTotal by remember { mutableIntStateOf(0) }
    var animatedErrors by remember { mutableIntStateOf(0) }

    LaunchedEffect(result) {
        for (i in 0..result.successCount) { animatedSuccess = i; kotlinx.coroutines.delay(12) }
        for (i in 0..result.totalRows) { animatedTotal = i; kotlinx.coroutines.delay(8) }
        for (i in 0..result.errorCount) { animatedErrors = i; kotlinx.coroutines.delay(15) }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp, CardShape, clip = false),
        shape = CardShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(
                            brush = Brush.linearGradient(listOf(primaryColor, if (hasErrors) CoralRed else EmeraldGreen)),
                            shape = RoundedCornerShape(10.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        if (hasErrors) Icons.Default.Warning else Icons.Default.CheckCircle,
                        null, tint = Color.White, modifier = Modifier.size(18.dp)
                    )
                }
                Column {
                    Text(
                        if (hasErrors) "Import Completed with Warnings" else "Import Successful",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        result.type.displayName,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                AnimatedStatChip(
                    modifier = Modifier.weight(1f),
                    label = "Total",
                    value = animatedTotal,
                    color = SapphireBlue,
                    bgColor = BlueSurface,
                    icon = Icons.Default.Description
                )
                AnimatedStatChip(
                    modifier = Modifier.weight(1f),
                    label = "Imported",
                    value = animatedSuccess,
                    color = EmeraldGreen,
                    bgColor = GreenSurface,
                    icon = Icons.Default.CheckCircle
                )
                AnimatedStatChip(
                    modifier = Modifier.weight(1f),
                    label = "Skipped",
                    value = animatedErrors,
                    color = if (hasErrors) CoralRed else EmeraldGreen,
                    bgColor = if (hasErrors) RedSurface else GreenSurface,
                    icon = if (hasErrors) Icons.Default.Warning else Icons.Default.CheckCircle
                )
            }

            if (result.errors.isNotEmpty()) {
                Spacer(Modifier.height(16.dp))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(Icons.Default.Warning, null, tint = CoralRed, modifier = Modifier.size(16.dp))
                    Text(
                        "Errors (${result.errors.size})",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = CoralRed
                    )
                }
                Spacer(Modifier.height(8.dp))
                Surface(
                    modifier = Modifier.fillMaxWidth().heightIn(max = 200.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = CoralRed.copy(alpha = 0.05f)
                ) {
                    Column(modifier = Modifier.padding(12.dp).verticalScroll(rememberScrollState())) {
                        result.errors.take(20).forEach { error ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Surface(
                                    shape = PillShape,
                                    color = CoralRed.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        "Row ${error.row}",
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = CoralRed
                                    )
                                }
                                Text(
                                    error.message,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        if (result.errors.size > 20) {
                            Text(
                                "... and ${result.errors.size - 20} more errors",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AnimatedStatChip(
    modifier: Modifier,
    label: String,
    value: Int,
    color: Color,
    bgColor: Color,
    icon: ImageVector
) {
    Surface(
        modifier = modifier,
        shape = ChipShape,
        color = bgColor
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .background(color.copy(alpha = 0.15f), RoundedCornerShape(6.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, null, tint = color, modifier = Modifier.size(12.dp))
                }
                Text(
                    "$value",
                    fontWeight = FontWeight.ExtraBold,
                    style = MaterialTheme.typography.titleMedium,
                    color = color
                )
            }
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun DownloadTemplateCard(
    type: ImportEntityType,
    isGenerating: Boolean,
    onDownloadXlsx: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp, CardShape, clip = false),
        shape = CardShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(
                            brush = Brush.linearGradient(listOf(GoldenAmber, CoralRed)),
                            shape = RoundedCornerShape(10.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Download, null, tint = Color.White, modifier = Modifier.size(18.dp))
                }
                Column {
                    Text("Download Template", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    Text("Get a pre-filled Excel file for ${type.displayName}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Spacer(Modifier.height(16.dp))

            Text(
                "The template includes headers and an example row. A second sheet provides field descriptions and valid values.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 18.sp
            )

            Spacer(Modifier.height(16.dp))

            Button(
                onClick = onDownloadXlsx,
                modifier = Modifier.height(48.dp),
                shape = ChipShape,
                enabled = !isGenerating,
                colors = ButtonDefaults.buttonColors(containerColor = GoldenAmber)
            ) {
                if (isGenerating) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Generate Excel (.xlsx)", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}
