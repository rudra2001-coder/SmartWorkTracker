package com.rudra.smartworktracker.ui.screens.bulkimport

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rudra.smartworktracker.data.importexport.ExcelTemplateGenerator
import com.rudra.smartworktracker.data.importexport.ImportEntityType
import com.rudra.smartworktracker.data.importexport.ImportManager
import com.rudra.smartworktracker.data.importexport.ImportResult
import com.rudra.smartworktracker.data.importexport.ImportTemplate
import com.rudra.smartworktracker.data.importexport.ImportTemplates
import com.rudra.smartworktracker.data.importexport.SampleImportData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class BulkImportUiState(
    val selectedType: ImportEntityType? = null,
    val template: ImportTemplate? = null,
    val parsedRows: List<Map<String, String>> = emptyList(),
    val detectedType: ImportEntityType? = null,
    val importResult: ImportResult? = null,
    val isLoading: Boolean = false,
    val previewHeaders: List<String> = emptyList(),
    val fileName: String = "",
    val isGeneratingTemplate: Boolean = false,
    val templateGenerated: Boolean = false,
    val isSampleData: Boolean = false,
    val showSamplePrompt: Boolean = false
)

sealed class BulkImportEvent {
    data class Success(val result: ImportResult) : BulkImportEvent()
    data class Error(val message: String) : BulkImportEvent()
    object FileParsed : BulkImportEvent()
    data class TemplateSaved(val fileName: String) : BulkImportEvent()
    object Idle : BulkImportEvent()
}

class BulkImportViewModel(private val context: Context) : ViewModel() {

    private val importManager = ImportManager(context)

    private val _uiState = MutableStateFlow(BulkImportUiState())
    val uiState: StateFlow<BulkImportUiState> = _uiState.asStateFlow()

    private val _event = MutableStateFlow<BulkImportEvent>(BulkImportEvent.Idle)
    val event: StateFlow<BulkImportEvent> = _event.asStateFlow()

    fun selectType(type: ImportEntityType) {
        _uiState.value = _uiState.value.copy(
            selectedType = type,
            template = ImportTemplates.getTemplate(type),
            parsedRows = emptyList(),
            importResult = null,
            detectedType = null,
            previewHeaders = emptyList(),
            fileName = "",
            templateGenerated = false
        )
    }

    fun onFileSelected(uri: Uri, fileName: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val isExcel = fileName.lowercase().endsWith(".xlsx") || fileName.lowercase().endsWith(".xls")
                val rows = if (isExcel) {
                    importManager.parseExcel(uri)
                } else {
                    importManager.parseCsv(uri)
                }

                if (rows.isEmpty()) {
                    _event.value = BulkImportEvent.Error("No data found in file. Check format and try again.")
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    return@launch
                }

                val headers = rows.first().keys.toList()
                val selectedType = _uiState.value.selectedType

                var detected: ImportEntityType? = null
                if (selectedType == null) {
                    detected = ImportTemplates.autoDetectType(headers.toSet())
                }

                _uiState.value = _uiState.value.copy(
                    parsedRows = rows,
                    fileName = fileName,
                    previewHeaders = headers,
                    detectedType = detected,
                    isLoading = false
                )
                _event.value = BulkImportEvent.FileParsed
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false)
                _event.value = BulkImportEvent.Error("Failed to parse file: ${e.localizedMessage}")
            }
        }
    }

    fun executeImport() {
        val state = _uiState.value
        val type = state.selectedType ?: state.detectedType
        if (type == null || state.parsedRows.isEmpty()) {
            _event.value = BulkImportEvent.Error("No data to import. Select a file first.")
            return
        }

        viewModelScope.launch {
            _uiState.value = state.copy(isLoading = true)
            try {
                val result = importManager.importData(type, state.parsedRows)
                _uiState.value = _uiState.value.copy(
                    importResult = result,
                    isLoading = false
                )
                _event.value = BulkImportEvent.Success(result)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false)
                _event.value = BulkImportEvent.Error("Import failed: ${e.localizedMessage}")
            }
        }
    }

    fun generateTemplate(uri: Uri) {
        val type = _uiState.value.selectedType ?: _uiState.value.detectedType ?: return
        val template = ImportTemplates.getTemplate(type)

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isGeneratingTemplate = true)
            try {
                withContext(Dispatchers.IO) {
                    context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                        ExcelTemplateGenerator.generate(template, outputStream)
                    }
                }
                _uiState.value = _uiState.value.copy(
                    isGeneratingTemplate = false,
                    templateGenerated = true
                )
                _event.value = BulkImportEvent.TemplateSaved("${type.displayName}_Template.xlsx")
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isGeneratingTemplate = false)
                _event.value = BulkImportEvent.Error("Failed to generate template: ${e.localizedMessage}")
            }
        }
    }

    fun loadSampleData() {
        val type = _uiState.value.selectedType ?: return
        val template = ImportTemplates.getTemplate(type)
        val sampleRows = SampleImportData.getSampleRows(type)
        val headers = sampleRows.firstOrNull()?.keys?.toList() ?: template.headers

        _uiState.value = _uiState.value.copy(
            parsedRows = sampleRows,
            previewHeaders = headers,
            fileName = "📦 Sample Data — ${type.displayName}",
            detectedType = type,
            importResult = null,
            isLoading = false,
            isSampleData = true,
            showSamplePrompt = false
        )
        _event.value = BulkImportEvent.FileParsed
    }

    fun clearEvent() {
        _event.value = BulkImportEvent.Idle
    }

    fun reset() {
        _uiState.value = BulkImportUiState()
        _event.value = BulkImportEvent.Idle
    }
}
